package net.pitan76.storagebox;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class DataComponentTypes {
    public static DataComponentType<Integer> ITEM_COUNT;
    public static DataComponentType<Integer> AUTO_COLLECT;
    public static DataComponentType<ItemStack> ITEM_DATA;

    public static void init() {
        ITEM_COUNT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, StorageBoxMod.id("size"), DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
        AUTO_COLLECT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, StorageBoxMod.id("auto"), DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
        ITEM_DATA = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, StorageBoxMod.id("item_data"), DataComponentType.<ItemStack>builder().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC).build());
    }
}
