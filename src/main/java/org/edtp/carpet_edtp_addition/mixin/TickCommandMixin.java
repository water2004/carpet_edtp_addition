package org.edtp.carpet_edtp_addition.mixin;

import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TickCommand;
import com.mojang.brigadier.builder.ArgumentBuilder;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    
    @Redirect(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
            remap = false
        ),
        require = 1
    )
    private static ArgumentBuilder<CommandSourceStack, ?> redirectTickCommandPermission(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> builder, Predicate<CommandSourceStack> originalRequirement) {
        return builder.requires(source -> CarpetEdtpAdditionSettings.tickCommandForAll.value() || originalRequirement.test(source));
    }
}
