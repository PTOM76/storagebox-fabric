package net.pitan76.storagebox;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

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
    public static final TagKey<Item> STORAGEBOX_BLACKLIST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(StorageBoxMod.MOD_ID, "storagebox_blacklist"));  // storageboxに入れることができないアイテム

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

        if (!storageBoxStack.has(DataComponentTypes.ITEM_DATA)) return null;

        ItemStack stack = storageBoxStack.get(DataComponentTypes.ITEM_DATA).copy();
        stack.setCount(1);
        return stack;
    }

    // stackのコンポーネントから数値のデータを取り出す
    public static int getComponentAsInt(ItemStack storageBoxStack, DataComponentType<Integer> type) {
        int data = 0;

        if (storageBoxStack.has(type)) {
            data = storageBoxStack.get(type);
        }
        if (type.equals(DataComponentTypes.AUTO_COLLECT) && !storageBoxStack.has(type)) {
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

    public static void setComponentAsInt(ItemStack storageBoxStack, DataComponentType<Integer> type, int data) {
        storageBoxStack.set(type, data);
    }

    public static void setComponentStack(ItemStack storageBoxStack, DataComponentType<ItemStack> type, ItemStack stack) {
        if (!stack.isEmpty())
            storageBoxStack.set(type, stack);
        else
            storageBoxStack.remove(type);
    }

    public static void removeComponent(ItemStack storageBoxStack, DataComponentType<?> type) {
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
    public static StorageBoxItem instance = new StorageBoxItem(new Properties().setId(StorageBoxMod.STORAGE_BOX_KEY));

    public StorageBoxItem(Properties settings) {
        super(settings.stacksTo(1));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(this));
    }
    public static void showBar(Player player, ItemStack storageBoxStack) {
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            if (stack != null) {
                player.sendOverlayMessage(Component.literal(stack.getItemName().getString() + "/" + calcItemNumByUnit(getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT), true, stack.getMaxStackSize())));
                return;
            }
        }
        player.sendOverlayMessage(Component.literal("Empty"));

    }

    public void dropItemStack(LivingEntity entity, ItemStack itemstack) {
        if (entity instanceof Player player) {
            player.drop(itemstack.copy(), false);
            itemstack.setCount(0);
        }
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack storageBoxStack = user.getItemInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int stackMax = stack.getMaxStackSize();
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
            user.setItemInHand(hand, stack);
            stack.setCount(itemInBoxCount);

            InteractionResult result;

            result = stack.use(world, user, hand);

            if (!(result instanceof InteractionResult.Success))
                canUse = false;

            int i = storageBoxStack.getCount();
            storageBoxStack.setCount(0);
            user.setItemInHand(hand, storageBoxStack);
            storageBoxStack.setCount(i);

            if (result == InteractionResult.FAIL || result == null) {
                return result;
            } else if (result instanceof InteractionResult.Success) {
                InteractionResult.Success success = (InteractionResult.Success) result;
                ItemStack resultStack = success.heldItemTransformedTo();

                if (resultStack == null) {
                    // 食べ物など一定の時間を使って消費するアイテム
                    if (user.isUsingItem()) {

                        if (stack.has(DataComponents.CONSUMABLE)) {
                            Consumable consumable = stack.get(DataComponents.CONSUMABLE);
                            storageBoxStack.set(DataComponents.CONSUMABLE, consumable);
                        }

                        if (stack.has(DataComponents.FOOD)) {
                            FoodProperties food = stack.get(DataComponents.FOOD);
                            storageBoxStack.set(DataComponents.FOOD, food);
                        }

                        user.stopUsingItem();
                        user.startUsingItem(hand);
                    }
                } else {
                    // バケツから液体バケツなどのサポート
                    // stack.setCount(itemInBoxCount - 1);  // ここで数を減らすとインベントリが埋まっている時にバンドルのようなアイテムが消えてしまう(1.21~1.21.1のみ)
                    if (success == InteractionResult.CONSUME || resultStack != stack) {
                        if (!user.getInventory().add(resultStack)) {
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
            return canUse ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!world.isClientSide() && storageBoxStack.equals(user.getItemBySlot(EquipmentSlot.MAINHAND))) {
            MenuProvider screenHandlerFactory = new SimpleMenuProvider(StorageBoxScreenHandler::new, Component.literal(""));
            user.openMenu(screenHandlerFactory);
        }

        return InteractionResult.SUCCESS;
    }

    public ItemStack finishUsingItem(ItemStack storageBoxStack, Level world, LivingEntity user) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            int stackMax = stack.getMaxStackSize();
            stack.setCount(stackMax);
            ItemStack result = item.finishUsingItem(stack, world, user);

            // ポーション => ガラス瓶などのサポート
            if (!canInsertStack(result, storageBoxStack)) {
                if (user instanceof Player Player) {
                    if (!Player.getInventory().add(result)) {
                        dropItemStack(user, result);
                    }
                } else {
                    dropItemStack(user, result);
                }
            }

            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));
        }

        storageBoxStack.remove(DataComponents.CONSUMABLE);
        storageBoxStack.remove(DataComponents.FOOD);

        return super.finishUsingItem(storageBoxStack, world, user);
    }

    @Override
    public boolean mineBlock(ItemStack storageBoxStack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            int stackMax = stack.getMaxStackSize();
            stack.setCount(stackMax);
            boolean result = item.mineBlock(stack, world, state, pos, miner);
            setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));
            return result;
        }

        return super.mineBlock(storageBoxStack, world, state, pos, miner);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack storageBoxStack, Player user, LivingEntity entity, InteractionHand hand) {
        InteractionResult result;
        Item item = getItem(storageBoxStack);

        if (item != null && hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
            int stackMax = stack.getMaxStackSize();
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
            result = item.interactLivingEntity(stack, user, entity, hand);

            if (user.isCreative()) { // クリエイティブで数に変化があればロールバックする(exam.鞍、名札が該当。染料は変わらない)
                if (stack.isEmpty() && !preStack.isEmpty() || canInsertStack(stack, storageBoxStack) && preStack.getCount() != stack.getCount()){    // コンポーネントは同じだが数だけ違う(コンポーネントが違うなら後で取り出す)
                    stack = preStack.copy();
                }

            }
            if (!stack.isEmpty()) {  // 中身に変化があれば取り出して空にする
                if (!canInsertStack(stack, storageBoxStack)) {  // 数以外のコンポーネントが変化した
                    if (!user.getInventory().add(stack)) {
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
            result = super.interactLivingEntity(storageBoxStack, user, entity, hand);
        }

        return result;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack storageBoxStack) {
        ItemUseAnimation result;
        Item item = getItem(storageBoxStack);

        if (item != null) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            result = item.getUseAnimation(stack);
        } else {
            result = super.getUseAnimation(storageBoxStack);
        }

        return result;
    }

    @Override
    public int getUseDuration(ItemStack storageBoxStack, LivingEntity entity) {
        Item item = getItem(storageBoxStack);

        if (item != null) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            return item.getUseDuration(stack, entity);
        }

        return super.getUseDuration(storageBoxStack, entity);
    }

    @Override
    public boolean releaseUsing(ItemStack storageBoxStack, Level world, LivingEntity user, int remainingUseTicks) {
        Item item = getItem(storageBoxStack);

        if (item == null) {
            return super.releaseUsing(storageBoxStack, world, user, remainingUseTicks);
        }

        ItemStack stack = getStackInStorageBox(storageBoxStack).copy();
        int stackMax = stack.getMaxStackSize();
        stack.setCount(stackMax);
        boolean b = item.releaseUsing(stack, world, user, remainingUseTicks);
        setItemStackSize(storageBoxStack, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - (stackMax - stack.getCount()));

        return b;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player user = context.getPlayer();
        InteractionHand hand = context.getHand();

        ItemStack storageBoxStack = user.getItemInHand(hand);
        if (hasStackInStorageBox(storageBoxStack)) {
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int stackMax = stack.getMaxStackSize();
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
            user.setItemInHand(hand, stack);
            stack.setCount(itemInBoxCount);
            ItemStack preStack = stack.copy();

            InteractionResult result;

            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
            result = stack.useOn(new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), stack, hit));
            stack = context.getPlayer().getItemInHand(context.getHand());  // ブロックを置いたorブロックにアイテムを使った時の手に変える
            if (result != InteractionResult.SUCCESS) {
                canUse = false;
            }

            storageBoxStack.setCount(0);
            user.setItemInHand(hand, storageBoxStack);
            storageBoxStack.setCount(1);

            /*
            if (result == ActionResult.SUCCESS || result == ActionResult.CONSUME) {
                stack.shrink(1);
            }
            */
            if (user.isCreative()) { // クリエイティブで数に変化があればロールバックする(exam.花火、ファイヤチャージ)
                if (stack.isEmpty() && !preStack.isEmpty() || canInsertStack(stack, storageBoxStack) && preStack.getCount() != stack.getCount()) {    // コンポーネントは同じだが数だけ違う(コンポーネントが違うなら後で取り出す)
                    stack = preStack.copy();
                }
            }
            if (!stack.isEmpty()) {  // 中身に変化があれば取り出して空にする
                if (!canInsertStack(stack, storageBoxStack)) {  // 数以外のコンポーネントが変化した
                    if (!user.getInventory().add(stack)) {
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
            return canUse ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public static void keyboardEvent(int type, Player player, ItemStack storageBoxStack) {
        if (player.containerMenu instanceof StorageBoxScreenHandler)
            return;     // StorageBoxScreenHandlerは増殖できてしまうので対策

        if (type == 0) {
            if (player.containerMenu != null && !(player.containerMenu instanceof InventoryMenu) && !(player.containerMenu.slots.size() <= 0)) {
                // コンテナー
                if (hasStackInStorageBox(storageBoxStack)) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    for (Slot slot : player.containerMenu.slots) {
                        if (slot.container == player.getInventory()) continue;
                        ItemStack stack = slot.getItem();
                        if (!stack.isEmpty()) continue;
                        ItemStack newStack = itemInBox.copy();
                        if(!slot.mayPlace(newStack)) continue;
                        if(!slot.isActive()) continue;
                        int stackMax = itemInBox.getMaxStackSize();
                        // 64より大きい
                        if (count > stackMax) {
                            newStack.setCount(stackMax);
                            slot.set(newStack);
                            count -= stackMax;
                        } else {
                            newStack.setCount(count);
                            slot.set(newStack);
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
                int stackMax = itemInBox.getMaxStackSize();
                int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                ItemStack giveStack = itemInBox.copy();
                if (count > stackMax) {
                    giveStack.setCount(stackMax);
                    if (canGive(player.getInventory().getNonEquipmentItems())) {
                        player.addItem(giveStack);
                    } else {
                        player.drop(giveStack, false);
                    }
                    setItemStackSize(storageBoxStack, count - stackMax);
                } else {
                    giveStack.setCount(count);
                    if (canGive(player.getInventory().getNonEquipmentItems())) {
                        player.addItem(giveStack);
                    } else {
                        player.drop(giveStack, false);
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
                int stackMax = itemInBox.getMaxStackSize();
                if (count > stackMax) {
                    dropStack.setCount(stackMax);
                    player.drop(dropStack, false);
                    setItemStackSize(storageBoxStack, count - stackMax);
                } else {
                    dropStack.setCount(count);
                    player.drop(dropStack, false);
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
                    removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
                }
                return;
            }
        }
        if (type == 2) {
            if (hasStackInStorageBox(storageBoxStack)) {
                if (!(player.containerMenu instanceof InventoryMenu) && player.containerMenu != null) {
                    ItemStack itemInBox = getStackInStorageBox(storageBoxStack);
                    int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
                    for (Slot slot : player.containerMenu.slots) {
                        if (slot.container == player.getInventory()) continue;
                        ItemStack stack = slot.getItem();
                        if (stack.getItem() == itemInBox.getItem()) {
                            if (!canInsertStack(stack, storageBoxStack)) continue;
                            if (!slot.mayPickup(player)) continue;
                            if(!slot.isActive()) continue;
                            int storageCount;
                            if (((long)count)+((long)stack.getCount()) > 2147483647){   //2147483647以上は収納しようとしない
                                storageCount = 2147483647-count;
                            } else {
                                storageCount = stack.getCount();
                            }
                            if (storageCount > 0) {
                                //count += storageCount;
                                // player.getInventory().removeOne(stack);
                                // stack.setCount(0);
                                ItemStack tryStack = slot.remove(storageCount);
                                slot.onTake(player, stack);
                                /*
                                この過程を踏まないと作業台の結果やクラフトスロットが更新されずに複製が起きる
                                */
                                int decCount = ItemStack.isSameItem(itemInBox, tryStack) ?
                                        Mth.clamp(tryStack.getCount(), 0, storageCount) : 0;
                                count += decCount;
                                tryStack.shrink(decCount);
                                if(!tryStack.isEmpty()){
                                    player.drop(tryStack, false);
                                }
                                //stack.shrink(storageCount);
                                //stack = ItemStack.EMPTY;
                                //slot.setStack(stack);

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
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
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
                            stack.shrink(storageCount);

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
                player.sendSystemMessage(Component.literal("§7[StorageBox] §cAutoCollect changed OFF"));
            } else {
                changeAutoCollect(storageBoxStack);
                player.sendSystemMessage(Component.literal("§7[StorageBox] §aAutoCollect changed ON"));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack storageBoxStack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        super.appendHoverText(storageBoxStack, context, displayComponent, textConsumer, type);
        if (hasStackInStorageBox(storageBoxStack)) {
            Item item = getItem(storageBoxStack);
            ItemStack stack = getStackInStorageBox(storageBoxStack);
            int count = getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            textConsumer.accept(Component.literal("§7Name: " + stack.getItemName().getString()));
            textConsumer.accept(Component.literal("§7Unit: " + calcItemNumByUnit(count , false, stack.getMaxStackSize())));
            textConsumer.accept(Component.literal("§7Items: " + count));
            textConsumer.accept(Component.literal("§7AutoCollect: " + (isAutoCollect(storageBoxStack) ? "ON" : "OFF")));
            textConsumer.accept(Component.literal("§7[Information]"));
            if (item != null)
                item.appendHoverText(stack, context, displayComponent, textConsumer, type);
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

    public static boolean canGive(NonNullList<ItemStack> inv) {
        for ( ItemStack stack : inv ) {
            if (stack.isEmpty()) return true;
        }

        return false;
    }

    public static boolean canInsertStack(ItemStack stack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageableItem()) return false;
        if (stack.is(STORAGEBOX_BLACKLIST)) return false;
        return true;
    }

    public static boolean canInsertStack(ItemStack stack, ItemStack storageBoxStack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageableItem()) return false;
        if (stack.is(STORAGEBOX_BLACKLIST)) return false;
        if (!stack.getComponents().isEmpty()) {
            ItemStack stackInBox = getStackInStorageBox(storageBoxStack);
            if (stackInBox == null || stackInBox.isEmpty()) return false;
            if (stackInBox.getComponents().isEmpty()) return false;
            if (!stackInBox.equals(stack) && !stackInBox.getComponents().equals(stack.getComponents())) return false;
        }
        return true;
    }
}
