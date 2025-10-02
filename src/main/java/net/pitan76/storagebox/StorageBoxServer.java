package net.pitan76.storagebox;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class StorageBoxServer {

    public static void init() {
        ServerPlayConnectionEvents.INIT.register((handler, server) ->
                ServerPlayNetworking.registerReceiver(handler, StorageBoxMod.id("key"), ((server1, player, handler1, buf, sender) -> {

            String str = buf.readString();
            player.server.execute(() -> {
                if (player.getMainHandStack().getItem() instanceof StorageBoxItem) {
                    ItemStack itemStack = player.getMainHandStack();
                    switch (str) {
                        case "put_out":
                            StorageBoxItem.keyboardEvent(0, player, itemStack);
                            break;
                        case "put_out_and_throw":
                            StorageBoxItem.keyboardEvent(1, player, itemStack);
                            break;
                        case "put_in":
                            StorageBoxItem.keyboardEvent(2, player, itemStack);
                            break;
                        case "auto_collect":
                            StorageBoxItem.keyboardEvent(3, player, itemStack);
                            break;
                    }
                }
            });
        })));
        ServerTickEvents.END_WORLD_TICK.register(world -> { // 万一スタックが整理MOD等で変化したら強制的に閉じる
            for (ServerPlayerEntity player : world.getPlayers()){
                if (player.isRemoved() || !(player.currentScreenHandler instanceof StorageBoxScreenHandler)) continue;
                StorageBoxScreenHandler storageBoxScreenHandler = (StorageBoxScreenHandler) player.currentScreenHandler;
                if (player.getMainHandStack().getItem() != StorageBoxItem.instance
                        || storageBoxScreenHandler.handStack != player.getMainHandStack()) {
                    player.closeHandledScreen();
                }
            }
        });
    }
}
