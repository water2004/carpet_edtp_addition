package org.edtp.carpet_edtp_addition.mixin;

import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin {

    // Targeting the mapped static method releaseBee with its descriptor so mixin can resolve it.
    @Inject(
        method = "releaseOccupant(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity$Occupant;Ljava/util/List;Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity$BeeReleaseStatus;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void preventReleaseInNetherEnd(
        Level world,
        BlockPos pos,
        BlockState state,
        @Coerce Object beeData,
        @Nullable List<?> entities,
        @Coerce Object beeState,
        @Nullable BlockPos flowerPos,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (world == null) {
            return;
        }
        if (CarpetEdtpAdditionSettings.isBeesDimCurfewEnabled(world)) {
            cir.setReturnValue(false);
        }
    }
}
