package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.edtp.carpet_edtp_addition.util.SkeletonTrapSpawnChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @ModifyExpressionValue(
        method = "tickThunder",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"
        )
    )
    private boolean requireValidSkeletonTrapSpawnPosition(boolean isLightningRod, @Local BlockPos pos) {
        return isLightningRod || !SkeletonTrapSpawnChecks.allowsSpawn((ServerLevel) (Object) this, pos);
    }
}
