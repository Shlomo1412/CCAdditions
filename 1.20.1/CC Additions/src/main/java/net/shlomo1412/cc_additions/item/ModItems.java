package net.shlomo1412.cc_additions.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shlomo1412.cc_additions.Cc_additions;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, Cc_additions.MODID);

    // Block items are registered automatically by ModBlocks

    public static final RegistryObject<Item> REMOTE_TERMINAL = ITEMS.register("remote_terminal",
        () -> new RemoteTerminalItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ADVANCED_REMOTE_TERMINAL = ITEMS.register("advanced_remote_terminal",
        () -> new AdvancedRemoteTerminalItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
