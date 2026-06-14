package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @Shadow
    protected NonNullList<ItemStack> items;

    @Shadow
    @Final
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    @Shadow
    private int litTimeRemaining;

    @Shadow
    private int cookingTimer;

    @Inject(
        method = "serverTick",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void passthroughNoRecipeInput(
        ServerLevel level,
        BlockPos pos,
        BlockState state,
        AbstractFurnaceBlockEntity blockEntity,
        CallbackInfo ci
    ) {
        if (!CarpetEdtpAdditionSettings.noFurnaceAsh.value()) {
            return;
        }

        AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin) (Object) blockEntity;
        if (self.passthroughNoRecipeInput(level, pos, state, blockEntity)) {
            ci.cancel();
        }
    }

    @SuppressWarnings("null")
    private boolean passthroughNoRecipeInput(
        ServerLevel level,
        BlockPos pos,
        BlockState state,
        AbstractFurnaceBlockEntity blockEntity
    ) {
        ItemStack inputStack = this.items.get(0);
        if (inputStack.isEmpty()) {
            return false;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
        if (this.quickCheck.getRecipeFor(recipeInput, level).isPresent()) {
            return false;
        }

        this.tickLitState(level, pos, state, blockEntity);

        ItemStack outputStack = this.items.get(2);
        int maxStackSize = Math.min(inputStack.getMaxStackSize(), blockEntity.getMaxStackSize(inputStack));
        int transferCount;

        if (outputStack.isEmpty()) {
            transferCount = Math.min(inputStack.getCount(), maxStackSize);
            this.items.set(2, inputStack.copyWithCount(transferCount));
        } else if (ItemStack.isSameItemSameComponents(outputStack, inputStack)) {
            transferCount = Math.min(inputStack.getCount(), maxStackSize - outputStack.getCount());
            if (transferCount <= 0) {
                return true;
            }
            outputStack.grow(transferCount);
        } else {
            return true;
        }

        inputStack.shrink(transferCount);
        this.cookingTimer = 0;
        blockEntity.setChanged();
        return true;
    }

    @SuppressWarnings("null")
    private void tickLitState(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
        boolean wasLit = this.litTimeRemaining > 0;
        if (this.litTimeRemaining > 0) {
            this.litTimeRemaining--;
        }

        boolean isLit = this.litTimeRemaining > 0;
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, isLit), 3);
            blockEntity.setChanged();
        }
    }
}
