package org.edtp.carpet_edtp_addition.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartIceSpeedMixin extends MinecartBehavior {
    protected OldMinecartIceSpeedMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double edtp$iceSpeedLimit(double original) {
        int mode = CarpetEdtpAdditionSettings.iceRailSpeed.value();
        if (mode == 0) {
            return original;
        }
        BlockState support = edtp$railSupport();
        double compensation = mode == 3 ? 1.0 / 0.75 : 1.0;
        if (support.is(Blocks.PACKED_ICE)) {
            return compensation;
        }
        if (support.is(Blocks.BLUE_ICE)) {
            return (mode == 1 ? 1.0 : 2.0) * compensation;
        }
        return original;
    }

    @ModifyArg(
        method = "moveAlongTrack",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"),
        index = 0
    )
    private double edtp$iceInternalSpeedLimit(double original) {
        if (CarpetEdtpAdditionSettings.iceRailSpeed.value() == 3) {
            BlockState support = edtp$railSupport();
            if (support.is(Blocks.PACKED_ICE) || support.is(Blocks.BLUE_ICE)) {
                // The vanilla internal cap must also allow 2 blocks/tick with its 0.75 passenger multiplier.
                return original / 0.75;
            }
        }
        return original;
    }

    @Unique
    private BlockState edtp$railSupport() {
        BlockPos pos = minecart.getCurrentBlockPosOrRailBelow();
        return BaseRailBlock.isRail(level().getBlockState(pos))
            ? level().getBlockState(pos.below()) : Blocks.AIR.defaultBlockState();
    }
}
