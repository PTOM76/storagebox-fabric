package net.pitan76.storagebox;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.lwjgl.glfw.GLFW;

public class StorageBoxClient implements ClientModInitializer {

    private static KeyMapping keyBinding_COLON;

    @Override
    public void onInitializeClient() {
        keyBinding_COLON = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.storagebox.colon",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_APOSTROPHE,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("storagebox", "main"))
        ));
        MenuScreens.register(StorageBoxScreenHandler.SCREEN_HANDLER_TYPE, StorageBoxScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isKeyPressed()) {
                Player player = client.player;
                if (player == null) return;

                if (player.getActiveItem().getItem() instanceof StorageBoxItem && !player.getActiveItem().isEmpty()) {
                    if (isKeyDownShift()) {
                        if (isKeyDownCtrl()) {
                            // ドロップ: (: + Shift + Ctrl)
                            ClientPlayNetworking.send(new KeyPayload("put_out_and_throw"));
                        } else {
                            // 取り出す or コンテナーへ一括収納: (: + Shift)
                            ClientPlayNetworking.send(new KeyPayload("put_out"));
                        }

                    } else {
                        if (isKeyDownCtrl()) {
                            // AutoCollect切り替え: (: + Ctrl)
                            ClientPlayNetworking.send(new KeyPayload("auto_collect"));
                        } else {
                            // コンテナーやインベントリからすべてストレージボックスへ一括収納: (:)
                            ClientPlayNetworking.send(new KeyPayload("put_in"));
                        }
                    }
                }
            }
            if (client.player != null) {
                Player player = client.player;
                if (player.getActiveItem().getItem() instanceof StorageBoxItem)
                    StorageBoxItem.showBar(player, player.getActiveItem());
            }
            coolDown--;
        });
    }

    private int coolDown = 0;

    private boolean isKeyPressed() {
        final Window mw = Minecraft.getInstance().getWindow();
        if (InputConstants.isKeyDown(mw, keyBinding_COLON.key.getValue())) {
            if (coolDown <= 0) {
                coolDown = 3;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean isKeyDownShift() {
        final Window mw = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(mw, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(mw, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean isKeyDownCtrl() {
        final Window mw = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(mw, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(mw, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
