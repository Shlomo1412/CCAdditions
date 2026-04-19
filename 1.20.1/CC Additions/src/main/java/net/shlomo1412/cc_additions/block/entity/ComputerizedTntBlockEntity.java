package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for Computerized TNT.
 * Stores fuse timer, explosion strength, and handles the countdown.
 */
public class ComputerizedTntBlockEntity extends BlockEntity {

    // Normal TNT has explosion power of 4.0
    public static final float DEFAULT_STRENGTH = 4.0f;
    public static final float MAX_STRENGTH = 20.0f; // 5x normal TNT
    public static final int DEFAULT_FUSE = 80; // 4 seconds (same as normal TNT)
    public static final int MIN_FUSE = 1;
    public static final int MAX_FUSE = 6000; // 5 minutes max

    private float explosionStrength = DEFAULT_STRENGTH;
    private int fuseTime = DEFAULT_FUSE;
    private int currentFuse = -1; // -1 means not ignited
    private boolean ignited = false;

    public ComputerizedTntBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTERIZED_TNT.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (ignited && currentFuse >= 0) {
            currentFuse--;
            
            if (currentFuse <= 0) {
                explode();
            }
        }
    }

    /**
     * Ignite the TNT, starting the countdown.
     * @return true if ignited, false if already ignited
     */
    public boolean ignite() {
        if (ignited) {
            return false;
        }
        ignited = true;
        currentFuse = fuseTime;
        setChanged();
        return true;
    }

    /**
     * Defuse the TNT, stopping the countdown.
     * @return true if defused, false if not ignited
     */
    public boolean defuse() {
        if (!ignited) {
            return false;
        }
        ignited = false;
        currentFuse = -1;
        setChanged();
        return true;
    }

    /**
     * Check if the TNT is currently ignited.
     */
    public boolean isIgnited() {
        return ignited;
    }

    /**
     * Get the remaining fuse time in ticks.
     * @return remaining ticks, or -1 if not ignited
     */
    public int getRemainingFuse() {
        return currentFuse;
    }

    /**
     * Set the fuse time (before ignition).
     * @param ticks fuse time in ticks
     * @return true if set, false if already ignited or invalid value
     */
    public boolean setFuseTime(int ticks) {
        if (ignited) {
            return false;
        }
        if (ticks < MIN_FUSE || ticks > MAX_FUSE) {
            return false;
        }
        fuseTime = ticks;
        setChanged();
        return true;
    }

    /**
     * Get the configured fuse time.
     */
    public int getFuseTime() {
        return fuseTime;
    }

    /**
     * Set the explosion strength.
     * @param strength explosion power (1.0 to 20.0)
     * @return true if set, false if already ignited or invalid value
     */
    public boolean setStrength(float strength) {
        if (ignited) {
            return false;
        }
        if (strength < 1.0f || strength > MAX_STRENGTH) {
            return false;
        }
        explosionStrength = strength;
        setChanged();
        return true;
    }

    /**
     * Get the configured explosion strength.
     */
    public float getStrength() {
        return explosionStrength;
    }

    private void explode() {
        if (level == null) return;

        Level lvl = level;
        BlockPos pos = worldPosition;
        float strength = explosionStrength;

        // Remove the block first
        lvl.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

        // Create the explosion
        lvl.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            strength, Level.ExplosionInteraction.TNT);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("ExplosionStrength", explosionStrength);
        tag.putInt("FuseTime", fuseTime);
        tag.putInt("CurrentFuse", currentFuse);
        tag.putBoolean("Ignited", ignited);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        explosionStrength = tag.getFloat("ExplosionStrength");
        if (explosionStrength < 1.0f || explosionStrength > MAX_STRENGTH) {
            explosionStrength = DEFAULT_STRENGTH;
        }
        fuseTime = tag.getInt("FuseTime");
        if (fuseTime < MIN_FUSE || fuseTime > MAX_FUSE) {
            fuseTime = DEFAULT_FUSE;
        }
        currentFuse = tag.getInt("CurrentFuse");
        ignited = tag.getBoolean("Ignited");
    }
}
