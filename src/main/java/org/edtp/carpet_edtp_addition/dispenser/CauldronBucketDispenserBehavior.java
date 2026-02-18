package org.edtp.carpet_edtp_addition.dispenser;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class CauldronBucketDispenserBehavior extends ItemDispenserBehavior {
    public enum Mode {
        WATER_BUCKET,
        LAVA_BUCKET,
        POWDER_SNOW_BUCKET,
        EMPTY_BUCKET
    }

    private final DispenserBehavior fallback;
    private final Mode mode;
    private boolean delegated;

    public CauldronBucketDispenserBehavior(DispenserBehavior fallback, Mode mode) {
        this.fallback = fallback;
        this.mode = mode;
    }

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        this.delegated = false;
        if (!CarpetEdtpAdditionSettings.dispenserFillsCauldron.value()) {
            this.delegated = true;
            return fallback.dispense(pointer, stack);
        }

        ItemStack handled = this.tryHandleCauldron(pointer, stack);
        if (handled != null) {
            return handled;
        }

        this.delegated = true;
        return fallback.dispense(pointer, stack);
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        if (this.delegated) {
            return;
        }
        super.playSound(pointer);
    }

    @Override
    protected void spawnParticles(BlockPointer pointer, net.minecraft.util.math.Direction side) {
        if (this.delegated) {
            this.delegated = false;
            return;
        }
        super.spawnParticles(pointer, side);
    }

    private ItemStack tryHandleCauldron(BlockPointer pointer, ItemStack stack) {
        World world = pointer.world();
        BlockPos targetPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
        BlockState targetState = world.getBlockState(targetPos);

        switch (mode) {
            case WATER_BUCKET -> {
                if (targetState.getBlock() instanceof AbstractCauldronBlock) {
                    return fillWithWater(pointer, stack, targetPos);
                }
            }
            case LAVA_BUCKET -> {
                if (targetState.getBlock() instanceof AbstractCauldronBlock) {
                    return fillWithLava(pointer, stack, targetPos);
                }
            }
            case POWDER_SNOW_BUCKET -> {
                if (targetState.getBlock() instanceof AbstractCauldronBlock) {
                    return fillWithPowderSnow(pointer, stack, targetPos);
                }
            }
            case EMPTY_BUCKET -> {
                if (targetState.isOf(Blocks.WATER_CAULDRON)) {
                    int level = targetState.get(LeveledCauldronBlock.LEVEL);
                    if (level == 3) {
                        return drainWater(pointer, stack, targetPos);
                    }
                } else if (targetState.isOf(Blocks.POWDER_SNOW_CAULDRON)) {
                    int level = targetState.get(LeveledCauldronBlock.LEVEL);
                    if (level == 3) {
                        return drainPowderSnow(pointer, stack, targetPos);
                    }
                } else if (targetState.isOf(Blocks.LAVA_CAULDRON)) {
                    return drainLava(pointer, stack, targetPos);
                }
            }
            default -> {
            }
        }

        return null;
    }

    private ItemStack fillWithWater(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        BlockState newState = Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3);
        world.setBlockState(pos, newState);
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_EMPTY);
        world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack fillWithLava(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        BlockState newState = Blocks.LAVA_CAULDRON.getDefaultState();
        world.setBlockState(pos, newState);
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
        world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack fillWithPowderSnow(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        BlockState newState = Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3);
        world.setBlockState(pos, newState);
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
        world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack drainWater(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_FILL);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.WATER_BUCKET));
    }

    private ItemStack drainLava(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.LAVA_BUCKET));
    }

    private ItemStack drainPowderSnow(BlockPointer pointer, ItemStack stack, BlockPos pos) {
        World world = pointer.world();
        world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
        playCauldronSound(world, pos, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.decrementStackWithRemainder(pointer, stack, new ItemStack(Items.POWDER_SNOW_BUCKET));
    }

    private void playCauldronSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
}
