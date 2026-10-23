package org.edtp.carpet_edtp_addition.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class FarmlandTramplingGameTests {
    @GameTest(environment = "carpet_edtp_addition_gametest:farmland_trampling")
    public void mobTramplingRuleProtectsFarmlandWithoutProtectingPlayers(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            var pig = EntityTypes.PIG.create(fixture.level, EntitySpawnReason.COMMAND);
            helper.assertTrue(pig != null, "Failed to create trampling mob");
            fixture.fallOn(pig, false);
            CarpetEdtpAdditionSettings.noMobFarmlandTrampling.set(null, true);
            fixture.fallOn(pig, true);
            fixture.fallOn(helper.makeMockPlayer(GameType.SURVIVAL), false);
            CarpetEdtpAdditionSettings.noMobFarmlandTrampling.set(null, false);
            fixture.fallOn(pig, false);
            helper.succeed();
        }
    }

    @GameTest(environment = "carpet_edtp_addition_gametest:farmland_trampling")
    public void featherFallingRuleOnlyProtectsPlayersWearingEnchantedBoots(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
            boots.enchant(fixture.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FEATHER_FALLING), 1);
            player.setItemSlot(EquipmentSlot.FEET, boots);
            fixture.fallOn(player, false);
            CarpetEdtpAdditionSettings.featherFallingPreventsFarmlandTrampling.set(null, true);
            fixture.fallOn(player, true);
            player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            fixture.fallOn(player, false);
            var pig = EntityTypes.PIG.create(fixture.level, EntitySpawnReason.COMMAND);
            helper.assertTrue(pig != null, "Failed to create trampling mob");
            fixture.fallOn(pig, false);
            helper.succeed();
        }
    }

    private static class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerLevel level;
        private final BlockPos pos;
        private final boolean originalMobRule = CarpetEdtpAdditionSettings.noMobFarmlandTrampling.value();
        private final boolean originalFeatherRule = CarpetEdtpAdditionSettings.featherFallingPreventsFarmlandTrampling.value();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            this.level = helper.getLevel();
            this.pos = helper.absolutePos(new BlockPos(3, 2, 3));
            helper.assertTrue(level.getGameRules().get(GameRules.MOB_GRIEFING), "Test requires mobGriefing");
            CarpetEdtpAdditionSettings.noMobFarmlandTrampling.set(null, false);
            CarpetEdtpAdditionSettings.featherFallingPreventsFarmlandTrampling.set(null, false);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        private void fallOn(Entity entity, boolean protectedFarmland) {
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
            // A distance of 2 makes the vanilla trample chance certain without causing fall damage.
            Blocks.FARMLAND.fallOn(level, level.getBlockState(pos), pos, entity, 2.0);
            helper.assertTrue(level.getBlockState(pos).is(protectedFarmland ? Blocks.FARMLAND : Blocks.DIRT),
                "Unexpected farmland state after trampling by " + entity.getType());
        }

        @Override
        public void close() {
            CarpetEdtpAdditionSettings.noMobFarmlandTrampling.set(null, originalMobRule);
            CarpetEdtpAdditionSettings.featherFallingPreventsFarmlandTrampling.set(null, originalFeatherRule);
        }
    }
}
