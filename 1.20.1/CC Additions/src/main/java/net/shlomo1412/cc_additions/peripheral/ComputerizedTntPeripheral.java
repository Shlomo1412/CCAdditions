package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.shlomo1412.cc_additions.block.entity.ComputerizedTntBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Peripheral for Computerized TNT.
 * Allows computers to control TNT ignition, fuse timer, and explosion strength.
 */
public class ComputerizedTntPeripheral implements IPeripheral {

    private final ComputerizedTntBlockEntity blockEntity;

    public ComputerizedTntPeripheral(ComputerizedTntBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "computerized_tnt";
    }

    /**
     * Ignite the TNT, starting the countdown.
     * @return true if successfully ignited, false if already ignited
     */
    @LuaFunction
    public final boolean ignite() {
        return blockEntity.ignite();
    }

    /**
     * Defuse the TNT, stopping the countdown.
     * @return true if successfully defused, false if not ignited
     */
    @LuaFunction
    public final boolean defuse() {
        return blockEntity.defuse();
    }

    /**
     * Check if the TNT is currently ignited.
     * @return true if ignited and counting down
     */
    @LuaFunction
    public final boolean isIgnited() {
        return blockEntity.isIgnited();
    }

    /**
     * Get the remaining fuse time in ticks.
     * @return remaining ticks until explosion, or -1 if not ignited
     */
    @LuaFunction
    public final int getRemainingFuse() {
        return blockEntity.getRemainingFuse();
    }

    /**
     * Set the fuse time (must be done before ignition).
     * @param ticks fuse time in ticks (1-6000, default 80 = 4 seconds)
     * @return true if set successfully, false if already ignited or invalid value
     * @throws LuaException if ticks is out of range
     */
    @LuaFunction
    public final boolean setFuse(int ticks) throws LuaException {
        if (ticks < ComputerizedTntBlockEntity.MIN_FUSE || ticks > ComputerizedTntBlockEntity.MAX_FUSE) {
            throw new LuaException("Fuse time must be between " + ComputerizedTntBlockEntity.MIN_FUSE + 
                " and " + ComputerizedTntBlockEntity.MAX_FUSE + " ticks");
        }
        return blockEntity.setFuseTime(ticks);
    }

    /**
     * Get the configured fuse time.
     * @return fuse time in ticks
     */
    @LuaFunction
    public final int getFuse() {
        return blockEntity.getFuseTime();
    }

    /**
     * Set the explosion strength (must be done before ignition).
     * Normal TNT has strength 4.0, max is 20.0 (5x normal).
     * @param strength explosion power (1.0-20.0)
     * @return true if set successfully, false if already ignited or invalid value
     * @throws LuaException if strength is out of range
     */
    @LuaFunction
    public final boolean setStrength(double strength) throws LuaException {
        if (strength < 1.0 || strength > ComputerizedTntBlockEntity.MAX_STRENGTH) {
            throw new LuaException("Strength must be between 1.0 and " + ComputerizedTntBlockEntity.MAX_STRENGTH);
        }
        return blockEntity.setStrength((float) strength);
    }

    /**
     * Get the configured explosion strength.
     * @return explosion power (default 4.0, max 20.0)
     */
    @LuaFunction
    public final double getStrength() {
        return blockEntity.getStrength();
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ComputerizedTntPeripheral tnt && 
               tnt.blockEntity == this.blockEntity;
    }
}
