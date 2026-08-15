package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @WrapWithCondition(
        method = "fallOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
        )
    )
    private boolean allowFarmlandTrampling(Entity sourceEntity, BlockState state, Level level, BlockPos pos) {
        if (sourceEntity instanceof Player player) {
            return !CarpetEdtpAdditionSettings.featherFallingPreventsFarmlandTrampling.value()
                || !hasFeatherFalling(player, level);
        }
        return !CarpetEdtpAdditionSettings.noMobFarmlandTrampling.value();
    }

    @Unique
    private static boolean hasFeatherFalling(Player player, Level level) {
        Holder<Enchantment> featherFalling = level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.FEATHER_FALLING);
        return EnchantmentHelper.getItemEnchantmentLevel(
            featherFalling,
            player.getItemBySlot(EquipmentSlot.FEET)
        ) > 0;
    }
}
