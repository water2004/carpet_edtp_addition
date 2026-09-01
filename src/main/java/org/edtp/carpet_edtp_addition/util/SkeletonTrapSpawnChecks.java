package org.edtp.carpet_edtp_addition.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacements;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public final class SkeletonTrapSpawnChecks {
    private SkeletonTrapSpawnChecks() {
    }

    public static boolean allowsSpawn(ServerLevel level, BlockPos pos) {
        if (!CarpetEdtpAdditionSettings.naturalSkeletonTraps.value()) {
            return true;
        }

        return SpawnPlacements.isSpawnPositionOk(EntityTypes.SKELETON_HORSE, level, pos)
            && level.noCollision(EntityTypes.SKELETON_HORSE.getSpawnAABB(pos.getX(), pos.getY(), pos.getZ()));
    }
}
