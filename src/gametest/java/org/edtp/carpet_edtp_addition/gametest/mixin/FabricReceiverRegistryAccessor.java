package org.edtp.carpet_edtp_addition.gametest.mixin;

import java.util.Set;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlobalReceiverRegistry.class)
public interface FabricReceiverRegistryAccessor {
    @Accessor("trackedAddons")
    Set<?> edtp$getTrackedAddons();
}
