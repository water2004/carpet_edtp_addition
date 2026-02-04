package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    @Shadow
    private Fluid fluid;

    /**
     * 实现"左右逢源"(Resonant Water)功能：拦截 ItemUsage.exchangeStack 的调用
     * 当快捷栏中当前水桶的左右都是水桶时，使用水不消耗桶
     */
    @Redirect(
        method = "use",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsage;exchangeStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack redirectExchangeStack(ItemStack itemStack, PlayerEntity user, ItemStack replacement) {
        // 只在规则启用时执行检测
        if (CarpetEdtpAdditionSettings.resonantWater.value() && 
            this.fluid == Fluids.WATER && 
            !user.isCreative()) {
            
            // 获取玩家背包
            PlayerInventory inv = user.getInventory();
            int currentSlot = inv.getSelectedSlot();

            // 只有当当前格子不在边缘时（0 到 8），才有左右邻居
            if (currentSlot > 0 && currentSlot < 8) {
                ItemStack leftStack = inv.getStack(currentSlot - 1);
                ItemStack rightStack = inv.getStack(currentSlot + 1);

                // 检测：左边是水桶 && 右边是水桶
                if (leftStack.isOf(Items.WATER_BUCKET) && rightStack.isOf(Items.WATER_BUCKET)) {
                    // 满足条件！不消耗水桶，返回原物品（满的水桶）
                    // 播放一个升级音效提示玩家触发了无限水
                    user.getEntityWorld().playSound(null, user.getX(), user.getY(), user.getZ(), 
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 0.5F, 1.0F);
                    
                    // 创建一个新的满水桶作为 replacement，这样虽然倒了水但桶不会变成空桶
                    return ItemUsage.exchangeStack(itemStack, user, new ItemStack(Items.WATER_BUCKET));
                }
            }
        }
        
        // 默认行为：使用原始的 replacement（通常是空桶）
        return ItemUsage.exchangeStack(itemStack, user, replacement);
    }
}
