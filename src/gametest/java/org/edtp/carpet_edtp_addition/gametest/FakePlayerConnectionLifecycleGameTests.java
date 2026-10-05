package org.edtp.carpet_edtp_addition.gametest;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.edtp.carpet_edtp_addition.gametest.mixin.FabricReceiverRegistryAccessor;
import org.edtp.carpet_edtp_addition.util.FakePlayerConnectionLifecycle;

public class FakePlayerConnectionLifecycleGameTests {
    private static final Set<UUID> WATCHED_PLAYERS = new HashSet<>();
    private static final Map<ServerGamePacketListenerImpl, Observation> OBSERVATIONS = new IdentityHashMap<>();

    static {
        ServerPlayConnectionEvents.INIT.register((listener, server) -> {
            if (WATCHED_PLAYERS.contains(listener.player.getUUID())) {
                OBSERVATIONS.computeIfAbsent(listener, ignored -> new Observation()).inits++;
            }
        });
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            Observation observation = OBSERVATIONS.get(listener);
            if (observation != null) {
                observation.joins++;
                observation.sender = sender;
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> {
            Observation observation = OBSERVATIONS.get(listener);
            if (observation != null) {
                observation.disconnects++;
                if (observation.throwOnDisconnect) {
                    throw new IllegalStateException("Intentional lifecycle GameTest listener failure");
                }
            }
        });
    }

    @GameTest
    public void disabledRulePreservesNativeJoinAndDisconnectBehavior(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, false)) {
            EntityPlayerMPFake player = fixture.spawnFake();
            assertNativeJoin(helper, player);
            helper.assertFalse(CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.defaultValue(),
                "Lifecycle rule must default to off");
            fixture.remove(player);
            helper.assertTrue(observation(player).disconnects == 0, "Disabled rule changed fake disconnect events");
            helper.assertTrue(isTracked(player), "Disabled rule changed native session retention");
            helper.succeed();
        }
    }

    @GameTest
    public void fakeExitCompletesOneDisconnectAndReleasesSession(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake player = fixture.spawnFake();
            assertNativeJoin(helper, player);
            player.connection.onDisconnect(new DisconnectionDetails(Component.literal("lifecycle-test")));
            assertDisconnected(helper, player);
            helper.assertFalse(helper.getLevel().getServer().getPlayerList().getPlayers().contains(player),
                "Fake player remained online after onDisconnect");
            ((FakePlayerConnectionLifecycle) player.connection).edtp$finishConnectionLifecycle();
            ServerNetworkingImpl.getAddon(player.connection).handleDisconnect();
            assertDisconnected(helper, player);
            helper.succeed();
        }
    }

    @GameTest
    public void ruleChangesKeepManagedSessionsPairedAndCanCleanOlderConnections(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake managed = fixture.spawnFake();
            CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.set(null, false);
            fixture.remove(managed);
            assertDisconnected(helper, managed);

            EntityPlayerMPFake older = fixture.spawnFake();
            CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.set(null, true);
            fixture.remove(older);
            assertDisconnected(helper, older);
            helper.succeed();
        }
    }

    @GameTest
    public void alreadyDisconnectedAddonIsNotNotifiedTwice(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake player = fixture.spawnFake();
            ServerNetworkingImpl.getAddon(player.connection).handleDisconnect();
            fixture.remove(player);
            assertDisconnected(helper, player);
            helper.succeed();
        }
    }

    @GameTest
    public void throwingListenerStillAllowsRemovalAndSessionCleanup(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake player = fixture.spawnFake();
            observation(player).throwOnDisconnect = true;
            fixture.remove(player);
            assertDisconnected(helper, player);
            helper.assertFalse(helper.getLevel().getServer().getPlayerList().getPlayers().contains(player),
                "Listener exception prevented player removal");
            ((FakePlayerConnectionLifecycle) player.connection).edtp$finishConnectionLifecycle();
            assertDisconnected(helper, player);
            helper.succeed();
        }
    }

    @GameTest
    public void earlierFailedNativeDisconnectStillReleasesSessionOnRemoval(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake player = fixture.spawnFake();
            observation(player).throwOnDisconnect = true;
            try {
                ServerNetworkingImpl.getAddon(player.connection).handleDisconnect();
                helper.fail("Expected the intentionally failing native disconnect listener");
            } catch (IllegalStateException expected) {
                helper.assertTrue(isTracked(player), "Test must reproduce a retained failed-disconnect session");
            }
            fixture.remove(player);
            assertDisconnected(helper, player);
            helper.succeed();
        }
    }

    @GameTest
    public void realPlayerRemovalDoesNotSynthesizeDisconnect(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            ServerPlayer real = fixture.spawnReal();
            assertNativeJoin(helper, real);
            fixture.remove(real);
            helper.assertTrue(observation(real).disconnects == 0 && isTracked(real),
                "Fake lifecycle rule changed real-player removal");
            ServerNetworkingImpl.getAddon(real.connection).handleDisconnect();
            assertDisconnected(helper, real);
            helper.succeed();
        }
    }

    @GameTest
    public void scheduledCarpetKillCompletesTheManagedLifecycle(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper, true);
        EntityPlayerMPFake player;
        try {
            player = fixture.spawnFake();
            player.kill(Component.literal("scheduled-lifecycle-test"));
            helper.assertTrue(observation(player).disconnects == 0, "Carpet kill should remain scheduled");
        } catch (RuntimeException | Error exception) {
            fixture.close();
            throw exception;
        }
        // Do not leave the global rule enabled while other GameTests execute.
        CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.set(null, fixture.originalRule);
        helper.runAfterDelay(3, () -> {
            try (fixture) {
                assertDisconnected(helper, player);
                helper.assertFalse(helper.getLevel().getServer().getPlayerList().getPlayers().contains(player),
                    "Scheduled Carpet kill did not remove the fake player");
                helper.succeed();
            }
        });
    }

    @GameTest
    public void respawnKeepsTheConnectionAndSameUuidRejoinUsesANewLifecycle(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            EntityPlayerMPFake first = fixture.spawnFake();
            ServerGamePacketListenerImpl listener = first.connection;
            ServerPlayer respawned = helper.getLevel().getServer().getPlayerList().respawn(first, false,
                net.minecraft.world.entity.Entity.RemovalReason.KILLED);
            fixture.players.add(respawned);
            helper.assertTrue(respawned.connection == listener, "Respawn replaced the network listener");
            helper.assertTrue(observation(respawned).disconnects == 0, "Respawn incorrectly disconnected the session");
            assertNativeJoin(helper, respawned);
            ((EntityPlayerMPFake) respawned).kill(Component.translatable("multiplayer.disconnect.duplicate_login"));
            assertDisconnected(helper, respawned);

            EntityPlayerMPFake second = fixture.spawnFake();
            helper.assertTrue(second.getUUID().equals(first.getUUID()), "Rejoin must use the same UUID");
            helper.assertTrue(second.connection != listener, "Rejoin reused the old connection");
            assertNativeJoin(helper, second);
            helper.assertTrue(observation(second).disconnects == 0 && isTracked(second),
                "Old connection cleanup affected the replacement");
            fixture.remove(second);
            assertDisconnected(helper, second);
            helper.succeed();
        }
    }

    @GameTest
    public void shadowPlayerAndShutdownUseTheSameCleanup(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, true)) {
            ServerPlayer real = fixture.spawnReal();
            EntityPlayerMPFake shadow = EntityPlayerMPFake.createShadow(helper.getLevel().getServer(), real);
            fixture.players.add(shadow);
            assertNativeJoin(helper, shadow);
            helper.getLevel().getServer().getPlayerList().removeAll();
            assertDisconnected(helper, shadow);
            fixture.remove(shadow);
            assertDisconnected(helper, shadow);
            helper.succeed();
        }
    }

    @GameTest
    public void tisRealPlayerTickSchedulingPreservesConnectionLifecycle(GameTestHelper helper) throws Exception {
        if (!FabricLoader.getInstance().isModLoaded("carpet-tis-addition")) {
            helper.succeed();
            return;
        }
        // Optional integration dependency belongs only to the test runtime.
        Field tickRule = Class.forName("carpettisaddition.CarpetTISAdditionSettings")
            .getField("fakePlayerTicksLikeRealPlayer");
        boolean original = tickRule.getBoolean(null);
        try {
            for (boolean ticksLikeReal : new boolean[] {false, true}) {
                tickRule.setBoolean(null, ticksLikeReal);
                try (Fixture fixture = new Fixture(helper, true)) {
                    EntityPlayerMPFake player = fixture.spawnFake();
                    player.tick();
                    helper.getLevel().getServer().getConnection().tick();
                    // Also exercise switching tick scheduling for an already connected fake.
                    tickRule.setBoolean(null, !ticksLikeReal);
                    player.tick();
                    helper.getLevel().getServer().getConnection().tick();
                    assertNativeJoin(helper, player);
                    helper.assertTrue(observation(player).disconnects == 0 && isTracked(player),
                        "TIS tick scheduling prematurely disconnected the fake");
                    player.kill(Component.translatable("multiplayer.disconnect.duplicate_login"));
                    assertDisconnected(helper, player);
                }
            }
            helper.succeed();
        } finally {
            tickRule.setBoolean(null, original);
        }
    }

    private static Observation observation(ServerPlayer player) {
        return OBSERVATIONS.get(player.connection);
    }

    private static boolean isTracked(ServerPlayer player) {
        return ((FabricReceiverRegistryAccessor) (Object) ServerNetworkingImpl.PLAY).edtp$getTrackedAddons()
            .contains(ServerNetworkingImpl.getAddon(player.connection));
    }

    private static void assertNativeJoin(GameTestHelper helper, ServerPlayer player) {
        Observation observation = observation(player);
        helper.assertTrue(observation != null && observation.inits == 1 && observation.joins == 1,
            "Expected exactly one native INIT/JOIN per connection");
        helper.assertTrue(observation.sender == ServerNetworkingImpl.getAddon(player.connection),
            "JOIN must provide the actual Fabric addon as its sender");
        observation.sender.sendPacket(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
    }

    private static void assertDisconnected(GameTestHelper helper, ServerPlayer player) {
        helper.assertTrue(observation(player).disconnects == 1, "Expected exactly one DISCONNECT per connection");
        helper.assertFalse(isTracked(player), "Fabric retained the disconnected network session");
    }

    private static final class Observation {
        int inits;
        int joins;
        int disconnects;
        PacketSender sender;
        boolean throwOnDisconnect;
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final GameProfile profile = new GameProfile(UUID.randomUUID(), "edtp-lifecycle");
        final List<ServerPlayer> players = new ArrayList<>();
        final List<EmbeddedChannel> channels = new ArrayList<>();
        final boolean originalRule = CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.value();

        Fixture(GameTestHelper helper, boolean enabled) {
            this.helper = helper;
            WATCHED_PLAYERS.add(profile.id());
            CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.set(null, enabled);
        }

        EntityPlayerMPFake spawnFake() {
            EntityPlayerMPFake player = EntityPlayerMPFake.respawnFake(helper.getLevel().getServer(),
                helper.getLevel(), profile, ClientInformation.createDefault());
            players.add(player);
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(
                new FakeClientConnection(PacketFlow.SERVERBOUND), player,
                CommonListenerCookie.createInitial(profile, false));
            return player;
        }

        ServerPlayer spawnReal() {
            CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
            ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                profile, cookie.clientInformation());
            players.add(player);
            Connection connection = new Connection(PacketFlow.SERVERBOUND);
            channels.add(new EmbeddedChannel(connection));
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
            return player;
        }

        void remove(ServerPlayer player) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }

        @Override
        public void close() {
            try {
                for (ServerPlayer player : players) {
                    if (helper.getLevel().getServer().getPlayerList().getPlayers().contains(player)) {
                        remove(player);
                    }
                    // Release native retained sessions in the rule-disabled control case too.
                    ServerNetworkingImpl.getAddon(player.connection).endSession();
                    OBSERVATIONS.remove(player.connection);
                }
                channels.forEach(EmbeddedChannel::finishAndReleaseAll);
            } finally {
                WATCHED_PLAYERS.remove(profile.id());
                CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.set(null, originalRule);
            }
        }
    }
}
