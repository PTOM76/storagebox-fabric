package net.pitan76.storagebox;

import net.legacyfabric.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;

public class StorageBoxServer {

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(StorageBoxMod.lfid("key"), (server, player, handler, buf, responseSender) -> {
            String str = buf.readString(256);
            if (str.equals("put_out"))
                server.submit(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem.keyboardEvent(0, player, itemStack);
                    }
                });
            if (str.equals("put_out_and_throw"))
                server.submit(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem.keyboardEvent(1, player, itemStack);
                    }
                });
            if (str.equals("put_in"))
                server.submit(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem.keyboardEvent(2, player, itemStack);
                    }
                });
            if (str.equals("auto_collect"))
                server.submit(() -> {
                    if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                        ItemStack itemStack = player.getMainHandStack();
                        StorageBoxItem.keyboardEvent(3, player, itemStack);
                    }
                });
        });
    }
}
