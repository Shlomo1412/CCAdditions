package net.shlomo1412.cc_additions;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.shlomo1412.cc_additions.block.ModBlocks;
import net.shlomo1412.cc_additions.block.entity.ModBlockEntities;
import net.shlomo1412.cc_additions.block.entity.ScannerBlockEntity;
import net.shlomo1412.cc_additions.block.entity.ScannerAdvancedBlockEntity;
import net.shlomo1412.cc_additions.item.ModCreativeTab;
import net.shlomo1412.cc_additions.item.ModItems;
import net.shlomo1412.cc_additions.peripheral.PeripheralProvider;
import net.shlomo1412.cc_additions.peripheral.ScannerPeripheral;
import net.shlomo1412.cc_additions.peripheral.ScannerAdvancedPeripheral;
import org.slf4j.Logger;

@Mod(Cc_additions.MODID)
public class Cc_additions {
    public static final String MODID = "cc_additions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Cc_additions() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register mod content
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register peripheral capability attachment
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, Cc_additions::attachPeripherals);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CC Additions initialized - Scanner peripherals ready!");
    }

    /**
     * Attach peripheral capabilities to block entities
     */
    public static void attachPeripherals(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ScannerBlockEntity scanner) {
            PeripheralProvider.attach(event, scanner, ScannerPeripheral::new);
        } else if (event.getObject() instanceof ScannerAdvancedBlockEntity advScanner) {
            PeripheralProvider.attach(event, advScanner, ScannerAdvancedPeripheral::new);
        }
    }
}
