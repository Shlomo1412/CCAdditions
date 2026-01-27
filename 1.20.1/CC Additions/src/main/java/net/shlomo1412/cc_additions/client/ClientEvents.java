package net.shlomo1412.cc_additions.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.shlomo1412.cc_additions.Cc_additions;
import net.shlomo1412.cc_additions.client.screen.PlayerConnectorScreen;
import net.shlomo1412.cc_additions.menu.ModMenuTypes;

@Mod.EventBusSubscriber(modid = Cc_additions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.PLAYER_CONNECTOR.get(), PlayerConnectorScreen::new);
        });
    }
}
