package org.edtp.carpet_edtp_addition.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.edtp.carpet_edtp_addition.gametest.SoundSuppressionTestHooks.SimulatedUpdateSuppressionException;

public class SoundSuppressionPacketGameTests {
    private static final BlockPos SENSOR_POS = new BlockPos(1, 2, 1);
    private static final BlockPos PLACEMENT_POS = new BlockPos(4, 2, 1);

    @GameTest
    public void disabledRulePlayerPlacementConsumesItem(GameTestHelper helper) {
        assertPlayerPlacement(helper, false, Blocks.SCULK_SENSOR.defaultBlockState(), false);
    }

    @GameTest
    public void enabledRuleNormalSensorStillConsumesItem(GameTestHelper helper) {
        assertPlayerPlacement(helper, true, Blocks.SCULK_SENSOR.defaultBlockState(), false);
    }

    @GameTest
    public void disabledRuleFilledShulkerPlacementConsumesItem(GameTestHelper helper) {
        assertPlayerPlacement(helper, false, Blocks.SCULK_SENSOR.defaultBlockState(), true);
    }

    @GameTest
    public void lecternSuppressesPlayerPlacementWithoutDisconnecting(GameTestHelper helper) {
        assertPlayerPlacement(helper, true, Blocks.LECTERN.defaultBlockState(), false);
    }

    @GameTest
    public void beeNestSuppressesPlayerPlacementWithoutDisconnecting(GameTestHelper helper) {
        assertPlayerPlacement(helper, true, Blocks.BEE_NEST.defaultBlockState(), false);
    }

    @GameTest
    public void shulkerSuppressesPlayerPlacementWithoutDisconnecting(GameTestHelper helper) {
        assertPlayerPlacement(helper, true, Blocks.SHULKER_BOX.defaultBlockState(), false);
    }

    @GameTest
    public void lecternSuppressesFilledShulkerConsumption(GameTestHelper helper) {
        assertPlayerPlacement(helper, true, Blocks.LECTERN.defaultBlockState(), true);
    }

    private static void assertPlayerPlacement(
        GameTestHelper helper,
        boolean enabled,
        BlockState replacement,
        boolean filledShulker
    ) {
        ServerLevel level = helper.getLevel();
        BlockPos sensorPos = helper.absolutePos(SENSOR_POS);
        BlockPos placementPos = helper.absolutePos(PLACEMENT_POS);
        boolean suppressing = !replacement.is(Blocks.SCULK_SENSOR);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, enabled);
        TestPlayer client = new TestPlayer(level, placementPos);
        boolean checkNextTick = false;

        try {
            level.setBlock(placementPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(placementPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(sensorPos, Blocks.SCULK_SENSOR.defaultBlockState(), Block.UPDATE_ALL);
            if (suppressing) {
                constructSuppressor(helper, level, sensorPos, replacement);
            }

            ItemStack held = new ItemStack(filledShulker ? Items.SHULKER_BOX : Items.STONE);
            if (filledShulker) {
                held.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 7))));
            }
            ItemStack expected = held.copy();
            client.player.setItemInHand(InteractionHand.MAIN_HAND, held);
            helper.assertTrue(client.player.connection.hasClientLoaded(), "The player has not acknowledged loading");
            helper.assertFalse(client.player.hasInfiniteMaterials(), "The placement control must use survival mode");

            // This is the real server packet queue and its vanilla exception boundary.
            // No test catch block swallows errors from placement or vibration dispatch.
            client.place(placementPos, 1);

            helper.assertTrue(
                level.getBlockState(placementPos).is(filledShulker ? Blocks.SHULKER_BOX : Blocks.STONE),
                "The player packet did not place the block"
            );
            List<Exception> errors = SoundSuppressionTestHooks.packetErrors(client.player.getUUID());
            if (suppressing) {
                helper.assertTrue(errors.size() == 1, "Expected exactly one error handled by vanilla, got " + errors.size());
                Exception error = errors.getFirst();
                helper.assertTrue(error instanceof IllegalArgumentException, "The handled error was not a sensor property error: " + error);
                helper.assertTrue(error.getMessage().contains("phase"), "The error did not involve the missing sensor phase");
                assertStackContains(helper, error, "net.minecraft.world.level.block.SculkSensorBlock", "canActivate");
                assertStackContains(helper, error, "net.minecraft.world.level.gameevent.GameEventDispatcher", "post");
                assertStackContains(helper, error, "net.minecraft.world.item.BlockItem", "place");
                assertStackContains(helper, error, "net.minecraft.network.PacketProcessor$ListenerAndPacket", "handle");
                helper.assertTrue(
                    ItemStack.matches(client.player.getMainHandItem(), expected),
                    "The suppressed placement consumed or changed the held item"
                );
            } else {
                helper.assertTrue(errors.isEmpty(), "Normal placement unexpectedly raised a packet error");
                helper.assertTrue(client.player.getMainHandItem().isEmpty(), "Normal survival placement did not consume the item");
            }

            if (filledShulker) {
                ShulkerBoxBlockEntity placed = (ShulkerBoxBlockEntity) level.getBlockEntity(placementPos);
                helper.assertTrue(
                    placed != null && ItemStack.matches(placed.getItem(0), new ItemStack(Items.DIAMOND, 7)),
                    "The placed shulker box did not retain its contents"
                );
            }

            helper.assertTrue(client.connection.isConnected(), "The suppressed packet disconnected the player");
            clearSensor(level, sensorPos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
            long gameTime = level.getGameTime();
            int expectedErrors = errors.size();
            helper.runAfterDelay(2, () -> {
                try {
                    helper.assertTrue(level.getGameTime() > gameTime, "The server stopped ticking after the packet error");
                    helper.assertTrue(client.player.isAlive(), "The test player did not survive the subsequent ticks");
                    helper.assertTrue(
                        ItemStack.matches(client.player.getMainHandItem(), suppressing ? expected : ItemStack.EMPTY),
                        "The held item result changed after subsequent ticks"
                    );
                    helper.assertTrue(
                        level.getBlockState(placementPos).is(filledShulker ? Blocks.SHULKER_BOX : Blocks.STONE),
                        "The placed block did not survive subsequent ticks"
                    );
                    if (filledShulker) {
                        ShulkerBoxBlockEntity placed = (ShulkerBoxBlockEntity) level.getBlockEntity(placementPos);
                        helper.assertTrue(
                            placed != null && ItemStack.matches(placed.getItem(0), new ItemStack(Items.DIAMOND, 7)),
                            "The placed shulker contents changed after subsequent ticks"
                        );
                    }
                    level.setBlock(placementPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    client.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
                    client.place(placementPos, 2);
                    helper.assertTrue(level.getBlockState(placementPos).is(Blocks.STONE), "The next player packet did not execute");
                    helper.assertTrue(client.player.getMainHandItem().isEmpty(), "The next placement did not consume its item");
                    helper.assertTrue(client.connection.isConnected(), "The connection did not survive subsequent ticks");
                    helper.assertTrue(
                        SoundSuppressionTestHooks.packetErrors(client.player.getUUID()).size() == expectedErrors,
                        "The follow-up packet raised another error"
                    );
                    helper.succeed();
                } finally {
                    level.setBlock(placementPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    client.close();
                }
            });
            checkNextTick = true;
        } finally {
            SoundSuppressionTestHooks.clear(sensorPos);
            clearSensor(level, sensorPos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
            if (!checkNextTick) {
                client.close();
            }
        }
    }

    private static void constructSuppressor(GameTestHelper helper, ServerLevel level, BlockPos pos, BlockState replacement) {
        BlockState active = Blocks.SCULK_SENSOR.defaultBlockState()
            .setValue(SculkSensorBlock.PHASE, SculkSensorPhase.ACTIVE)
            .setValue(SculkSensorBlock.POWER, 15);
        level.setBlock(pos, active, Block.UPDATE_ALL);
        SculkSensorBlockEntity original = (SculkSensorBlockEntity) level.getBlockEntity(pos);

        // Only apparatus construction uses simulated update suppression. Player packets below
        // must produce their own vanilla IllegalArgumentException through the registered listener.
        SoundSuppressionTestHooks.arm(pos);
        boolean interrupted = false;
        try {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } catch (SimulatedUpdateSuppressionException expected) {
            interrupted = true;
        } finally {
            SoundSuppressionTestHooks.clear(pos);
        }
        helper.assertTrue(interrupted, "The fixture's update was not suppressed");
        level.setBlock(pos, replacement, Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockEntity(pos) == original, "The original sensor was not retained");
        helper.assertTrue(original.getBlockState() == replacement, "The sensor did not cache the replacement state");
    }

    private static void clearSensor(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SculkSensorBlockEntity sensor) {
            // Restore the cached type so vanilla teardown unregisters the original listener.
            sensor.setBlockState(Blocks.SCULK_SENSOR.defaultBlockState());
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void assertStackContains(GameTestHelper helper, Exception error, String className, String method) {
        helper.assertTrue(
            Arrays.stream(error.getStackTrace()).anyMatch(frame -> frame.getClassName().equals(className) && frame.getMethodName().equals(method)),
            "The error bypassed " + className + "." + method
        );
    }

    private static final class TestPlayer implements AutoCloseable {
        private final ServerPlayer player;
        private final Connection connection;
        private final EmbeddedChannel channel;

        private TestPlayer(ServerLevel level, BlockPos placementPos) {
            UUID playerId = UUID.randomUUID();
            CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(playerId, "sound-" + playerId.toString().substring(0, 8)),
                false
            );
            this.player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
            this.connection = new Connection(PacketFlow.SERVERBOUND);
            this.channel = new EmbeddedChannel(this.connection);
            level.getServer().getPlayerList().placeNewPlayer(this.connection, this.player, cookie);
            this.player.setGameMode(GameType.SURVIVAL);
            this.player.setNoGravity(true);
            this.player.connection.teleport(placementPos.getX() + 0.5, placementPos.getY(), placementPos.getZ() + 2.5, 180, 0);
            Object message;
            while ((message = this.channel.readOutbound()) != null) {
                if (message instanceof ClientboundPlayerPositionPacket teleport) {
                    this.send(new ServerboundAcceptTeleportationPacket(teleport.id()));
                }
            }
            this.send(new ServerboundPlayerLoadedPacket());
            SoundSuppressionTestHooks.capturePacketErrors(this.player.getUUID());
        }

        private void place(BlockPos pos, int sequence) {
            BlockHitResult hit = new BlockHitResult(Vec3.atBottomCenterOf(pos), Direction.UP, pos.below(), false);
            this.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, sequence));
        }

        private void send(Packet<ServerGamePacketListener> packet) {
            var processor = this.player.level().getServer().packetProcessor();
            processor.scheduleIfPossible(this.player.connection, packet);
            processor.processQueuedPackets();
        }

        @Override
        public void close() {
            SoundSuppressionTestHooks.stopCapturingPacketErrors(this.player.getUUID());
            this.player.level().getServer().getPlayerList().remove(this.player);
            this.channel.finishAndReleaseAll();
        }
    }
}
