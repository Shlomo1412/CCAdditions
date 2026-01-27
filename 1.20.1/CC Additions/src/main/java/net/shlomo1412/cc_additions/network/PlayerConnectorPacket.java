package net.shlomo1412.cc_additions.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.shlomo1412.cc_additions.block.entity.PlayerConnectorBlockEntity;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to pair/unpair with a Player Connector.
 */
public class PlayerConnectorPacket {

    private final BlockPos pos;
    private final boolean pair; // true = pair, false = unpair

    public PlayerConnectorPacket(BlockPos pos, boolean pair) {
        this.pos = pos;
        this.pair = pair;
    }

    public PlayerConnectorPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.pair = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(pair);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof PlayerConnectorBlockEntity connector) {
                if (pair) {
                    // Only pair if not already paired
                    if (!connector.isPaired()) {
                        connector.pairToPlayer(player);
                    }
                } else {
                    // Only unpair if paired to this player
                    if (connector.isPairedTo(player)) {
                        connector.unpair();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
