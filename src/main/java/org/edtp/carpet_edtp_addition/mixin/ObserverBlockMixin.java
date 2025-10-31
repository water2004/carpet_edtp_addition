package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {
    
    /**
     * 当 disableObservers 规则开启时,观察者不检测方块更新
     * 通过阻止 getStateForNeighborUpdate 方法安排更新来实现
     */
    @Inject(
        method = "getStateForNeighborUpdate",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableObserverUpdates(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        Random random,
        CallbackInfoReturnable<BlockState> cir
    ) {
        if (CarpetEdtpAdditionSettings.disableObservers.value()) {
            // 直接返回当前状态,不安排任何更新
            cir.setReturnValue(state);
        }
    }
}
