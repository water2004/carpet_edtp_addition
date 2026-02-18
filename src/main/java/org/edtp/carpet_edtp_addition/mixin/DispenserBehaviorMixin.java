package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Items;
import org.edtp.carpet_edtp_addition.dispenser.CauldronBucketDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBehavior.class)
public interface DispenserBehaviorMixin {
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    private static void registerCauldronBucketBehavior(CallbackInfo ci) {
        DispenserBehavior waterFallback = DispenserBlock.BEHAVIORS.get(Items.WATER_BUCKET);
        DispenserBehavior lavaFallback = DispenserBlock.BEHAVIORS.get(Items.LAVA_BUCKET);
        DispenserBehavior powderSnowFallback = DispenserBlock.BEHAVIORS.get(Items.POWDER_SNOW_BUCKET);
        DispenserBehavior emptyFallback = DispenserBlock.BEHAVIORS.get(Items.BUCKET);

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
