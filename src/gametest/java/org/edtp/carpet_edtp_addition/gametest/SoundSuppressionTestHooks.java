package org.edtp.carpet_edtp_addition.gametest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

public final class SoundSuppressionTestHooks {
    private static final Set<BlockPos> ARMED_POSITIONS = ConcurrentHashMap.newKeySet();

    private SoundSuppressionTestHooks() {
    }

    public static void arm(BlockPos pos) {
        ARMED_POSITIONS.add(pos.immutable());
    }

    public static void clear(BlockPos pos) {
        ARMED_POSITIONS.remove(pos);
    }

    public static void throwIfArmed(BlockPos pos) {
        if (ARMED_POSITIONS.remove(pos)) {
            throw new SimulatedUpdateSuppressionException();
        }
    }

    public static final class SimulatedUpdateSuppressionException extends RuntimeException {
        public SimulatedUpdateSuppressionException() {
            super("Simulated Minecraft 1.21 update suppression");
        }
    }
}
