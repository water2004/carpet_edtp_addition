package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.chunk.LevelChunk;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @WrapOperation(
        method = "setBlockState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V",
            ordinal = 0
        )
    )
    private void deferActiveSculkSensorRemoval(
        LevelChunk chunk,
        BlockPos pos,
        Operation<Void> original,
        @Share("deferredSculkSensorRemoval") LocalBooleanRef deferred
    ) {
        BlockEntity blockEntity = chunk.getBlockEntity(pos);
        if (CarpetEdtpAdditionSettings.soundSuppressionReintroduced.value()
            && blockEntity instanceof SculkSensorBlockEntity
            && blockEntity.getBlockState().hasProperty(SculkSensorBlock.PHASE)
            && blockEntity.getBlockState().getValue(SculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE) {
            // In 1.21, the sensor notified its neighbours before its block entity was removed.
            // If that update was suppressed, control never reached the removal and the sensor survived.
            deferred.set(true);
            return;
        }

        original.call(chunk, pos);
    }

    @WrapOperation(
        method = "setBlockState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;affectNeighborsAfterRemoval(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Z)V"
        )
    )
    private void removeDeferredSculkSensorAfterVanillaSideEffects(
        BlockState oldState,
        ServerLevel level,
        BlockPos pos,
        boolean movedByPiston,
        Operation<Void> original,
        @Share("deferredSculkSensorRemoval") LocalBooleanRef deferred
    ) {
        original.call(oldState, level, pos, movedByPiston);
        removeDeferredSculkSensor(pos, deferred);
    }

    @WrapOperation(
        method = "setBlockState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;isValidBlockState(Lnet/minecraft/world/level/block/state/BlockState;)Z"
        )
    )
    private boolean allowRetainedSculkSensorState(BlockEntity blockEntity, BlockState state, Operation<Boolean> original) {
        if (CarpetEdtpAdditionSettings.soundSuppressionReintroduced.value()
            && blockEntity instanceof SculkSensorBlockEntity) {
            return true;
        }
        return original.call(blockEntity, state);
    }

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void cleanUpDeferredSculkSensor(
        BlockPos pos,
        BlockState state,
        int flags,
        CallbackInfoReturnable<BlockState> cir,
        @Share("deferredSculkSensorRemoval") LocalBooleanRef deferred
    ) {
        removeDeferredSculkSensor(pos, deferred);
    }

    @Unique
    private void removeDeferredSculkSensor(BlockPos pos, LocalBooleanRef deferred) {
        if (deferred.get()) {
            ((LevelChunk) (Object) this).removeBlockEntity(pos);
            deferred.set(false);
        }
    }
}
