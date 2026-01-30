package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.shlomo1412.cc_additions.block.entity.FingerprintReaderBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Peripheral that fires events when players interact with the fingerprint reader.
 * Provides comprehensive player data on each scan.
 */
public class FingerprintReaderPeripheral implements IPeripheral {

    private final FingerprintReaderBlockEntity blockEntity;
    private final List<IComputerAccess> computers = new ArrayList<>();

    public FingerprintReaderPeripheral(FingerprintReaderBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "fingerprint_reader";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof FingerprintReaderPeripheral p && p.blockEntity == this.blockEntity;
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        blockEntity.addPeripheral(this);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
        if (computers.isEmpty()) {
            blockEntity.removePeripheral(this);
        }
    }

    /**
     * Fire the player_scanned event to all connected computers.
     */
    public void firePlayerScannedEvent(Map<String, Object> playerData) {
        for (IComputerAccess computer : computers) {
            computer.queueEvent("fingerprint", playerData);
        }
    }

    // ==================== Lua Documentation Methods ====================

    @LuaFunction
    public final String getHelp() {
        return "Fingerprint Reader - Listen for 'fingerprint' events.\n" +
               "When a player right-clicks the reader, an event is fired with complete player data.\n" +
               "Usage: local event, data = os.pullEvent('fingerprint')\n" +
               "The data table contains: identity, health, food, position, movement, inventory, " +
               "armor, effects, experience, attributes, gameInfo, statistics, and more.";
    }

    // ==================== Static Data Collection ====================

    /**
     * Collect ALL available data about a player.
     */
    public static Map<String, Object> collectPlayerData(ServerPlayer player) {
        Map<String, Object> data = new HashMap<>();

        // Identity
        data.put("identity", collectIdentity(player));

        // Health & Vitals
        data.put("health", collectHealth(player));

        // Food & Hunger
        data.put("food", collectFood(player));

        // Position & Location
        data.put("position", collectPosition(player));

        // Movement & Velocity
        data.put("movement", collectMovement(player));

        // State Flags
        data.put("state", collectState(player));

        // Experience
        data.put("experience", collectExperience(player));

        // Game Info
        data.put("gameInfo", collectGameInfo(player));

        // Attributes
        data.put("attributes", collectAttributes(player));

        // Inventory
        data.put("inventory", collectInventory(player));

        // Hotbar
        data.put("hotbar", collectHotbar(player));

        // Armor
        data.put("armor", collectArmor(player));

        // Held Items
        data.put("heldItems", collectHeldItems(player));

        // Effects
        data.put("effects", collectEffects(player));

        // Ender Chest
        data.put("enderChest", collectEnderChest(player));

        // Statistics
        data.put("statistics", collectStatistics(player));

        // Scoreboard
        data.put("scoreboard", collectScoreboard(player));

        // Respawn & Bed
        data.put("respawn", collectRespawn(player));

        // Advancements count
        data.put("advancementsCompleted", countAdvancements(player));

        // Timestamps
        data.put("timestamps", collectTimestamps(player));

        // Entity info
        data.put("entity", collectEntityInfo(player));

        return data;
    }

    private static Map<String, Object> collectIdentity(ServerPlayer player) {
        Map<String, Object> identity = new HashMap<>();
        identity.put("name", player.getName().getString());
        identity.put("uuid", player.getUUID().toString());
        identity.put("displayName", player.getDisplayName().getString());
        identity.put("gameProfileName", player.getGameProfile().getName());
        if (player.getTeam() != null) {
            identity.put("team", player.getTeam().getName());
            identity.put("teamColor", player.getTeam().getColor().getName());
        }
        return identity;
    }

    private static Map<String, Object> collectHealth(ServerPlayer player) {
        Map<String, Object> health = new HashMap<>();
        health.put("current", player.getHealth());
        health.put("max", player.getMaxHealth());
        health.put("absorption", player.getAbsorptionAmount());
        health.put("armor", player.getArmorValue());
        health.put("armorToughness", player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        health.put("isDead", player.isDeadOrDying());
        health.put("deathTime", player.deathTime);
        health.put("hurtTime", player.hurtTime);
        health.put("invulnerableTime", player.invulnerableTime);
        return health;
    }

    private static Map<String, Object> collectFood(ServerPlayer player) {
        Map<String, Object> food = new HashMap<>();
        FoodData foodData = player.getFoodData();
        food.put("hunger", foodData.getFoodLevel());
        food.put("saturation", foodData.getSaturationLevel());
        food.put("exhaustion", foodData.getExhaustionLevel());
        food.put("needsFood", foodData.needsFood());
        return food;
    }

    private static Map<String, Object> collectPosition(ServerPlayer player) {
        Map<String, Object> position = new HashMap<>();
        position.put("x", player.getX());
        position.put("y", player.getY());
        position.put("z", player.getZ());
        position.put("blockX", player.getBlockX());
        position.put("blockY", player.getBlockY());
        position.put("blockZ", player.getBlockZ());
        position.put("yaw", player.getYRot());
        position.put("pitch", player.getXRot());
        position.put("headYaw", player.getYHeadRot());
        position.put("dimension", player.level().dimension().location().toString());
        position.put("biome", player.level().getBiome(player.blockPosition()).unwrapKey()
            .map(key -> key.location().toString()).orElse("unknown"));
        position.put("lightLevel", player.level().getMaxLocalRawBrightness(player.blockPosition()));
        position.put("isInChunk", player.level().hasChunkAt(player.blockPosition()));
        return position;
    }

    private static Map<String, Object> collectMovement(ServerPlayer player) {
        Map<String, Object> movement = new HashMap<>();
        Vec3 vel = player.getDeltaMovement();
        movement.put("velocityX", vel.x);
        movement.put("velocityY", vel.y);
        movement.put("velocityZ", vel.z);
        movement.put("speed", Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z));
        movement.put("horizontalSpeed", Math.sqrt(vel.x * vel.x + vel.z * vel.z));
        movement.put("fallDistance", player.fallDistance);
        movement.put("walkDistance", player.walkDist);
        movement.put("movementSpeed", player.getAttributeValue(Attributes.MOVEMENT_SPEED));
        movement.put("flyingSpeed", player.getAbilities().getFlyingSpeed());
        movement.put("walkingSpeed", player.getAbilities().getWalkingSpeed());
        return movement;
    }

    private static Map<String, Object> collectState(ServerPlayer player) {
        Map<String, Object> state = new HashMap<>();
        state.put("isOnGround", player.onGround());
        state.put("isSneaking", player.isCrouching());
        state.put("isSprinting", player.isSprinting());
        state.put("isSwimming", player.isSwimming());
        state.put("isFlying", player.getAbilities().flying);
        state.put("isGliding", player.isFallFlying());
        state.put("isSleeping", player.isSleeping());
        state.put("isBlocking", player.isBlocking());
        state.put("isUsingItem", player.isUsingItem());
        state.put("isInWater", player.isInWater());
        state.put("isUnderWater", player.isUnderWater());
        state.put("isInLava", player.isInLava());
        state.put("isOnFire", player.isOnFire());
        state.put("isInvisible", player.isInvisible());
        state.put("isInvulnerable", player.isInvulnerable());
        state.put("isPassenger", player.isPassenger());
        state.put("isVehicle", player.isVehicle());
        state.put("hasPassengers", player.isVehicle());
        state.put("isDescending", player.isDescending());
        state.put("isVisuallySwimming", player.isVisuallySwimming());
        state.put("isAutoSpinAttack", player.isAutoSpinAttack());
        state.put("isBaby", player.isBaby());
        state.put("isAffectedByPotions", player.isAffectedByPotions());
        state.put("canFreeze", player.canFreeze());
        state.put("isFullyFrozen", player.isFullyFrozen());
        state.put("freezingProgress", player.getPercentFrozen());
        state.put("ticksFrozen", player.getTicksFrozen());
        state.put("air", player.getAirSupply());
        state.put("maxAir", player.getMaxAirSupply());
        if (player.isPassenger() && player.getVehicle() != null) {
            state.put("vehicleType", player.getVehicle().getType().toShortString());
        }
        return state;
    }

    private static Map<String, Object> collectExperience(ServerPlayer player) {
        Map<String, Object> xp = new HashMap<>();
        xp.put("level", player.experienceLevel);
        xp.put("progress", player.experienceProgress);
        xp.put("total", player.totalExperience);
        xp.put("pointsToNextLevel", player.getXpNeededForNextLevel());
        return xp;
    }

    private static Map<String, Object> collectGameInfo(ServerPlayer player) {
        Map<String, Object> game = new HashMap<>();
        GameType gameType = player.gameMode.getGameModeForPlayer();
        game.put("gameMode", gameType.getName());
        game.put("gameModeId", gameType.getId());
        game.put("isCreative", player.isCreative());
        game.put("isSpectator", player.isSpectator());
        game.put("canFly", player.getAbilities().mayfly);
        game.put("canBuild", player.getAbilities().mayBuild);
        game.put("isInstabuild", player.getAbilities().instabuild);
        game.put("score", player.getScore());
        game.put("ping", player.latency);
        game.put("chatVisibility", player.getChatVisibility().name());
        game.put("allowsListing", player.allowsListing());
        game.put("hasRecipeBook", true);
        game.put("permissionLevel", player.getServer() != null ? 
            player.getServer().getProfilePermissions(player.getGameProfile()) : 0);
        return game;
    }

    private static Map<String, Object> collectAttributes(ServerPlayer player) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("maxHealth", player.getAttributeValue(Attributes.MAX_HEALTH));
        attrs.put("movementSpeed", player.getAttributeValue(Attributes.MOVEMENT_SPEED));
        attrs.put("attackDamage", player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        attrs.put("attackSpeed", player.getAttributeValue(Attributes.ATTACK_SPEED));
        attrs.put("attackKnockback", player.getAttributeValue(Attributes.ATTACK_KNOCKBACK));
        attrs.put("armor", player.getAttributeValue(Attributes.ARMOR));
        attrs.put("armorToughness", player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        attrs.put("knockbackResistance", player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        attrs.put("luck", player.getAttributeValue(Attributes.LUCK));
        return attrs;
    }

    private static List<Map<String, Object>> collectInventory(ServerPlayer player) {
        List<Map<String, Object>> inventory = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            inventory.add(itemStackToDetailedMap(stack, i));
        }
        return inventory;
    }

    private static List<Map<String, Object>> collectHotbar(ServerPlayer player) {
        List<Map<String, Object>> hotbar = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            hotbar.add(itemStackToDetailedMap(stack, i));
        }
        return hotbar;
    }

    private static List<Map<String, Object>> collectArmor(ServerPlayer player) {
        List<Map<String, Object>> armor = new ArrayList<>();
        String[] slotNames = {"feet", "legs", "chest", "head"};
        int i = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            Map<String, Object> item = itemStackToDetailedMap(stack, i);
            item.put("slotName", slotNames[i]);
            armor.add(item);
            i++;
        }
        return armor;
    }

    private static Map<String, Object> collectHeldItems(ServerPlayer player) {
        Map<String, Object> held = new HashMap<>();
        held.put("mainHand", itemStackToDetailedMap(player.getMainHandItem(), -1));
        held.put("offHand", itemStackToDetailedMap(player.getOffhandItem(), -1));
        held.put("selectedSlot", player.getInventory().selected);
        if (player.isUsingItem()) {
            held.put("usingItem", itemStackToDetailedMap(player.getUseItem(), -1));
            held.put("useItemRemainingTicks", player.getUseItemRemainingTicks());
        }
        return held;
    }

    private static List<Map<String, Object>> collectEffects(ServerPlayer player) {
        List<Map<String, Object>> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            Map<String, Object> effectData = new HashMap<>();
            effectData.put("id", effect.getEffect().getDescriptionId().replace("effect.", "").replace("minecraft.", ""));
            effectData.put("name", effect.getEffect().getDescriptionId());
            effectData.put("duration", effect.getDuration());
            effectData.put("durationSeconds", effect.getDuration() / 20.0);
            effectData.put("amplifier", effect.getAmplifier());
            effectData.put("level", effect.getAmplifier() + 1);
            effectData.put("ambient", effect.isAmbient());
            effectData.put("visible", effect.isVisible());
            effectData.put("showIcon", effect.showIcon());
            effectData.put("infinite", effect.isInfiniteDuration());
            effects.add(effectData);
        }
        return effects;
    }

    private static List<Map<String, Object>> collectEnderChest(ServerPlayer player) {
        List<Map<String, Object>> enderChest = new ArrayList<>();
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack stack = player.getEnderChestInventory().getItem(i);
            if (!stack.isEmpty()) {
                enderChest.add(itemStackToDetailedMap(stack, i));
            }
        }
        return enderChest;
    }

    private static Map<String, Object> collectStatistics(ServerPlayer player) {
        Map<String, Object> stats = new HashMap<>();
        ServerStatsCounter serverStats = player.getStats();

        // General stats
        stats.put("playTime", serverStats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)));
        stats.put("timeSinceDeath", serverStats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH)));
        stats.put("timeSinceRest", serverStats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)));
        stats.put("walkDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) / 100.0);
        stats.put("sprintDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)) / 100.0);
        stats.put("swimDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM)) / 100.0);
        stats.put("fallDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.FALL_ONE_CM)) / 100.0);
        stats.put("climbDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.CLIMB_ONE_CM)) / 100.0);
        stats.put("flyDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM)) / 100.0);
        stats.put("walkOnWaterDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.WALK_ON_WATER_ONE_CM)) / 100.0);
        stats.put("walkUnderWaterDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM)) / 100.0);
        stats.put("boatDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.BOAT_ONE_CM)) / 100.0);
        stats.put("minecartDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.MINECART_ONE_CM)) / 100.0);
        stats.put("horseDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.HORSE_ONE_CM)) / 100.0);
        stats.put("pigDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.PIG_ONE_CM)) / 100.0);
        stats.put("striderDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.STRIDER_ONE_CM)) / 100.0);
        stats.put("elytraDistance", serverStats.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM)) / 100.0);

        stats.put("jumps", serverStats.getValue(Stats.CUSTOM.get(Stats.JUMP)));
        stats.put("deaths", serverStats.getValue(Stats.CUSTOM.get(Stats.DEATHS)));
        stats.put("mobKills", serverStats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)));
        stats.put("playerKills", serverStats.getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)));
        stats.put("damageDealt", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)) / 10.0);
        stats.put("damageDealtAbsorbed", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_ABSORBED)) / 10.0);
        stats.put("damageDealtResisted", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_RESISTED)) / 10.0);
        stats.put("damageTaken", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN)) / 10.0);
        stats.put("damageBlocked", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)) / 10.0);
        stats.put("damageAbsorbed", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_ABSORBED)) / 10.0);
        stats.put("damageResisted", serverStats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED)) / 10.0);

        stats.put("itemsDropped", serverStats.getValue(Stats.CUSTOM.get(Stats.DROP)));
        stats.put("itemsEnchanted", serverStats.getValue(Stats.CUSTOM.get(Stats.ENCHANT_ITEM)));
        stats.put("animalsBred", serverStats.getValue(Stats.CUSTOM.get(Stats.ANIMALS_BRED)));
        stats.put("fishCaught", serverStats.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT)));
        stats.put("raidWins", serverStats.getValue(Stats.CUSTOM.get(Stats.RAID_WIN)));
        stats.put("raidTriggers", serverStats.getValue(Stats.CUSTOM.get(Stats.RAID_TRIGGER)));
        stats.put("targetsHit", serverStats.getValue(Stats.CUSTOM.get(Stats.TARGET_HIT)));

        stats.put("talkedToVillagers", serverStats.getValue(Stats.CUSTOM.get(Stats.TALKED_TO_VILLAGER)));
        stats.put("tradedWithVillagers", serverStats.getValue(Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER)));
        stats.put("cakesEaten", serverStats.getValue(Stats.CUSTOM.get(Stats.EAT_CAKE_SLICE)));
        stats.put("cauldronsFilled", serverStats.getValue(Stats.CUSTOM.get(Stats.FILL_CAULDRON)));
        stats.put("cauldronsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.USE_CAULDRON)));
        stats.put("armorCleaned", serverStats.getValue(Stats.CUSTOM.get(Stats.CLEAN_ARMOR)));
        stats.put("bannersCleaned", serverStats.getValue(Stats.CUSTOM.get(Stats.CLEAN_BANNER)));
        stats.put("shulkerBoxesCleaned", serverStats.getValue(Stats.CUSTOM.get(Stats.CLEAN_SHULKER_BOX)));

        stats.put("chestsOpened", serverStats.getValue(Stats.CUSTOM.get(Stats.OPEN_CHEST)));
        stats.put("enderChestsOpened", serverStats.getValue(Stats.CUSTOM.get(Stats.OPEN_ENDERCHEST)));
        stats.put("shulkerBoxesOpened", serverStats.getValue(Stats.CUSTOM.get(Stats.OPEN_SHULKER_BOX)));
        stats.put("barrelsOpened", serverStats.getValue(Stats.CUSTOM.get(Stats.OPEN_BARREL)));

        stats.put("noteBlocksPlayed", serverStats.getValue(Stats.CUSTOM.get(Stats.PLAY_NOTEBLOCK)));
        stats.put("noteBlocksTuned", serverStats.getValue(Stats.CUSTOM.get(Stats.TUNE_NOTEBLOCK)));
        stats.put("recordsPlayed", serverStats.getValue(Stats.CUSTOM.get(Stats.PLAY_RECORD)));
        stats.put("bellsRung", serverStats.getValue(Stats.CUSTOM.get(Stats.BELL_RING)));

        stats.put("beaconsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_BEACON)));
        stats.put("brewingStandsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_BREWINGSTAND)));
        stats.put("craftingTablesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_CRAFTING_TABLE)));
        stats.put("furnacesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_FURNACE)));
        stats.put("blastFurnacesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_BLAST_FURNACE)));
        stats.put("smokersUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_SMOKER)));
        stats.put("lecternsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_LECTERN)));
        stats.put("anvilsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_ANVIL)));
        stats.put("grindstonesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_GRINDSTONE)));
        stats.put("smithingTablesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_SMITHING_TABLE)));
        stats.put("loomsUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_LOOM)));
        stats.put("stonetcuttersUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_STONECUTTER)));
        stats.put("cartographyTablesUsed", serverStats.getValue(Stats.CUSTOM.get(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE)));

        stats.put("potFlowersPlanted", serverStats.getValue(Stats.CUSTOM.get(Stats.POT_FLOWER)));
        stats.put("trappedChestsTriggered", serverStats.getValue(Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST)));
        stats.put("timesSlept", serverStats.getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED)));

        return stats;
    }

    private static Map<String, Object> collectScoreboard(ServerPlayer player) {
        Map<String, Object> scoreboardData = new HashMap<>();
        Scoreboard scoreboard = player.getScoreboard();
        String playerName = player.getScoreboardName();

        Map<String, Integer> scores = new HashMap<>();
        for (Objective objective : scoreboard.getObjectives()) {
            if (scoreboard.hasPlayerScore(playerName, objective)) {
                Score score = scoreboard.getOrCreatePlayerScore(playerName, objective);
                scores.put(objective.getName(), score.getScore());
            }
        }
        scoreboardData.put("scores", scores);

        if (player.getTeam() != null) {
            Map<String, Object> team = new HashMap<>();
            team.put("name", player.getTeam().getName());
            team.put("color", player.getTeam().getColor().getName());
            team.put("friendlyFire", player.getTeam().isAllowFriendlyFire());
            team.put("seeFriendlyInvisibles", player.getTeam().canSeeFriendlyInvisibles());
            scoreboardData.put("team", team);
        }

        return scoreboardData;
    }

    private static Map<String, Object> collectRespawn(ServerPlayer player) {
        Map<String, Object> respawn = new HashMap<>();
        BlockPos respawnPos = player.getRespawnPosition();
        if (respawnPos != null) {
            respawn.put("x", respawnPos.getX());
            respawn.put("y", respawnPos.getY());
            respawn.put("z", respawnPos.getZ());
            respawn.put("dimension", player.getRespawnDimension().location().toString());
            respawn.put("angle", player.getRespawnAngle());
            respawn.put("forced", player.isRespawnForced());
        } else {
            respawn.put("isWorldSpawn", true);
        }

        player.getSleepingPos().ifPresent(bedPos -> {
            Map<String, Object> bed = new HashMap<>();
            bed.put("x", bedPos.getX());
            bed.put("y", bedPos.getY());
            bed.put("z", bedPos.getZ());
            respawn.put("currentBed", bed);
        });

        return respawn;
    }

    private static int countAdvancements(ServerPlayer player) {
        // Count completed advancements
        return (int) player.getServer().getAdvancements().getAllAdvancements().stream()
            .filter(adv -> player.getAdvancements().getOrStartProgress(adv).isDone())
            .count();
    }

    private static Map<String, Object> collectTimestamps(ServerPlayer player) {
        Map<String, Object> timestamps = new HashMap<>();
        timestamps.put("ticksExisted", player.tickCount);
        timestamps.put("lastHurtTime", player.getLastHurtByMobTimestamp());
        timestamps.put("sleepTimer", player.getSleepTimer());
        return timestamps;
    }

    private static Map<String, Object> collectEntityInfo(ServerPlayer player) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("entityId", player.getId());
        entity.put("entityType", player.getType().toShortString());
        entity.put("width", player.getBbWidth());
        entity.put("height", player.getBbHeight());
        entity.put("eyeHeight", player.getEyeHeight());

        // Bounding box
        Map<String, Object> boundingBox = new HashMap<>();
        boundingBox.put("minX", player.getBoundingBox().minX);
        boundingBox.put("minY", player.getBoundingBox().minY);
        boundingBox.put("minZ", player.getBoundingBox().minZ);
        boundingBox.put("maxX", player.getBoundingBox().maxX);
        boundingBox.put("maxY", player.getBoundingBox().maxY);
        boundingBox.put("maxZ", player.getBoundingBox().maxZ);
        entity.put("boundingBox", boundingBox);

        // Look vector
        Vec3 look = player.getLookAngle();
        Map<String, Object> lookVector = new HashMap<>();
        lookVector.put("x", look.x);
        lookVector.put("y", look.y);
        lookVector.put("z", look.z);
        entity.put("lookVector", lookVector);

        return entity;
    }

    private static Map<String, Object> itemStackToDetailedMap(ItemStack stack, int slot) {
        Map<String, Object> item = new HashMap<>();
        if (stack.isEmpty()) {
            item.put("empty", true);
            item.put("name", "minecraft:air");
            item.put("count", 0);
        } else {
            item.put("empty", false);
            item.put("name", stack.getItem().builtInRegistryHolder().key().location().toString());
            item.put("count", stack.getCount());
            item.put("maxCount", stack.getMaxStackSize());
            item.put("damage", stack.getDamageValue());
            item.put("maxDamage", stack.getMaxDamage());
            item.put("durabilityPercent", stack.getMaxDamage() > 0 ? 
                (1.0 - (double) stack.getDamageValue() / stack.getMaxDamage()) * 100 : 100);
            item.put("enchanted", stack.isEnchanted());
            item.put("repairable", stack.isRepairable());
            item.put("isDamaged", stack.isDamaged());
            item.put("isDamageableItem", stack.isDamageableItem());
            item.put("isStackable", stack.isStackable());
            item.put("rarity", stack.getRarity().name());

            if (stack.hasCustomHoverName()) {
                item.put("displayName", stack.getHoverName().getString());
                item.put("hasCustomName", true);
            } else {
                item.put("displayName", stack.getHoverName().getString());
                item.put("hasCustomName", false);
            }

            // Enchantments
            if (stack.isEnchanted()) {
                List<Map<String, Object>> enchants = new ArrayList<>();
                stack.getEnchantmentTags().forEach(tag -> {
                    if (tag instanceof net.minecraft.nbt.CompoundTag compoundTag) {
                        Map<String, Object> enchant = new HashMap<>();
                        enchant.put("id", compoundTag.getString("id"));
                        enchant.put("level", compoundTag.getInt("lvl"));
                        enchants.add(enchant);
                    }
                });
                item.put("enchantments", enchants);
            }

            // NBT presence
            item.put("hasNbt", stack.hasTag());
        }
        if (slot >= 0) {
            item.put("slot", slot);
        }
        return item;
    }
}
