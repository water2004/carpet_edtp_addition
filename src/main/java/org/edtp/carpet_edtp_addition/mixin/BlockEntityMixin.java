package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Inject(method = "validateBlockState", at = @At("HEAD"), cancellable = true)
    private void allowSuppressedSculkSensorState(BlockState state, CallbackInfo ci) {
        if (CarpetEdtpAdditionSettings.soundSuppressionReintroduced.value()
            && (Object) this instanceof SculkSensorBlockEntity) {
            ci.cancel();
        }
    }
}
