package net.pitan76.storagebox;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

public class DataComponentTypes {
    public static ComponentType<Integer> ITEM_COUNT;
    public static ComponentType<Integer> AUTO_COLLECT;
    public static ComponentType<ItemStack> ITEM_DATA;

    public static void init() {
        ITEM_COUNT = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("size"), ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());
        AUTO_COLLECT = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("auto"), ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());
        ITEM_DATA = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("item_data"), ComponentType.<ItemStack>builder().codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC).build());
    }
}
