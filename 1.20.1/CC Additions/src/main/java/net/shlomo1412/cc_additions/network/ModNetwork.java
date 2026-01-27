package net.shlomo1412.cc_additions.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.shlomo1412.cc_additions.Cc_additions;

import java.util.Optional;

/**
 * Network handler for CC Additions mod.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Cc_additions.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++,
            PlayerConnectorPacket.class,
            PlayerConnectorPacket::encode,
            PlayerConnectorPacket::new,
            PlayerConnectorPacket::handle,
            Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToAllClients(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
