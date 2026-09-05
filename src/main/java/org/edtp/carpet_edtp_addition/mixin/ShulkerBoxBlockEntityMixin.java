package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.edtp.carpet_edtp_addition.bundle.StrongerBundlePolicy;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {
    
    /**
     * 当 strongerBundle 规则开启时,禁止收纳袋通过自动化方式放入潜影盒
     * 防止无限嵌套循环
     */
    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void preventBundleAutoInsertion(int slot, ItemStack stack, @Nullable Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (StrongerBundlePolicy.blocksShulkerBoxInsertion(stack)) {
            cir.setReturnValue(false);
        }
    }
}
