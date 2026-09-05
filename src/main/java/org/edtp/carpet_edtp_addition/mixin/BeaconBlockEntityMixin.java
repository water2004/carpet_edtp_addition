package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
    @ModifyExpressionValue(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getGameTime()J")
    )
    private static long staggerPeriodicWork(
        long gameTime, Level level, BlockPos pos, BlockState state, BeaconBlockEntity beacon
    ) {
        if (level.isClientSide() || !CarpetEdtpAdditionSettings.staggeredBeacons.value()) {
            return gameTime;
        }
        // Shift only the 80-tick base/effect/sound cycle; keep beam scanning every tick.
        return gameTime + Math.floorMod(Mth.getSeed(pos), 80L);
    }
}
