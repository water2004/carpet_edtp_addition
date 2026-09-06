package org.edtp.carpet_edtp_addition.gametest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class PreserveLightOnUpgradeGameTests {
    @GameTest
    public void bothLightDeletionFixesRespectRuleChangesAfterCaching(GameTestHelper helper) {
        helper.assertFalse(CarpetEdtpAdditionSettings.preserveLightOnUpgrade.defaultValue(), "Rule must default to off");
        // DFU caches its converters. The rule must be read for each chunk, not when a converter is built.
        for (int targetVersion : new int[] {3451, 4537}) {
            for (boolean enabled : new boolean[] {false, true, false, true}) {
                withRule(enabled, () -> {
                    CompoundTag input = modernChunk();
                    CompoundTag output = upgrade(input, targetVersion - 1, targetVersion);
                    if (enabled) {
                        assertLightPreserved(helper, input, output);
                    } else {
                        assertLightDeleted(helper, output);
                    }
                });
            }
        }
        helper.succeed();
    }

    @GameTest
    public void invalidAndMissingLightMarkersStillUseVanillaFixes(GameTestHelper helper) {
        withRule(true, () -> {
            for (int targetVersion : new int[] {3451, 4537}) {
                CompoundTag invalid = modernChunk();
                invalid.putBoolean("isLightOn", false);
                assertLightDeleted(helper, upgrade(invalid, targetVersion - 1, targetVersion));
                CompoundTag missing = modernChunk();
                missing.remove("isLightOn");
                assertLightDeleted(helper, upgrade(missing, targetVersion - 1, targetVersion));
            }
        });
        helper.succeed();
    }

    @GameTest
    public void fullUpgradePreservesLightButStillRenamesBlocks(GameTestHelper helper) {
        withRule(true, () -> {
            CompoundTag input = modernChunk();
            CompoundTag paletteEntry = sections(input).getCompoundOrEmpty(0)
                .getCompoundOrEmpty("block_states").getListOrEmpty("palette").getCompoundOrEmpty(0);
            paletteEntry.putString("Name", "minecraft:chain");
            CompoundTag output = DataFixTypes.CHUNK.updateToCurrentVersion(DataFixers.getDataFixer(), input.copy(), 4536);
            assertLightPreserved(helper, input, output);
            String renamedBlock = sections(output).getCompoundOrEmpty(0)
                .getCompoundOrEmpty("block_states").getListOrEmpty("palette").getCompoundOrEmpty(0).getStringOr("Name", "");
            helper.assertTrue(renamedBlock.equals("minecraft:iron_chain"), "Unrelated block migration was skipped");
            SerializableChunkData parsed = SerializableChunkData.parse(
                helper.getLevel(), helper.getLevel().palettedContainerFactory(), output
            );
            helper.assertTrue(parsed != null, "Upgraded chunk could not be parsed by the current serializer");
            assertLightPreserved(helper, input, parsed.write());
        });
        helper.succeed();
    }

    @GameTest
    public void explicitCacheErasureStillDeletesPreservedLight(GameTestHelper helper) {
        withRule(true, () -> {
            CompoundTag output = upgrade(modernChunk(), 4536, 4537);
            helper.assertTrue(output.getBooleanOr("isLightOn", false), "The fixture was not preserved before cache erasure");
            CompoundTag heightmaps = new CompoundTag();
            heightmaps.putLongArray("WORLD_SURFACE", new long[37]);
            output.put("Heightmaps", heightmaps);
            helper.assertTrue(WorldUpgrader.verifyChunkPosAndEraseCache(new ChunkPos(0, 0), output),
                "Explicit cache erasure reported no change");
            assertLightDeleted(helper, output);
            helper.assertFalse(output.contains("Heightmaps"), "Explicit heightmap erasure was blocked");
        });
        helper.succeed();
    }

    @GameTest
    public void heightMigrationStillInvalidatesOldLighting(GameTestHelper helper) {
        withRule(true, () -> {
            CompoundTag input = legacyHeightChunk();
            CompoundTag heightUpgraded = upgrade(input, 2831, 2832);
            CompoundTag oldLevel = heightUpgraded.getCompoundOrEmpty("Level");
            helper.assertFalse(oldLevel.getBooleanOr("isLightOn", true), "Height migration did not invalidate lighting");
            helper.assertTrue(oldLevel.contains("below_zero_retrogen"), "Fixture did not exercise height migration");
            CompoundTag output = DataFixTypes.CHUNK.updateToCurrentVersion(DataFixers.getDataFixer(), input.copy(), 2831);
            assertLightDeleted(helper, output);
            helper.assertTrue(output.contains("below_zero_retrogen"), "Below-zero terrain migration was lost");
        });
        helper.succeed();
    }

    @GameTest
    public void normalServerLightUpdatesStillPropagateAndClear(GameTestHelper helper) {
        withRule(true, () -> {
            var level = helper.getLevel();
            BlockPos center = helper.absolutePos(new BlockPos(3, 3, 3));
            // A sealed room isolates this test from light emitted by neighboring test structures.
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean wall = Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2;
                        level.setBlockAndUpdate(center.offset(x, y, z), (wall ? Blocks.STONE : Blocks.AIR).defaultBlockState());
                    }
                }
            }
            awaitLightTasks(helper, center);
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center) == 0, "Sealed room was not dark");
            level.setBlockAndUpdate(center, Blocks.GLOWSTONE.defaultBlockState());
            awaitLightTasks(helper, center);
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center) == 15, "Light source did not light up");
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center.east()) == 14, "Block light did not propagate");
            level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
            awaitLightTasks(helper, center);
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center.east()) == 0, "Removed source left stale light");
        });
        helper.succeed();
    }

    private static void awaitLightTasks(GameTestHelper helper, BlockPos pos) {
        var engine = helper.getLevel().getChunkSource().getLightEngine();
        var pending = engine.waitForPendingTasks(pos.getX() >> 4, pos.getZ() >> 4).orTimeout(5, TimeUnit.SECONDS);
        helper.getLevel().getServer().managedBlock(() -> {
            engine.tryScheduleUpdate();
            return pending.isDone();
        });
        pending.join();
    }

    private static CompoundTag upgrade(CompoundTag input, int fromVersion, int toVersion) {
        return DataFixTypes.CHUNK.update(DataFixers.getDataFixer(), input.copy(), fromVersion, toVersion);
    }

    private static CompoundTag modernChunk() {
        CompoundTag chunk = new CompoundTag();
        chunk.putInt("xPos", 0);
        chunk.putInt("zPos", 0);
        chunk.putString("Status", "minecraft:full");
        chunk.putBoolean("isLightOn", true);
        ListTag sections = new ListTag();
        for (int y : new int[] {-1, 0, 1}) {
            CompoundTag section = lightSection(y);
            CompoundTag blocks = new CompoundTag();
            ListTag palette = new ListTag();
            CompoundTag air = new CompoundTag();
            air.putString("Name", "minecraft:air");
            palette.add(air);
            blocks.put("palette", palette);
            section.put("block_states", blocks);
            CompoundTag biomes = new CompoundTag();
            ListTag biomePalette = new ListTag();
            biomePalette.add(StringTag.valueOf("minecraft:plains"));
            biomes.put("palette", biomePalette);
            section.put("biomes", biomes);
            sections.add(section);
        }
        chunk.put("sections", sections);
        return chunk;
    }

    private static CompoundTag legacyHeightChunk() {
        CompoundTag root = new CompoundTag();
        CompoundTag context = new CompoundTag();
        context.putString("dimension", "minecraft:overworld");
        context.putString("generator", "minecraft:noise");
        root.put("__context", context);
        CompoundTag level = new CompoundTag();
        level.putString("Status", "full");
        level.putBoolean("isLightOn", true);
        int[] biomes = new int[1024];
        Arrays.fill(biomes, 1);
        level.putIntArray("Biomes", biomes);
        ListTag sections = new ListTag();
        CompoundTag section = lightSection(0);
        ListTag palette = new ListTag();
        CompoundTag bedrock = new CompoundTag();
        bedrock.putString("Name", "minecraft:bedrock");
        palette.add(bedrock);
        section.put("Palette", palette);
        section.putLongArray("BlockStates", new long[256]);
        sections.add(section);
        level.put("Sections", sections);
        root.put("Level", level);
        return root;
    }

    private static CompoundTag lightSection(int y) {
        CompoundTag section = new CompoundTag();
        section.putByte("Y", (byte) y);
        byte[] blockLight = new byte[2048];
        byte[] skyLight = new byte[2048];
        for (int i = 0; i < blockLight.length; i++) {
            blockLight[i] = (byte) (i + y);
            skyLight[i] = (byte) (255 - i + y);
        }
        section.putByteArray("BlockLight", blockLight);
        section.putByteArray("SkyLight", skyLight);
        return section;
    }

    private static ListTag sections(CompoundTag chunk) {
        return chunk.getListOrEmpty("sections");
    }

    private static void assertLightPreserved(GameTestHelper helper, CompoundTag before, CompoundTag after) {
        helper.assertTrue(after.getBooleanOr("isLightOn", false), "Valid light marker was deleted");
        ListTag oldSections = sections(before);
        ListTag newSections = sections(after);
        helper.assertTrue(oldSections.size() == newSections.size(), "Section count changed");
        for (int i = 0; i < oldSections.size(); i++) {
            CompoundTag oldSection = oldSections.getCompoundOrEmpty(i);
            CompoundTag newSection = newSections.getCompoundOrEmpty(i);
            helper.assertTrue(oldSection.getByteOr("Y", (byte) 0) == newSection.getByteOr("Y", (byte) 0), "Section Y changed");
            for (String field : new String[] {"BlockLight", "SkyLight"}) {
                helper.assertTrue(Arrays.equals(oldSection.getByteArray(field).orElseThrow(), newSection.getByteArray(field).orElseThrow()),
                    field + " bytes changed in section " + i);
            }
        }
    }

    private static void assertLightDeleted(GameTestHelper helper, CompoundTag chunk) {
        helper.assertFalse(chunk.getBooleanOr("isLightOn", false), "Invalid light marker was promoted to valid");
        for (var tag : sections(chunk)) {
            CompoundTag section = (CompoundTag) tag;
            helper.assertFalse(section.contains("BlockLight") || section.contains("SkyLight"), "Vanilla light deletion was skipped");
        }
    }

    private static void withRule(boolean enabled, Runnable test) {
        boolean original = CarpetEdtpAdditionSettings.preserveLightOnUpgrade.value();
        try {
            CarpetEdtpAdditionSettings.preserveLightOnUpgrade.set(null, enabled);
            test.run();
        } finally {
            CarpetEdtpAdditionSettings.preserveLightOnUpgrade.set(null, original);
        }
    }
}
