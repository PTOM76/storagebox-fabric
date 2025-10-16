package net.pitan76.storagebox;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public static final TagKey<Item> STORAGEBOX_BLACKLIST = TagKey.of(RegistryKeys.ITEM, Identifier.of(StorageBoxMod.MOD_ID, "storagebox_blacklist"));  // storageboxに入れることができないアイテム

    public static Item getItem(ItemStack storageBoxStack) {
        ItemStack stack = getStackInStorageBox(storageBoxStack);
        if (stack != null)
            return stack.getItem();

        return null;
    }

    public static boolean hasStackInStorageBox(ItemStack storageBoxStack) {
        return getStackInStorageBox(storageBoxStack) != null;
    }

    // nulll のときは hasStackInStorageBox(storageBoxStack) で判定すること
    public static ItemStack getStackInStorageBox(ItemStack storageBoxStack) {
        if (storageBoxStack.getComponents().isEmpty()) return null;

        if (!storageBoxStack.contains(DataComponentTypes.ITEM_DATA)) return null;

        ItemStack stack = storageBoxStack.get(DataComponentTypes.ITEM_DATA).copy();
        stack.setCount(1);
        return stack;
    }

    // stackのコンポーネントから数値のデータを取り出す
    public static int getComponentAsInt(ItemStack storageBoxStack, ComponentType<Integer> type) {
        int data = 0;

        if (storageBoxStack.contains(type)) {
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
        setComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT, Math.max(size, 0));
    }

    //
    public static StorageBoxItem instance = new StorageBoxItem(new Settings().registryKey(StorageBoxMod.STORAGE_BOX_KEY));

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
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack storageBoxStack = user.getStackInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int stackMax = stack.getMaxCount();
            boolean canUse = true;
            int countInBox = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > stackMax) {
                countIsOverMax = true;
                itemInBoxCount = stackMax;
                countInBox -= stackMax;
            }
            stack.setCount(0);
            user.setStackInHand(hand, stack);
            stack.setCount(itemInBoxCount);

            ActionResult result;

            result = stack.use(world, user, hand);

            if (!(result instanceof ActionResult.Success))
                canUse = false;

            int i = storageBoxStack.getCount();
            storageBoxStack.setCount(0);
            user.setStackInHand(hand, storageBoxStack);
            storageBoxStack.setCount(i);

            if (result == ActionResult.FAIL || result == null) {
                return result;
            } else if (result instanceof ActionResult.Success) {
                ActionResult.Success success = (ActionResult.Success) result;
                ItemStack resultStack = success.getNewHandStack();

                if (resultStack == null) {
                    // 食べ物など一定の時間を使って消費するアイテム
                    if (user.isUsingItem()) {

                        if (stack.contains(net.minecraft.component.DataComponentTypes.CONSUMABLE)) {
                            ConsumableComponent consumable = stack.get(net.minecraft.component.DataComponentTypes.CONSUMABLE);
                            storageBoxStack.set(net.minecraft.component.DataComponentTypes.CONSUMABLE, consumable);
                        }

                        if (stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
                            FoodComponent food = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
                            storageBoxStack.set(net.minecraft.component.DataComponentTypes.FOOD, food);
                        }

                        user.stopUsingItem();
                        user.setCurrentHand(hand);
                    }
                } else {
                    // バケツから液体バケツなどのサポート
                    // stack.setCount(itemInBoxCount - 1);  // ここで数を減らすとインベントリが埋まっている時にバンドルのようなアイテムが消えてしまう(1.21~1.21.1のみ)
                    if (success == ActionResult.CONSUME || resultStack != stack) {
                        if (!user.getInventory().insertStack(resultStack)) {
                            dropItemStack(user, resultStack);
                        }
                        stack.setCount(0);
                    }
                }
            }

            if (countIsOverMax) {
                countInBox += stack.getCount();
            } else {
                countInBox = stack.getCount();
            }

            if (countInBox <= 0) {
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
            } else {
                setItemStackSize(storageBoxStack, countInBox);
                //setItemStack(storageBoxStack, stack);
            }
            return canUse ? ActionResult.SUCCESS : ActionResult.PASS;
        }

        if (!world.isClient() && storageBoxStack.equals(user.getEquippedStack(EquipmentSlot.MAINHAND))) {
            NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory(StorageBoxScreenHandler::new, Text.literal(""));
            user.openHandledScreen(screenHandlerFactory);
        }

        return ActionResult.SUCCESS;
    }

    public ItemStack finishUsing(ItemStack storageBoxStack, World world, LivingEntity user) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            int stackMax = stack.getMaxCount();
            stack.setCount(stackMax);
            ItemStack result = item.finishUsing(stack, world, user);

            // ポーション => ガラス瓶などのサポート
            if (!canInsertStack(result, storageBoxStack)){
                if (user instanceof PlayerEntity playerEntity) {
                    if (!playerEntity.getInventory().insertStack(result)){
                        dropItemStack(user, result);
                    }
                } else {
                    dropItemStack(user, result);
                }
            }

            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));
        }
        storageBoxStack.remove(net.minecraft.component.DataComponentTypes.CONSUMABLE);

        return super.finishUsing(storageBoxStack, world, user);
    }

    @Override
    public boolean postMine(ItemStack storageBoxStack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            int stackMax = stack.getMaxCount();
            stack.setCount(stackMax);
            boolean result = item.postMine(stack, world, state, pos, miner);
            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));
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
            int stackMax = stack.getMaxCount();
            int countInBox = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > stackMax) {
                countIsOverMax = true;
                itemInBoxCount = stackMax;
                countInBox -= stackMax;
            }

            stack.setCount(itemInBoxCount);
            ItemStack preStack = stack.copy();
            result = item.useOnEntity(stack, user, entity, hand);

            if (user.isInCreativeMode()) { // クリエイティブで数に変化があればロールバックする(exam.鞍、名札が該当。染料は変わらない)
                if (stack.isEmpty() && !preStack.isEmpty() || canInsertStack(stack, storageBoxStack) && preStack.getCount() != stack.getCount()){    // コンポーネントは同じだが数だけ違う(コンポーネントが違うなら後で取り出す)
                    stack = preStack.copy();
                }

            }
            if (!stack.isEmpty()) {  // 中身に変化があれば取り出して空にする
                if (!canInsertStack(stack, storageBoxStack)) {  // 数以外のコンポーネントが変化した
                    if (!user.getInventory().insertStack(stack)) {
                        dropItemStack(user, stack);
                    }
                    stack.setCount(0);
                }
            }

            if (countIsOverMax) {
                countInBox += stack.getCount();
            } else {
                countInBox = stack.getCount();
            }
            if (countInBox <= 0) {
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
            } else {
                setItemStackSize(storageBoxStack, countInBox);
                //setItemStack(storageBoxStack, stack);
            }
            //setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));
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
    public boolean onStoppedUsing(ItemStack storageBoxStack, World world, LivingEntity user, int remainingUseTicks) {
        Item item = getItem(storageBoxStack);

        if (item == null) {
            return super.onStoppedUsing(storageBoxStack, world, user, remainingUseTicks);
        }

        ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
        int stackMax = stack.getMaxCount();
        stack.setCount(stackMax);
        boolean b = item.onStoppedUsing(stack, world, user, remainingUseTicks);
        setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));

        return b;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        Hand hand = context.getHand();

        ItemStack storageBoxStack = user.getStackInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int stackMax = stack.getMaxCount();
            boolean canUse = true;
            int countInBox = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            int itemInBoxCount = countInBox;
            boolean countIsOverMax = false;
            if (countInBox > stackMax) {
                countIsOverMax = true;
                itemInBoxCount = stackMax;
                countInBox -= stackMax;
            }
            stack.setCount(0);
            user.setStackInHand(hand, stack);
            stack.setCount(itemInBoxCount);
            ItemStack preStack = stack.copy();

            ActionResult result;

            BlockHitResult hit = new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock());
            result = stack.useOnBlock(new ItemUsageContext(context.getWorld(), context.getPlayer(), context.getHand(), stack, hit));
            stack = context.getPlayer().getStackInHand(context.getHand());  // ブロックを置いたorブロックにアイテムを使った時の手に変える
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
            if (user.isInCreativeMode()) { // クリエイティブで数に変化があればロールバックする(exam.花火、ファイヤチャージ)
                if (stack.isEmpty() && !preStack.isEmpty() || canInsertStack(stack, storageBoxStack) && preStack.getCount() != stack.getCount()) {    // コンポーネントは同じだが数だけ違う(コンポーネントが違うなら後で取り出す)
                    stack = preStack.copy();
                }
            }
            if (!stack.isEmpty()) {  // 中身に変化があれば取り出して空にする
                if (!canInsertStack(stack, storageBoxStack)) {  // 数以外のコンポーネントが変化した
                    if (!user.getInventory().insertStack(stack)) {
                        dropItemStack(user, stack);
                    }
                    stack.setCount(0);
                }
            }
            if (countIsOverMax) {
                countInBox += stack.getCount();
            } else {
                countInBox = stack.getCount();
            }

            if (countInBox <= 0) {
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
            } else {
                setItemStackSize(storageBoxStack, countInBox);
                //setItemStack(storageBoxStack, stack);
            }
            return canUse ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return super.useOnBlock(context);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public static void keyboardEvent(int type, PlayerEntity player, ItemStack storageBoxStack) {
        if (player.currentScreenHandler instanceof StorageBoxScreenHandler)
            return;     // StorageBoxScreenHandlerは増殖できてしまうので対策

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
                        int stackMax = itemInBox.getMaxCount();
                        // 64より大きい
                        if (count > stackMax) {
                            newStack.setCount(stackMax);
                            slot.setStack(newStack);
                            count -= stackMax;
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
                int stackMax = itemInBox.getMaxCount();
                int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                ItemStack giveStack = itemInBox.copy();
                if (count > stackMax) {
                    giveStack.setCount(stackMax);
                    if (canGive(player.getInventory().main)) {
                        player.giveItemStack(giveStack);
                    } else {
                        player.dropItem(giveStack, false);
                    }
                    setItemStackSize(storageBoxStack, count - stackMax);
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
                int stackMax = itemInBox.getMaxCount();
                if (count > stackMax) {
                    dropStack.setCount(stackMax);
                    player.dropItem(dropStack, false);
                    setItemStackSize(storageBoxStack, count - stackMax);
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
                            int storageCount;
                            if (((long)count)+((long)stack.getCount()) > 2147483647){   //2147483647以上は収納しようとしない
                                storageCount = 2147483647-count;
                            } else {
                                storageCount = stack.getCount();
                            }
                            if (storageCount > 0) {
                                count += storageCount;
                                // player.getInventory().removeOne(stack);
                                // stack.setCount(0);
                                stack.decrement(storageCount);
                                //stack = ItemStack.EMPTY;
                                slot.setStack(stack);

                            }
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
                        int storageCount;
                        if (((long)count)+((long)stack.getCount()) > 2147483647){   //2147483647以上は収納しようとしない
                            storageCount = 2147483647-count;
                        } else {
                            storageCount = stack.getCount();
                        }
                        if (storageCount > 0) {
                            count += storageCount;
                            stack.decrement(storageCount);

                        }
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
        if (stack.isIn(STORAGEBOX_BLACKLIST)) return false;
        return true;
    }

    public static boolean canInsertStack(ItemStack stack, ItemStack storageBoxStack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageable()) return false;
        if (stack.isIn(STORAGEBOX_BLACKLIST)) return false;
        if (!stack.getComponents().isEmpty()) {
            ItemStack stackInBox = getStackInStorageBox(storageBoxStack);
            if (stackInBox == null || stackInBox.isEmpty()) return false;
            if (stackInBox.getComponents().isEmpty()) return false;
            if (!stackInBox.equals(stack) && !stackInBox.getComponents().equals(stack.getComponents())) return false;
        }
        return true;
    }
}
