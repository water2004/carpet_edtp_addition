package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin {

    // Targeting the mapped static method releaseBee with its descriptor so mixin can resolve it.
    @Inject(
        method = "releaseBee(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeehiveBlockEntity$BeeData;Ljava/util/List;Lnet/minecraft/block/entity/BeehiveBlockEntity$BeeState;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void preventReleaseInNetherEnd(
        World world,
        BlockPos pos,
        BlockState state,
        @Coerce Object beeData,
        @Nullable List<?> entities,
        @Coerce Object beeState,
        @Nullable BlockPos flowerPos,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!CarpetEdtpAdditionSettings.noBeesInNetherEnd.value()) {
            return;
        }
        if (world == null) {
            return;
        }
        if (world.getRegistryKey() == World.NETHER || world.getRegistryKey() == World.END) {
            cir.setReturnValue(false);
        }
    }
}
