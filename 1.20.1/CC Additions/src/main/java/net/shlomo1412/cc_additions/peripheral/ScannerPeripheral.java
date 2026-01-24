package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.shlomo1412.cc_additions.block.entity.ScannerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The Scanner peripheral implementation.
 * Provides Lua functions to scan blocks and entities in a configurable range.
 * Maximum range is 256 blocks in any direction.
 */
public class ScannerPeripheral implements IPeripheral {
    
    private static final int MAX_RANGE = 256;
    
    private final ScannerBlockEntity scanner;

    public ScannerPeripheral(ScannerBlockEntity scanner) {
        this.scanner = scanner;
    }

    @Override
    public String getType() {
        return "scanner";
    }

    /**
     * Clamps a range value to the maximum allowed range (256).
     */
    private int clampRange(int range) {
        return Math.min(Math.max(range, 1), MAX_RANGE);
    }

    /**
     * Scan for blocks in the specified range.
     *
     * @param xRange The range in the X direction (clamped to 1-256)
     * @param yRange The range in the Y direction (clamped to 1-256)
     * @param zRange The range in the Z direction (clamped to 1-256)
     * @return A list of maps containing block information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> scan(int xRange, int yRange, int zRange) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        // Clamp ranges to max
        xRange = clampRange(xRange);
        yRange = clampRange(yRange);
        zRange = clampRange(zRange);

        BlockPos scannerPos = scanner.getBlockPos();
        List<Map<String, Object>> results = new ArrayList<>();

        for (int x = -xRange; x <= xRange; x++) {
            for (int y = -yRange; y <= yRange; y++) {
                for (int z = -zRange; z <= zRange; z++) {
                    BlockPos pos = scannerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    // Skip air blocks
                    if (state.isAir()) {
                        continue;
                    }

                    Map<String, Object> blockData = new HashMap<>();
                    blockData.put("x", x);
                    blockData.put("y", y);
                    blockData.put("z", z);
                    
                    ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    blockData.put("name", blockId != null ? blockId.toString() : "unknown");
                    
                    // Add block state properties
                    Map<String, Object> stateData = new HashMap<>();
                    for (Property<?> property : state.getProperties()) {
                        stateData.put(property.getName(), getPropertyValue(state, property));
                    }
                    if (!stateData.isEmpty()) {
                        blockData.put("state", stateData);
                    }

                    // Add metadata
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("hardness", state.getDestroySpeed(level, pos));
                    metadata.put("requiresToolForDrops", state.requiresCorrectToolForDrops());
                    metadata.put("lightEmission", state.getLightEmission());
                    metadata.put("isFlammable", state.isFlammable(level, pos, net.minecraft.core.Direction.UP));
                    metadata.put("hasBlockEntity", state.hasBlockEntity());
                    blockData.put("metadata", metadata);

                    results.add(blockData);
                }
            }
        }

        return results;
    }

    /**
     * Scan for entities in the specified range.
     *
     * @param xRange The range in the X direction (clamped to 1-256)
     * @param yRange The range in the Y direction (clamped to 1-256)
     * @param zRange The range in the Z direction (clamped to 1-256)
     * @return A list of maps containing entity information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> scanForEntities(int xRange, int yRange, int zRange) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        // Clamp ranges to max
        xRange = clampRange(xRange);
        yRange = clampRange(yRange);
        zRange = clampRange(zRange);

        BlockPos scannerPos = scanner.getBlockPos();
        AABB scanArea = new AABB(
            scannerPos.getX() - xRange, scannerPos.getY() - yRange, scannerPos.getZ() - zRange,
            scannerPos.getX() + xRange + 1, scannerPos.getY() + yRange + 1, scannerPos.getZ() + zRange + 1
        );

        List<Entity> entities = level.getEntities(null, scanArea);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Entity entity : entities) {
            Map<String, Object> entityData = new HashMap<>();
            
            // Relative position from scanner
            entityData.put("x", entity.getX() - scannerPos.getX());
            entityData.put("y", entity.getY() - scannerPos.getY());
            entityData.put("z", entity.getZ() - scannerPos.getZ());

            // Entity type
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            entityData.put("name", entityId != null ? entityId.toString() : "unknown");
            entityData.put("displayName", entity.getDisplayName().getString());
            
            // UUID
            entityData.put("uuid", entity.getUUID().toString());

            // Entity metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", entity.getId());
            metadata.put("isAlive", entity.isAlive());
            metadata.put("isOnGround", entity.onGround());
            metadata.put("isInWater", entity.isInWater());
            metadata.put("isOnFire", entity.isOnFire());
            metadata.put("isSilent", entity.isSilent());
            metadata.put("isInvisible", entity.isInvisible());
            metadata.put("isGlowing", entity.isCurrentlyGlowing());
            
            // Motion
            metadata.put("motionX", entity.getDeltaMovement().x);
            metadata.put("motionY", entity.getDeltaMovement().y);
            metadata.put("motionZ", entity.getDeltaMovement().z);
            
            // Rotation
            metadata.put("yaw", entity.getYRot());
            metadata.put("pitch", entity.getXRot());

            // Living entity specific data
            if (entity instanceof LivingEntity living) {
                metadata.put("health", living.getHealth());
                metadata.put("maxHealth", living.getMaxHealth());
                metadata.put("armorValue", living.getArmorValue());
                metadata.put("isBaby", living.isBaby());
            }

            // Player specific data
            if (entity instanceof Player player) {
                metadata.put("isPlayer", true);
                metadata.put("gameMode", player.isCreative() ? "creative" : 
                    (player.isSpectator() ? "spectator" : "survival"));
                metadata.put("experienceLevel", player.experienceLevel);
                metadata.put("foodLevel", player.getFoodData().getFoodLevel());
            } else {
                metadata.put("isPlayer", false);
            }

            entityData.put("metadata", metadata);
            results.add(entityData);
        }

        return results;
    }

    /**
     * Get the maximum allowed scan range.
     *
     * @return The maximum range (256)
     */
    @LuaFunction
    public final int getMaxRange() {
        return MAX_RANGE;
    }

    /**
     * Get the scanner's position in the world.
     *
     * @return A map containing x, y, z coordinates
     */
    @LuaFunction
    public final Map<String, Integer> getPosition() {
        BlockPos pos = scanner.getBlockPos();
        Map<String, Integer> position = new HashMap<>();
        position.put("x", pos.getX());
        position.put("y", pos.getY());
        position.put("z", pos.getZ());
        return position;
    }

    /**
     * Helper to get a block state property value as a string.
     */
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String getPropertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ScannerPeripheral o && scanner == o.scanner;
    }

    @Override
    public Object getTarget() {
        return scanner;
    }
}
