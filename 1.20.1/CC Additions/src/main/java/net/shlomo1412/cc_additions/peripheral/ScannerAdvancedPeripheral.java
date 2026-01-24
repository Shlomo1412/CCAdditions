package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.shlomo1412.cc_additions.block.entity.ScannerAdvancedBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The Advanced Scanner peripheral implementation.
 * Provides Lua functions to scan blocks and entities with unlimited range.
 * Supports custom scan origin positions for world-wide scanning.
 */
public class ScannerAdvancedPeripheral implements IPeripheral {

    private final ScannerAdvancedBlockEntity scanner;

    public ScannerAdvancedPeripheral(ScannerAdvancedBlockEntity scanner) {
        this.scanner = scanner;
    }

    @Override
    public String getType() {
        return "advanced_scanner";
    }

    // ==================== Position Management ====================

    /**
     * Set a custom scan origin position for world-wide scanning.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     */
    @LuaFunction(mainThread = true)
    public final void setScanPosition(int x, int y, int z) {
        scanner.setCustomScanPosition(new BlockPos(x, y, z));
    }

    /**
     * Get the current scan origin position.
     *
     * @return A map containing x, y, z coordinates of the scan origin
     */
    @LuaFunction
    public final Map<String, Object> getScanPosition() {
        BlockPos pos = scanner.getScanOrigin();
        Map<String, Object> position = new HashMap<>();
        position.put("x", pos.getX());
        position.put("y", pos.getY());
        position.put("z", pos.getZ());
        position.put("isCustom", scanner.hasCustomScanPosition());
        return position;
    }

    /**
     * Reset the scan position to the scanner's own position.
     */
    @LuaFunction(mainThread = true)
    public final void resetScanPosition() {
        scanner.clearCustomScanPosition();
    }

    /**
     * Get the scanner's actual position in the world.
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

    // ==================== Block Scanning ====================

    /**
     * Scan for blocks in the specified range from the scan origin.
     * Unlike the basic scanner, this has NO range limit.
     *
     * @param xRange The range in the X direction
     * @param yRange The range in the Y direction
     * @param zRange The range in the Z direction
     * @return A list of maps containing block information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> scan(int xRange, int yRange, int zRange) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        // Ensure positive ranges
        xRange = Math.max(1, Math.abs(xRange));
        yRange = Math.max(1, Math.abs(yRange));
        zRange = Math.max(1, Math.abs(zRange));

        BlockPos origin = scanner.getScanOrigin();
        List<Map<String, Object>> results = new ArrayList<>();

        for (int x = -xRange; x <= xRange; x++) {
            for (int y = -yRange; y <= yRange; y++) {
                for (int z = -zRange; z <= zRange; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    
                    // Check if position is loaded
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    
                    BlockState state = level.getBlockState(pos);

                    // Skip air blocks
                    if (state.isAir()) {
                        continue;
                    }

                    results.add(createBlockData(level, pos, state, x, y, z));
                }
            }
        }

        return results;
    }

    /**
     * Get block information at a specific world position.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     * @return A map containing block information, or nil if not loaded/air
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final @Nullable Map<String, Object> getBlockAt(int x, int y, int z) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        BlockPos pos = new BlockPos(x, y, z);
        
        if (!level.isLoaded(pos)) {
            return null;
        }
        
        BlockState state = level.getBlockState(pos);
        
        if (state.isAir()) {
            return null;
        }

        BlockPos origin = scanner.getScanOrigin();
        return createBlockData(level, pos, state, 
            pos.getX() - origin.getX(), 
            pos.getY() - origin.getY(), 
            pos.getZ() - origin.getZ());
    }

    // ==================== Entity Scanning ====================

    /**
     * Scan for entities in the specified range from the scan origin.
     * Unlike the basic scanner, this has NO range limit.
     *
     * @param xRange The range in the X direction
     * @param yRange The range in the Y direction
     * @param zRange The range in the Z direction
     * @return A list of maps containing entity information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> scanForEntities(int xRange, int yRange, int zRange) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        xRange = Math.max(1, Math.abs(xRange));
        yRange = Math.max(1, Math.abs(yRange));
        zRange = Math.max(1, Math.abs(zRange));

        BlockPos origin = scanner.getScanOrigin();
        AABB scanArea = new AABB(
            origin.getX() - xRange, origin.getY() - yRange, origin.getZ() - zRange,
            origin.getX() + xRange + 1, origin.getY() + yRange + 1, origin.getZ() + zRange + 1
        );

        List<Entity> entities = level.getEntities(null, scanArea);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Entity entity : entities) {
            results.add(createEntityData(entity, origin));
        }

        return results;
    }

    /**
     * Get entities within a specific cubic area defined by two corners.
     *
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param startZ Start Z coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param endZ End Z coordinate
     * @return A list of maps containing entity information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> getEntitiesInCubic(int startX, int startY, int startZ, 
                                                               int endX, int endY, int endZ) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        // Ensure proper min/max ordering
        int minX = Math.min(startX, endX);
        int minY = Math.min(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxX = Math.max(startX, endX);
        int maxY = Math.max(startY, endY);
        int maxZ = Math.max(startZ, endZ);

        AABB scanArea = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        List<Entity> entities = level.getEntities(null, scanArea);
        List<Map<String, Object>> results = new ArrayList<>();

        BlockPos origin = scanner.getScanOrigin();
        for (Entity entity : entities) {
            results.add(createEntityData(entity, origin));
        }

        return results;
    }

    /**
     * Get blocks within a specific cubic area defined by two corners.
     *
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param startZ Start Z coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param endZ End Z coordinate
     * @return A list of maps containing block information
     * @throws LuaException If the scanner is not in a valid world
     */
    @LuaFunction(mainThread = true)
    public final List<Map<String, Object>> getBlocksInCubic(int startX, int startY, int startZ,
                                                             int endX, int endY, int endZ) throws LuaException {
        Level level = scanner.getLevel();
        if (level == null) {
            throw new LuaException("Scanner is not in a valid world");
        }

        // Ensure proper min/max ordering
        int minX = Math.min(startX, endX);
        int minY = Math.min(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxX = Math.max(startX, endX);
        int maxY = Math.max(startY, endY);
        int maxZ = Math.max(startZ, endZ);

        BlockPos origin = scanner.getScanOrigin();
        List<Map<String, Object>> results = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) {
                        continue;
                    }

                    results.add(createBlockData(level, pos, state,
                        x - origin.getX(),
                        y - origin.getY(),
                        z - origin.getZ()));
                }
            }
        }

        return results;
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> createBlockData(Level level, BlockPos pos, BlockState state, 
                                                 int relX, int relY, int relZ) {
        Map<String, Object> blockData = new HashMap<>();
        blockData.put("x", relX);
        blockData.put("y", relY);
        blockData.put("z", relZ);
        blockData.put("absoluteX", pos.getX());
        blockData.put("absoluteY", pos.getY());
        blockData.put("absoluteZ", pos.getZ());

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

        return blockData;
    }

    private Map<String, Object> createEntityData(Entity entity, BlockPos origin) {
        Map<String, Object> entityData = new HashMap<>();

        // Relative position from scan origin
        entityData.put("x", entity.getX() - origin.getX());
        entityData.put("y", entity.getY() - origin.getY());
        entityData.put("z", entity.getZ() - origin.getZ());

        // Absolute position
        entityData.put("absoluteX", entity.getX());
        entityData.put("absoluteY", entity.getY());
        entityData.put("absoluteZ", entity.getZ());

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
        return entityData;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String getPropertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ScannerAdvancedPeripheral o && scanner == o.scanner;
    }

    @Override
    public Object getTarget() {
        return scanner;
    }
}
