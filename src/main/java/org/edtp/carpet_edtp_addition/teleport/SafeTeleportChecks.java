package org.edtp.carpet_edtp_addition.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;
import org.jspecify.annotations.Nullable;

public final class SafeTeleportChecks {
    private SafeTeleportChecks() {
    }

    @Nullable
    public static String findFailureTranslationKey(ServerLevel level, double x, double y, double z) {
        if (!CarpetEdtpAdditionSettings.safeTeleport.value()) {
            return null;
        }
        if (y < level.getMinY()) {
            return "carpet.rule.safeTeleport.void";
        }

        BlockPos targetPos = BlockPos.containing(x, y, z);
        if (hasCollision(level, targetPos) || hasCollision(level, targetPos.above())) {
            return "carpet.rule.safeTeleport.suffocation";
        }
        return null;
    }

    private static boolean hasCollision(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }
}
