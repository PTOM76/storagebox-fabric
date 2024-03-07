package ml.pkom.storagebox;

import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

public class DataComponentTypes {
    public static DataComponentType<Integer> ITEM_COUNT;
    public static DataComponentType<Integer> AUTO_COLLECT;
    public static DataComponentType<ItemStack> ITEM_DATA;

    public static void init() {
        ITEM_COUNT = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("size"), DataComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());
        AUTO_COLLECT = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("auto"), DataComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build());
        ITEM_DATA = Registry.register(Registries.DATA_COMPONENT_TYPE, StorageBoxMod.id("item_data"), DataComponentType.<ItemStack>builder().codec(ItemStack.CODEC).packetCodec(ItemStack.PACKET_CODEC).build());
    }
}
