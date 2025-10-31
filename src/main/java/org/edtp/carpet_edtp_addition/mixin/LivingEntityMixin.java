package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    /**
     * Prevents armor stands from taking knockback when the unPushableArmorStands rule is enabled
     */
    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void preventArmorStandKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ArmorStandEntity && CarpetEdtpAdditionSettings.unPushableArmorStands.value()) {
            ci.cancel();
        }
    }
    
    /**
     * 禁止玩家使用传送门
     * 只对 PlayerEntity 实例生效
     */
    @Inject(
        method = "canUsePortals(Z)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disablePlayerPortals(boolean allowVehicles, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity && CarpetEdtpAdditionSettings.noPlayerPortals.value()) {
            cir.setReturnValue(false);
        }
    }
}