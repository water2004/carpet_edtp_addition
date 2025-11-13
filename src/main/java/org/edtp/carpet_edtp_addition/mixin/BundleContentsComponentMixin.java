package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContentsComponent.class)
public class BundleContentsComponentMixin {
    
    /**
     * 修改 canBeBundled 方法,允许潜影盒放入收纳袋
     */
    @Inject(method = "canBeBundled", at = @At("HEAD"), cancellable = true)
    private static void allowShulkerBoxes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetEdtpAdditionSettings.strongerBundle.value()) {
            // 如果是潜影盒,允许放入
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    if (!stack.isEmpty()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
    
    /**
     * 修改 getOccupancy 方法,设置潜影盒占用空间为 1/8
     * 这样一个收纳袋最多可以放 8 个潜影盒
     */
    @Inject(method = "getOccupancy", at = @At("HEAD"), cancellable = true)
    private static void setShulkerBoxOccupancy(ItemStack stack, CallbackInfoReturnable<Fraction> cir) {
        if (CarpetEdtpAdditionSettings.strongerBundle.value()) {
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    // 设置潜影盒占用 1/8 空间,所以最多可以放 8 个
                    cir.setReturnValue(Fraction.getFraction(1, 8));
                }
            }
        }
    }
}
