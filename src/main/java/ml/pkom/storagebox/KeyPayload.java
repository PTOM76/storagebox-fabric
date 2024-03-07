package ml.pkom.storagebox;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public class KeyPayload implements CustomPayload {
    public static final CustomPayload.Id<KeyPayload> ID = new Id<>(StorageBoxMod.id("key"));
    public static final PacketCodec<PacketByteBuf, KeyPayload> CODEC = PacketCodecs.STRING.xmap(KeyPayload::new, KeyPayload::getData).cast();
    public String data;

    public KeyPayload(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
