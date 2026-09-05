package org.edtp.carpet_edtp_addition.gametest.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.edtp.carpet_edtp_addition.gametest.SoundSuppressionTestHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerGameTestMixin {
    // Observe vanilla's error handler without cancelling or replacing it.
    @Inject(method = "onPacketError", at = @At("HEAD"))
    private void recordPlayerPacketError(Packet<?> packet, Exception error, CallbackInfo ci) {
        if ((Object) this instanceof ServerGamePacketListenerImpl listener) {
            SoundSuppressionTestHooks.recordPacketError(listener.player.getUUID(), error);
        }
    }
}
