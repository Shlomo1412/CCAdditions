package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.shlomo1412.cc_additions.block.entity.PlayerConnectorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Peripheral that provides access to paired player data.
 */
public class PlayerConnectorPeripheral implements IPeripheral {

    private final PlayerConnectorBlockEntity blockEntity;

    public PlayerConnectorPeripheral(PlayerConnectorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "player_connector";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof PlayerConnectorPeripheral p && p.blockEntity == this.blockEntity;
    }

    // ==================== Pairing Info ====================

    @LuaFunction
    public final boolean isPaired() {
        return blockEntity.isPaired();
    }

    @LuaFunction
    public final @Nullable String getPairedPlayerName() {
        return blockEntity.getPairedPlayerName();
    }

    @LuaFunction
    public final @Nullable String getPairedPlayerUUID() {
        UUID uuid = blockEntity.getPairedPlayerUUID();
        return uuid != null ? uuid.toString() : null;
    }

    @LuaFunction
    public final boolean isOnline() {
        return blockEntity.isPairedPlayerOnline();
    }

    // ==================== Health & Food ====================

    @LuaFunction
    public final @Nullable Double getHealth() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? (double) player.getHealth() : null;
    }

    @LuaFunction
    public final @Nullable Double getMaxHealth() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? (double) player.getMaxHealth() : null;
    }

    @LuaFunction
    public final @Nullable Double getAbsorption() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? (double) player.getAbsorptionAmount() : null;
    }

    @LuaFunction
    public final @Nullable Integer getHunger() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        FoodData food = player.getFoodData();
        return food.getFoodLevel();
    }

    @LuaFunction
    public final @Nullable Double getSaturation() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        FoodData food = player.getFoodData();
        return (double) food.getSaturationLevel();
    }

    @LuaFunction
    public final @Nullable Integer getAir() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getAirSupply() : null;
    }

    @LuaFunction
    public final @Nullable Integer getMaxAir() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getMaxAirSupply() : null;
    }

    @LuaFunction
    public final @Nullable Integer getArmorValue() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getArmorValue() : null;
    }

    // ==================== Movement & Position ====================

    @LuaFunction
    public final @Nullable Map<String, Object> getPosition() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Map<String, Object> pos = new HashMap<>();
        pos.put("x", player.getX());
        pos.put("y", player.getY());
        pos.put("z", player.getZ());
        return pos;
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getBlockPosition() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        BlockPos pos = player.blockPosition();
        Map<String, Object> result = new HashMap<>();
        result.put("x", pos.getX());
        result.put("y", pos.getY());
        result.put("z", pos.getZ());
        return result;
    }

    @LuaFunction
    public final @Nullable String getDimension() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.level().dimension().location().toString() : null;
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getVelocity() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Vec3 vel = player.getDeltaMovement();
        Map<String, Object> result = new HashMap<>();
        result.put("x", vel.x);
        result.put("y", vel.y);
        result.put("z", vel.z);
        return result;
    }

    @LuaFunction
    public final @Nullable Double getSpeed() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Vec3 vel = player.getDeltaMovement();
        return Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z);
    }

    @LuaFunction
    public final @Nullable Double getHorizontalSpeed() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Vec3 vel = player.getDeltaMovement();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getRotation() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("yaw", player.getYRot());
        result.put("pitch", player.getXRot());
        return result;
    }

    // ==================== State Checks ====================

    @LuaFunction
    public final @Nullable Boolean isSneaking() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isCrouching() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isCrouching() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isCrouching() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isSprinting() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isSprinting() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isRunning() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isSprinting() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isWalking() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        Vec3 vel = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        return horizontalSpeed > 0.01 && !player.isSprinting();
    }

    @LuaFunction
    public final @Nullable Boolean isSwimming() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isSwimming() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isFlying() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getAbilities().flying : null;
    }

    @LuaFunction
    public final @Nullable Boolean isOnGround() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.onGround() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isInWater() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isInWater() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isInLava() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isInLava() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isOnFire() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isOnFire() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isSleeping() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isSleeping() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isBlocking() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isBlocking() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isGliding() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isFallFlying() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isInvisible() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isInvisible() : null;
    }

    // ==================== Experience ====================

    @LuaFunction
    public final @Nullable Integer getExperienceLevel() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.experienceLevel : null;
    }

    @LuaFunction
    public final @Nullable Double getExperienceProgress() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? (double) player.experienceProgress : null;
    }

    @LuaFunction
    public final @Nullable Integer getTotalExperience() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.totalExperience : null;
    }

    // ==================== Game Mode ====================

    @LuaFunction
    public final @Nullable String getGameMode() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        GameType gameType = player.gameMode.getGameModeForPlayer();
        return gameType.getName();
    }

    // ==================== Inventory ====================

    @LuaFunction
    public final @Nullable List<Map<String, Object>> getInventory() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        List<Map<String, Object>> inventory = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            inventory.add(itemStackToMap(stack, i));
        }
        return inventory;
    }

    @LuaFunction
    public final @Nullable List<Map<String, Object>> getHotbar() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        List<Map<String, Object>> hotbar = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            hotbar.add(itemStackToMap(stack, i));
        }
        return hotbar;
    }

    @LuaFunction
    public final @Nullable Integer getSelectedSlot() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getInventory().selected : null;
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getMainHandItem() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return itemStackToMap(player.getMainHandItem(), -1);
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getOffHandItem() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return itemStackToMap(player.getOffhandItem(), -1);
    }

    @LuaFunction
    public final @Nullable List<Map<String, Object>> getArmorItems() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        List<Map<String, Object>> armor = new ArrayList<>();
        int slot = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            armor.add(itemStackToMap(stack, slot++));
        }
        return armor;
    }

    // ==================== Effects ====================

    @LuaFunction
    public final @Nullable List<Map<String, Object>> getEffects() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        List<Map<String, Object>> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            Map<String, Object> effectData = new HashMap<>();
            effectData.put("name", effect.getEffect().getDescriptionId());
            effectData.put("id", effect.getEffect().getDescriptionId().replace("effect.", "").replace("minecraft.", ""));
            effectData.put("duration", effect.getDuration());
            effectData.put("amplifier", effect.getAmplifier());
            effectData.put("ambient", effect.isAmbient());
            effectData.put("visible", effect.isVisible());
            effects.add(effectData);
        }
        return effects;
    }

    @LuaFunction
    public final @Nullable Boolean hasEffect(String effectName) {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        for (MobEffectInstance effect : player.getActiveEffects()) {
            String id = effect.getEffect().getDescriptionId();
            if (id.contains(effectName)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Attributes ====================

    @LuaFunction
    public final @Nullable Double getMovementSpeed() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return player.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @LuaFunction
    public final @Nullable Double getAttackDamage() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @LuaFunction
    public final @Nullable Double getAttackSpeed() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return player.getAttributeValue(Attributes.ATTACK_SPEED);
    }

    @LuaFunction
    public final @Nullable Double getLuck() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        return player.getAttributeValue(Attributes.LUCK);
    }

    // ==================== Misc ====================

    @LuaFunction
    public final @Nullable Integer getScore() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.getScore() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isCreative() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isCreative() : null;
    }

    @LuaFunction
    public final @Nullable Boolean isSpectator() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        return player != null ? player.isSpectator() : null;
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getBedPosition() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        return player.getSleepingPos().map(pos -> {
            Map<String, Object> result = new HashMap<>();
            result.put("x", pos.getX());
            result.put("y", pos.getY());
            result.put("z", pos.getZ());
            return result;
        }).orElse(null);
    }

    @LuaFunction
    public final @Nullable Map<String, Object> getRespawnPosition() {
        ServerPlayer player = blockEntity.getPairedPlayer();
        if (player == null) return null;
        
        BlockPos respawn = player.getRespawnPosition();
        if (respawn == null) return null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("x", respawn.getX());
        result.put("y", respawn.getY());
        result.put("z", respawn.getZ());
        result.put("dimension", player.getRespawnDimension().location().toString());
        return result;
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> itemStackToMap(ItemStack stack, int slot) {
        Map<String, Object> item = new HashMap<>();
        if (stack.isEmpty()) {
            item.put("name", "minecraft:air");
            item.put("count", 0);
            item.put("empty", true);
        } else {
            item.put("name", stack.getItem().builtInRegistryHolder().key().location().toString());
            item.put("count", stack.getCount());
            item.put("maxCount", stack.getMaxStackSize());
            item.put("damage", stack.getDamageValue());
            item.put("maxDamage", stack.getMaxDamage());
            item.put("enchanted", stack.isEnchanted());
            item.put("empty", false);
            if (stack.hasCustomHoverName()) {
                item.put("displayName", stack.getHoverName().getString());
            }
        }
        if (slot >= 0) {
            item.put("slot", slot);
        }
        return item;
    }
}
