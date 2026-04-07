package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.Level;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public class BeeMixin {
	@Inject(method = "wantsToEnterHive", at = @At("HEAD"), cancellable = true)
	private void forceEnterHiveInNetherEnd(CallbackInfoReturnable<Boolean> cir) {
		Level world = ((EntityAccessor)(Object)this).carpet_edtp_addition$getWorld();
		if (CarpetEdtpAdditionSettings.isBeesDimCurfewEnabled(world)) {
			cir.setReturnValue(true);
		}
	}
}

@Mixin(Entity.class)
interface EntityAccessor {
	@Accessor("level")
	Level carpet_edtp_addition$getWorld();
}
