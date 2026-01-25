package net.shlomo1412.cc_additions.item;

import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.shlomo1412.cc_additions.block.ModBlocks;
import net.shlomo1412.cc_additions.block.RemoteTerminalWallBlock;
import net.shlomo1412.cc_additions.block.entity.RemoteTerminalWallBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Remote Terminal - allows viewing a paired computer's terminal remotely.
 * Right-click on a computer to pair, shift+right-click in air to unpair.
 * This basic version is view-only.
 */
public class RemoteTerminalItem extends Item {

    private static final String TAG_PAIRED = "PairedComputer";
    private static final String TAG_COMPUTER_ID = "ComputerId";
    private static final String TAG_COMPUTER_POS_X = "ComputerPosX";
    private static final String TAG_COMPUTER_POS_Y = "ComputerPosY";
    private static final String TAG_COMPUTER_POS_Z = "ComputerPosZ";
    private static final String TAG_COMPUTER_DIMENSION = "ComputerDimension";
    private static final String TAG_COMPUTER_LABEL = "ComputerLabel";

    public RemoteTerminalItem(Properties properties) {
        super(properties);
    }

    /**
     * Check if this remote terminal is paired to a computer.
     */
    public static boolean isPaired(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_PAIRED);
    }

    /**
     * Get the paired computer's ID.
     */
    public static int getPairedComputerId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(TAG_COMPUTER_ID) : -1;
    }

    /**
     * Get the paired computer's position.
     */
    @Nullable
    public static BlockPos getPairedPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(TAG_PAIRED)) return null;
        return new BlockPos(
            tag.getInt(TAG_COMPUTER_POS_X),
            tag.getInt(TAG_COMPUTER_POS_Y),
            tag.getInt(TAG_COMPUTER_POS_Z)
        );
    }

    /**
     * Get the paired computer's dimension.
     */
    @Nullable
    public static ResourceKey<Level> getPairedDimension(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COMPUTER_DIMENSION)) return null;
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString(TAG_COMPUTER_DIMENSION)));
    }

    /**
     * Get the paired computer's label.
     */
    @Nullable
    public static String getPairedLabel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COMPUTER_LABEL)) return null;
        return tag.getString(TAG_COMPUTER_LABEL);
    }

    /**
     * Pair this remote terminal to a computer.
     */
    public static void pairToComputer(ItemStack stack, AbstractComputerBlockEntity computer, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_PAIRED, true);
        tag.putInt(TAG_COMPUTER_ID, computer.getComputerID());
        
        BlockPos pos = computer.getBlockPos();
        tag.putInt(TAG_COMPUTER_POS_X, pos.getX());
        tag.putInt(TAG_COMPUTER_POS_Y, pos.getY());
        tag.putInt(TAG_COMPUTER_POS_Z, pos.getZ());
        tag.putString(TAG_COMPUTER_DIMENSION, level.dimension().location().toString());
        
        String label = computer.getLabel();
        if (label != null) {
            tag.putString(TAG_COMPUTER_LABEL, label);
        } else {
            tag.remove(TAG_COMPUTER_LABEL);
        }
    }

    /**
     * Unpair this remote terminal.
     */
    public static void unpair(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_PAIRED);
            tag.remove(TAG_COMPUTER_ID);
            tag.remove(TAG_COMPUTER_POS_X);
            tag.remove(TAG_COMPUTER_POS_Y);
            tag.remove(TAG_COMPUTER_POS_Z);
            tag.remove(TAG_COMPUTER_DIMENSION);
            tag.remove(TAG_COMPUTER_LABEL);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction clickedFace = context.getClickedFace();

        if (player == null) return InteractionResult.PASS;

        // Check if clicked on a computer - pair to it
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractComputerBlockEntity computer) {
            if (!level.isClientSide) {
                // Pair to this computer
                pairToComputer(stack, computer, level);
                
                String label = computer.getLabel();
                String computerName = label != null ? label : "Computer #" + computer.getComputerID();
                player.displayClientMessage(
                    Component.translatable("message.cc_additions.remote_paired", computerName)
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Check if clicked on a horizontal face (wall) - place on wall
        if (clickedFace.getAxis().isHorizontal()) {
            BlockPos placePos = pos.relative(clickedFace);
            
            // Check if the position is valid for placement
            if (level.getBlockState(placePos).canBeReplaced()) {
                Block wallBlock = getWallBlock();
                BlockState wallState = wallBlock.defaultBlockState()
                    .setValue(RemoteTerminalWallBlock.FACING, clickedFace);
                
                // Check if it can survive on this wall
                if (wallState.canSurvive(level, placePos)) {
                    if (!level.isClientSide) {
                        level.setBlock(placePos, wallState, 3);
                        
                        // Transfer NBT data to the block entity
                        BlockEntity placedBE = level.getBlockEntity(placePos);
                        if (placedBE instanceof RemoteTerminalWallBlockEntity wallTerminal) {
                            wallTerminal.loadFromItem(stack);
                        }
                        
                        // Consume the item if not in creative
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * Get the wall block to place. Override in subclass for advanced version.
     */
    protected Block getWallBlock() {
        return ModBlocks.REMOTE_TERMINAL_WALL.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift+right-click in air to unpair
        if (player.isShiftKeyDown()) {
            if (isPaired(stack)) {
                if (!level.isClientSide) {
                    unpair(stack);
                    player.displayClientMessage(
                        Component.translatable("message.cc_additions.remote_unpaired")
                            .withStyle(ChatFormatting.YELLOW),
                        true
                    );
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            return InteractionResultHolder.pass(stack);
        }

        // Regular right-click - try to open paired computer's terminal
        if (!isPaired(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.cc_additions.remote_not_paired")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            return openRemoteTerminal(stack, level, player) 
                ? InteractionResultHolder.success(stack) 
                : InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * Try to open the paired computer's terminal (view-only).
     */
    private boolean openRemoteTerminal(ItemStack stack, Level level, Player player) {
        BlockPos pairedPos = getPairedPos(stack);
        ResourceKey<Level> pairedDim = getPairedDimension(stack);
        
        if (pairedPos == null || pairedDim == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_invalid")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return false;
        }

        // Get the correct dimension
        ServerLevel targetLevel = level.getServer() != null 
            ? level.getServer().getLevel(pairedDim) 
            : null;
            
        if (targetLevel == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_dimension_unloaded")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return false;
        }

        // Check if chunk is loaded
        if (!targetLevel.isLoaded(pairedPos)) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_not_loaded")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return false;
        }

        // Get the computer block entity
        BlockEntity blockEntity = targetLevel.getBlockEntity(pairedPos);
        if (!(blockEntity instanceof AbstractComputerBlockEntity computer)) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_computer_missing")
                    .withStyle(ChatFormatting.RED),
                true
            );
            // Computer was removed - unpair
            unpair(stack);
            return false;
        }

        // Get or create the server computer
        ServerComputer serverComputer = computer.createServerComputer();
        if (serverComputer == null) {
            player.displayClientMessage(
                Component.translatable("message.cc_additions.remote_computer_error")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return false;
        }

        // Update label in case it changed
        String currentLabel = computer.getLabel();
        CompoundTag tag = stack.getOrCreateTag();
        if (currentLabel != null) {
            tag.putString(TAG_COMPUTER_LABEL, currentLabel);
        } else {
            tag.remove(TAG_COMPUTER_LABEL);
        }

        // Open the computer terminal using NetworkHooks
        final ServerComputer finalComputer = serverComputer;
        final ItemStack finalStack = stack;
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
                    p -> isPaired(finalStack) && isComputerStillValid(finalStack, finalLevel),
                    finalComputer
                );
            }
        }, buf -> new ComputerContainerData(finalComputer, ItemStack.EMPTY).toBytes(buf));

        return true;
    }

    /**
     * Check if the paired computer is still valid.
     */
    private boolean isComputerStillValid(ItemStack stack, Level level) {
        BlockPos pos = getPairedPos(stack);
        ResourceKey<Level> dim = getPairedDimension(stack);
        if (pos == null || dim == null || level.getServer() == null) return false;
        
        ServerLevel targetLevel = level.getServer().getLevel(dim);
        if (targetLevel == null || !targetLevel.isLoaded(pos)) return false;
        
        BlockEntity be = targetLevel.getBlockEntity(pos);
        return be instanceof AbstractComputerBlockEntity;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (isPaired(stack)) {
            int computerId = getPairedComputerId(stack);
            String label = getPairedLabel(stack);
            BlockPos pos = getPairedPos(stack);

            if (label != null && !label.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.cc_additions.remote_paired_to", label)
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("tooltip.cc_additions.remote_paired_to", "Computer #" + computerId)
                    .withStyle(ChatFormatting.GREEN));
            }

            if (pos != null) {
                tooltip.add(Component.translatable("tooltip.cc_additions.remote_position", 
                    pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.translatable("tooltip.cc_additions.remote_view_only")
                .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.cc_additions.remote_not_paired")
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("tooltip.cc_additions.remote_shift_unpair")
            .withStyle(ChatFormatting.DARK_GRAY));
        
        tooltip.add(Component.translatable("tooltip.cc_additions.remote_hang_on_wall")
            .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isPaired(stack);
    }
}
