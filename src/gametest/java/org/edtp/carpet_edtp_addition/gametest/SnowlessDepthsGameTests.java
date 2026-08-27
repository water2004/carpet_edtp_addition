package org.edtp.carpet_edtp_addition.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class SnowlessDepthsGameTests {
    private static final BlockPos SURFACE_BLOCK = new BlockPos(3, 0, 3);
    private static final int SNOWLINE_Y = 0;
    private static final int BELOW_ZERO_Y = -1;

    @GameTest(skyAccess = true)
    public void snowyPlainsStillAccumulatesSnow(GameTestHelper helper) {
        assertNaturalSnowfall(helper, Biomes.SNOWY_PLAINS);
    }

    @GameTest(skyAccess = true)
    public void snowyTaigaStillAccumulatesSnow(GameTestHelper helper) {
        assertNaturalSnowfall(helper, Biomes.SNOWY_TAIGA);
    }

    @GameTest(skyAccess = true)
    public void groveStillAccumulatesSnow(GameTestHelper helper) {
        assertNaturalSnowfall(helper, Biomes.GROVE);
    }

    @GameTest(skyAccess = true)
    public void frozenPeaksStillAccumulatesSnow(GameTestHelper helper) {
        assertNaturalSnowfall(helper, Biomes.FROZEN_PEAKS);
    }

    @GameTest(skyAccess = true)
    public void snowDoesNotFormOrGrowBelowZero(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper);
        BlockPos reference = helper.absolutePos(SURFACE_BLOCK);
        BlockPos emptySnowPos = new BlockPos(reference.getX(), BELOW_ZERO_Y, reference.getZ());
        BlockPos layeredSnowPos = emptySnowPos.east(2);

        prepareWeatherColumn(level, emptySnowPos, Blocks.STONE.defaultBlockState());
        prepareWeatherColumn(level, layeredSnowPos, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(layeredSnowPos, Blocks.SNOW.defaultBlockState());
        setBiome(helper, level, emptySnowPos.offset(-2, -2, -2), layeredSnowPos.offset(2, 2, 2), Biomes.SNOWY_PLAINS);

        assertPrecipitationTargets(helper, level, emptySnowPos);
        assertPrecipitationTargets(helper, level, layeredSnowPos);
        level.tickPrecipitation(emptySnowPos);
        level.tickPrecipitation(layeredSnowPos);

        helper.assertTrue(level.getBlockState(emptySnowPos).isAir(), "Snow formed below Y=0");
        helper.assertTrue(level.getBlockState(layeredSnowPos).is(Blocks.SNOW), "Existing snow was removed below Y=0");
        helper.assertTrue(
            level.getBlockState(layeredSnowPos).getValue(SnowLayerBlock.LAYERS) == 1,
            "Snow layers grew below Y=0"
        );
        helper.succeed();
    }

    @GameTest(skyAccess = true, maxTicks = 40)
    public void snowGolemStillPlacesSnowBelowZero(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper);
        BlockPos reference = helper.absolutePos(SURFACE_BLOCK);
        BlockPos snowPos = new BlockPos(reference.getX(), BELOW_ZERO_Y, reference.getZ());

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = snowPos.offset(dx, -1, dz);
                level.setBlockAndUpdate(floorPos, Blocks.STONE.defaultBlockState());
                for (int y = BELOW_ZERO_Y; y <= BELOW_ZERO_Y + 2; y++) {
                    level.setBlockAndUpdate(new BlockPos(floorPos.getX(), y, floorPos.getZ()), Blocks.AIR.defaultBlockState());
                }
            }
        }

        helper.assertTrue(level.getGameRules().get(GameRules.MOB_GRIEFING), "mobGriefing must be enabled for this test");
        SnowGolem golem = EntityTypes.SNOW_GOLEM.create(level, EntitySpawnReason.COMMAND);
        helper.assertTrue(golem != null, "Failed to create snow golem");
        golem.snapTo(snowPos.getX() + 0.5, snowPos.getY(), snowPos.getZ() + 0.5, 0.0F, 0.0F);
        helper.assertTrue(level.addFreshEntity(golem), "Failed to spawn snow golem");

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getBlockState(snowPos).is(Blocks.SNOW), "Snow golem did not place snow below Y=0");
            helper.succeed();
        });
    }

    @GameTest(skyAccess = true)
    public void cauldronStillCollectsPowderSnowBelowZero(GameTestHelper helper) {
        ServerLevel level = prepareTest(helper);
        BlockPos reference = helper.absolutePos(SURFACE_BLOCK);
        BlockPos precipitationPos = new BlockPos(reference.getX(), BELOW_ZERO_Y, reference.getZ());
        BlockPos cauldronPos = precipitationPos.below();

        prepareWeatherColumn(level, precipitationPos, Blocks.CAULDRON.defaultBlockState());
        setBiome(helper, level, precipitationPos.offset(-2, -2, -2), precipitationPos.offset(2, 2, 2), Biomes.SNOWY_PLAINS);
        assertPrecipitationTargets(helper, level, precipitationPos);
        helper.assertTrue(
            level.getBiome(cauldronPos).value().getPrecipitationAt(cauldronPos, level.getSeaLevel()) == Biome.Precipitation.SNOW,
            "Test cauldron is not receiving snow precipitation"
        );

        for (int attempt = 0; attempt < 1000 && level.getBlockState(cauldronPos).is(Blocks.CAULDRON); attempt++) {
            level.tickPrecipitation(precipitationPos);
        }

        helper.assertTrue(
            level.getBlockState(cauldronPos).is(Blocks.POWDER_SNOW_CAULDRON),
            "Cauldron did not collect powder snow below Y=0"
        );
        helper.assertTrue(
            level.getBlockState(cauldronPos).getValue(LayeredCauldronBlock.LEVEL) == 1,
            "Powder snow cauldron started at an unexpected level"
        );
        helper.assertTrue(level.getBlockState(precipitationPos).isAir(), "Snow formed above the cauldron below Y=0");
        helper.succeed();
    }

    private static void assertNaturalSnowfall(GameTestHelper helper, ResourceKey<Biome> biome) {
        ServerLevel level = prepareTest(helper);
        BlockPos reference = helper.absolutePos(SURFACE_BLOCK);
        BlockPos snowPos = new BlockPos(reference.getX(), SNOWLINE_Y, reference.getZ());

        prepareWeatherColumn(level, snowPos, Blocks.STONE.defaultBlockState());
        setBiome(helper, level, snowPos.offset(-2, -2, -2), snowPos.offset(2, 2, 2), biome);
        assertPrecipitationTargets(helper, level, snowPos);
        level.tickPrecipitation(snowPos);
        helper.assertTrue(level.getBlockState(snowPos).is(Blocks.SNOW), "Snow did not form at Y=0 in " + biome.identifier());
        helper.succeed();
    }

    private static ServerLevel prepareTest(GameTestHelper helper) {
        CarpetEdtpAdditionSettings.snowlessDepths.set(null, true);
        ServerLevel level = helper.getLevel();
        level.setRainLevel(1.0F);
        return level;
    }

    private static void prepareWeatherColumn(ServerLevel level, BlockPos precipitationPos, BlockState support) {
        for (int y = precipitationPos.getY(); y <= level.getMaxY(); y++) {
            level.setBlock(new BlockPos(precipitationPos.getX(), y, precipitationPos.getZ()), Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlockAndUpdate(precipitationPos.below(), support);
    }

    private static void setBiome(
        GameTestHelper helper,
        ServerLevel level,
        BlockPos from,
        BlockPos to,
        ResourceKey<Biome> biomeKey
    ) {
        Holder.Reference<Biome> biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        var result = FillBiomeCommand.fill(level, from, to, biome);
        if (result.right().isPresent()) {
            helper.fail("Failed to set test biome: " + result.right().get().getMessage());
        }
    }

    private static void assertPrecipitationTargets(GameTestHelper helper, ServerLevel level, BlockPos expected) {
        BlockPos actual = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, expected);
        helper.assertTrue(
            actual.equals(expected),
            "Precipitation targeted " + actual.toShortString() + " instead of " + expected.toShortString()
        );
    }
}
