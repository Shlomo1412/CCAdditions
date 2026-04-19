package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the VS Ship Controller peripheral.
 */
public class ShipControllerBlockEntity extends BlockEntity {

    public ShipControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHIP_CONTROLLER.get(), pos, state);
    }
}
