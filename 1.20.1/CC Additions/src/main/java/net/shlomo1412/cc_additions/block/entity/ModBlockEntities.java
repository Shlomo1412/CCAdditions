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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
