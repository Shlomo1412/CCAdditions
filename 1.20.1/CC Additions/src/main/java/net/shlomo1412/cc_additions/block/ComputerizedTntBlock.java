package net.shlomo1412.cc_additions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.shlomo1412.cc_additions.block.entity.ComputerizedTntBlockEntity;
import net.shlomo1412.cc_additions.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Computerized TNT - a TNT block that can be controlled by computers.
 * Allows setting the fuse timer and explosion strength via Lua.
 */
public class ComputerizedTntBlock extends Block implements EntityBlock {

    public ComputerizedTntBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerizedTntBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.COMPUTERIZED_TNT.get() ?
            (lvl, pos, st, be) -> ((ComputerizedTntBlockEntity) be).tick() : null;
    }
}
