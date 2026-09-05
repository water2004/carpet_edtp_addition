package org.edtp.carpet_edtp_addition.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.edtp.carpet_edtp_addition.bundle.StrongerBundlePolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.class)
public class BundleContentsComponentMixin {
    
    /**
     * 修改 canBeBundled 方法,允许潜影盒放入收纳袋
     */
    @Inject(method = "canItemBeInBundle", at = @At("HEAD"), cancellable = true)
    private static void allowShulkerBoxes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (StrongerBundlePolicy.allowsInBundle(stack)) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * 修改 getOccupancy 方法,设置潜影盒占用空间为 1/8
     * 这样一个收纳袋最多可以放 8 个潜影盒
     */
    @Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
    private static void setShulkerBoxOccupancy(
        ItemInstance stack,
        CallbackInfoReturnable<DataResult<Fraction>> cir
    ) {
        if (StrongerBundlePolicy.usesShulkerBoxWeight(stack)) {
            cir.setReturnValue(DataResult.success(Fraction.getFraction(1, 8)));
        }
    }
}
