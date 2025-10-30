package org.edtp.carpet_edtp_addition.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TickCommand;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    
    @ModifyArg(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
            ordinal = 0
        )
    )
    private static Predicate<ServerCommandSource> modifyTickCommandPermission(Predicate<ServerCommandSource> original) {
        return source -> CarpetEdtpAdditionSettings.tickCommandForAll.value() || original.test(source);
    }
}
