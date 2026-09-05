package org.edtp.carpet_edtp_addition.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

public final class SoundSuppressionTestHooks {
    private static final Set<BlockPos> ARMED_POSITIONS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, List<Exception>> PACKET_ERRORS = new ConcurrentHashMap<>();

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

    public static void capturePacketErrors(UUID playerId) {
        PACKET_ERRORS.put(playerId, new ArrayList<>());
    }

    public static void recordPacketError(UUID playerId, Exception error) {
        List<Exception> errors = PACKET_ERRORS.get(playerId);
        if (errors != null) {
            errors.add(error);
        }
    }

    public static List<Exception> packetErrors(UUID playerId) {
        return List.copyOf(PACKET_ERRORS.get(playerId));
    }

    public static void stopCapturingPacketErrors(UUID playerId) {
        PACKET_ERRORS.remove(playerId);
    }

    public static final class SimulatedUpdateSuppressionException extends RuntimeException {
        public SimulatedUpdateSuppressionException() {
            super("Simulated Minecraft 1.21 update suppression");
        }
    }
}
