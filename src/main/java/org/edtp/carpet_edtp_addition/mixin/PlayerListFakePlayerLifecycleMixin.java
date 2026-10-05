package org.edtp.carpet_edtp_addition.mixin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.edtp.carpet_edtp_addition.util.FakePlayerConnectionLifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListFakePlayerLifecycleMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void edtp$finishFakePlayerLifecycle(ServerPlayer player, CallbackInfo ci) {
        edtp$finishLifecycle(player);
    }

    @Inject(method = "removeAll", at = @At("HEAD"))
    private void edtp$finishFakePlayerLifecyclesOnShutdown(CallbackInfo ci) {
        // Carpet ignores the shutdown kick, and fake connections never receive channelInactive.
        // Snapshot because a disconnect listener may modify the player list.
        for (ServerPlayer player : List.copyOf(((PlayerList) (Object) this).getPlayers())) {
            edtp$finishLifecycle(player);
        }
    }

    @Unique
    private static void edtp$finishLifecycle(ServerPlayer player) {
        if (player.connection instanceof FakePlayerConnectionLifecycle lifecycle) {
            lifecycle.edtp$finishConnectionLifecycle();
        }
    }
}
