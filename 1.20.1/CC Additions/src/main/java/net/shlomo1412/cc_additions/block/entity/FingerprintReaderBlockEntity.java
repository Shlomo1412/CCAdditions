package net.shlomo1412.cc_additions.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shlomo1412.cc_additions.peripheral.FingerprintReaderPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Block entity for the Fingerprint Reader.
 * Fires events to connected computers when players interact with it.
 */
public class FingerprintReaderBlockEntity extends BlockEntity {

    private final List<FingerprintReaderPeripheral> peripherals = new ArrayList<>();

    public FingerprintReaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FINGERPRINT_READER.get(), pos, state);
    }

    /**
     * Register a peripheral to receive events.
     */
    public void addPeripheral(FingerprintReaderPeripheral peripheral) {
        if (!peripherals.contains(peripheral)) {
            peripherals.add(peripheral);
        }
    }

    /**
     * Unregister a peripheral.
     */
    public void removePeripheral(FingerprintReaderPeripheral peripheral) {
        peripherals.remove(peripheral);
    }

    /**
     * Called when a player right-clicks the fingerprint reader.
     */
    public void onPlayerInteract(Player player) {
        if (level == null || level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Collect all player data
        Map<String, Object> playerData = FingerprintReaderPeripheral.collectPlayerData(serverPlayer);

        // Fire event to all connected peripherals
        for (FingerprintReaderPeripheral peripheral : peripherals) {
            peripheral.firePlayerScannedEvent(playerData);
        }
    }
}
