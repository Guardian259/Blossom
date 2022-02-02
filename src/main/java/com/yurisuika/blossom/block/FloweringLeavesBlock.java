package com.yurisuika.blossom.block;


import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class FloweringLeavesBlock extends LeavesBlock implements Fertilizable {
    private final Block shearedBlock;

    public static final int MAX_DISTANCE = 7;
    public static final IntProperty DISTANCE;
    public static final BooleanProperty PERSISTENT;
    private static final int field_31112 = 1;
    public static final int MAX_AGE = 3;
    public static final IntProperty AGE;

    public FloweringLeavesBlock(Block shearedBlock, Settings settings) {
        super(settings);
        this.shearedBlock = shearedBlock;
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(PERSISTENT, false).with(this.getAgeProperty(), 0));
    }

    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    public boolean hasRandomTicks(BlockState state) {
        int i = this.getAge(state);
        return ((Integer)state.get(DISTANCE) == 7 && !(Boolean)state.get(PERSISTENT)) || i < 3;
    }

    public IntProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 3;
    }

    protected int getAge(BlockState state) {
        return (Integer)state.get(this.getAgeProperty());
    }

    public BlockState withAge(int age) {
        return (BlockState)this.getDefaultState().with(this.getAgeProperty(), age);
    }

    public boolean isMature(BlockState state) {
        return (Integer)state.get(this.getAgeProperty()) >= this.getMaxAge();
    }

    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!(Boolean)state.get(PERSISTENT) && (Integer)state.get(DISTANCE) == 7) {
            dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }

        if (world.getBaseLightLevel(pos, 0) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
                world.setBlockState(pos, this.withAge(i + 1), 2);
            }
        }
    }

    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, updateDistanceFromLogs(state, world, pos), 3);
    }

    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int i = this.getAge(state) + this.getGrowthAmount(world);
        int j = this.getMaxAge();
        if (i > j) {
            i = j;
        }

        world.setBlockState(pos, this.withAge(i), 2);
    }

    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 2, 5);
    }

    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        int i = getDistanceFromLog(neighborState) + 1;
        if (i != 1 || (Integer)state.get(DISTANCE) != i) {
            world.createAndScheduleBlockTick(pos, this, 1);
        }

        return state;
    }

    private static BlockState updateDistanceFromLogs(BlockState state, WorldAccess world, BlockPos pos) {
        int i = 7;
        Mutable mutable = new Mutable();
        Direction[] var5 = Direction.values();
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Direction direction = var5[var7];
            mutable.set(pos, direction);
            i = Math.min(i, getDistanceFromLog(world.getBlockState(mutable)) + 1);
            if (i == 1) {
                break;
            }
        }

        return (BlockState)state.with(DISTANCE, i);
    }

    private static int getDistanceFromLog(BlockState state) {
        if (state.isIn(BlockTags.LOGS)) {
            return 0;
        } else {
            return state.getBlock() instanceof net.minecraft.block.LeavesBlock ? (Integer)state.get(DISTANCE) : 7;
        }
    }

    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.hasRain(pos.up())) {
            if (random.nextInt(15) == 1) {
                BlockPos blockPos = pos.down();
                BlockState blockState = world.getBlockState(blockPos);
                if (!blockState.isOpaque() || !blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) {
                    double d = (double)pos.getX() + random.nextDouble();
                    double e = (double)pos.getY() - 0.05D;
                    double f = (double)pos.getZ() + random.nextDouble();
                    world.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(new Property[]{DISTANCE, PERSISTENT, AGE});
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return updateDistanceFromLogs((BlockState)this.getDefaultState().with(PERSISTENT, true), ctx.getWorld(), ctx.getBlockPos());
    }

    static {
        DISTANCE = Properties.DISTANCE_1_7;
        PERSISTENT = Properties.PERSISTENT;
        AGE = Properties.AGE_7;
    }

    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return !this.isMature(state);
    }

    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, pos, state);
    }

    public static void dropApple(World world, BlockPos pos) {
        dropStack(world, pos, new ItemStack(Items.APPLE, 3));
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        int i = (Integer)state.get(AGE);
        boolean bl = false;
        if (i == 3) {
            Item item = itemStack.getItem();
            if (itemStack.isOf(Items.SHEARS)) {
                world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CROP_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                dropApple(world, pos);
                itemStack.damage(1, player, (playerx) -> {
                    playerx.sendToolBreakStatus(hand);
                });
                bl = true;
                world.emitGameEvent(player, GameEvent.SHEAR, pos);
                world.setBlockState(pos, this.shearedBlock.getDefaultState()
                        .with(DISTANCE, state.get(DISTANCE))
                        .with(PERSISTENT, state.get(PERSISTENT))
                );
            }

            if (!world.isClient() && bl) {
                player.incrementStat(Stats.USED.getOrCreateStat(item));
            }
        return ActionResult.SUCCESS;

        } else {
            return ActionResult.PASS;
        }
    }
}