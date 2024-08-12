package net.pitan76.storagebox;

import net.fabricmc.api.ClientModInitializer;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.legacyfabric.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.legacyfabric.fabric.api.networking.v1.PacketByteBufs;
import net.legacyfabric.fabric.api.registry.v1.RegistryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.PacketByteBuf;
import net.pitan76.storagebox.mixin.KeyBindingAccessor;
import org.lwjgl.input.Keyboard;

public class StorageBoxClient implements ClientModInitializer {

    private static KeyBinding keyBinding_COLON;

    @Override
    public void onInitializeClient() {
        keyBinding_COLON = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.storagebox.colon",
                Keyboard.KEY_APOSTROPHE,
                "key.storagebox.category"
        ));

        //.register(StorageBoxScreenHandler.SCREEN_HANDLER_TYPE, StorageBoxScreen::new);

        /*
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

         */


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            /*
            if (isKeyPressed()) {
                PlayerEntity player = client.player;
                if (player == null) return;
                if (player.getMainHandStack() == null) return;

                if (player.getMainHandStack().getItem() instanceof StorageBoxItem && !player.getMainHandStack().isEmpty()) {
                    if (isKeyDownShift()) {
                        if (isKeyDownCtrl()) {
                            // ドロップ: (: + Shift + Ctrl)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            NbtCompound tag = new NbtCompound();tag.putString("type", "put_out_and_throw");
                            BUF.writeNbtCompound(tag);
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        } else {
                            // 取り出す or コンテナーへ一括収納: (: + Shift)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            NbtCompound tag = new NbtCompound();tag.putString("type", "put_out");
                            BUF.writeNbtCompound(tag);
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        }

                    } else {
                        if (isKeyDownCtrl()) {
                            // AutoCollect切り替え: (: + Ctrl)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            NbtCompound tag = new NbtCompound();tag.putString("type", "auto_collect");
                            BUF.writeNbtCompound(tag);
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        } else {
                            // コンテナーやインベントリからすべてストレージボックスへ一括収納: (:)
                            PacketByteBuf BUF = PacketByteBufs.create();
                            NbtCompound tag = new NbtCompound();tag.putString("type", "put_in");
                            BUF.writeNbtCompound(tag);
                            ClientPlayNetworking.send(StorageBoxMod.id("key"), BUF);
                        }
                    }
                }
            }

             */
            if (client.player != null) {
                PlayerEntity player = client.player;
                if (player.getMainHandStack().getItem() instanceof StorageBoxItem)
                    StorageBoxItem.showBar(player, player.getMainHandStack());
            }
            coolDown--;
        });
    }

    private int coolDown = 0;

    /*
    private boolean isKeyPressed() {
        Window mw = new Window(MinecraftClient.getInstance());
        if (.isKeyPressed(mw.getHandle(), ((KeyBindingAccessor) keyBinding_COLON).getBoundKey().getCode())) {
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
        final Window mw = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean isKeyDownCtrl() {
        final Window mw = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

     */
}
