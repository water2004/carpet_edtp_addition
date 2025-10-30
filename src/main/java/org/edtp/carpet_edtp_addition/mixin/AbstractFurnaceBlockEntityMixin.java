package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    
    /**
     * 修改 getCookTime 方法中的常量 200 为 1
     * 使得没有配方的物品只需要1tick即可输出
     */
    @ModifyConstant(
        method = "getCookTime",
        constant = @Constant(intValue = 200)
    )
    private static int modifyDefaultCookTime(int original) {
        if (CarpetEdtpAdditionSettings.noFurnaceAsh.value()) {
            return 1;
        }
        return original;
    }
    
    /**
     * 修改 canAcceptRecipeOutput 方法，当没有配方但规则开启时，允许接受输出
     */
    @Inject(
        method = "canAcceptRecipeOutput",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void allowPassthroughOutput(
        DynamicRegistryManager dynamicRegistryManager,
        @Nullable RecipeEntry<?> recipe,
        SingleStackRecipeInput input,
        DefaultedList<ItemStack> inventory,
        int maxCount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!CarpetEdtpAdditionSettings.noFurnaceAsh.value()) {
            return;
        }
        
        ItemStack inputStack = inventory.get(0);
        
        // 如果没有配方但有输入物品，检查是否可以直接输出
        if (recipe == null && !inputStack.isEmpty()) {
            ItemStack outputStack = inventory.get(2);
            
            // 输出槽为空，可以接受
            if (outputStack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }
            
            // 输出槽有物品，检查是否可以堆叠
            if (ItemStack.areItemsAndComponentsEqual(outputStack, inputStack)) {
                // 计算可以添加的数量
                int maxStackSize = Math.min(inputStack.getMaxCount(), maxCount);
                // 如果输出槽还有空间，可以接受
                if (outputStack.getCount() < maxStackSize) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            
            // 无法接受输出
            cir.setReturnValue(false);
        }
    }
    
    /**
     * 修改 craftRecipe 方法，当没有配方但规则开启时，直接输出原物品（尽可能多）
     */
    @Inject(
        method = "craftRecipe",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void passthroughCraft(
        DynamicRegistryManager dynamicRegistryManager,
        @Nullable RecipeEntry<?> recipe,
        SingleStackRecipeInput input,
        DefaultedList<ItemStack> inventory,
        int maxCount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!CarpetEdtpAdditionSettings.noFurnaceAsh.value()) {
            return;
        }
        
        // 如果没有配方但有输入物品，直接原样输出
        if (recipe == null && !inventory.get(0).isEmpty()) {
            ItemStack inputStack = inventory.get(0);
            ItemStack outputStack = inventory.get(2);
            
            // 计算最大堆叠数
            int maxStackSize = Math.min(inputStack.getMaxCount(), maxCount);
            
            if (outputStack.isEmpty()) {
                // 输出槽为空，尽可能多地输出
                int transferCount = Math.min(inputStack.getCount(), maxStackSize);
                inventory.set(2, inputStack.copyWithCount(transferCount));
                inputStack.decrement(transferCount);
                cir.setReturnValue(true);
                return;
            } else if (ItemStack.areItemsAndComponentsEqual(outputStack, inputStack)) {
                // 输出槽有相同物品，尝试堆叠
                int availableSpace = maxStackSize - outputStack.getCount();
                if (availableSpace > 0) {
                    // 计算实际转移数量
                    int transferCount = Math.min(inputStack.getCount(), availableSpace);
                    outputStack.increment(transferCount);
                    inputStack.decrement(transferCount);
                    cir.setReturnValue(true);
                    return;
                }
            }
            
            // 无法输出
            cir.setReturnValue(false);
        }
    }
}
