package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.shlomo1412.cc_additions.block.entity.ShipReaderBlockEntity;
import net.shlomo1412.cc_additions.integration.VS2Helper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Peripheral for reading all ship data from Valkyrien Skies 2.
 * Provides comprehensive read-only access to ship properties.
 */
public class ShipReaderPeripheral implements IPeripheral {

    private final ShipReaderBlockEntity blockEntity;

    public ShipReaderPeripheral(ShipReaderBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "ship_reader";
    }

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
     * Get the ship's unique ID.
     * @return ship ID, or -1 if not on a ship
     */
    @LuaFunction
    public final long getShipId() {
        if (blockEntity.getLevel() == null) return -1;
        return VS2Helper.getShipId(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    /**
     * Get the ship's name/slug.
     * @return ship name, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable String getShipName() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getShipName(blockEntity.getLevel(), blockEntity.getBlockPos());
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
     * Get the ship's rotation as euler angles in degrees.
     * @return table with pitch, yaw, roll, or nil if not on a ship
     */
    @LuaFunction
    public final @Nullable Map<String, Double> getRotationEuler() {
        if (blockEntity.getLevel() == null) return null;
        return VS2Helper.getRotationEuler(blockEntity.getLevel(), blockEntity.getBlockPos());
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

    /**
     * Get ALL ship data in a single call.
     * Returns a comprehensive table with all ship properties including:
     * - isOnShip, id, name
     * - position, rotation (quaternion), rotationEuler
     * - velocity, angularVelocity
     * - mass, centerOfMass, scale
     * - isStatic
     * - transformMatrix (4x4), inertiaTensor (3x3)
     * - worldBoundingBox, shipBoundingBox
     * @return table with all ship data
     */
    @LuaFunction
    public final Map<String, Object> getAll() {
        if (blockEntity.getLevel() == null) {
            return Map.of("isOnShip", false);
        }
        return VS2Helper.getAllShipData(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ShipReaderPeripheral reader && 
               reader.blockEntity == this.blockEntity;
    }
}
