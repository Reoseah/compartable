package io.github.reoseah.compartable.core;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.ByteObjectPair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PartContainerPackets {
    public static final Identifier UPDATE = new Identifier("compartable", "u");
    public static final byte REMOVE = 0;
    public static final byte ADD = 1;

    public static void syncPartReplacement(PartContainerBlockEntity container, BlockState part, BlockState replacementPart, @Nullable PlayerEntity syncUnneeded) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(container.getPos());
        buffer.writeVarInt(2);
        buffer.writeByte(REMOVE);
        buffer.writeVarInt(Block.STATE_IDS.getRawId(part));
        buffer.writeByte(ADD);
        buffer.writeVarInt(Block.STATE_IDS.getRawId(replacementPart));
        PlayerLookup.tracking(container).forEach(player -> {
            if (player == syncUnneeded) {
                return;
            }
            ServerPlayNetworking.send(player, UPDATE, buffer);
        });
    }

    @Environment(EnvType.CLIENT)
    public static void receiveUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        BlockPos pos = buffer.readBlockPos();
        int updateCount = buffer.readVarInt();
        List<ByteObjectPair<?>> updates = new ArrayList<>();
        for (int i = 0; i < updateCount; i++) {
            byte type = buffer.readByte();
            BlockState state = Block.STATE_IDS.get(buffer.readVarInt());
            updates.add(ByteObjectPair.of(type, state));
        }

        client.execute(() -> {
            BlockEntity entity = client.world.getBlockEntity(pos);

            if (entity instanceof PartContainerBlockEntity container) {
                for (ByteObjectPair<?> update : updates) {
                    if (update.leftByte() == REMOVE) {
                        container.getParts().remove((BlockState) update.right());
                    } else if (update.leftByte() == ADD) {
                        container.getParts().put((BlockState) update.right(), null);
                    }
                }
            }
        });
    }

    private PartContainerPackets() {
    }
}
