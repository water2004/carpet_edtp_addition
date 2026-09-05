package org.edtp.carpet_edtp_addition.bucket;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public final class ResonantWaterBehavior {
    private ResonantWaterBehavior() {
    }

    public static boolean tryPreserveWaterBucket(Player player, Fluid fluid) {
        if (!CarpetEdtpAdditionSettings.resonantWater.value()
            || fluid != Fluids.WATER
            || player.isCreative()) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        if (selectedSlot <= 0 || selectedSlot >= 8) {
            return false;
        }

        ItemStack leftStack = inventory.getItem(selectedSlot - 1);
        ItemStack rightStack = inventory.getItem(selectedSlot + 1);
        if (!leftStack.is(Items.WATER_BUCKET) || !rightStack.is(Items.WATER_BUCKET)) {
            return false;
        }

        player.level().playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
            SoundSource.PLAYERS,
            0.5F,
            1.0F
        );
        return true;
    }
}
