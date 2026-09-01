package org.edtp.carpet_edtp_addition.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.edtp.carpet_edtp_addition.util.SkeletonTrapSpawnChecks;

public class NaturalSkeletonTrapsGameTests {
    private static final BlockPos SPAWN_POS = new BlockPos(3, 2, 3);

    @GameTest
    public void validFullBlockAllowsSkeletonTrap(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper, true);
        BlockPos spawnPos = prepareSpawnArea(helper, level, Blocks.STONE.defaultBlockState());

        helper.assertTrue(
            SkeletonTrapSpawnChecks.allowsSpawn(level, spawnPos),
            "A clear skeleton trap position above a full block was rejected"
        );
        helper.succeed();
    }

    @GameTest
    public void bedrockRejectsSkeletonTrap(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper, true);
        BlockPos spawnPos = prepareSpawnArea(helper, level, Blocks.BEDROCK.defaultBlockState());

        helper.assertFalse(
            SkeletonTrapSpawnChecks.allowsSpawn(level, spawnPos),
            "A skeleton trap was allowed to spawn on bedrock"
        );
        helper.succeed();
    }

    @GameTest
    public void bottomSlabRejectsSkeletonTrap(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper, true);
        BlockPos spawnPos = prepareSpawnArea(helper, level, Blocks.STONE_SLAB.defaultBlockState());

        helper.assertFalse(
            SkeletonTrapSpawnChecks.allowsSpawn(level, spawnPos),
            "A skeleton trap was allowed to spawn on a bottom slab"
        );
        helper.succeed();
    }

    @GameTest
    public void nearbyCollisionRejectsSkeletonTrap(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper, true);
        BlockPos spawnPos = prepareSpawnArea(helper, level, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(spawnPos.west(), Blocks.STONE.defaultBlockState());

        helper.assertFalse(
            SkeletonTrapSpawnChecks.allowsSpawn(level, spawnPos),
            "A skeleton trap was allowed to intersect a neighboring block"
        );
        helper.succeed();
    }

    @GameTest
    public void disabledRulePreservesVanillaSpawn(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper, false);
        BlockPos spawnPos = prepareSpawnArea(helper, level, Blocks.BEDROCK.defaultBlockState());

        helper.assertTrue(
            SkeletonTrapSpawnChecks.allowsSpawn(level, spawnPos),
            "The disabled rule changed vanilla skeleton trap spawning"
        );
        helper.succeed();
    }

    private static ServerLevel prepareTest(GameTestHelper helper, boolean enabled) {
        CarpetEdtpAdditionSettings.naturalSkeletonTraps.set(null, enabled);
        return helper.getLevel();
    }

    private static BlockPos prepareSpawnArea(GameTestHelper helper, ServerLevel level, BlockState support) {
        BlockPos spawnPos = helper.absolutePos(SPAWN_POS);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.setBlockAndUpdate(spawnPos.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        level.setBlockAndUpdate(spawnPos.below(), support);
        return spawnPos;
    }
}
