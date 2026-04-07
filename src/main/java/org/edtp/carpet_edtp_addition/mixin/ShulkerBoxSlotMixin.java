package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {
    
    /**
     * 当 strongerBundle 规则开启时,禁止收纳袋手动放入潜影盒
     * 防止无限嵌套循环
     */
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void preventBundleInsertion(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetEdtpAdditionSettings.strongerBundle.value()) {
            System.out.println("ShulkerBoxSlot canInsert called! Stack: " + stack.getItem().getName().getString());
            // 如果是收纳袋,禁止放入
            if (stack.getItem() instanceof BundleItem) {
                System.out.println("Blocking bundle insertion into shulker box via GUI!");
                cir.setReturnValue(false);
            }
        }
    }
}
