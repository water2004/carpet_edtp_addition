package org.edtp.carpet_edtp_addition.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.LookTarget;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
    
    @Inject(
        method = "teleport",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkSafeTeleport(
        ServerCommandSource source,
        Entity target,
        ServerWorld world,
        double x,
        double y,
        double z,
        Set<PositionFlag> movementFlags,
        float yaw,
        float pitch,
        @Nullable LookTarget facingLocation,
        CallbackInfo ci
    ) throws CommandSyntaxException {
        if (!CarpetEdtpAdditionSettings.safeTeleport.value()) {
            return;
        }
        
        BlockPos targetPos = BlockPos.ofFloored(x, y, z);
        
        // 检查是否在虚空中 (y < 世界最小高度)
        if (y < world.getBottomY()) {
            source.sendError(Text.translatable("carpet.rule.safeTeleport.void"));
            ci.cancel();
            return;
        }
        
        // 检查目标位置是否有方块(可能导致窒息)
        BlockState stateAtPos = world.getBlockState(targetPos);
        BlockState stateAbove = world.getBlockState(targetPos.up());
        
        // 如果目标位置或其上方一格有实心方块,则视为不安全
        if (!stateAtPos.isAir() && !stateAtPos.getCollisionShape(world, targetPos).isEmpty()) {
            source.sendError(Text.translatable("carpet.rule.safeTeleport.suffocation"));
            ci.cancel();
            return;
        }
        
        if (!stateAbove.isAir() && !stateAbove.getCollisionShape(world, targetPos.up()).isEmpty()) {
            source.sendError(Text.translatable("carpet.rule.safeTeleport.suffocation"));
            ci.cancel();
            return;
        }
    }
}
