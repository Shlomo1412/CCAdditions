package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shlomo1412.cc_additions.Cc_additions;
import net.shlomo1412.cc_additions.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Cc_additions.MODID);

    public static final RegistryObject<BlockEntityType<ScannerBlockEntity>> SCANNER =
        BLOCK_ENTITIES.register("scanner", () ->
            BlockEntityType.Builder.of(ScannerBlockEntity::new, ModBlocks.SCANNER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ScannerAdvancedBlockEntity>> SCANNER_ADVANCED =
        BLOCK_ENTITIES.register("scanner_advanced", () ->
            BlockEntityType.Builder.of(ScannerAdvancedBlockEntity::new, ModBlocks.SCANNER_ADVANCED.get()).build(null));

    public static final RegistryObject<BlockEntityType<RemoteTerminalWallBlockEntity>> REMOTE_TERMINAL_WALL =
        BLOCK_ENTITIES.register("remote_terminal_wall", () ->
            BlockEntityType.Builder.of(RemoteTerminalWallBlockEntity::new, 
                ModBlocks.REMOTE_TERMINAL_WALL.get(), 
                ModBlocks.ADVANCED_REMOTE_TERMINAL_WALL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PlayerConnectorBlockEntity>> PLAYER_CONNECTOR =
        BLOCK_ENTITIES.register("player_connector", () ->
            BlockEntityType.Builder.of(PlayerConnectorBlockEntity::new, 
                ModBlocks.PLAYER_CONNECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FingerprintReaderBlockEntity>> FINGERPRINT_READER =
        BLOCK_ENTITIES.register("fingerprint_reader", () ->
            BlockEntityType.Builder.of(FingerprintReaderBlockEntity::new, 
                ModBlocks.FINGERPRINT_READER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ComputerizedTntBlockEntity>> COMPUTERIZED_TNT =
        BLOCK_ENTITIES.register("computerized_tnt", () ->
            BlockEntityType.Builder.of(ComputerizedTntBlockEntity::new,
                ModBlocks.COMPUTERIZED_TNT.get()).build(null));

    // VS2 Integration block entities
    public static final RegistryObject<BlockEntityType<ShipReaderBlockEntity>> SHIP_READER =
        BLOCK_ENTITIES.register("ship_reader", () ->
            BlockEntityType.Builder.of(ShipReaderBlockEntity::new,
                ModBlocks.SHIP_READER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ShipControllerBlockEntity>> SHIP_CONTROLLER =
        BLOCK_ENTITIES.register("ship_controller", () ->
            BlockEntityType.Builder.of(ShipControllerBlockEntity::new,
                ModBlocks.SHIP_CONTROLLER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
