package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartPassengerSpeedMixin {
    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.75))
    private double edtp$passengerMovementMultiplier(double original) {
        return CarpetEdtpAdditionSettings.minecartNoPassengerSpeedPenalty.value() ? 1.0 : original;
    }
}
