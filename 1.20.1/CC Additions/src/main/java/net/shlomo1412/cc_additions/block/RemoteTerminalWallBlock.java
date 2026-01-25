package net.shlomo1412.cc_additions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.shlomo1412.cc_additions.block.entity.RemoteTerminalWallBlockEntity;
import net.shlomo1412.cc_additions.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Wall-mounted Remote Terminal block.
 * Right-click to open the paired computer's terminal.
 * Shift+Right-click to remove and get the item back.
 */
public class RemoteTerminalWallBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Shapes for each facing direction (thin like an item frame)
    protected static final VoxelShape NORTH_SHAPE = Block.box(2, 1, 14, 14, 13, 16);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(2, 1, 0, 14, 13, 2);
    protected static final VoxelShape EAST_SHAPE = Block.box(0, 1, 2, 2, 13, 14);
    protected static final VoxelShape WEST_SHAPE = Block.box(14, 1, 2, 16, 13, 14);

    private final boolean advanced;

    public RemoteTerminalWallBlock(Properties properties, boolean advanced) {
        super(properties);
        this.advanced = advanced;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The block is placed on a wall, so we need to determine which wall
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(FACING, clickedFace);
        }
        return null; // Can't place on top/bottom
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wallPos = pos.relative(facing.getOpposite());
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, 
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                  InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RemoteTerminalWallBlockEntity wallTerminal)) {
            return InteractionResult.PASS;
        }

        // Shift+Right-click to remove
        if (player.isShiftKeyDown()) {
            // Drop the item with NBT data
            ItemStack drop = wallTerminal.createItemStack();
            popResource(level, pos, drop);
            level.removeBlock(pos, false);
            return InteractionResult.CONSUME;
        }

        // Regular right-click to open the terminal
        wallTerminal.openTerminal(player);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteTerminalWallBlockEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // Get the block entity to preserve NBT
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof RemoteTerminalWallBlockEntity wallTerminal) {
            return List.of(wallTerminal.createItemStack());
        }
        // Fallback
        return List.of(new ItemStack(advanced ? ModItems.ADVANCED_REMOTE_TERMINAL.get() : ModItems.REMOTE_TERMINAL.get()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof RemoteTerminalWallBlockEntity wallTerminal) {
            return wallTerminal.createItemStack();
        }
        return new ItemStack(advanced ? ModItems.ADVANCED_REMOTE_TERMINAL.get() : ModItems.REMOTE_TERMINAL.get());
    }
}
