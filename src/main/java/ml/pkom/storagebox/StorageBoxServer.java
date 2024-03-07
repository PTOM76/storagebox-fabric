package ml.pkom.storagebox;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class StorageBoxServer {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(KeyPayload.ID, KeyPayload.CODEC);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, KeyPayload.ID, ((payload, context) -> {
                String str = payload.getData();
                ServerPlayerEntity player = context.player();
                player.server.execute(() -> {
                            if (context.player().getMainHandStack().getItem() instanceof StorageBoxItem) {
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
                        }
                );

            }));
        });
    }
}
