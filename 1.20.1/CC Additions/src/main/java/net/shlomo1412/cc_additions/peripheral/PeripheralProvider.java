package net.shlomo1412.cc_additions.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.shlomo1412.cc_additions.Cc_additions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Helper class that lazily creates peripherals for block entities and attaches them as capabilities.
 */
public class PeripheralProvider<O extends BlockEntity> implements ICapabilityProvider {
    
    public static final Capability<IPeripheral> CAPABILITY_PERIPHERAL = 
        CapabilityManager.get(new CapabilityToken<>() {});
    
    private static final ResourceLocation PERIPHERAL_CAP = 
        new ResourceLocation(Cc_additions.MODID, "peripheral");

    private final O blockEntity;
    private final Function<O, IPeripheral> factory;
    private @Nullable LazyOptional<IPeripheral> peripheral;

    private PeripheralProvider(O blockEntity, Function<O, IPeripheral> factory) {
        this.blockEntity = blockEntity;
        this.factory = factory;
    }

    /**
     * Attach a peripheral provider to a block entity.
     *
     * @param event       The capability attach event
     * @param blockEntity The block entity to attach to
     * @param factory     A factory function that creates the peripheral
     * @param <O>         The type of block entity
     */
    public static <O extends BlockEntity> void attach(
        AttachCapabilitiesEvent<BlockEntity> event,
        O blockEntity,
        Function<O, IPeripheral> factory
    ) {
        var provider = new PeripheralProvider<>(blockEntity, factory);
        event.addCapability(PERIPHERAL_CAP, provider);
        event.addListener(provider::invalidate);
    }

    private void invalidate() {
        if (peripheral != null) {
            peripheral.invalidate();
        }
        peripheral = null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if (capability != CAPABILITY_PERIPHERAL) {
            return LazyOptional.empty();
        }
        if (blockEntity.isRemoved()) {
            return LazyOptional.empty();
        }

        var peripheral = this.peripheral;
        return (peripheral == null
            ? (this.peripheral = LazyOptional.of(() -> factory.apply(blockEntity)))
            : peripheral).cast();
    }
}
