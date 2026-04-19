package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.level.ServerLevel;
import net.shlomo1412.cc_additions.block.entity.ShipControllerBlockEntity;
import net.shlomo1412.cc_additions.integration.VS2Helper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Peripheral for controlling Valkyrien Skies 2 ships.
 * Provides non-cheaty physics control (forces, torques, velocities) but no direct position/rotation setting.
 */
public class ShipControllerPeripheral implements IPeripheral {

    private final ShipControllerBlockEntity blockEntity;
    
    // Maximum force magnitude to prevent physics explosions (configurable in future)
    private static final double MAX_FORCE = 1_000_000.0; // 1 million Newtons
    private static final double MAX_TORQUE = 1_000_000.0;
    private static final double MAX_VELOCITY = 1000.0; // m/s

    public ShipControllerPeripheral(ShipControllerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "ship_controller";
    }

    private void validateForce(double x, double y, double z) throws LuaException {
        double magnitude = Math.sqrt(x*x + y*y + z*z);
        if (magnitude > MAX_FORCE) {
            throw new LuaException("Force magnitude exceeds maximum of " + MAX_FORCE + " N");
        }
    }

    private void validateTorque(double x, double y, double z) throws LuaException {
        double magnitude = Math.sqrt(x*x + y*y + z*z);
        if (magnitude > MAX_TORQUE) {
            throw new LuaException("Torque magnitude exceeds maximum of " + MAX_TORQUE + " Nm");
        }
    }

    private void validateVelocity(double x, double y, double z) throws LuaException {
        double magnitude = Math.sqrt(x*x + y*y + z*z);
        if (magnitude > MAX_VELOCITY) {
            throw new LuaException("Velocity magnitude exceeds maximum of " + MAX_VELOCITY + " m/s");
        }
    }

    // ==================== READ FUNCTIONS (for convenience) ====================

    /**
     * Check if this block is on a VS2 ship.
     * @return true if on a ship, false otherwise
     */
    @LuaFunction
    public final boolean isOnShip() {
        if (blockEntity.getLevel() == null) return false;
        return VS2Helper.isOnShip(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's position in world coordinates.
     * @return table with x, y, z, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable Map<String, Double> getPosition() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getPosition(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's rotation as a quaternion.
     * @return table with w, x, y, z, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable Map<String, Double> getRotation() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getRotation(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's linear velocity.
     * @return table with x, y, z in m/s, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable Map<String, Double> getVelocity() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getVelocity(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's angular velocity.
     * @return table with x, y, z in rad/s, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable Map<String, Double> getAngularVelocity() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getAngularVelocity(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's mass in kilograms.
     * @return mass in kg, or 0 if not on a ship
     */
    @LuaFunction
    public final double getMass() {
        if (blockEntity.getLevel() == null) return 0;
        return VS2Helper.getMass(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Check if the ship's physics are disabled (static mode).
     * @return true if physics disabled, false otherwise
     */
    @LuaFunction
    public final boolean isStatic() {
        if (blockEntity.getLevel() == null) return false;
        return VS2Helper.isStatic(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    // ==================== FORCE CONTROL ====================

    /**
     * Apply a force in world coordinates (Newtons).
     * @param x force X component
     * @param y force Y component
     * @param z force Z component
     * @return true if successful, false if not on a ship
     * @throws LuaException if force exceeds maximum
     */
    @LuaFunction
    public final boolean applyWorldForce(double x, double y, double z) throws LuaException {
        validateForce(x, y, z);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.applyWorldForce(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Apply a torque in world coordinates (Newton-meters).
     * @param x torque X component
     * @param y torque Y component
     * @param z torque Z component
     * @return true if successful, false if not on a ship
     * @throws LuaException if torque exceeds maximum
     */
    @LuaFunction
    public final boolean applyWorldTorque(double x, double y, double z) throws LuaException {
        validateTorque(x, y, z);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.applyWorldTorque(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Apply a force in ship-relative coordinates (rotates with ship).
     * @param x force X component
     * @param y force Y component
     * @param z force Z component
     * @return true if successful, false if not on a ship
     * @throws LuaException if force exceeds maximum
     */
    @LuaFunction
    public final boolean applyModelForce(double x, double y, double z) throws LuaException {
        validateForce(x, y, z);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.applyModelForce(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Apply a torque in ship-relative coordinates (rotates with ship).
     * @param x torque X component
     * @param y torque Y component
     * @param z torque Z component
     * @return true if successful, false if not on a ship
     * @throws LuaException if torque exceeds maximum
     */
    @LuaFunction
    public final boolean applyModelTorque(double x, double y, double z) throws LuaException {
        validateTorque(x, y, z);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.applyModelTorque(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Apply a force at a specific position on the ship (ship coordinates).
     * This creates both linear force and torque based on the offset from center of mass.
     * @param fx force X component
     * @param fy force Y component
     * @param fz force Z component
     * @param px position X in ship coordinates
     * @param py position Y in ship coordinates
     * @param pz position Z in ship coordinates
     * @return true if successful, false if not on a ship
     * @throws LuaException if force exceeds maximum
     */
    @LuaFunction
    public final boolean applyForceAtPosition(double fx, double fy, double fz, 
                                               double px, double py, double pz) throws LuaException {
        validateForce(fx, fy, fz);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.applyForceAtPosition(serverLevel, blockEntity.getBlockPos(), fx, fy, fz, px, py, pz);
    }

    // ==================== VELOCITY CONTROL ====================

    /**
     * Set the ship's linear velocity directly (m/s).
     * @param x velocity X component
     * @param y velocity Y component
     * @param z velocity Z component
     * @return true if successful, false if not on a ship
     * @throws LuaException if velocity exceeds maximum
     */
    @LuaFunction
    public final boolean setVelocity(double x, double y, double z) throws LuaException {
        validateVelocity(x, y, z);
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.setVelocity(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Set the ship's angular velocity directly (rad/s).
     * @param x angular velocity X component
     * @param y angular velocity Y component
     * @param z angular velocity Z component
     * @return true if successful, false if not on a ship
     */
    @LuaFunction
    public final boolean setAngularVelocity(double x, double y, double z) throws LuaException {
        // Angular velocity doesn't have the same magnitude concerns
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.setAngularVelocity(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Add to the ship's current linear velocity (m/s).
     * @param x velocity X component to add
     * @param y velocity Y component to add
     * @param z velocity Z component to add
     * @return true if successful, false if not on a ship
     * @throws LuaException if resulting velocity would exceed maximum
     */
    @LuaFunction
    public final boolean addVelocity(double x, double y, double z) throws LuaException {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        
        // Check current + new doesn't exceed max
        Map<String, Double> current = getVelocity();
        if (current != null) {
            double newX = current.get("x") + x;
            double newY = current.get("y") + y;
            double newZ = current.get("z") + z;
            validateVelocity(newX, newY, newZ);
        }
        
        return VS2Helper.addVelocity(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    /**
     * Add to the ship's current angular velocity (rad/s).
     * @param x angular velocity X component to add
     * @param y angular velocity Y component to add
     * @param z angular velocity Z component to add
     * @return true if successful, false if not on a ship
     */
    @LuaFunction
    public final boolean addAngularVelocity(double x, double y, double z) throws LuaException {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.addAngularVelocity(serverLevel, blockEntity.getBlockPos(), x, y, z);
    }

    // ==================== STATE CONTROL ====================

    /**
     * Enable or disable physics for the ship.
     * When static, the ship will not respond to forces or gravity.
     * @param isStatic true to disable physics, false to enable
     * @return true if successful, false if not on a ship
     */
    @LuaFunction
    public final boolean setStatic(boolean isStatic) throws LuaException {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return false;
        return VS2Helper.setStatic(serverLevel, blockEntity.getBlockPos(), isStatic);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ShipControllerPeripheral controller && 
               controller.blockEntity == this.blockEntity;
    }
}
