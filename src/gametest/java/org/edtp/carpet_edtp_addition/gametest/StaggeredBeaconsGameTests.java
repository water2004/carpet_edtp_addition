package org.edtp.carpet_edtp_addition.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ServerLevelData;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class StaggeredBeaconsGameTests {
    @GameTest(skyAccess = true)
    public void workIsStaggeredWithTheSamePeriodAndSurvivesReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long originalTime = level.getGameTime();
        boolean originalRule = CarpetEdtpAdditionSettings.staggeredBeacons.value();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        List<BeaconBlockEntity> beacons = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.IRON_BLOCK);
                if (x > 0 && x < 7 && z > 0 && z < 7) {
                    BlockPos pos = helper.absolutePos(new BlockPos(x, 1, z));
                    level.setBlockAndUpdate(pos, Blocks.BEACON.defaultBlockState());
                    beacons.add((BeaconBlockEntity) level.getBlockEntity(pos));
                }
            }
        }

        try {
            helper.assertFalse(CarpetEdtpAdditionSettings.staggeredBeacons.defaultValue(), "Rule must default to off");
            checkSchedule(helper, beacons, player, false);
            int[] phases = checkSchedule(helper, beacons, player, true);

            // Exercise actual block entity serialization, without storing any extra phase state.
            for (int i = 0; i < beacons.size(); i++) {
                BeaconBlockEntity beacon = beacons.get(i);
                BeaconBlockEntity restored = (BeaconBlockEntity) BlockEntity.loadStatic(
                    beacon.getBlockPos(), beacon.getBlockState(),
                    beacon.saveWithFullMetadata(level.registryAccess()), level.registryAccess()
                );
                helper.assertTrue(restored != null, "Could not reload beacon");
                level.setBlockEntity(restored);
                beacons.set(i, restored);
            }
            helper.assertTrue(
                Arrays.equals(phases, checkSchedule(helper, beacons, player, true)),
                "Beacon phases changed after saving and reloading"
            );
            checkSchedule(helper, beacons, player, false);
            helper.succeed();
        } finally {
            setTime(level, originalTime);
            CarpetEdtpAdditionSettings.staggeredBeacons.set(null, originalRule);
        }
    }

    private static int[] checkSchedule(
        GameTestHelper helper, List<BeaconBlockEntity> beacons, Player player, boolean enabled
    ) {
        ServerLevel level = helper.getLevel();
        CarpetEdtpAdditionSettings.staggeredBeacons.set(null, enabled);
        int[] phases = new int[beacons.size()];
        int[] workPerTick = new int[80];
        for (int i = 0; i < beacons.size(); i++) {
            BeaconBlockEntity beacon = beacons.get(i);
            BeaconMenu menu = menu(beacon, player);
            tickAt(level, beacon, -2);
            tickAt(level, beacon, -1);
            int lastWork = -1;
            int workCount = 0;
            for (int tick = 0; tick < 240; tick++) {
                // A sentinel lets the real updateBase call reveal when it ran; no test injection.
                menu.setData(BeaconBlockEntity.DATA_LEVELS, -1);
                tickAt(level, beacon, tick);
                if (menu.getLevels() != -1) {
                    helper.assertTrue(menu.getLevels() == 1, "Beacon did not scan its real iron base");
                    if (workCount == 0) {
                        phases[i] = tick;
                        workPerTick[tick]++;
                    } else {
                        helper.assertTrue(tick - lastWork == 80, "Beacon work interval changed from 80 ticks");
                    }
                    if (!enabled) {
                        helper.assertTrue(tick % 80 == 0, "Disabled rule changed vanilla timing");
                    }
                    lastWork = tick;
                    workCount++;
                }
            }
            helper.assertTrue(workCount == 3, "Beacon must scan exactly three times in 240 ticks");
        }
        if (enabled) {
            // The test runner randomizes the structure's absolute position. Check spreading,
            // not a particular hash distribution that only holds at one set of coordinates.
            helper.assertTrue(Arrays.stream(workPerTick).filter(count -> count > 0).count() > 1,
                "36 neighboring beacons were not spread across enough ticks");
            helper.assertTrue(Arrays.stream(workPerTick).max().orElseThrow() < beacons.size(),
                "Too many neighboring beacons still worked on the same tick");
        }
        return phases;
    }

    @GameTest(skyAccess = true, padding = 2)
    public void effectDurationAmplifierAndRangeStayVanilla(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long originalTime = level.getGameTime();
        boolean originalRule = CarpetEdtpAdditionSettings.staggeredBeacons.value();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "beacon-test"), false
        );
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        BlockPos pos = helper.absolutePos(new BlockPos(3, 5, 3));
        List<ChunkPos> forcedChunks = new ArrayList<>();
        try {
            // Range-edge players can leave the small test structure. Load their entity sections too.
            for (int x = pos.getX() >> 4; x <= (pos.getX() + 53) >> 4; x++) {
                ChunkPos chunk = new ChunkPos(x, pos.getZ() >> 4);
                if (level.setChunkForced(chunk.x(), chunk.z(), true)) {
                    forcedChunks.add(chunk);
                }
                level.waitForEntities(chunk, 0);
            }
            level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
            for (boolean enabled : new boolean[] {false, true}) {
                CarpetEdtpAdditionSettings.staggeredBeacons.set(null, enabled);
                for (int baseLevels = 1; baseLevels <= 4; baseLevels++) {
                    prepareBase(level, pos, baseLevels);
                    level.setBlockAndUpdate(pos, Blocks.BEACON.defaultBlockState());
                    BeaconBlockEntity beacon = (BeaconBlockEntity) level.getBlockEntity(pos);
                    BeaconMenu menu = menu(beacon, player);
                    menu.setData(BeaconBlockEntity.DATA_PRIMARY, BeaconMenu.encodeEffect(MobEffects.SPEED));
                    menu.setData(BeaconBlockEntity.DATA_SECONDARY, BeaconMenu.encodeEffect(MobEffects.REGENERATION));
                    player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                    tickAt(level, beacon, -2);
                    tickAt(level, beacon, -1);
                    int lastRefresh = -1;
                    int refreshCount = 0;
                    int duration = (9 + baseLevels * 2) * 20;
                    for (int tick = 0; tick < 240; tick++) {
                        player.removeAllEffects();
                        tickAt(level, beacon, tick);
                        if (player.hasEffect(MobEffects.SPEED)) {
                            helper.assertTrue(menu.getLevels() == baseLevels, "Wrong beacon base level");
                            assertEffect(helper, player, duration, 0);
                            helper.assertTrue(player.hasEffect(MobEffects.REGENERATION) == (baseLevels == 4),
                                "Secondary effect did not require a four-level base");
                            if (baseLevels == 4) {
                                helper.assertTrue(player.getEffect(MobEffects.REGENERATION).getDuration() == duration,
                                    "Secondary effect duration changed");
                            }
                            if (refreshCount > 0) {
                                helper.assertTrue(tick - lastRefresh == 80, "Effect refresh interval changed");
                            }
                            lastRefresh = tick;
                            refreshCount++;
                        }
                    }
                    helper.assertTrue(refreshCount == 3, "Effects must refresh three times in 240 ticks");

                    player.removeAllEffects();
                    player.setPos(pos.getX() + baseLevels * 10 + 10.5, pos.getY() + 1, pos.getZ() + 0.5);
                    tickAt(level, beacon, lastRefresh + 80);
                    assertEffect(helper, player, duration, 0);
                    player.removeAllEffects();
                    player.setPos(pos.getX() + baseLevels * 10 + 13, pos.getY() + 1, pos.getZ() + 0.5);
                    tickAt(level, beacon, lastRefresh + 160);
                    helper.assertFalse(player.hasEffect(MobEffects.SPEED), "Beacon affected a player outside its range");
                    if (baseLevels == 4) {
                        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                        menu.setData(BeaconBlockEntity.DATA_SECONDARY, BeaconMenu.encodeEffect(MobEffects.SPEED));
                        tickAt(level, beacon, lastRefresh + 240);
                        assertEffect(helper, player, duration, 1);
                        helper.assertFalse(player.hasEffect(MobEffects.REGENERATION), "Level II mode granted regeneration");
                    }
                }
            }
            helper.succeed();
        } finally {
            player.removeAllEffects();
            level.getServer().getPlayerList().remove(player);
            channel.finishAndReleaseAll();
            for (ChunkPos chunk : forcedChunks) {
                level.setChunkForced(chunk.x(), chunk.z(), false);
            }
            setTime(level, originalTime);
            CarpetEdtpAdditionSettings.staggeredBeacons.set(null, originalRule);
        }
    }

    @GameTest(skyAccess = true)
    public void beamObstructionStillUpdatesBetweenPeriodicChecks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long originalTime = level.getGameTime();
        boolean originalRule = CarpetEdtpAdditionSettings.staggeredBeacons.value();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        try {
            CarpetEdtpAdditionSettings.staggeredBeacons.set(null, true);
            prepareBase(level, pos, 1);
            level.setBlockAndUpdate(pos, Blocks.BEACON.defaultBlockState());
            BeaconBlockEntity beacon = (BeaconBlockEntity) level.getBlockEntity(pos);
            BeaconMenu menu = menu(beacon, helper.makeMockPlayer(GameType.SURVIVAL));
            tickAt(level, beacon, -2);
            tickAt(level, beacon, -1);
            int phase = -1;
            menu.setData(BeaconBlockEntity.DATA_LEVELS, 0);
            for (int tick = 0; tick < 80; tick++) {
                tickAt(level, beacon, tick);
                if (menu.getLevels() == 1) {
                    phase = tick;
                    break;
                }
            }
            helper.assertTrue(phase >= 0, "Beacon did not activate");
            helper.assertFalse(beacon.getBeamSections().isEmpty(), "Active beacon has no beam");
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            tickAt(level, beacon, phase + 1);
            helper.assertTrue(beacon.getBeamSections().isEmpty(), "Beam obstruction waited for the 80-tick cycle");
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            tickAt(level, beacon, phase + 2);
            helper.assertFalse(beacon.getBeamSections().isEmpty(), "Beam restoration waited for the 80-tick cycle");

            level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
            for (int tick = phase + 3; tick < phase + 80; tick++) {
                tickAt(level, beacon, tick);
                helper.assertTrue(menu.getLevels() == 1, "Base was checked before its next scheduled tick");
            }
            tickAt(level, beacon, phase + 80);
            helper.assertTrue(menu.getLevels() == 0, "Broken base was not detected on the next scheduled tick");
            helper.succeed();
        } finally {
            setTime(level, originalTime);
            CarpetEdtpAdditionSettings.staggeredBeacons.set(null, originalRule);
        }
    }

    private static void assertEffect(GameTestHelper helper, Player player, int duration, int amplifier) {
        var effect = player.getEffect(MobEffects.SPEED);
        helper.assertTrue(effect != null, "Beacon did not apply speed");
        helper.assertTrue(effect.getDuration() == duration, "Effect duration changed");
        helper.assertTrue(effect.getAmplifier() == amplifier, "Effect amplifier changed");
    }

    private static void prepareBase(ServerLevel level, BlockPos pos, int levels) {
        for (int depth = 1; depth <= 4; depth++) {
            for (int dx = -depth; dx <= depth; dx++) {
                for (int dz = -depth; dz <= depth; dz++) {
                    level.setBlockAndUpdate(pos.offset(dx, -depth, dz),
                        (depth <= levels ? Blocks.IRON_BLOCK : Blocks.AIR).defaultBlockState());
                }
            }
        }
    }

    private static BeaconMenu menu(BeaconBlockEntity beacon, Player player) {
        return (BeaconMenu) beacon.createMenu(0, player.getInventory(), player);
    }

    private static void tickAt(ServerLevel level, BeaconBlockEntity beacon, long time) {
        // Run the real injected tick synchronously. Restore world time before returning to the test runner.
        setTime(level, time);
        BeaconBlockEntity.tick(level, beacon.getBlockPos(), beacon.getBlockState(), beacon);
    }

    private static void setTime(ServerLevel level, long time) {
        ((ServerLevelData) level.getLevelData()).setGameTime(time);
    }
}
