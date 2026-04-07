package org.edtp.carpet_edtp_addition.util;

import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockHardnessModifiers {
    
    /**
     * 获取方块硬度修改值
     * @param block 方块
     * @param world 世界
     * @param pos 位置
     * @return 修改后的硬度值，如果没有修改则返回空
     */
    public static Optional<Float> getHardness(Block block, BlockGetter world, BlockPos pos) {
        // 易碎黑曜石
        if (CarpetEdtpAdditionSettings.softObsidian.value() && (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN)) {
            return Optional.of(Blocks.END_STONE.defaultDestroyTime());
        }
        
        // 坚韧粘液块
        if (CarpetEdtpAdditionSettings.toughSlimeBlocks.value() && (block == Blocks.SLIME_BLOCK || block == Blocks.HONEY_BLOCK)) {
            return Optional.of(Blocks.END_STONE.defaultDestroyTime());
        }
        
        return Optional.empty();
    }
}