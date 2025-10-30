package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.edtp.carpet_edtp_addition.util.BlockHardnessModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = AbstractBlockState.class, priority = 999)
public abstract class AbstractBlockStateMixin {
    @Shadow
    public abstract Block getBlock();

    /**
     * 修改方块硬度
     */
    @ModifyReturnValue(method = "getHardness", at = @At("RETURN"))
    public float getBlockHardness(float hardness, @Local(argsOnly = true) BlockView world, @Local(argsOnly = true) BlockPos pos) {
        Optional<Float> optional = BlockHardnessModifiers.getHardness(this.getBlock(), world, pos);
        return optional.orElse(hardness);
    }
}