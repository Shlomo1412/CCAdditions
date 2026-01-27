package net.shlomo1412.cc_additions.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shlomo1412.cc_additions.block.ModBlocks;
import net.shlomo1412.cc_additions.block.entity.PlayerConnectorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Container menu for the Player Connector GUI.
 * This menu allows players to pair/unpair with the connector.
 */
public class PlayerConnectorMenu extends AbstractContainerMenu {

    private final PlayerConnectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // Client-side constructor (from network packet)
    public PlayerConnectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    // Server-side constructor
    public PlayerConnectorMenu(int containerId, Inventory playerInventory, PlayerConnectorBlockEntity blockEntity) {
        super(ModMenuTypes.PLAYER_CONNECTOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static PlayerConnectorBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        Level level = playerInventory.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PlayerConnectorBlockEntity connector) {
            return connector;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not a PlayerConnectorBlockEntity");
    }

    public PlayerConnectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isPaired() {
        return blockEntity.isPaired();
    }

    @Nullable
    public String getPairedPlayerName() {
        return blockEntity.getPairedPlayerName();
    }

    @Nullable
    public UUID getPairedPlayerUUID() {
        return blockEntity.getPairedPlayerUUID();
    }

    public boolean isPairedTo(Player player) {
        return blockEntity.isPairedTo(player);
    }

    public boolean isPairedPlayerOnline() {
        return blockEntity.isPairedPlayerOnline();
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No inventory slots, so no quick move
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.PLAYER_CONNECTOR.get());
    }
}
