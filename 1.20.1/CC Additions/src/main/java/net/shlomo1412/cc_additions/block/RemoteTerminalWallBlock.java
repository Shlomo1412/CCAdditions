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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * Can be placed on walls, floor, or ceiling.
 * Right-click to open the paired computer's terminal.
 * Shift+Right-click to remove and get the item back.
 */
public class RemoteTerminalWallBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Shapes for each facing direction (thin like an item frame)
    // The facing direction is the direction the screen points (opposite of attachment surface)
    protected static final VoxelShape NORTH_SHAPE = Block.box(2, 2, 14, 14, 14, 16);  // Attached to south wall
    protected static final VoxelShape SOUTH_SHAPE = Block.box(2, 2, 0, 14, 14, 2);    // Attached to north wall
    protected static final VoxelShape EAST_SHAPE = Block.box(0, 2, 2, 2, 14, 14);     // Attached to west wall
    protected static final VoxelShape WEST_SHAPE = Block.box(14, 2, 2, 16, 14, 14);   // Attached to east wall
    protected static final VoxelShape UP_SHAPE = Block.box(2, 0, 2, 14, 2, 14);       // Attached to floor (facing up)
    protected static final VoxelShape DOWN_SHAPE = Block.box(2, 14, 2, 14, 16, 14);   // Attached to ceiling (facing down)

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
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The block faces the direction of the clicked face (screen points away from surface)
        Direction clickedFace = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, clickedFace);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachPos = pos.relative(facing.getOpposite());
        return level.getBlockState(attachPos).isFaceSturdy(level, attachPos, facing);
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
