package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import org.edtp.carpet_edtp_addition.dispenser.CauldronBucketDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenseItemBehavior.class)
public interface DispenserBehaviorMixin {
    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void registerCauldronBucketBehavior(CallbackInfo ci) {
        DispenseItemBehavior waterFallback = DispenserBlock.DISPENSER_REGISTRY.get(Items.WATER_BUCKET);
        DispenseItemBehavior lavaFallback = DispenserBlock.DISPENSER_REGISTRY.get(Items.LAVA_BUCKET);
        DispenseItemBehavior powderSnowFallback = DispenserBlock.DISPENSER_REGISTRY.get(Items.POWDER_SNOW_BUCKET);
        DispenseItemBehavior emptyFallback = DispenserBlock.DISPENSER_REGISTRY.get(Items.BUCKET);

        if (waterFallback != null) {
            DispenserBlock.registerBehavior(
                Items.WATER_BUCKET,
                new CauldronBucketDispenserBehavior(waterFallback, CauldronBucketDispenserBehavior.Mode.WATER_BUCKET)
            );
        }
        if (lavaFallback != null) {
            DispenserBlock.registerBehavior(
                Items.LAVA_BUCKET,
                new CauldronBucketDispenserBehavior(lavaFallback, CauldronBucketDispenserBehavior.Mode.LAVA_BUCKET)
            );
        }
        if (powderSnowFallback != null) {
            DispenserBlock.registerBehavior(
                Items.POWDER_SNOW_BUCKET,
                new CauldronBucketDispenserBehavior(powderSnowFallback, CauldronBucketDispenserBehavior.Mode.POWDER_SNOW_BUCKET)
            );
        }
        if (emptyFallback != null) {
            DispenserBlock.registerBehavior(
                Items.BUCKET,
                new CauldronBucketDispenserBehavior(emptyFallback, CauldronBucketDispenserBehavior.Mode.EMPTY_BUCKET)
            );
        }
    }
}
