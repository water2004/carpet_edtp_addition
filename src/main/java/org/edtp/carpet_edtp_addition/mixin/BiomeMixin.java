package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    private void preventSnowAccumulationBelowYZero(
        LevelReader level,
        BlockPos pos,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (CarpetEdtpAdditionSettings.snowlessDepths.value() && pos.getY() < 0) {
            cir.setReturnValue(false);
        }
    }
}
