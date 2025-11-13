package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
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
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void preventBundleAutoInsertion(int slot, ItemStack stack, @Nullable Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetEdtpAdditionSettings.strongerBundle.value()) {
            // 如果是收纳袋,禁止放入
            if (stack.getItem() instanceof BundleItem) {
                
                cir.setReturnValue(false);
            }
        }
    }
}
