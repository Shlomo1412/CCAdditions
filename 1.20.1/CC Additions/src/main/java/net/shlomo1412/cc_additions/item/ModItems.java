package net.shlomo1412.cc_additions.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.shlomo1412.cc_additions.Cc_additions;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, Cc_additions.MODID);

    // Block items are registered automatically by ModBlocks

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
