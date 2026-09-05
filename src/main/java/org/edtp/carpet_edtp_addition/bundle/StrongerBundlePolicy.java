package org.edtp.carpet_edtp_addition.bundle;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public final class StrongerBundlePolicy {
    private StrongerBundlePolicy() {
    }

    public static boolean allowsInBundle(ItemStack stack) {
        return CarpetEdtpAdditionSettings.strongerBundle.value()
            && !stack.isEmpty()
            && stack.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean usesShulkerBoxWeight(ItemInstance stack) {
        return CarpetEdtpAdditionSettings.strongerBundle.value()
            && stack.typeHolder().value() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean blocksShulkerBoxInsertion(ItemStack stack) {
        return CarpetEdtpAdditionSettings.strongerBundle.value()
            && stack.getItem() instanceof BundleItem;
    }
}
