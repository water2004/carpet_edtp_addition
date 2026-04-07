package org.edtp.carpet_edtp_addition.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

@SuppressWarnings("null")
public class CauldronBucketDispenserBehavior extends DefaultDispenseItemBehavior {
    public enum Mode {
        WATER_BUCKET,
        LAVA_BUCKET,
        POWDER_SNOW_BUCKET,
        EMPTY_BUCKET
    }

    private final DispenseItemBehavior fallback;
    private final Mode mode;
    private boolean delegated;

    public CauldronBucketDispenserBehavior(DispenseItemBehavior fallback, Mode mode) {
        this.fallback = fallback;
        this.mode = mode;
    }

    @Override
    protected ItemStack execute(BlockSource pointer, ItemStack stack) {
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
    protected void playSound(BlockSource pointer) {
        if (this.delegated) {
            return;
        }
        super.playSound(pointer);
    }

    @Override
    protected void playAnimation(BlockSource pointer, Direction side) {
        if (this.delegated) {
            this.delegated = false;
            return;
        }
        super.playAnimation(pointer, side);
    }

    private ItemStack tryHandleCauldron(BlockSource pointer, ItemStack stack) {
        Level world = pointer.level();
        BlockPos targetPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
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
                if (targetState.is(Blocks.WATER_CAULDRON)) {
                    int level = targetState.getValue(LayeredCauldronBlock.LEVEL);
                    if (level == 3) {
                        return drainWater(pointer, stack, targetPos);
                    }
                } else if (targetState.is(Blocks.POWDER_SNOW_CAULDRON)) {
                    int level = targetState.getValue(LayeredCauldronBlock.LEVEL);
                    if (level == 3) {
                        return drainPowderSnow(pointer, stack, targetPos);
                    }
                } else if (targetState.is(Blocks.LAVA_CAULDRON)) {
                    return drainLava(pointer, stack, targetPos);
                }
            }
            default -> {
            }
        }

        return null;
    }

    private ItemStack fillWithWater(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        BlockState newState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        world.setBlockAndUpdate(pos, newState);
        playCauldronSound(world, pos, SoundEvents.BUCKET_EMPTY);
        world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack fillWithLava(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        BlockState newState = Blocks.LAVA_CAULDRON.defaultBlockState();
        world.setBlockAndUpdate(pos, newState);
        playCauldronSound(world, pos, SoundEvents.BUCKET_EMPTY_LAVA);
        world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack fillWithPowderSnow(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        BlockState newState = Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        world.setBlockAndUpdate(pos, newState);
        playCauldronSound(world, pos, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
        world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.BUCKET));
    }

    private ItemStack drainWater(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        playCauldronSound(world, pos, SoundEvents.BUCKET_FILL);
        world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.WATER_BUCKET));
    }

    private ItemStack drainLava(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        playCauldronSound(world, pos, SoundEvents.BUCKET_FILL_LAVA);
        world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.LAVA_BUCKET));
    }

    private ItemStack drainPowderSnow(BlockSource pointer, ItemStack stack, BlockPos pos) {
        Level world = pointer.level();
        world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        playCauldronSound(world, pos, SoundEvents.BUCKET_FILL_POWDER_SNOW);
        world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        return this.consumeWithRemainder(pointer, stack, new ItemStack(Items.POWDER_SNOW_BUCKET));
    }

    private void playCauldronSound(Level world, BlockPos pos, SoundEvent sound) {
        world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
