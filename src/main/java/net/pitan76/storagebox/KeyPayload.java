package net.pitan76.storagebox;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class KeyPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KeyPayload> ID = new Type<>(StorageBoxMod.id("key"));
    public static final StreamCodec<FriendlyByteBuf, KeyPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(KeyPayload::new, KeyPayload::getData).cast();
    public String data;

    public KeyPayload(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
