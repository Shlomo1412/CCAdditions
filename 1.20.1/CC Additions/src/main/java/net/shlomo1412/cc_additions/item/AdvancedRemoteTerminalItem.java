package net.shlomo1412.cc_additions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.shlomo1412.cc_additions.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Advanced Remote Terminal - currently same as basic but with different appearance.
 * Future features: Works across dimensions with unlimited range, full control.
 * Right-click on a computer to pair, shift+right-click in air to unpair.
 * Right-click on a wall to hang it.
 */
public class AdvancedRemoteTerminalItem extends RemoteTerminalItem {

    public AdvancedRemoteTerminalItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    protected Block getWallBlock() {
        return ModBlocks.ADVANCED_REMOTE_TERMINAL_WALL.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
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
        } else {
            tooltip.add(Component.translatable("tooltip.cc_additions.remote_not_paired")
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("tooltip.cc_additions.remote_shift_unpair")
            .withStyle(ChatFormatting.DARK_GRAY));
        
        tooltip.add(Component.translatable("tooltip.cc_additions.remote_hang_on_wall")
            .withStyle(ChatFormatting.DARK_GRAY));
        
        tooltip.add(Component.translatable("tooltip.cc_additions.advanced_remote_coming_soon"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isPaired(stack);
    }
}
