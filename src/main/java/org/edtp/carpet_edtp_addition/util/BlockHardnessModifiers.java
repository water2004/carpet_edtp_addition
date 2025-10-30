package org.edtp.carpet_edtp_addition.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

import java.util.Optional;

public class BlockHardnessModifiers {
    
    /**
     * 获取方块硬度修改值
     * @param block 方块
     * @param world 世界
     * @param pos 位置
     * @return 修改后的硬度值，如果没有修改则返回空
     */
    public static Optional<Float> getHardness(Block block, BlockView world, BlockPos pos) {
        // 易碎黑曜石
        if (CarpetEdtpAdditionSettings.softObsidian.value() && (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN)) {
            return Optional.of(Blocks.END_STONE.getHardness());
        }
        
        return Optional.empty();
    }
}