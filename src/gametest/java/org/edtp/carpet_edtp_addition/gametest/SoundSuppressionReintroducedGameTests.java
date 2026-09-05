package org.edtp.carpet_edtp_addition.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.edtp.carpet_edtp_addition.gametest.SoundSuppressionTestHooks.SimulatedUpdateSuppressionException;

public class SoundSuppressionReintroducedGameTests {
    private static final BlockPos TEST_POS = new BlockPos(2, 2, 2);

    @GameTest
    public void disabledRuleRejectsMismatchedSculkSensorState(GameTestHelper helper) {
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        BlockPos pos = helper.absolutePos(TEST_POS);

        boolean rejected = false;
        try {
            new SculkSensorBlockEntity(pos, Blocks.BEE_NEST.defaultBlockState());
        } catch (IllegalStateException expected) {
            rejected = true;
        }

        helper.assertTrue(rejected, "The disabled rule accepted a mismatched sculk sensor block entity");
        helper.succeed();
    }

    @GameTest
    public void regularSensorSupportsVanillaBlockEntityTargets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            assertRegularSuppressorTarget(helper, level, pos, Blocks.BEE_NEST.defaultBlockState());
            assertRegularSuppressorTarget(helper, level, pos, Blocks.LECTERN.defaultBlockState());
            assertRegularSuppressorTarget(helper, level, pos, Blocks.SHULKER_BOX.defaultBlockState());
            helper.succeed();
        } finally {
            clear(level, pos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    @GameTest
    public void calibratedSensorUsesReplacementHorizontalFacing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            BlockState lectern = Blocks.LECTERN.defaultBlockState()
                .setValue(CalibratedSculkSensorBlock.FACING, Direction.NORTH);
            level.setBlock(pos, lectern, Block.UPDATE_ALL);
            CalibratedSculkSensorBlockEntity sensor = new CalibratedSculkSensorBlockEntity(pos, lectern);
            level.setBlockEntity(sensor);
            level.setBlock(pos.south(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);

            boolean wrongFrequencyRejected = !sensor.getVibrationUser()
                .canReceiveVibration(level, pos.east(), GameEvent.STEP, null);
            helper.assertTrue(wrongFrequencyRejected, "The calibrated suppressor ignored the replacement block's facing/input");
            assertIllegalArgument(
                helper,
                () -> sensor.getVibrationUser().canReceiveVibration(level, pos.east(), GameEvent.EXPLODE, null),
                "A matching calibrated vibration did not trigger sound suppression"
            );
            helper.succeed();
        } finally {
            clear(level, pos);
            level.setBlock(pos.south(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    @GameTest
    public void calibratedSensorAllowsShulkerVanillaOutcome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            BlockState shulker = Blocks.SHULKER_BOX.defaultBlockState();
            level.setBlock(pos, shulker, Block.UPDATE_ALL);
            CalibratedSculkSensorBlockEntity sensor = new CalibratedSculkSensorBlockEntity(pos, shulker);
            level.setBlockEntity(sensor);

            assertIllegalArgument(
                helper,
                () -> sensor.getVibrationUser().canReceiveVibration(level, pos.east(), GameEvent.STEP, null),
                "A calibrated sensor under a shulker box was unexpectedly filtered"
            );
            helper.assertTrue(
                shulker.getAnalogOutputSignal(level, pos, Direction.NORTH) == 0,
                "Vanilla shulker comparator handling did not remain safe for a sensor block entity"
            );
            helper.succeed();
        } finally {
            clear(level, pos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    @GameTest
    public void mismatchedSensorSurvivesSaveAndLoad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            BlockState beeNest = Blocks.BEE_NEST.defaultBlockState();
            SculkSensorBlockEntity sensor = new SculkSensorBlockEntity(pos, beeNest);
            sensor.setLastVibrationFrequency(7);
            CompoundTag tag = sensor.saveWithFullMetadata(level.registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(pos, beeNest, tag, level.registryAccess());

            helper.assertTrue(loaded instanceof SculkSensorBlockEntity, "The mismatched sensor was lost while loading");
            helper.assertTrue(loaded.getBlockState() == beeNest, "The replacement block state was not restored");
            helper.assertTrue(
                ((SculkSensorBlockEntity) loaded).getLastVibrationFrequency() == 7,
                "The sensor data did not survive save/load"
            );
            helper.succeed();
        } finally {
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    @GameTest
    public void simulatedUpdateSuppressionCreatesLecternSuppressor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            BlockState activeSensor = Blocks.SCULK_SENSOR.defaultBlockState()
                .setValue(SculkSensorBlock.PHASE, SculkSensorPhase.ACTIVE)
                .setValue(SculkSensorBlock.POWER, 15);
            level.setBlock(pos, activeSensor, Block.UPDATE_ALL);
            BlockEntity original = level.getBlockEntity(pos);
            helper.assertTrue(original instanceof SculkSensorBlockEntity, "The active sensor block entity was not created");

            SoundSuppressionTestHooks.arm(pos);
            boolean suppressed = false;
            try {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } catch (SimulatedUpdateSuppressionException expected) {
                suppressed = true;
            }

            helper.assertTrue(suppressed, "The test update was not suppressed");
            helper.assertTrue(level.getBlockState(pos).isAir(), "The suppressed block change did not reach the 1.21 intermediate state");
            helper.assertTrue(level.getBlockEntity(pos) == original, "The active sensor block entity was removed before suppression");

            BlockState lectern = Blocks.LECTERN.defaultBlockState();
            level.setBlock(pos, lectern, Block.UPDATE_ALL);
            helper.assertTrue(level.getBlockEntity(pos) == original, "The sensor block entity was not retained under the lectern");
            helper.assertTrue(original.getBlockState() == lectern, "The retained sensor did not cache the lectern state");
            assertIllegalArgument(
                helper,
                () -> ((SculkSensorBlockEntity) original).getVibrationUser()
                    .canReceiveVibration(level, pos.east(), GameEvent.STEP, null),
                "The constructed lectern suppressor did not suppress sound"
            );
            helper.succeed();
        } finally {
            SoundSuppressionTestHooks.clear(pos);
            clear(level, pos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    @GameTest
    public void unsuppressedRemovalStillCleansUpSensor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(TEST_POS);
        CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, true);

        try {
            BlockState activeSensor = Blocks.SCULK_SENSOR.defaultBlockState()
                .setValue(SculkSensorBlock.PHASE, SculkSensorPhase.ACTIVE)
                .setValue(SculkSensorBlock.POWER, 15);
            level.setBlock(pos, activeSensor, Block.UPDATE_ALL);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

            helper.assertTrue(level.getBlockEntity(pos) == null, "A normally removed active sensor block entity leaked");
            helper.succeed();
        } finally {
            clear(level, pos);
            CarpetEdtpAdditionSettings.soundSuppressionReintroduced.set(null, false);
        }
    }

    private static void assertRegularSuppressorTarget(
        GameTestHelper helper,
        ServerLevel level,
        BlockPos pos,
        BlockState target
    ) {
        clear(level, pos);
        level.setBlock(pos, target, Block.UPDATE_ALL);
        SculkSensorBlockEntity sensor = new SculkSensorBlockEntity(pos, target);
        level.setBlockEntity(sensor);

        helper.assertTrue(level.getBlockEntity(pos) == sensor, "The sensor was rejected under " + target.getBlock());
        assertIllegalArgument(
            helper,
            () -> sensor.getVibrationUser().canReceiveVibration(level, pos.east(), GameEvent.STEP, null),
            "The sensor under " + target.getBlock() + " did not suppress sound"
        );
    }

    private static void assertIllegalArgument(GameTestHelper helper, Runnable action, String message) {
        boolean threw = false;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        helper.assertTrue(threw, message);
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SculkSensorBlockEntity sensor) {
            // Test teardown must unregister the listener retained during the block entity swap.
            sensor.setBlockState((sensor instanceof CalibratedSculkSensorBlockEntity
                ? Blocks.CALIBRATED_SCULK_SENSOR : Blocks.SCULK_SENSOR).defaultBlockState());
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
