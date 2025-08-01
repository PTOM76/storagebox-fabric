package net.pitan76.storagebox;

import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.PacketByteBuf;
import net.pitan76.storagebox.mixin.KeyBindingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static net.pitan76.storagebox.StorageBoxItem.getItem;
import static net.pitan76.storagebox.StorageBoxItem.getStackInStorageBox;

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
        ScreenProviderRegistry.INSTANCE.registerFactory(StorageBoxMod.id("container"), StorageBoxScreen.FACTORY);

        ColorProviderRegistry.ITEM.register(((storageBoxStack, tintIndex) -> {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            if (stack == null || stack.isEmpty()) return -1;
            if (stack.getItem() instanceof ItemColorProvider) {
                ItemColorProvider provider = (ItemColorProvider) getItem(stack);
                return provider.getColor(stack, tintIndex);
            }

            try {
                return ColorProviderRegistry.ITEM.get(stack.getItem()).getColor(stack, tintIndex);
            } catch (NullPointerException e) {
                return -1;
            }

        }), StorageBoxItem.instance);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isKeyPressed()) {
                PlayerEntity player = client.player;
                if (player == null) return;
                if (player.getMainHandStack() == null) return;

                if (player.getMainHandStack().getItem() instanceof StorageBoxItem && player.getMainHandStack().hasTag()) {
                    if (isKeyDownShift()) {
                        if (isKeyDownCtrl()) {
                            // ドロップ: (: + Shift + Ctrl)
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString("put_out_and_throw");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), buf);
                        } else {
                            // 取り出す or コンテナーへ一括収納: (: + Shift)
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString("put_out");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), buf);
                        }

                    } else {
                        if (isKeyDownCtrl()) {
                            // AutoCollect切り替え: (: + Ctrl)
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString("auto_collect");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), buf);
                        } else {
                            // コンテナーやインベントリからすべてストレージボックスへ一括収納: (:)
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeString("put_in");
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), buf);
                        }
                    }
                }
            }
            if (client.player != null) {
                PlayerEntity player = client.player;
                if (player.getMainHandStack().getItem() instanceof StorageBoxItem)
                    StorageBoxItem.showBar(player, player.getMainHandStack());
            }
            coolDown--;
        });
    }

    private int coolDown = 0;

    private boolean isKeyPressed() {
        final Window mw = MinecraftClient.getInstance().window;
        if (InputUtil.isKeyPressed(mw.getHandle(), ((KeyBindingAccessor) keyBinding_COLON).getKeyCode().getKeyCode())) {
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
        final Window mw = MinecraftClient.getInstance().window;
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean isKeyDownCtrl() {
        final Window mw = MinecraftClient.getInstance().window;
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
