package net.pitan76.storagebox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.itemgroup.ItemGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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
            Item item = Item.byRawId(itemId);
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
        if (!storageBoxStack.hasNbt()) return null;
        NbtCompound nbt = storageBoxStack.getNbt();

        // 以前のシステムとの互換性
        if (nbt.contains("item")) {
            setItemStack(storageBoxStack, ItemStack.fromNbt(nbt.getCompound("item")));
            if (nbt.contains("countInBox")) {
                setItemStackSize(storageBoxStack, nbt.getInt("countInBox"));
                nbt.remove("countInBox");
            }
            nbt.remove("item");
            storageBoxStack.setNbt(nbt);
        }
        if (nbt.contains(KEY_ITEM_ID)) {
            return fixItemId(storageBoxStack).map(ItemStack::new).orElse(null);
        }

        if (!nbt.contains(KEY_ITEM_DATA)) return null;

        nbt = nbt.getCompound(KEY_ITEM_DATA);
        if (nbt.isEmpty()) return null;

        // convert 1.12 Item ID
//        if (nbt.contains("Damage", 99)) {
//            String old = nbt.getString("id");
//            int oldDamage = Math.max(0, nbt.getShort("Damage"));
//            String newId = StorageBoxUtil.oldItemIDtoNewItemID(old, oldDamage);
//            if (!old.equals(newId)) {
//                // update item id
//                nbt.putString("id", newId);
//            }
//        }
        result = ItemStack.fromNbt(nbt);
        result.count = 1;
        //System.out.println("damage in stack: " + result.getDamage() + ", damage in nbt: " + nbt.getInt("Damage"));

        return result;
    }

    // stackのNBTから数値のデータを取り出す
    public static int getItemDataAsInt(ItemStack storageBoxStack, String key) {

        int data = -1;
        NbtCompound nbt = storageBoxStack.getNbt();

        if (nbt != null) {
            if (key.equals(KEY_SIZE) && nbt.contains("countInBox"))
                return nbt.getInt("countInBox");

            data = nbt.getInt(key);
        }
        if (key.equals(KEY_AUTO) && (nbt == null || !nbt.contains(key))) {
            // 0 = true, 1 = false
            Boolean defaultAutoCollect = ModConfig.getBoolean("DefaultAutoCollect");
            if (defaultAutoCollect == null) return 0;
            return defaultAutoCollect ? 0 : 1;
        }

        return data;
    }

    public static boolean isAutoCollect(ItemStack storageBoxStack) {
        if (storageBoxStack.hasNbt()) {
            NbtCompound nbt = storageBoxStack.getNbt();
            if (!nbt.contains(KEY_AUTO) && nbt.contains("autoCollect")) {
                return nbt.getBoolean("autoCollect");
            }
        }
        return getItemDataAsInt(storageBoxStack, KEY_AUTO) == 0;
    }

    public static void changeAutoCollect(ItemStack storageBoxStack) {
        int value = isAutoCollect(storageBoxStack) ? 1 : 0;
        setItemDataAsInt(storageBoxStack, KEY_AUTO, value);
    }

    public static void setItemDataAsInt(ItemStack storageBoxStack, String key, int data) {
        NbtCompound stackNbt = storageBoxStack.getNbt();
        if (stackNbt == null) stackNbt = new NbtCompound();

        stackNbt.putInt(key, data);
        storageBoxStack.setNbt(stackNbt);
    }

    public static void setItemDataAsInt(ItemStack storageBoxStack, String key, NbtCompound nbt) {
        NbtCompound stackNbt = storageBoxStack.getNbt();
        if (stackNbt == null) stackNbt = new NbtCompound();

        if (nbt != null)
            stackNbt.put(key, nbt);
        else if (stackNbt.contains(key)) stackNbt.remove(key);
        storageBoxStack.setNbt(stackNbt);
    }

    public static void removeItemDataAsInt(ItemStack stack, String key) {
        setItemDataAsInt(stack, key, null);
    }

    public static void setItemStack(ItemStack storageBoxStack, ItemStack newStack) {
        if (storageBoxStack == null) return;
        if (newStack == null || newStack.count == 0) {
            setItemDataAsInt(storageBoxStack, KEY_ITEM_DATA, null);
            return;
        }
        NbtCompound nbt = new NbtCompound();
        newStack.toNbt(nbt);
        setItemDataAsInt(storageBoxStack, KEY_ITEM_DATA, nbt);
    }

    public static void setItemStackSize(ItemStack storageBoxStack, int size) {
        if (storageBoxStack == null || storageBoxStack.count == 0) return;
        setItemDataAsInt(storageBoxStack, KEY_SIZE, size);
    }

    //
    public static StorageBoxItem instance = new StorageBoxItem();

    public StorageBoxItem() {
        super();
        setMaxCount(1);
        setTranslationKey("storagebox");
        setItemGroup(ItemGroup.MISC);
    }

    @Environment(EnvType.CLIENT)
    public static void showBar(ItemStack storageBoxStack) {
        StringBuilder sb;
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);

            sb = calcItemNumByUnit(getItemDataAsInt(storageBoxStack, KEY_SIZE), true, stack.getMaxCount());
            sb.insert(0, stack.getCustomName() + " / ");
        } else {
            sb = new StringBuilder("Empty");
        }

        String string = sb.toString();

        MinecraftClient client = MinecraftClient.getInstance();
        Window window = new Window(client);

        int x = (int) (window.getScaledWidth() / 2 - (double) client.textRenderer.getStringWidth(string) / 2);
        int y = (int) (window.getScaledHeight() - 80);

        client.textRenderer.draw(string, x, y, 0xFFFFFF);
    }

    public void dropItemStack(LivingEntity entity, ItemStack itemstack) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.dropItem(itemstack.copy(), false);
            itemstack.count = 0;
        }
    }

    @Override
    public ItemStack onStartUse(ItemStack storageBoxStack, World world, PlayerEntity user) {
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
            user.inventory.setInvStack(user.inventory.selectedSlot, stack);
            stack.count = itemInBoxCount;

            ItemStack result;

            result = stack.onStartUse(world, user);

            user.inventory.setInvStack(user.inventory.selectedSlot, storageBoxStack);

            if (stack.equalsIgnoreNbt(result)) {
                // 食べ物など一定の時間を使って消費するアイテム
                if (user.isUsingItem()) {
                    user.stopUsingItem();
                    user.setUseItem(storageBoxStack, stack.getMaxUseTime());
                }
            } else {
                // バケツ => 液体バケツなどのサポート
                if (result != null && result.count != 0)
                    user.inventory.insertStack(result);
//                if (result.getResult().equals(ActionResult.CONSUME)) {
//                    stack.setCount(stack.getCount() - 1);
//                }
            }

            if (countIsOverMax) {
                countInBox += stack.count;
            } else {
                countInBox = stack.count;
            }

            if (stack.count <= 0) {
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
        if (!world.isClient) {
            user.openHandledScreen(StorageBoxScreenHandler.FACTORY);
        } else {
            MinecraftClient.getInstance().setScreen(new StorageBoxScreen(user.inventory, new LiteralText("")));
        }
        return storageBoxStack;
    }

    public ItemStack onFinishUse(ItemStack storageBoxStack, World world, PlayerEntity user) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.count = 64;
            ItemStack result = item.onFinishUse(stack, world, user);

            // ポーション => ガラス瓶などのサポート
            if (!stack.getItem().equals(result.getItem()))
                dropItemStack(user, result);
            setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.count));
        }

        return super.onFinishUse(storageBoxStack, world, user);
    }

    @Override
    public boolean onBlockBroken(ItemStack stack, World world, Block block, BlockPos pos, LivingEntity entity) {
        Item item = getItem(stack);

        if (item != null && hasStackInStorageBox(stack)) {
            ItemStack itemStack = getStackInStorageBox(stack).copy();
            itemStack.count = 64;
            boolean result = item.onBlockBroken(itemStack, world, block, pos, entity);
            setItemStackSize(stack, getItemDataAsInt(stack, KEY_SIZE) - (64 - itemStack.count));
            return result;
        }

        return super.onBlockBroken(stack, world, block, pos, entity);
    }

    @Override
    public boolean onEntityHit(ItemStack stack, LivingEntity entity1, LivingEntity entity2) {
        Item item = getItem(stack);

        if (item != null && hasStackInStorageBox(stack)) {
            ItemStack itemStack = getStackInStorageBox(stack).copy();
            itemStack.count = 64;
            boolean result = item.onEntityHit(itemStack, entity1, entity2);
            setItemStackSize(stack, getItemDataAsInt(stack, KEY_SIZE) - (64 - itemStack.count));
            return result;
        }

        return super.onEntityHit(stack, entity1, entity2);
    }

    @Override
    public boolean canUseOnEntity(ItemStack storageBoxStack, PlayerEntity user, LivingEntity entity) {
        boolean result;
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.count = 64;
            result = item.canUseOnEntity(stack, user, entity);
            setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.count));
        } else {
            result = super.canUseOnEntity(storageBoxStack, user, entity);
        }

        return result;
    }

    @Override
    public UseAction getUseAction(ItemStack storageBoxStack) {
        UseAction result;
        Item item = getItem(storageBoxStack);

        if (item != null) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            result = item.getUseAction(stack);
        } else {
            result = super.getUseAction(storageBoxStack);
        }

        return result;
    }

    @Override
    public int getMaxUseTime(ItemStack storageBoxStack) {
        Item item = getItem(storageBoxStack);

        if (item != null) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            return item.getMaxUseTime(stack);
        }

        return super.getMaxUseTime(storageBoxStack);
    }

    @Override
    public void onUseStopped(ItemStack storageBoxStack, World world, PlayerEntity user, int remainingUseTicks) {
        Item item = getItem(storageBoxStack);

        if (item == null) {
            super.onUseStopped(storageBoxStack, world, user, remainingUseTicks);
            return;
        }

        ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
        stack.count = 64;
        item.onUseStopped(stack, world, user, remainingUseTicks);
        setItemStackSize(storageBoxStack, getItemDataAsInt(storageBoxStack, KEY_SIZE) - (64 - stack.count));
    }

    @Override
    public boolean use(ItemStack storageBoxStack, PlayerEntity user, World world, BlockPos pos, Direction direction, float facingX, float facingY, float facingZ) {
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

            user.inventory.setInvStack(user.inventory.selectedSlot, stack);
            stack.count = itemInBoxCount;

            boolean canUse = stack.use(user, world, pos, direction, facingX, facingY, facingZ);

            user.inventory.setInvStack(user.inventory.selectedSlot, storageBoxStack);

//            if (result == ActionResult.SUCCESS) {
//                stack.decrement(1);
//            }

            if (countIsOverMax) {
                countInBox += stack.count;
            } else {
                countInBox = stack.count;
            }

            if (stack.count <= 0) {
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
        return super.use(storageBoxStack, user, world, pos, direction, facingX, facingY, facingZ);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public static void keyboardEvent(int type, PlayerEntity player, ItemStack storageBoxStack) {
        if (type == 0) {
            if (player.openScreenHandler != null && !(player.openScreenHandler instanceof PlayerScreenHandler) && !(player.openScreenHandler.slots.size() <= 0)) {
                // コンテナー
                if (hasStackInStorageBox(storageBoxStack)) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
                    for (Slot slot : player.openScreenHandler.slots) {
                        if (slot.inventory == player.inventory) continue;
                        ItemStack stack = slot.getStack();
                        if (stack != null && stack.count == 0) continue;
                        ItemStack newStack = itemInBox.copy();

                        // 64より大きい
                        if (count > 64) {
                            newStack.count = 64;
                            slot.setStack(newStack);
                            count -= 64;
                        } else {
                            newStack.count = count;
                            slot.setStack(newStack);
                            removeItemDataAsInt(storageBoxStack, KEY_SIZE);
                            removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
                            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
                            removeItemDataAsInt(storageBoxStack, KEY_AUTO);
                            break;
                        }
                    }
                    setItemStackSize(storageBoxStack, count);
                    return;
                }
            }
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
                ItemStack giveStack = itemInBox.copy();
                if (count > 64) {
                    giveStack.count = 64;
                    if (canGive(player.inventory.main)) {
                        player.inventory.insertStack(giveStack);
                    } else {
                        player.dropItem(giveStack, false);
                    }
                    setItemStackSize(storageBoxStack, count - 64);
                } else {
                    giveStack.count = count;
                    if (canGive(player.inventory.main)) {
                        player.inventory.insertStack(giveStack);
                    } else {
                        player.dropItem(giveStack, false);
                    }
                    removeItemDataAsInt(storageBoxStack, KEY_SIZE);
                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
                    removeItemDataAsInt(storageBoxStack, KEY_AUTO);
                }
                return;
            }
        }
        if (type == 1) {
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
                ItemStack dropStack = itemInBox.copy();
                if (count > 64) {
                    dropStack.count = 64;
                    player.dropItem(dropStack, false);
                    setItemStackSize(storageBoxStack, count - 64);
                } else {
                    dropStack.count = count;
                    player.dropItem(dropStack, false);
                    removeItemDataAsInt(storageBoxStack, KEY_SIZE);
                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
                    removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
                    removeItemDataAsInt(storageBoxStack, KEY_AUTO);
                }
                return;
            }
        }
        if (type == 2) {
            if (hasStackInStorageBox(storageBoxStack)) {
                if (!(player.openScreenHandler instanceof PlayerScreenHandler) && player.openScreenHandler != null) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
                    for (Slot slot : player.openScreenHandler.slots) {
                        if (slot.inventory == player.inventory) continue;
                        ItemStack stack = slot.getStack();
                        if (stack.getItem() == itemInBox.getItem()) {
                            if (!canInsertStack(stack, storageBoxStack)) continue;
                            count += stack.count;

                            for (int i = 0; i < player.inventory.getInvSize(); i++) {
                                if (player.inventory.getInvStack(i) == stack) {
                                    player.inventory.removeInvStack(i);
                                    break;
                                }
                            }

                            stack.count = 0;
                            stack = null;
                            slot.setStack(stack);
                        }
                    }
                    setItemStackSize(storageBoxStack, count);
                    return;
                }
            }
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
                for (ItemStack stack : player.inventory.main) {
                    if (stack.getItem() == itemInBox.getItem()) {
                        if (!canInsertStack(stack, storageBoxStack)) continue;
                        count += stack.count;

                        for (int i = 0; i < player.inventory.getInvSize(); i++) {
                            if (player.inventory.getInvStack(i) == stack) {
                                player.inventory.removeInvStack(i);
                                break;
                            }
                        }

                        stack.count = 0;
                    }
                }
                setItemStackSize(storageBoxStack, count);
                return;
            }
        }
        if (type == 3) {
            if (isAutoCollect(storageBoxStack)) {
                changeAutoCollect(storageBoxStack);
                player.sendMessage(new LiteralText("§7[StorageBox] §cAutoCollect changed OFF"));
            } else {
                changeAutoCollect(storageBoxStack);
                player.sendMessage(new LiteralText("§7[StorageBox] §aAutoCollect changed ON"));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack storageBoxStack, PlayerEntity player, List<String> tooltip, boolean advanced) {
        super.appendTooltip(storageBoxStack, player, tooltip, advanced);
        if (hasStackInStorageBox(storageBoxStack)) {
            Item item = getItem(storageBoxStack);
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int count = getItemDataAsInt(storageBoxStack, KEY_SIZE);
            tooltip.add("§7Name: " + stack.getCustomName());
            tooltip.add("§7Unit: " + calcItemNumByUnit(count , false, stack.getMaxCount()));
            tooltip.add("§7Items: " + count);
            tooltip.add("§7AutoCollect: " + (isAutoCollect(storageBoxStack) ? "ON" : "OFF"));
            tooltip.add("§7[Information]");
            if (item != null)
                item.appendTooltip(stack, player, tooltip, advanced);
        }
    }

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
            if (stack == null || stack.count == 0) return true;
        }

        return false;
    }

    public static boolean canGive(ItemStack[] inv) {
        for ( ItemStack stack : inv ) {
            if (stack == null || stack.count == 0) return true;
        }

        return false;
    }



    public static boolean canInsertStack(ItemStack stack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageable()) return false;
        return true;
    }

    public static boolean canInsertStack(ItemStack stack, ItemStack storageBoxStack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageable()) return false;
        if (stack.hasNbt()) {
            ItemStack stackInBox = getStackInStorageBox(storageBoxStack);
            if (stackInBox == null || stackInBox.count == 0) return false;
            if (!stackInBox.hasNbt()) return false;
            if (!stackInBox.getNbt().equals(stack.getNbt())) return false;
        }
        return true;
    }
}
