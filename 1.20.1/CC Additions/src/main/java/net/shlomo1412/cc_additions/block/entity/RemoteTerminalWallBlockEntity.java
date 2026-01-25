package net.shlomo1412.cc_additions.block.entity;

import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.shlomo1412.cc_additions.block.RemoteTerminalWallBlock;
import net.shlomo1412.cc_additions.item.ModItems;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for wall-mounted remote terminals.
 * Stores the pairing data and handles opening the terminal.
 */
public class RemoteTerminalWallBlockEntity extends BlockEntity {

    private static final String TAG_PAIRED = "PairedComputer";
    private static final String TAG_COMPUTER_ID = "ComputerId";
    private static final String TAG_COMPUTER_POS_X = "ComputerPosX";
    private static final String TAG_COMPUTER_POS_Y = "ComputerPosY";
    private static final String TAG_COMPUTER_POS_Z = "ComputerPosZ";
    private static final String TAG_COMPUTER_DIMENSION = "ComputerDimension";
    private static final String TAG_COMPUTER_LABEL = "ComputerLabel";

    private boolean paired = false;
    private int computerId = -1;
    private BlockPos computerPos = BlockPos.ZERO;
    private String computerDimension = "";
    private String computerLabel = "";

    public RemoteTerminalWallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REMOTE_TERMINAL_WALL.get(), pos, state);
    }

    public boolean isPaired() {
        return paired;
    }

    public int getComputerId() {
        return computerId;
    }

    @Nullable
    public BlockPos getComputerPos() {
        return paired ? computerPos : null;
    }

    @Nullable
    public ResourceKey<Level> getComputerDimension() {
        if (!paired || computerDimension.isEmpty()) return null;
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(computerDimension));
    }

    @Nullable
    public String getComputerLabel() {
        return paired && !computerLabel.isEmpty() ? computerLabel : null;
    }

    /**
     * Load pairing data from an item stack.
     */
    public void loadFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean(TAG_PAIRED)) {
            this.paired = true;
            this.computerId = tag.getInt(TAG_COMPUTER_ID);
            this.computerPos = new BlockPos(
                tag.getInt(TAG_COMPUTER_POS_X),
                tag.getInt(TAG_COMPUTER_POS_Y),
                tag.getInt(TAG_COMPUTER_POS_Z)
            );
            this.computerDimension = tag.getString(TAG_COMPUTER_DIMENSION);
            this.computerLabel = tag.getString(TAG_COMPUTER_LABEL);
        }
        setChanged();
    }

    /**
     * Create an item stack with the pairing data.
     */
    public ItemStack createItemStack() {
        boolean advanced = getBlockState().getBlock() instanceof RemoteTerminalWallBlock wallBlock && wallBlock.isAdvanced();
        ItemStack stack = new ItemStack(advanced ? ModItems.ADVANCED_REMOTE_TERMINAL.get() : ModItems.REMOTE_TERMINAL.get());
        
        if (paired) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(TAG_PAIRED, true);
            tag.putInt(TAG_COMPUTER_ID, computerId);
            tag.putInt(TAG_COMPUTER_POS_X, computerPos.getX());
            tag.putInt(TAG_COMPUTER_POS_Y, computerPos.getY());
            tag.putInt(TAG_COMPUTER_POS_Z, computerPos.getZ());
            tag.putString(TAG_COMPUTER_DIMENSION, computerDimension);
            if (!computerLabel.isEmpty()) {
                tag.putString(TAG_COMPUTER_LABEL, computerLabel);
            }
        }
        
        return stack;
    }

    /**
     * Open the paired computer's terminal for the player.
     */
    public void openTerminal(Player player) {
        if (level == null || level.isClientSide) return;

        if (!paired) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_not_paired")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        ResourceKey<Level> pairedDim = getComputerDimension();
        if (pairedDim == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_invalid")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        ServerLevel targetLevel = level.getServer() != null 
            ? level.getServer().getLevel(pairedDim) 
            : null;
            
        if (targetLevel == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_dimension_unloaded")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        if (!targetLevel.isLoaded(computerPos)) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_not_loaded")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        BlockEntity blockEntity = targetLevel.getBlockEntity(computerPos);
        if (!(blockEntity instanceof AbstractComputerBlockEntity computer)) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_computer_missing")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        ServerComputer serverComputer = computer.createServerComputer();
        if (serverComputer == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_computer_error")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Update label if it changed
        String currentLabel = computer.getLabel();
        if (currentLabel != null && !currentLabel.equals(this.computerLabel)) {
            this.computerLabel = currentLabel;
            setChanged();
        }

        final ServerComputer finalComputer = serverComputer;
        final Level finalLevel = level;
        
        NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.cc_additions.remote_terminal");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player menuPlayer) {
                return new ComputerMenuWithoutInventory(
                    ModRegistry.Menus.COMPUTER.get(),
                    id,
                    inventory,
                    p -> isPaired() && isComputerStillValid(finalLevel),
                    finalComputer
                );
            }
        }, buf -> new ComputerContainerData(finalComputer, ItemStack.EMPTY).toBytes(buf));
    }

    private boolean isComputerStillValid(Level level) {
        if (!paired || level.getServer() == null) return false;
        
        ResourceKey<Level> dim = getComputerDimension();
        if (dim == null) return false;
        
        ServerLevel targetLevel = level.getServer().getLevel(dim);
        if (targetLevel == null || !targetLevel.isLoaded(computerPos)) return false;
        
        BlockEntity be = targetLevel.getBlockEntity(computerPos);
        return be instanceof AbstractComputerBlockEntity;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_PAIRED, paired);
        if (paired) {
            tag.putInt(TAG_COMPUTER_ID, computerId);
            tag.putInt(TAG_COMPUTER_POS_X, computerPos.getX());
            tag.putInt(TAG_COMPUTER_POS_Y, computerPos.getY());
            tag.putInt(TAG_COMPUTER_POS_Z, computerPos.getZ());
            tag.putString(TAG_COMPUTER_DIMENSION, computerDimension);
            tag.putString(TAG_COMPUTER_LABEL, computerLabel);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.paired = tag.getBoolean(TAG_PAIRED);
        if (paired) {
            this.computerId = tag.getInt(TAG_COMPUTER_ID);
            this.computerPos = new BlockPos(
                tag.getInt(TAG_COMPUTER_POS_X),
                tag.getInt(TAG_COMPUTER_POS_Y),
                tag.getInt(TAG_COMPUTER_POS_Z)
            );
            this.computerDimension = tag.getString(TAG_COMPUTER_DIMENSION);
            this.computerLabel = tag.getString(TAG_COMPUTER_LABEL);
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
}
