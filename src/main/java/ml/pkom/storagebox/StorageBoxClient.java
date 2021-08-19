package ml.pkom.storagebox;

import ml.pkom.storagebox.mixin.KeyBindingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class StorageBoxClient implements ClientModInitializer {

    private static KeyBinding keyBinding_COLON;

    @Override
    public void onInitializeClient() {
        keyBinding_COLON = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.storagebox.colon",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_APOSTROPHE,
                "key.storagebox.category"
        ));
        ScreenRegistry.register(StorageBoxScreenHandler.SCREEN_HANDLER_TYPE, StorageBoxScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isKeyPressed()) {
                PlayerEntity player = client.player;
                if (player.getMainHandStack().getItem() instanceof StorageBoxItem && player.getMainHandStack().hasTag()) {
                    if (isKeyDownShift()) {
                        if (isKeyDownCtrl()) {
                            // ドロップ: (: + Shift + Ctrl)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            BUF.writeString("put_out_and_throw");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        } else {
                            // 取り出す: (: + Shift)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            BUF.writeString("put_out");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        }

                    } else {
                        if (isKeyDownCtrl()) {
                            // コンテナーへ一括収納: (: + Ctrl)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            BUF.writeString("put_out_with_chest");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        } else {
                            // コンテナーやインベントリからすべてストレージボックスへ一括収納: (:)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            BUF.writeString("put_in");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        }
                    }
                }
            }
            if (client.player != null) {
                PlayerEntity player = client.player;
                if (player.getMainHandStack().getItem() instanceof StorageBoxItem && player.getMainHandStack().hasTag())
                    StorageBoxItem.showBar(player, player.getMainHandStack());
            }
        });
        //ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new ModelProvider());
    }

    private boolean isKeyPressed() {
        final Window mw = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(mw.getHandle(), ((KeyBindingAccessor) keyBinding_COLON).getBoundKey().getCode());
    }

    private boolean isKeyDownShift() {
        final Window mw = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean isKeyDownCtrl() {
        final Window mw = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
