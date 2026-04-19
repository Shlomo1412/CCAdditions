package net.shlomo1412.cc_additions.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shlomo1412.cc_additions.Cc_additions;
import net.shlomo1412.cc_additions.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, Cc_additions.MODID);

    public static final RegistryObject<Block> SCANNER = registerBlock("scanner",
        () -> new ScannerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SCANNER_ADVANCED = registerBlock("scanner_advanced",
        () -> new ScannerAdvancedBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 8.0f)
            .requiresCorrectToolForDrops()));

    // Wall-mounted remote terminals (no block items - placed by remote terminal items)
    public static final RegistryObject<Block> REMOTE_TERMINAL_WALL = BLOCKS.register("remote_terminal_wall",
        () -> new RemoteTerminalWallBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(0.5f)
            .noOcclusion(), false));

    public static final RegistryObject<Block> ADVANCED_REMOTE_TERMINAL_WALL = BLOCKS.register("advanced_remote_terminal_wall",
        () -> new RemoteTerminalWallBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(0.5f)
            .noOcclusion(), true));

    public static final RegistryObject<Block> PLAYER_CONNECTOR = registerBlock("player_connector",
        () -> new PlayerConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()));

    public static final RegistryObject<Block> FINGERPRINT_READER = registerBlock("fingerprint_reader",
        () -> new FingerprintReaderBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()));

    public static final RegistryObject<Block> COMPUTERIZED_TNT = registerBlock("computerized_tnt",
        () -> new ComputerizedTntBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE)
            .strength(0.0f)
            .sound(net.minecraft.world.level.block.SoundType.GRASS)));

    // VS2 Integration blocks (always registered, but only functional when VS2 is loaded)
    public static final RegistryObject<Block> SHIP_READER = registerBlock("ship_reader",
        () -> new ShipReaderBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()));

    public static final RegistryObject<Block> SHIP_CONTROLLER = registerBlock("ship_controller",
        () -> new ShipControllerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, registeredBlock);
        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
