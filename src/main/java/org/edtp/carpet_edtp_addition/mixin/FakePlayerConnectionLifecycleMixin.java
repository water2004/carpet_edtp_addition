package org.edtp.carpet_edtp_addition.mixin;

import carpet.patches.NetHandlerPlayServerFake;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.edtp.carpet_edtp_addition.util.FakePlayerConnectionLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServerFake.class)
public abstract class FakePlayerConnectionLifecycleMixin extends ServerGamePacketListenerImpl
    implements FakePlayerConnectionLifecycle {
    @Unique
    private static final Logger EDTP_LOGGER = LoggerFactory.getLogger("CarpetEdtpAddition");

    @Unique
    private boolean edtp$managedLifecycle;

    @Unique
    private boolean edtp$lifecycleFinished;

    protected FakePlayerConnectionLifecycleMixin(
        MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie
    ) {
        super(server, connection, player, cookie);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void edtp$rememberLifecycleRule(CallbackInfo ci) {
        // Disabling the rule later must not abandon a connection created while it was enabled.
        edtp$managedLifecycle = CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.value();
    }

    @Override
    public void edtp$finishConnectionLifecycle() {
        if (edtp$lifecycleFinished
            || !(edtp$managedLifecycle || CarpetEdtpAdditionSettings.fakePlayerConnectionLifecycle.value())) {
            return;
        }
        // Set before invoking callbacks: a listener may synchronously remove this player again.
        edtp$lifecycleFinished = true;
        ServerPlayNetworkAddon addon = ServerNetworkingImpl.getAddon(this);
        try {
            // Fabric owns event dispatch and disconnect deduplication. JOIN already uses this addon.
            addon.handleDisconnect();
        } catch (RuntimeException exception) {
            EDTP_LOGGER.error("Fabric disconnect listener failed for Carpet fake player {} ({})",
                player.getPlainTextName(), player.getUUID(), exception);
        } finally {
            // Idempotent cleanup also covers an earlier failed native disconnect whose flag is set.
            // Fabric's handleDisconnect does not reach endSession when a listener throws.
            addon.endSession();
        }
    }
}
