package org.edtp.carpet_edtp_addition.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    
    @Inject(
        method = "performTeleport",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkSafeTeleport(
        CommandSourceStack source,
        Entity target,
        ServerLevel world,
        double x,
        double y,
        double z,
        Set<Relative> movementFlags,
        float yaw,
        float pitch,
        @Nullable LookAt facingLocation,
        CallbackInfo ci
    ) throws CommandSyntaxException {
        if (!CarpetEdtpAdditionSettings.safeTeleport.value()) {
            return;
        }
        
        BlockPos targetPos = BlockPos.containing(x, y, z);
        
        // 检查是否在虚空中 (y < 世界最小高度)
        if (y < world.getMinY()) {
            source.sendFailure(Component.translatable("carpet.rule.safeTeleport.void"));
            ci.cancel();
            return;
        }
        
        // 检查目标位置是否有方块(可能导致窒息)
        BlockState stateAtPos = world.getBlockState(targetPos);
        BlockState stateAbove = world.getBlockState(targetPos.above());
        
        // 如果目标位置或其上方一格有实心方块,则视为不安全
        if (!stateAtPos.isAir() && !stateAtPos.getCollisionShape(world, targetPos).isEmpty()) {
            source.sendFailure(Component.translatable("carpet.rule.safeTeleport.suffocation"));
            ci.cancel();
            return;
        }
        
        if (!stateAbove.isAir() && !stateAbove.getCollisionShape(world, targetPos.above()).isEmpty()) {
            source.sendFailure(Component.translatable("carpet.rule.safeTeleport.suffocation"));
            ci.cancel();
            return;
        }
    }
}
