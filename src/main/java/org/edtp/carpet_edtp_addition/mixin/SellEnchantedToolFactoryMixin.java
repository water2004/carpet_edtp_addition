package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.edtp.carpet_edtp_addition.villager.VillagerTradeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class SellEnchantedToolFactoryMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void adjustToolsmithTrades(ServerLevel level, CallbackInfo ci) {
        VillagerTradeModifiers.adjustToolsmithTrades((Villager) (Object) this, level);
    }
}
