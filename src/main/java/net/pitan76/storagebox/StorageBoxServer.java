package net.pitan76.storagebox;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StorageBoxServer {

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(KeyPayload.ID, KeyPayload.CODEC);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, KeyPayload.ID, ((payload, context) -> {
                String str = payload.getData();
                ServerPlayer player = context.player();

                server.execute(() -> {
                            if (context.player().getActiveItem().getItem() instanceof StorageBoxItem) {
                                ItemStack itemStack = player.getActiveItem();
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
                        }
                );

            }));
        });
        ServerTickEvents.END_LEVEL_TICK.register(world -> { // 万一スタックが整理MOD等で変化したら強制的に閉じる
            for(ServerPlayer player : world.getPlayers((p) -> true)){
                if (!player.isRemoved()) {
                    if(player.containerMenu instanceof StorageBoxScreenHandler storageBoxScreenHandler){
                        if(player.getActiveItem().getItem() != StorageBoxItem.instance || storageBoxScreenHandler.handStack != player.getActiveItem()){
                            player.closeContainer();
                        }
                    }
                }

            }
        });
    }
}
