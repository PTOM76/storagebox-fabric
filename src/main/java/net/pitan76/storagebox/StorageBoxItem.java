package net.pitan76.storagebox;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageBoxItem extends Item {
    /*
    NBT:
    - StorageSize(int): Item count (アイテム数)
    - StorageAuto(int): Auto collect (自動回収)
    - StorageItemData(ItemStack): ItemStack (アイテムのスタックデータ) Ex. ItemStack.fromNbt(nbt)
     */

    public static String KEY_ITEM_ID = "StorageItem"; // Old
    public static String KEY_SIZE = "StorageSize";
    public static String KEY_AUTO = "StorageAuto"; // 0 = true, 1 = false
    public static String KEY_ITEM_DATA = "StorageItemData";

    public static Item getItem(ItemStack storageBoxStack) {
        ItemStack stack = getStackInStorageBox(storageBoxStack);
        if (stack != null)
            return stack.getItem();

        // 1.12以前の数値IDのみしか含まれていない場合
        /*
        int itemId = getComponentAsInt(storageBoxStack, KEY_ITEM_ID);
        if (itemId == 0) return null;

        int size = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
        if (size > 0)
            return Item.byRawId(itemId)
            ;
         */

        return null;
    }

    public static boolean hasStackInStorageBox(ItemStack storageBoxStack) {
        return getStackInStorageBox(storageBoxStack) != null;
    }

    // nulll のときは hasStackInStorageBox(storageBoxStack) で判定すること
    public static ItemStack getStackInStorageBox(ItemStack storageBoxStack) {
        ItemStack result;
        if (storageBoxStack.getComponents().isEmpty()) return null;

        /*
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
            int itemId = getComponentAsInt(storageBoxStack, KEY_ITEM_ID);

            if (itemId != 0 && getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) > 0)
                return new ItemStack(Item.byRawId(itemId));
        }

         */

        if (!storageBoxStack.contains(DataComponentTypes.ITEM_DATA)) return null;

        ItemStack stack = storageBoxStack.get(DataComponentTypes.ITEM_DATA).copy();
        stack.setCount(1);
        return stack;
    }

    // stackのコンポーネントから数値のデータを取り出す
    public static int getComponentAsInt(ItemStack storageBoxStack, ComponentType<Integer> type) {
        int data = 0;
        //NbtCompound nbt = storageBoxStack.getNbt();

        if (storageBoxStack.contains(type)) {
            //if (key.equals(DataComponentTypes.ITEM_COUNT) && nbt.contains("countInBox"))
            //    return nbt.getInt("countInBox");

            data = storageBoxStack.get(type);
        }
        if (type.equals(DataComponentTypes.AUTO_COLLECT) && !storageBoxStack.contains(type)) {
            // 0 = true, 1 = false
            Boolean defaultAutoCollect = ModConfig.getBoolean("DefaultAutoCollect");
            if (defaultAutoCollect == null) return 0;
            return defaultAutoCollect ? 0 : 1;
        }

        return data;
    }

    public static boolean isAutoCollect(ItemStack storageBoxStack) {
        /*
        if (storageBoxStack.hasNbt()) {
            NbtCompound nbt = storageBoxStack.getNbt();
            if (!nbt.contains(DataComponentTypes.AUTO_COLLECT) && nbt.contains("autoCollect")) {
                return nbt.getBoolean("autoCollect");
            }
        }
        */
        return getComponentAsInt(storageBoxStack, DataComponentTypes.AUTO_COLLECT) == 0;
    }

    public static void changeAutoCollect(ItemStack storageBoxStack) {
        int value = isAutoCollect(storageBoxStack) ? 1 : 0;
        setComponentAsInt(storageBoxStack, DataComponentTypes.AUTO_COLLECT, value);
    }

    public static void setComponentAsInt(ItemStack storageBoxStack, ComponentType<Integer> type, int data) {
        storageBoxStack.set(type, data);
    }

    public static void setComponentStack(ItemStack storageBoxStack, ComponentType<ItemStack> type, ItemStack stack) {
        if (!stack.isEmpty())
            storageBoxStack.set(type, stack);
        else
            storageBoxStack.remove(type);
    }

    public static void removeComponent(ItemStack storageBoxStack, ComponentType<?> type) {
        storageBoxStack.remove(type);
    }

    public static void setItemStack(ItemStack storageBoxStack, ItemStack newStack) {
        if (storageBoxStack == ItemStack.EMPTY) return;
        if (newStack == null || ItemStack.EMPTY == newStack || newStack.isEmpty()) {
            setComponentStack(storageBoxStack, DataComponentTypes.ITEM_DATA, ItemStack.EMPTY);
            return;
        }

        setComponentStack(storageBoxStack, DataComponentTypes.ITEM_DATA, newStack);
    }

    public static void setItemStackSize(ItemStack storageBoxStack, int size) {
        if (storageBoxStack == ItemStack.EMPTY) return;
        setComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT, size);
    }

    //
    public static StorageBoxItem instance = new StorageBoxItem(new Settings());

    public StorageBoxItem(Settings settings) {
        super(settings.maxCount(1));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(this));

    }
    public static void showBar(PlayerEntity player, ItemStack storageBoxStack) {
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            if (stack != null) {
            player.sendMessage(Text.literal(stack.getName().getString() + "/" + calcItemNumByUnit(getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT), true, stack.getMaxCount())), true);
            return;
            }
        }
        player.sendMessage(Text.literal("Empty"), true);

    }

    public void dropItemStack(LivingEntity entity, ItemStack itemstack) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.dropItem(itemstack.copy(), false);
            itemstack.setCount(0);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack storageBoxStack = user.getStackInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            boolean canUse = true;
            int countInBox = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > 64) {
                countIsOverMax = true;
                itemInBoxCount = 64;
                countInBox -= 64;
            }
            stack.setCount(0);
            user.setStackInHand(hand, stack);
            stack.setCount(itemInBoxCount);

            TypedActionResult<ItemStack> result;

            result = stack.use(world, user, hand);
            if (!result.equals(TypedActionResult.success(stack)) || !result.equals(TypedActionResult.consume(stack)))
                canUse = false;

            int i = storageBoxStack.getCount();
            storageBoxStack.setCount(0);
            user.setStackInHand(hand, storageBoxStack);
            storageBoxStack.setCount(i);


            if (result.getResult() == ActionResult.FAIL) {
                return new TypedActionResult<>(result.getResult(), storageBoxStack);
            } else if (stack.getItem().equals(result.getValue().getItem())) {
                // 食べ物など一定の時間を使って消費するアイテム
                if (user.isUsingItem()) {
                    user.stopUsingItem();
                    user.setCurrentHand(hand);
                }
            } else {
                // バケツ => 液体バケツなどのサポート
                if (!result.getValue().isEmpty())
                    user.getInventory().offerOrDrop(result.getValue());
                if (result.getResult().equals(ActionResult.CONSUME)) {
                    stack.setCount(stack.getCount() - 1);
                }
            }

            if (countIsOverMax) {
                countInBox += stack.getCount();
            } else {
                countInBox = stack.getCount();
            }

            if (stack.getCount() <= 0) {
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);

            } else {
                setItemStackSize(storageBoxStack, countInBox);
                setItemStack(storageBoxStack, stack);
            }
            return canUse ? TypedActionResult.success(storageBoxStack) : TypedActionResult.pass(storageBoxStack);
        }
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory(StorageBoxScreenHandler::new, Text.literal(""));
            user.openHandledScreen(screenHandlerFactory);
        }
        return TypedActionResult.success(storageBoxStack);
    }

    public ItemStack finishUsing(ItemStack storageBoxStack, World world, LivingEntity user) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.setCount(64);
            ItemStack result = item.finishUsing(stack, world, user);

            // ポーション => ガラス瓶などのサポート
            if (!stack.getItem().equals((result.getItem())))
                dropItemStack(user, result);
            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (64 - stack.getCount()));
        }

        return super.finishUsing(storageBoxStack, world, user);
    }

    @Override
    public boolean postMine(ItemStack storageBoxStack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.setCount(64);
            boolean result = item.postMine(stack, world, state, pos, miner);
            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (64 - stack.getCount()));
            return result;
        }

        return super.postMine(storageBoxStack, world, state, pos, miner);
    }

    @Override
    public ActionResult useOnEntity(ItemStack storageBoxStack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ActionResult result;
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            stack.setCount(64);
            result = item.useOnEntity(stack, user, entity, hand);
            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (64 - stack.getCount()));
        } else {
            result = super.useOnEntity(storageBoxStack, user, entity, hand);
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
    public int getMaxUseTime(ItemStack storageBoxStack, LivingEntity entity) {
        Item item = getItem(storageBoxStack);

        if (item != null) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            return item.getMaxUseTime(stack, entity);
        }

        return super.getMaxUseTime(storageBoxStack, entity);
    }

    @Override
    public void onStoppedUsing(ItemStack storageBoxStack, World world, LivingEntity user, int remainingUseTicks) {
        Item item = getItem(storageBoxStack);

        if (item == null) {
            super.onStoppedUsing(storageBoxStack, world, user, remainingUseTicks);
            return;
        }

        ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
        stack.setCount(64);
        item.onStoppedUsing(stack, world, user, remainingUseTicks);
        setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (64 - stack.getCount()));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        Hand hand = context.getHand();

        ItemStack storageBoxStack = user.getStackInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            boolean canUse = true;
            int countInBox = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > 64) {
                countIsOverMax = true;
                itemInBoxCount = 64;
                countInBox -= 64;
            }
            stack.setCount(0);
            user.setStackInHand(hand, stack);
            stack.setCount(itemInBoxCount);

            ActionResult result;

            BlockHitResult hit = new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock());
            result = stack.useOnBlock(new ItemUsageContext(context.getWorld(), context.getPlayer(), context.getHand(), stack, hit));

            if (result != ActionResult.SUCCESS) {
                canUse = false;
            }

            storageBoxStack.setCount(0);
            user.setStackInHand(hand, storageBoxStack);
            storageBoxStack.setCount(1);

            /*
            if (result == ActionResult.SUCCESS || result == ActionResult.CONSUME) {
                stack.decrement(1);
            }
            */

            if (countIsOverMax) {
                countInBox += stack.getCount();
            } else {
                countInBox = stack.getCount();
            }

            if (stack.getCount() <= 0) {
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
            } else {
                setItemStackSize(storageBoxStack, countInBox);
                setItemStack(storageBoxStack, stack);
            }
            return canUse ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return super.useOnBlock(context);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public static void keyboardEvent(int type, PlayerEntity player, ItemStack storageBoxStack) {
        if (type == 0) {
            if (player.currentScreenHandler != null && !(player.currentScreenHandler instanceof PlayerScreenHandler) && !(player.currentScreenHandler.slots.size() <= 0)) {
                // コンテナー
                if (hasStackInStorageBox(storageBoxStack)) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    for (Slot slot : player.currentScreenHandler.slots) {
                        if (slot.inventory == player.getInventory()) continue;
                        ItemStack stack = slot.getStack();
                        if (!stack.isEmpty()) continue;
                        ItemStack newStack = itemInBox.copy();

                        // 64より大きい
                        if (count > 64) {
                            newStack.setCount(64);
                            slot.setStack(newStack);
                            count -= 64;
                        } else {
                            newStack.setCount(count);
                            slot.setStack(newStack);
                            removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                            removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                            removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
                            break;
                        }
                    }
                    setItemStackSize(storageBoxStack, count);
                    return;
                }
            }
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                ItemStack giveStack = itemInBox.copy();
                if (count > 64) {
                    giveStack.setCount(64);
                    if (canGive(player.getInventory().main)) {
                        player.giveItemStack(giveStack);
                    } else {
                        player.dropItem(giveStack, false);
                    }
                    setItemStackSize(storageBoxStack, count - 64);
                } else {
                    giveStack.setCount(count);
                    if (canGive(player.getInventory().main)) {
                        player.giveItemStack(giveStack);
                    } else {
                        player.dropItem(giveStack, false);
                    }
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                    removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
                }
                return;
            }
        }
        if (type == 1) {
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                ItemStack dropStack = itemInBox.copy();
                if (count > 64) {
                    dropStack.setCount(64);
                    player.dropItem(dropStack, false);
                    setItemStackSize(storageBoxStack, count - 64);
                } else {
                    dropStack.setCount(count);
                    player.dropItem(dropStack, false);
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                    removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
                }
                return;
            }
        }
        if (type == 2) {
            if (hasStackInStorageBox(storageBoxStack)) {
                if (!(player.currentScreenHandler instanceof PlayerScreenHandler) && player.currentScreenHandler != null) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    for (Slot slot : player.currentScreenHandler.slots) {
                        if (slot.inventory == player.getInventory()) continue;
                        ItemStack stack = slot.getStack();
                        if (stack.getItem() == itemInBox.getItem()) {
                            if (!canInsertStack(stack, storageBoxStack)) continue;
                            count += stack.getCount();
                            player.getInventory().removeOne(stack);
                            stack.setCount(0);
                            stack = ItemStack.EMPTY;
                            slot.setStack(stack);
                        }
                    }
                    setItemStackSize(storageBoxStack, count);
                    return;
                }
            }
            if (hasStackInStorageBox(storageBoxStack)) {
                ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() == itemInBox.getItem()) {
                        if (!canInsertStack(stack, storageBoxStack)) continue;
                        count += stack.getCount();
                        player.getInventory().removeOne(stack);
                        stack.setCount(0);
                    }
                }
                setItemStackSize(storageBoxStack, count);
                return;
            }
        }
        if (type == 3) {
            if (isAutoCollect(storageBoxStack)) {
                changeAutoCollect(storageBoxStack);
                player.sendMessage(Text.literal("§7[StorageBox] §cAutoCollect changed OFF"), false);
            } else {
                changeAutoCollect(storageBoxStack);
                player.sendMessage(Text.literal("§7[StorageBox] §aAutoCollect changed ON"), false);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack storageBoxStack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(storageBoxStack, context, tooltip, type);
        if (hasStackInStorageBox(storageBoxStack)) {
            Item item = getItem(storageBoxStack);
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            tooltip.add(Text.literal("§7Name: " + stack.getItem().getName().getString()));
            tooltip.add(Text.literal("§7Unit: " + calcItemNumByUnit(count , false, stack.getMaxCount())));
            tooltip.add(Text.literal("§7Items: " + count));
            tooltip.add(Text.literal("§7AutoCollect: " + (isAutoCollect(storageBoxStack) ? "ON" : "OFF")));
            tooltip.add(Text.literal("§7[Information]"));
            if (item != null)
                item.appendTooltip(stack, context, tooltip, type);
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

    public static boolean canGive(DefaultedList<ItemStack> inv) {
        for ( ItemStack stack : inv ) {
            if (stack.isEmpty()) return true;
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
        if (!stack.getComponents().isEmpty()) {
            ItemStack stackInBox = getStackInStorageBox(storageBoxStack);
            if (stackInBox == null || stackInBox.isEmpty()) return false;
            if (stackInBox.getComponents().isEmpty()) return false;
            if (!stackInBox.equals(stack) && !stackInBox.getComponents().equals(stack.getComponents())) return false;
        }
        return true;
    }
}
