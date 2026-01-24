package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Advanced Scanner peripheral.
 * Stores custom scan position for world-wide scanning.
 */
public class ScannerAdvancedBlockEntity extends BlockEntity {
    
    @Nullable
    private BlockPos customScanPosition = null;

    public ScannerAdvancedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCANNER_ADVANCED.get(), pos, state);
    }

    /**
     * Get the current scan origin position.
     * @return Custom position if set, otherwise the scanner's own position.
     */
    public BlockPos getScanOrigin() {
        return customScanPosition != null ? customScanPosition : getBlockPos();
    }

    /**
     * Set a custom scan origin position.
     */
    public void setCustomScanPosition(@Nullable BlockPos pos) {
        this.customScanPosition = pos;
        setChanged();
    }

    /**
     * Check if a custom scan position is set.
     */
    public boolean hasCustomScanPosition() {
        return customScanPosition != null;
    }

    /**
     * Clear the custom scan position (revert to scanner's position).
     */
    public void clearCustomScanPosition() {
        this.customScanPosition = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (customScanPosition != null) {
            tag.putInt("ScanPosX", customScanPosition.getX());
            tag.putInt("ScanPosY", customScanPosition.getY());
            tag.putInt("ScanPosZ", customScanPosition.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ScanPosX")) {
            customScanPosition = new BlockPos(
                tag.getInt("ScanPosX"),
                tag.getInt("ScanPosY"),
                tag.getInt("ScanPosZ")
            );
        } else {
            customScanPosition = null;
        }
    }
}
