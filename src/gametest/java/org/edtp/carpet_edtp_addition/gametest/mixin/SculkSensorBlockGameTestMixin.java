package org.edtp.carpet_edtp_addition.gametest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.gametest.SoundSuppressionTestHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public abstract class SculkSensorBlockGameTestMixin {
    @Inject(method = "affectNeighborsAfterRemoval", at = @At("TAIL"))
    private void simulateUpdateSuppression(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        boolean movedByPiston,
        CallbackInfo ci
    ) {
        SoundSuppressionTestHooks.throwIfArmed(pos);
    }
}
