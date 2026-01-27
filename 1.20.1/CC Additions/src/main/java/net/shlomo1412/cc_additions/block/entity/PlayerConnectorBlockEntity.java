package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shlomo1412.cc_additions.menu.PlayerConnectorMenu;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity for the Player Connector.
 * Stores the paired player's UUID and provides access to their data.
 */
public class PlayerConnectorBlockEntity extends BlockEntity implements MenuProvider {

    private static final String TAG_PAIRED_UUID = "PairedPlayerUUID";
    private static final String TAG_PAIRED_NAME = "PairedPlayerName";

    @Nullable
    private UUID pairedPlayerUUID = null;
    @Nullable
    private String pairedPlayerName = null;

    public PlayerConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYER_CONNECTOR.get(), pos, state);
    }

    /**
     * Check if this connector is paired to a player.
     */
    public boolean isPaired() {
        return pairedPlayerUUID != null;
    }

    /**
     * Get the paired player's UUID.
     */
    @Nullable
    public UUID getPairedPlayerUUID() {
        return pairedPlayerUUID;
    }

    /**
     * Get the paired player's name.
     */
    @Nullable
    public String getPairedPlayerName() {
        return pairedPlayerName;
    }

    /**
     * Get the paired player entity if they are online.
     */
    @Nullable
    public ServerPlayer getPairedPlayer() {
        if (pairedPlayerUUID == null || level == null || level.isClientSide) {
            return null;
        }
        if (level.getServer() != null) {
            return level.getServer().getPlayerList().getPlayer(pairedPlayerUUID);
        }
        return null;
    }

    /**
     * Check if the paired player is currently online.
     */
    public boolean isPairedPlayerOnline() {
        return getPairedPlayer() != null;
    }

    /**
     * Pair this connector to a player.
     */
    public void pairToPlayer(Player player) {
        this.pairedPlayerUUID = player.getUUID();
        this.pairedPlayerName = player.getName().getString();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Unpair this connector.
     */
    public void unpair() {
        this.pairedPlayerUUID = null;
        this.pairedPlayerName = null;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Check if the given player is the one paired to this connector.
     */
    public boolean isPairedTo(Player player) {
        return pairedPlayerUUID != null && pairedPlayerUUID.equals(player.getUUID());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (pairedPlayerUUID != null) {
            tag.putUUID(TAG_PAIRED_UUID, pairedPlayerUUID);
        }
        if (pairedPlayerName != null) {
            tag.putString(TAG_PAIRED_NAME, pairedPlayerName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID(TAG_PAIRED_UUID)) {
            this.pairedPlayerUUID = tag.getUUID(TAG_PAIRED_UUID);
        } else {
            this.pairedPlayerUUID = null;
        }
        if (tag.contains(TAG_PAIRED_NAME)) {
            this.pairedPlayerName = tag.getString(TAG_PAIRED_NAME);
        } else {
            this.pairedPlayerName = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.cc_additions.player_connector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PlayerConnectorMenu(containerId, inventory, this);
    }
}
