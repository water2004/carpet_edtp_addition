package org.edtp.carpet_edtp_addition.mixin;

import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.edtp.carpet_edtp_addition.teleport.SafeTeleportChecks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {
    @Inject(method = "performTeleport", at = @At("HEAD"), cancellable = true)
    private static void checkSafeTeleport(
        CommandSourceStack source,
        Entity target,
        ServerLevel level,
        double x,
        double y,
        double z,
        Set<Relative> movementFlags,
        float yaw,
        float pitch,
        @Nullable LookAt facingLocation,
        CallbackInfo ci
    ) {
        String failureTranslationKey = SafeTeleportChecks.findFailureTranslationKey(level, x, y, z);
        if (failureTranslationKey != null) {
            source.sendFailure(Component.translatable(failureTranslationKey));
            ci.cancel();
        }
    }
}
