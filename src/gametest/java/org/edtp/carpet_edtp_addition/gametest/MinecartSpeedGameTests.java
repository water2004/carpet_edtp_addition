package org.edtp.carpet_edtp_addition.gametest;

import carpet.api.settings.InvalidRuleValueException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class MinecartSpeedGameTests {
    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void allSpeedModesAndPassengerRuleCombinationsMoveRealMinecarts(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            double[] packedLimits = {0.4, 1.0, 1.0, 4.0 / 3.0};
            double[] blueLimits = {0.4, 1.0, 2.0, 8.0 / 3.0};
            double[] bluePassengerSpeeds = {0.4, 1.0, 1.5, 2.0};
            for (int mode = 0; mode <= 3; mode++) {
                for (boolean removePenalty : new boolean[] {false, true}) {
                    fixture.rules(mode, removePenalty);
                    for (boolean passenger : new boolean[] {false, true}) {
                        fixture.track(Blocks.PACKED_ICE, Fixture.RAIL);
                        Minecart packed = fixture.cart(passenger);
                        assertNear(helper, packed.getBehavior().getMaxSpeed(fixture.level), packedLimits[mode],
                            "Passenger rule changed the packed ice speed limit");
                        fixture.assertStep(packed, 10.0, packedLimits[mode]);

                        fixture.track(Blocks.BLUE_ICE, Fixture.RAIL);
                        Minecart blue = fixture.cart(passenger);
                        assertNear(helper, blue.getBehavior().getMaxSpeed(fixture.level), blueLimits[mode],
                            "Passenger rule changed the blue ice speed limit");
                        double expected = passenger && !removePenalty ? bluePassengerSpeeds[mode] : blueLimits[mode];
                        fixture.assertStep(blue, 10.0, expected);
                    }
                }
            }
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void passengerMultiplierWorksIndependentlyWithoutChangingDrag(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.track(Blocks.STONE, Fixture.RAIL);
            for (int mode = 0; mode <= 3; mode++) {
                for (boolean removePenalty : new boolean[] {false, true}) {
                    fixture.rules(mode, removePenalty);
                    Minecart occupied = fixture.cart(true);
                    fixture.assertStep(occupied, 0.2, removePenalty ? 0.2 : 0.15);
                    assertNear(helper, occupied.getDeltaMovement().x, 0.2 * 0.997,
                        "Movement multiplier changed passenger drag");
                    Minecart empty = fixture.cart(false);
                    fixture.assertStep(empty, 0.2, 0.2);
                    assertNear(helper, empty.getDeltaMovement().x, 0.2 * 0.96,
                        "Passenger rule changed empty cart drag");
                }
            }
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void normalBlocksAndOrdinaryIceKeepVanillaSpeedLimits(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            for (int mode = 0; mode <= 3; mode++) {
                fixture.rules(mode, true);
                for (Block support : new Block[] {Blocks.STONE, Blocks.ICE}) {
                    fixture.track(support, Fixture.RAIL);
                    fixture.assertStep(fixture.cart(false), 10.0, 0.4);
                    fixture.assertStep(fixture.cart(true), 10.0, 0.4);
                }
            }
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void westboundMovementUsesTheSameLimits(GameTestHelper helper) throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.track(Blocks.BLUE_ICE, Fixture.RAIL);
            fixture.rules(3, false);
            Minecart cart = fixture.cart(true);
            cart.setInitialPos(fixture.start.getX() + 5.75, fixture.start.getY() + 0.0625, fixture.start.getZ() + 0.5);
            fixture.assertStep(cart, -10.0, -2.0);
            fixture.rules(3, true);
            cart.setInitialPos(fixture.start.getX() + 5.75, fixture.start.getY() + 0.0625, fixture.start.getZ() + 0.5);
            fixture.assertStep(cart, -10.0, -8.0 / 3.0);
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void poweredRailAccelerationAndBrakingRemainVanilla(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            for (int mode = 0; mode <= 3; mode++) {
                for (boolean removePenalty : new boolean[] {false, true}) {
                    fixture.rules(mode, removePenalty);
                    double multiplier = removePenalty ? 1.0 : 0.75;
                    fixture.track(Blocks.BLUE_ICE, Fixture.POWERED_RAIL.setValue(PoweredRailBlock.POWERED, true));
                    Minecart powered = fixture.cart(true);
                    fixture.assertStep(powered, 0.2, 0.2 * multiplier);
                    assertNear(helper, powered.getDeltaMovement().x, 0.2 * 0.997 + 0.06,
                        "Powered rail acceleration changed");

                    fixture.track(Blocks.BLUE_ICE, Fixture.POWERED_RAIL.setValue(PoweredRailBlock.POWERED, false));
                    Minecart braking = fixture.cart(true);
                    fixture.assertStep(braking, 0.2, 0.1 * multiplier);
                    assertNear(helper, braking.getDeltaMovement().x, 0.1 * 0.997,
                        "Unpowered rail braking changed");
                    Minecart stopped = fixture.cart(true);
                    fixture.assertStep(stopped, 0.02, 0.0);
                    assertNear(helper, stopped.getDeltaMovement().x, 0.0, "Low-speed brake did not stop the cart");
                }
            }
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void leavingIceRestoresLimitWithoutErasingStoredSpeed(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.rules(3, false);
            fixture.track(Blocks.BLUE_ICE, Fixture.RAIL);
            Minecart cart = fixture.cart(true);
            fixture.assertStep(cart, 10.0, 2.0);

            fixture.track(Blocks.STONE, Fixture.RAIL);
            fixture.assertStep(cart, cart.getDeltaMovement().x, 0.4);
            assertNear(helper, cart.getDeltaMovement().x, 2.0 * 0.997,
                "Returning to a vanilla rail erased the stored speed");

            fixture.track(Blocks.BLUE_ICE, Fixture.RAIL);
            fixture.assertStep(cart, cart.getDeltaMovement().x, 0.75 * 2.0 * 0.997);
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void ruleChangesApplyToExistingCarts(GameTestHelper helper) throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.track(Blocks.BLUE_ICE, Fixture.RAIL);
            fixture.rules(3, false);
            Minecart cart = fixture.cart(true);
            fixture.assertStep(cart, 10.0, 2.0);
            fixture.rules(3, true);
            fixture.assertStep(cart, 10.0, 8.0 / 3.0);
            fixture.rules(0, true);
            fixture.assertStep(cart, 0.2, 0.2);
            fixture.rules(0, false);
            fixture.assertStep(cart, 0.2, 0.15);
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void derailedCartsUseVanillaClampingAndAirDrag(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.rules(3, true);
            fixture.track(Blocks.BLUE_ICE, Blocks.AIR.defaultBlockState());
            Minecart cart = fixture.cart(true);
            fixture.assertStep(cart, 10.0, 0.4);
            assertNear(helper, cart.getDeltaMovement().x, 0.4 * 0.95F,
                "Derailed cart retained the ice limit or changed vanilla air drag");
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:minecart_speed")
    public void iceSpeedRuleRejectsInvalidValuesWithoutChangingExistingIntegerRules(GameTestHelper helper)
        throws InvalidRuleValueException {
        try (Fixture fixture = new Fixture(helper)) {
            helper.assertTrue(CarpetEdtpAdditionSettings.iceRailSpeed.defaultValue() == 0, "Ice speed must default to vanilla");
            helper.assertFalse(CarpetEdtpAdditionSettings.minecartNoPassengerSpeedPenalty.defaultValue(),
                "Passenger multiplier rule must default to off");
            helper.assertTrue(CarpetEdtpAdditionSettings.iceRailSpeed.strict(), "Ice speed options must be strict");
            helper.assertTrue(CarpetEdtpAdditionSettings.iceRailSpeed.suggestions().equals(List.of("0", "1", "2", "3")),
                "Unexpected ice speed options");
            fixture.rules(2, false);
            for (String invalid : new String[] {"-1", "4", "true", "1.5"}) {
                boolean rejected = false;
                try {
                    CarpetEdtpAdditionSettings.iceRailSpeed.set(null, invalid);
                } catch (InvalidRuleValueException expected) {
                    rejected = true;
                }
                helper.assertTrue(rejected, "Accepted invalid ice speed: " + invalid);
                helper.assertTrue(CarpetEdtpAdditionSettings.iceRailSpeed.value() == 2, "Invalid input changed the rule");
            }
            boolean rejected = false;
            try {
                CarpetEdtpAdditionSettings.iceRailSpeed.set(null, 4);
            } catch (InvalidRuleValueException expected) {
                rejected = true;
            }
            helper.assertTrue(rejected, "Typed setter accepted an invalid ice speed");
            helper.assertFalse(CarpetEdtpAdditionSettings.villagerMaxEnchantLevel.strict(),
                "Existing integer rule validation changed");
            helper.succeed();
        }
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-6,
            message + ": expected " + expected + ", got " + actual);
    }

    private static class Fixture implements AutoCloseable {
        private static final BlockState RAIL = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST);
        private static final BlockState POWERED_RAIL = Blocks.POWERED_RAIL.defaultBlockState()
            .setValue(PoweredRailBlock.SHAPE, RailShape.EAST_WEST);
        private final GameTestHelper helper;
        private final ServerLevel level;
        private final BlockPos start;
        private final int originalSpeed = CarpetEdtpAdditionSettings.iceRailSpeed.value();
        private final boolean originalPenalty = CarpetEdtpAdditionSettings.minecartNoPassengerSpeedPenalty.value();
        private final List<Entity> entities = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            this.level = helper.getLevel();
            this.start = helper.absolutePos(new BlockPos(0, 2, 3));
        }

        private void rules(int speed, boolean removePenalty) throws InvalidRuleValueException {
            CarpetEdtpAdditionSettings.iceRailSpeed.set(null, speed);
            CarpetEdtpAdditionSettings.minecartNoPassengerSpeedPenalty.set(null, removePenalty);
        }

        private void track(Block support, BlockState rail) {
            BlockPos powerSource = start.east(1).north();
            level.setBlock(powerSource, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            for (int x = 0; x < 8; x++) {
                BlockPos pos = start.east(x);
                level.setBlock(pos.below(), support.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos, rail, Block.UPDATE_CLIENTS);
                for (int y = 1; y <= 3; y++) {
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            if (rail.is(Blocks.POWERED_RAIL) && rail.getValue(PoweredRailBlock.POWERED)) {
                level.setBlock(powerSource, Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                helper.assertTrue(level.getBlockState(start.east(1)).getValue(PoweredRailBlock.POWERED),
                    "Test powered rail has no redstone power");
            }
        }

        private Minecart cart(boolean passenger) {
            Minecart cart = EntityTypes.MINECART.create(level, EntitySpawnReason.COMMAND);
            helper.assertTrue(cart != null, "Failed to create minecart");
            helper.assertTrue(cart.getBehavior() instanceof OldMinecartBehavior, "Test requires default minecart movement");
            cart.setInitialPos(start.getX() + 1.25, start.getY() + 0.0625, start.getZ() + 0.5);
            entities.add(cart);
            if (passenger) {
                var rider = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
                helper.assertTrue(rider != null, "Failed to create passenger");
                rider.setNoAi(true);
                entities.add(rider);
                helper.assertTrue(rider.startRiding(cart, true, false), "Passenger failed to mount minecart");
            }
            return cart;
        }

        private void assertStep(AbstractMinecart cart, double velocity, double expected) {
            // Exercise the actual injected server tick; entities stay unregistered to avoid unrelated collisions.
            double x = cart.getX();
            cart.setDeltaMovement(velocity, 0.0, 0.0);
            cart.tick();
            assertNear(helper, cart.getX() - x, expected, "Unexpected displacement in one tick");
        }

        @Override
        public void close() throws InvalidRuleValueException {
            for (Entity entity : entities) {
                entity.discard();
            }
            rules(originalSpeed, originalPenalty);
        }
    }
}
