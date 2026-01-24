package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Scanner peripheral.
 */
public class ScannerBlockEntity extends BlockEntity {
    
    public ScannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCANNER.get(), pos, state);
    }
}
