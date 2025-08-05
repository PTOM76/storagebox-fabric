package net.pitan76.storagebox;

import com.mojang.nbt.tags.CompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Optional;

public class StorageBoxItem extends Item {
    /*
    NBT:
    - StorageItem(String): Item ID (アイテムID)
    - StorageDamage(int): Item damage (アイテムのダメージ値 (メタデータ))
    - StorageSize(int): Item count (アイテム数)
    - StorageAuto(int): Auto collect (自動回収)
    - StorageItemData(ItemStack): ItemStack (アイテムのスタックデータ) Ex. ItemStack.fromTag(nbt)
     */

    public static String KEY_ITEM_ID = "StorageItem"; // Old
    private static String KEY_DAMAGE = "StorageDamage";
    public static String KEY_SIZE = "StorageSize";
    public static String KEY_AUTO = "StorageAuto"; // 0 = true, 1 = false
    public static String KEY_ITEM_DATA = "StorageItemData";

    public static Item getItem(ItemStack storageBoxStack) {
        ItemStack stack = getStackInStorageBox(storageBoxStack);
        if (stack != null)
            return stack.getItem();

        Optional<Item> item = fixItemId(storageBoxStack);
        return item.orElse(null);
    }

    public static Optional<Item> fixItemId(ItemStack storageBoxStack) {
        // 以前の数値IDのみしか含まれていない場合
        int itemId = getItemDataAsInt(storageBoxStack, KEY_ITEM_ID);

        if (itemId != -1 && getItemDataAsInt(storageBoxStack, KEY_SIZE) > 0) {
            Item item = Item.getItem(itemId);
            int damage = getItemDataAsInt(storageBoxStack, KEY_DAMAGE);
            if (damage != -1) {
                setItemStack(storageBoxStack, new ItemStack(item, 1, damage));
                removeItemDataAsInt(storageBoxStack, KEY_DAMAGE);
            } else {
                setItemStack(storageBoxStack, new ItemStack(item));
            }
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
            return Optional.of(item);
        }

        return Optional.empty();
    }

    public static boolean hasStackInStorageBox(ItemStack storageBoxStack) {
        return getStackInStorageBox(storageBoxStack) != null;
    }

    // null のときは hasStackInStorageBox(storageBoxStack) で判定すること
    public static ItemStack getStackInStorageBox(ItemStack storageBoxStack) {
        ItemStack result;
        CompoundTag nbt = storageBoxStack.getData();
        
        if (nbt.containsKey(KEY_ITEM_ID)) {
            return fixItemId(storageBoxStack).map(ItemStack::new).orElse(null);
        }

        if (!nbt.containsKey(KEY_ITEM_DATA)) return null;

        nbt = nbt.getCompound(KEY_ITEM_DATA);
        
        result = ItemStack.readItemStackFromNbt(nbt);
        result.stackSize = 1;
        
        return result;
    }

    // stackのNBTから数値のデータを取り出す
    public static int getItemDataAsInt(ItemStack storageBoxStack, String key) {
        int data = -1;
        CompoundTag nbt = storageBoxStack.getData();

        if (nbt != null) {
            if (key.equals(KEY_SIZE) && nbt.containsKey("countInBox"))
                return nbt.getInteger("countInBox");

            data = nbt.getInteger(key);
        }
        if (key.equals(KEY_AUTO) && (nbt == null || !nbt.containsKey(key))) {
            // 0 = true, 1 = false
            Boolean defaultAutoCollect = ModConfig.getBoolean("DefaultAutoCollect");
            if (defaultAutoCollect == null) return 0;
            return defaultAutoCollect ? 0 : 1;
        }

        return data;
    }

    public static boolean isAutoCollect(ItemStack storageBoxStack) {
        CompoundTag nbt = storageBoxStack.getData();
        if (!nbt.containsKey(KEY_AUTO) && nbt.containsKey("autoCollect")) {
            return nbt.getBoolean("autoCollect");
        }
        
        return getItemDataAsInt(storageBoxStack, KEY_AUTO) == 0;
    }

    public static void changeAutoCollect(ItemStack storageBoxStack) {
        int value = isAutoCollect(storageBoxStack) ? 1 : 0;
        setItemDataAsInt(storageBoxStack, KEY_AUTO, value);
    }

    public static void setItemDataAsInt(ItemStack storageBoxStack, String key, int data) {
        CompoundTag stackNbt = storageBoxStack.getData();
        if (stackNbt == null) stackNbt = new CompoundTag();

        stackNbt.putInt(key, data);
        storageBoxStack.setData(stackNbt);
    }

    public static void setItemDataAsInt(ItemStack storageBoxStack, String key, CompoundTag nbt) {
        CompoundTag stackNbt = storageBoxStack.getData();
        if (stackNbt == null) stackNbt = new CompoundTag();

        if (nbt != null)
            stackNbt.put(key, nbt);
        storageBoxStack.setData(stackNbt);
    }

    public static void removeItemDataAsInt(ItemStack stack, String key) {
        setItemDataAsInt(stack, key, null);
    }

    public static void setItemStack(ItemStack storageBoxStack, ItemStack newStack) {
        if (storageBoxStack == null) return;
        if (newStack == null || newStack.stackSize == 0) {
            setItemDataAsInt(storageBoxStack, KEY_ITEM_DATA, null);
            return;
        }
        CompoundTag nbt = new CompoundTag();
        newStack.writeToNBT(nbt);
        setItemDataAsInt(storageBoxStack, KEY_ITEM_DATA, nbt);
    }

    public static void setItemStackSize(ItemStack storageBoxStack, int size) {
        if (storageBoxStack == null || storageBoxStack.stackSize == 0) return;
        setItemDataAsInt(storageBoxStack, KEY_SIZE, size);
    }

    //
    public static StorageBoxItem instance = new StorageBoxItem();

    public StorageBoxItem() {
        super("storagebox", "storagebox", 10000);
    }

    @Environment(EnvType.CLIENT)
    public static void showBar(ItemStack storageBoxStack) {
        StringBuilder sb;
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);

            sb = calcItemNumByUnit(getItemDataAsInt(storageBoxStack, KEY_SIZE), true, stack.getMaxStackSize());
            sb.insert(0, stack.getCustomName() + " / ");
        } else {
            sb = new StringBuilder("Empty");
        }

        String string = sb.toString();

        Minecraft client = Minecraft.getMinecraft();
        //Window window = new Window(client);

//        int x = (int) (window.getScaledWidth() / 2 - (double) client.textRenderer.getStringWidth(string) / 2);
//        int y = (int) (window.getScaledHeight() - 80);

        client.font.drawString(string, 20, 20, 0xFFFFFF);
    }

    public void dropItemStack(Entity entity, ItemStack itemStack) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            player.dropItem(itemStack.getItem().id, itemStack.stackSize);
            itemStack.stackSize = 0;
        }
    }

    @Override
    public ItemStack onUseItem(ItemStack storageBoxStack, World world, Player user) {
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int countInBox = getItemDataAsInt(storageBoxStack, KEY_SIZE);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > 64) {
                countIsOverMax = true;
                itemInBoxCount = 64;
                countInBox -= 64;
            }
            user.inventory.setItem(user.inventory.getCurrentItemIndex(), stack);
            stack.stackSize = itemInBoxCount;

            ItemStack result;

            result = stack.useItemRightClick(world, user);

            user.inventory.setItem(user.inventory.getCurrentItemIndex(), storageBoxStack);

            if (stack.isItemEqual(result)) {
                // 食べ物など一定の時間を使って消費するアイテム
//                if (user.isUsingItem()) {
//                    user.stopUsingItem();
//                    user.setUseItem(storageBoxStack, stack.getMaxUseTime());
//                }
            } else {
                // バケツ => 液体バケツなどのサポート
//                if (result != null && result.stackSize != 0)
//                    user.inventory.insertStack(result);
//                if (result.getResult().equals(ActionResult.CONSUME)) {
//                    stack.setCount(stack.getCount() - 1);
//                }
            }

            if (countIsOverMax) {
                countInBox += stack.stackSize;
            } else {
                countInBox = stack.stackSize;
            }

            if (stack.stackSize <= 0) {
                removeItemDataAsInt(storageBoxStack, KEY_SIZE);
                removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
                removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
                removeItemDataAsInt(storageBoxStack, KEY_AUTO);

            } else {
                setItemStackSize(storageBoxStack, countInBox);
                setItemStack(storageBoxStack, stack);
            }
            return storageBoxStack;
        }
//        if (!world.isClientSide) {
//            user.displayContainerScreen(StorageBoxScreenHandler.FACTORY);
//        } else {
//            Minecraft.getMinecraft().displayScreen(new StorageBoxScreen(user.inventory, Text.text()));
//        }
        return storageBoxStack;
    }

//    public ItemStack onFinishUse(ItemStack storageBoxStack, World world, Player user) {
//        Item item = getItem(storageBoxStack);
//
//        if (item != null && hasStackInStorageBox(storageBoxStack)) {
//            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
//            stack.stackSize = 64;
//            ItemStack result = item.onFinishUse(stack, world, user);
//
//            // ポーション => ガラス瓶などのサポート
//            if (!stack.getItem().equals(result.getItem()))
//                dropItemStack(user, result);
//            setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.stackSize));
//        }
//
//        return super.onFinishUse(storageBoxStack, world, user);
//    }

    @Override
    public boolean onBlockDestroyed(World world, ItemStack stack, int removedBlockId, int x, int y, int z, Side side, Mob mob) {
        Item item = getItem(stack);

        if (item != null && hasStackInStorageBox(stack)) {
            ItemStack itemStack = getStackInStorageBox(stack).copy();
            itemStack.stackSize = 64;
            boolean result = item.onBlockDestroyed(world, itemStack, removedBlockId, x, y, z, side, mob);
            setItemStackSize(stack, getItemDataAsInt(stack, KEY_SIZE) - (64 - itemStack.stackSize));
            return result;
        }

        return super.onBlockDestroyed(world, stack, removedBlockId, x, y, z, side, mob);
    }

    @Override
    public boolean hitEntity(ItemStack stack, Mob target, Mob attacker) {
        Item item = getItem(stack);

        if (item != null && hasStackInStorageBox(stack)) {
            ItemStack itemStack = getStackInStorageBox(stack).copy();
            itemStack.stackSize = 64;
            boolean result = item.hitEntity(itemStack, target, attacker);
            setItemStackSize(stack, getItemDataAsInt(stack, KEY_SIZE) - (64 - itemStack.stackSize));
            return result;
        }

        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean useItemOnEntity(ItemStack storageBoxStack, Mob mob, Player user) {
        boolean result;
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.stackSize = 64;
            result = item.useItemOnEntity(stack, mob, user);
            setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.stackSize));
        } else {
            result = super.useItemOnEntity(storageBoxStack, mob, user);
        }

        return result;
    }

    //    @Override
//    public UseAction getUseAction(ItemStack storageBoxStack) {
//        UseAction result;
//        Item item = getItem(storageBoxStack);
//
//        if (item != null) {
//            ItemStack stack = getStackInStorageBox(storageBoxStack);
//            result = item.getUseAction(stack);
//        } else {
//            result = super.getUseAction(storageBoxStack);
//        }
//
//        return result;
//    }

//    @Override
//    public int getMaxUseTime(ItemStack storageBoxStack) {
//        Item item = getItem(storageBoxStack);
//
//        if (item != null) {
//            ItemStack stack = getStackInStorageBox(storageBoxStack);
//            return item.getMaxUseTime(stack);
//        }
//
//        return super.getMaxUseTime(storageBoxStack);
//    }

//    @Override
//    public void onUseStopped(ItemStack storageBoxStack, World world, Player user, int remainingUseTicks) {
//        Item item = getItem(storageBoxStack);
//
//        if (item == null) {
//            super.onUseStopped(storageBoxStack, world, user, remainingUseTicks);
//            return;
//        }
//
//        ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
//        stack.stackSize = 64;
//        item.onUseStopped(stack, world, user, remainingUseTicks);
//        setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.stackSize));
//    }

    @Override
    public boolean onUseItemOnBlock(ItemStack storageBoxStack, Player user, World world, int blockX, int blockY, int blockZ, Side side, double xPlaced, double yPlaced) {
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int countInBox = getItemDataAsInt(storageBoxStack, KEY_SIZE);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > 64) {
                countIsOverMax = true;
                itemInBoxCount = 64;
                countInBox -= 64;
            }

            user.inventory.setItem(user.inventory.getCurrentItemIndex(), stack);
            stack.stackSize = itemInBoxCount;

            boolean canUse = stack.useItem(user, world, blockX, blockY, blockZ, side, xPlaced, yPlaced);

            user.inventory.setItem(user.inventory.getCurrentItemIndex(), storageBoxStack);

//            if (result == ActionResult.SUCCESS) {
//                stack.decrement(1);
//            }

            if (countIsOverMax) {
                countInBox += stack.stackSize;
            } else {
                countInBox = stack.stackSize;
            }

            if (stack.stackSize <= 0) {
                removeItemDataAsInt(storageBoxStack, KEY_SIZE);
                removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
                removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
                removeItemDataAsInt(storageBoxStack, KEY_AUTO);
            } else {
                setItemStackSize(storageBoxStack, countInBox);
                setItemStack(storageBoxStack, stack);
            }
            return canUse;
        }
        return super.onUseItemOnBlock(storageBoxStack, user, world, blockX, blockY, blockZ, side, xPlaced, yPlaced);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
//    public static void keyboardEvent(int type, Player player, ItemStack storageBoxStack) {
//        if (type == 0) {
//            if (player != null && !(player.openScreenHandler instanceof PlayerScreenHandler) && !(player.openScreenHandler.slots.size() <= 0)) {
//                // コンテナー
//                if (hasStackInStorageBox(storageBoxStack)) {
//                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
//                    int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//                    for (Slot slot : player.openScreenHandler.slots) {
//                        if (slot.getContainer() == player.inventory) continue;
//                        ItemStack stack = slot.getItemStack();
//                        if (stack != null && stack.stackSize == 0) continue;
//                        ItemStack newStack = itemInBox.copy();
//
//                        // 64より大きい
//                        if (count > 64) {
//                            newStack.stackSize = 64;
//                            slot.set(newStack);
//                            count -= 64;
//                        } else {
//                            newStack.stackSize = count;
//                            slot.set(newStack);
//                            removeItemDataAsInt(storageBoxStack, KEY_SIZE);
//                            removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
//                            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
//                            removeItemDataAsInt(storageBoxStack, KEY_AUTO);
//                            break;
//                        }
//                    }
//                    setItemStackSize(storageBoxStack, count);
//                    return;
//                }
//            }
//            if (hasStackInStorageBox(storageBoxStack)) {
//                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
//                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//                ItemStack giveStack = itemInBox.copy();
//                if (count > 64) {
//                    giveStack.stackSize = 64;
//                    if (canGive(player.inventory.mainInventory)) {
//                        player.inventory.insertItem(giveStack, true);
//                    } else {
//                        player.dropItem(giveStack.itemID, giveStack.stackSize);
//                    }
//                    setItemStackSize(storageBoxStack, count - 64);
//                } else {
//                    giveStack.stackSize = count;
//                    if (canGive(player.inventory.mainInventory)) {
//                        player.inventory.insertItem(giveStack, true);
//                    } else {
//                        player.dropItem(giveStack.itemID, giveStack.stackSize);
//                    }
//                    removeItemDataAsInt(storageBoxStack, KEY_SIZE);
//                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
//                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
//                    removeItemDataAsInt(storageBoxStack, KEY_AUTO);
//                }
//                return;
//            }
//        }
//        if (type == 1) {
//            if (hasStackInStorageBox(storageBoxStack)) {
//                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
//                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//                ItemStack dropStack = itemInBox.copy();
//                if (count > 64) {
//                    dropStack.stackSize = 64;
//                    player.dropItem(dropStack.itemID, dropStack.stackSize);
//                    setItemStackSize(storageBoxStack, count - 64);
//                } else {
//                    dropStack.stackSize = count;
//                    player.dropItem(dropStack.itemID, dropStack.stackSize);
//                    removeItemDataAsInt(storageBoxStack, KEY_SIZE);
//                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
//                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
//                    removeItemDataAsInt(storageBoxStack, KEY_AUTO);
//                }
//                return;
//            }
//        }
//        if (type == 2) {
//            if (hasStackInStorageBox(storageBoxStack)) {
//                if (!(player.openScreenHandler instanceof PlayerScreenHandler) && player.openScreenHandler != null) {
//                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
//                    int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//                    for (Slot slot : player.openScreenHandler.slots) {
//                        if (slot.getContainer() == player.inventory) continue;
//                        ItemStack stack = slot.getItemStack();
//                        if (stack.getItem() == itemInBox.getItem()) {
//                            if (!canInsertStack(stack, storageBoxStack)) continue;
//                            count += stack.stackSize;
//
//                            for (int i = 0; i < player.inventory.getContainerSize(); i++) {
//                                if (player.inventory.getItem(i) == stack) {
//                                    player.inventory.setItem(i, null);
//                                    break;
//                                }
//                            }
//
//                            stack.stackSize = 0;
//                            stack = null;
//                            slot.set(stack);
//                        }
//                    }
//                    setItemStackSize(storageBoxStack, count);
//                    return;
//                }
//            }
//            if (hasStackInStorageBox(storageBoxStack)) {
//                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
//                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//                for (ItemStack stack : player.inventory.mainInventory) {
//                    if (stack.getItem().equals(itemInBox.getItem())) {
//                        if (!canInsertStack(stack, storageBoxStack)) continue;
//                        count += stack.stackSize;
//
//                        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
//                            if (player.inventory.getItem(i) == stack) {
//                                player.inventory.setItem(i, null);
//                                break;
//                            }
//                        }
//
//                        stack.stackSize = 0;
//                    }
//                }
//                setItemStackSize(storageBoxStack, count);
//                return;
//            }
//        }
//        if (type == 3) {
//            if (isAutoCollect(storageBoxStack)) {
//                changeAutoCollect(storageBoxStack);
//                player.sendMessage("§7[StorageBox] §cAutoCollect changed OFF");
//            } else {
//                changeAutoCollect(storageBoxStack);
//                player.sendMessage("§7[StorageBox] §aAutoCollect changed ON");
//            }
//        }
//    }

//    @Override
//    public void appendTooltip(ItemStack storageBoxStack, PlayerEntity player, List<String> tooltip, boolean advanced) {
//        super.appendTooltip(storageBoxStack, player, tooltip, advanced);
//        if (hasStackInStorageBox(storageBoxStack)) {
//            Item item = getItem(storageBoxStack);
//            ItemStack stack = getStackInStorageBox(storageBoxStack);
//            int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
//            tooltip.add("§7Name: " + stack.getCustomName());
//            tooltip.add("§7Unit: " + calcItemNumByUnit(count , false, stack.getMaxCount()));
//            tooltip.add("§7Items: " + count);
//            tooltip.add("§7AutoCollect: " + (isAutoCollect(storageBoxStack) ? "ON" : "OFF"));
//            tooltip.add("§7[Information]");
//            if (item != null)
//                item.appendTooltip(stack, player, tooltip, advanced);
//        }
//    }

    public static StringBuilder calcItemNumByUnit(int count, boolean appendItemNum, int maxStackCount) {
        StringBuilder sb = new StringBuilder("Empty");
        sb.setLength(0);
        int LCNUM = 9 * 6 * maxStackCount;
        int n = count;
        int i = n / LCNUM;
        boolean isHigherUnit = false;
        if (i >= 1) {
            isHigherUnit = true;
            sb.append(i).append("LC");
            n -= i * LCNUM;
        }
        i = n / maxStackCount;
        if (i >= 1) {
            isHigherUnit = true;
            if (sb.length() >= 1) {
                sb.append('+');
            }
            sb.append(i).append("stacks");
            n -= i * maxStackCount;
        }
        if (n >= 1) {
            if (sb.length() >= 1) {
                sb.append('+');
            }
            sb.append(n).append("items");
        }
        if (isHigherUnit && appendItemNum) {
            sb.append('(').append(count).append("items)");
        }
        return sb;
    }



    public static boolean canGive(List<ItemStack> inv) {
        for ( ItemStack stack : inv ) {
            if (stack == null || stack.stackSize == 0) return true;
        }

        return false;
    }

    public static boolean canGive(ItemStack[] inv) {
        for ( ItemStack stack : inv ) {
            if (stack == null || stack.stackSize == 0) return true;
        }

        return false;
    }



    public static boolean canInsertStack(ItemStack stack) {
        if (stack.getItem().equals(StorageBoxItem.instance)) return false;
        if (stack.isItemStackDamageable()) return false;
        return true;
    }

    public static boolean canInsertStack(ItemStack stack, ItemStack storageBoxStack) {
        if (stack.getItem().equals(StorageBoxItem.instance)) return false;
        if (stack.isItemStackDamageable()) return false;
        ItemStack stackInBox = getStackInStorageBox(storageBoxStack);
        if (stackInBox == null || stackInBox.stackSize == 0) return false;
        if (!stackInBox.getData().equals(stack.getData())) return false;

        return true;
    }
}
