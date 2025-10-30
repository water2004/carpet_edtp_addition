package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}