package org.edtp.carpet_edtp_addition.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.ChunkDeleteLightFix;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkDeleteLightFix.class)
public abstract class ChunkDeleteLightFixMixin {
    @ModifyArg(
        method = "makeRule",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/fixes/ChunkDeleteLightFix;fixTypeEverywhereTyped(Ljava/lang/String;Lcom/mojang/datafixers/types/Type;Ljava/util/function/Function;)Lcom/mojang/datafixers/TypeRewriteRule;"
        ),
        index = 2
    )
    private Function<Typed<?>, Typed<?>> preserveValidLighting(Function<Typed<?>, Typed<?>> original) {
        // DFU caches this converter: check the rule while converting each chunk, not while building it.
        return chunk -> {
            if (CarpetEdtpAdditionSettings.preserveLightOnUpgrade.value()
                && chunk.get(DSL.remainderFinder()).get("isLightOn").asBoolean(false)) {
                return chunk;
            }
            return original.apply(chunk);
        };
    }
}
