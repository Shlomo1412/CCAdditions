package net.shlomo1412.cc_additions.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shlomo1412.cc_additions.Cc_additions;
import net.shlomo1412.cc_additions.block.ModBlocks;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cc_additions.MODID);

    public static final RegistryObject<CreativeModeTab> CC_ADDITIONS_TAB = CREATIVE_MODE_TABS.register("cc_additions_tab",
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.ADVANCED_REMOTE_TERMINAL.get()))
            .title(Component.translatable("itemGroup.cc_additions"))
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.SCANNER.get());
                output.accept(ModBlocks.SCANNER_ADVANCED.get());
                output.accept(ModItems.REMOTE_TERMINAL.get());
                output.accept(ModItems.ADVANCED_REMOTE_TERMINAL.get());
                output.accept(ModBlocks.PLAYER_CONNECTOR.get());
                output.accept(ModBlocks.FINGERPRINT_READER.get());
                output.accept(ModBlocks.COMPUTERIZED_TNT.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
