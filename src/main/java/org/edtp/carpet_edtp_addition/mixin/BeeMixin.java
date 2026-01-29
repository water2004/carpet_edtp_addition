package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.world.World;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeeEntity.class)
public class BeeMixin {
	@Inject(method = "canEnterHive", at = @At("HEAD"), cancellable = true)
	private void forceEnterHiveInNetherEnd(CallbackInfoReturnable<Boolean> cir) {
		if (!CarpetEdtpAdditionSettings.noBeesInNetherEnd.value()) {
			return;
		}
		World world = ((EntityAccessor)(Object)this).carpet_edtp_addition$getWorld();
		if (world != null && (world.getRegistryKey() == World.NETHER || world.getRegistryKey() == World.END)) {
			cir.setReturnValue(true);
		}
	}
}

@Mixin(Entity.class)
interface EntityAccessor {
	@Accessor("world")
	World carpet_edtp_addition$getWorld();
}
