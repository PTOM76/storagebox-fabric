package ml.pkom.storagebox;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class StorageBoxItem extends Item {

    public static CompoundTag getNBT(ItemStack stack) {
        if (hasNBT(stack))
            return stack.getTag();
        stack.setTag(newNBT());
        return stack.getTag();
    }

    public static void setNBT(ItemStack stack) {
        stack.setTag(getNBT(stack));
    }

    public static CompoundTag newNBT() {
        return new CompoundTag();
    }

    public static boolean hasNBT(ItemStack stack) {
        return stack.hasTag();
    }

    public static ItemStack getStackInBox(ItemStack stack) {
        return ItemStack.fromTag(getNBT(stack).getCompound("item"));
    }

    public static boolean hasItemNBT(ItemStack stack) {
        return getNBT(stack).contains("item");
    }

    public static StorageBoxItem instance = new StorageBoxItem(new Settings());

    public StorageBoxItem(Settings settings) {
        super(settings.group(ItemGroup.MISC).maxCount(1));
    }

    public static void showBar(PlayerEntity player, ItemStack stack) {
        if (getNBT(stack) != null) if (hasItemNBT(stack)) {
            ItemStack itemInBox = getStackInBox(stack);
            player.sendMessage(new LiteralText(itemInBox.getName().getString() + "/" + instance.calcItemNumByUnit(getNBT(stack).getInt("countInBox"), true, itemInBox.getMaxCount())), true);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (getNBT(itemStack) != null) if (hasItemNBT(itemStack)) {
            ItemStack itemInBox = getStackInBox(itemStack);
            boolean canUse = true;
            if (canUse) {
                int countInBox = getNBT(itemStack).getInt("countInBox");
                int itemInBoxCount = countInBox;
                boolean countIsOverMax = false;
                if (countInBox > 64) {
                    countIsOverMax = true;
                    itemInBoxCount = 64;
                    countInBox -= 64;
                }
                itemInBox.setCount(0);
                user.setStackInHand(hand, itemInBox);
                itemInBox.setCount(itemInBoxCount);

                TypedActionResult<ItemStack> result;

                result = itemInBox.use(world, user, hand);
                if (!result.equals(TypedActionResult.success(itemInBox)) || !result.equals(TypedActionResult.consume(itemInBox)))
                    canUse = false;

                // useが動作しないのでほぼ無理やり
                if (itemInBox.isFood()) {
                    if (user.getHungerManager().isNotFull()) {
                        result = TypedActionResult.consume(itemInBox);
                        itemInBox = itemInBox.finishUsing(world, user);
                    }
                }

                int itemCount = itemStack.getCount();
                itemStack.setCount(0);
                user.setStackInHand(hand, itemStack);
                itemStack.setCount(itemCount);

                if (result.equals(TypedActionResult.consume(itemInBox))) {
                    itemInBox.setCount(itemInBox.getCount() - 1);
                    //System.out.println(itemInBox.getCount());
                }

                if (countIsOverMax) {
                    countInBox += itemInBox.getCount();
                } else {
                    countInBox = itemInBox.getCount();
                }

                if (itemInBox.getCount() <= 0) {
                    getNBT(itemStack).remove("countInBox");
                    getNBT(itemStack).remove("item");

                } else {
                    getNBT(itemStack).putInt("countInBox", countInBox);
                    getNBT(itemStack).put("item", itemInBox.toTag(newNBT()));
                }
                setNBT(itemStack);
            }
            return canUse ? TypedActionResult.success(itemStack) : TypedActionResult.pass(itemStack);
        }
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory((id, playerInv, player) -> new StorageBoxScreenHandler(id, playerInv, player), new LiteralText(""));
            if (screenHandlerFactory != null) {
                user.openHandledScreen(screenHandlerFactory);
            }
        }
        return TypedActionResult.success(itemStack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        Hand hand = context.getHand();

        ItemStack itemStack = user.getStackInHand(hand);
        if (getNBT(itemStack) != null) if (hasItemNBT(itemStack)) {
            ItemStack itemInBox = getStackInBox(itemStack);
            boolean canUse = true;
            // if (itemInBox.getItem() instanceof BlockItem) canUse = ((ReBlockItem) itemInBox.getItem()).canPlace(new ItemPlacementContext(context), context.getWorld().getBlockState(user.getBlockPos()));
            if (canUse) {
                int countInBox = getNBT(itemStack).getInt("countInBox");
                int itemInBoxCount = countInBox;
                boolean countIsOverMax = false;
                if (countInBox > 64) {
                    countIsOverMax = true;
                    itemInBoxCount = 64;
                    countInBox -= 64;
                }
                itemInBox.setCount(0);
                user.setStackInHand(hand, itemInBox);
                itemInBox.setCount(itemInBoxCount);

                ActionResult result;

                result = itemInBox.useOnBlock(context);

                if (result != ActionResult.SUCCESS) {
                    canUse = false;
                }

                itemStack.setCount(0);
                user.setStackInHand(hand, itemStack);
                itemStack.setCount(1);

                if (result == ActionResult.SUCCESS || result == ActionResult.CONSUME) {
                    itemInBox.setCount(itemInBox.getCount() - 1);
                    //System.out.println(itemInBox.getCount());
                }

                if (countIsOverMax) {
                    countInBox += itemInBox.getCount();
                } else {
                    countInBox = itemInBox.getCount();
                }

                if (itemInBox.getCount() <= 0) {
                    getNBT(itemStack).remove("countInBox");
                    getNBT(itemStack).remove("item");
                } else {
                    getNBT(itemStack).putInt("countInBox", countInBox);
                    getNBT(itemStack).put("item", itemInBox.toTag(newNBT()));
                }
                setNBT(itemStack);
            }
            return canUse ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return super.useOnBlock(context);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public void keyboardEvent(int type, PlayerEntity player, ItemStack itemStack) {
        if (type == 0) {
            if (player.currentScreenHandler != null && !(player.currentScreenHandler instanceof PlayerScreenHandler) && !(player.currentScreenHandler.slots.size() <= 0)) {
                ItemStack itemInBox = getStackInBox(itemStack);
                int count = getNBT(itemStack).getInt("countInBox");
                for (Slot slot : player.currentScreenHandler.slots) {
                    if (slot.inventory == player.inventory) continue;
                    ItemStack stack = slot.getStack();
                    if (!stack.isEmpty()) continue;
                    // 64より大きい
                    if (count > 64) {
                        ItemStack setStack = itemInBox.copy();
                        setStack.setCount(64);
                        slot.setStack(setStack);
                        count -= 64;
                    } else {
                        ItemStack setStack = itemInBox.copy();
                        setStack.setCount(count);
                        slot.setStack(setStack);
                        getNBT(itemStack).remove("countInBox");
                        getNBT(itemStack).remove("item");
                        break;
                    }
                }
                if (hasItemNBT(itemStack))
                    getNBT(itemStack).putInt("countInBox", count);
                setNBT(itemStack);
                return;
            }
            ItemStack itemInBox = getStackInBox(itemStack);
            int count = getNBT(itemStack).getInt("countInBox");
            ItemStack giveStack = itemInBox.copy();
            if (count > 64) {
                giveStack.setCount(64);
                if (canGive(player.inventory.main)) {
                    player.giveItemStack(giveStack);
                } else {
                    player.dropItem(giveStack, false);
                }
                getNBT(itemStack).putInt("countInBox", count - 64);
            } else {
                giveStack.setCount(count);
                if (canGive(player.inventory.main)) {
                    player.giveItemStack(giveStack);
                } else {
                    player.dropItem(giveStack, false);
                }                getNBT(itemStack).remove("countInBox");
                getNBT(itemStack).remove("item");
            }
            setNBT(itemStack);
            return;
        }
        if (type == 1) {
            ItemStack itemInBox = getStackInBox(itemStack);
            int count = getNBT(itemStack).getInt("countInBox");
            ItemStack dropStack = itemInBox.copy();
            if (count > 64) {
                dropStack.setCount(64);
                player.dropItem(dropStack, false);
                getNBT(itemStack).putInt("countInBox", count - 64);
            } else {
                dropStack.setCount(count);
                player.dropItem(dropStack, false);
                getNBT(itemStack).remove("countInBox");
                getNBT(itemStack).remove("item");
            }
            setNBT(itemStack);
            return;
        }
        if (type == 2) {
            if (!(player.currentScreenHandler instanceof PlayerScreenHandler) && player.currentScreenHandler != null) {
                ItemStack itemInBox = getStackInBox(itemStack);
                int count = getNBT(itemStack).getInt("countInBox");
                for (Slot slot : player.currentScreenHandler.slots) {
                    if (slot.inventory == player.inventory) continue;
                    ItemStack stack = slot.getStack();
                    if (stack.getItem() == itemInBox.getItem()) {
                        if (!StorageBoxSlot.canInsertStack(stack)) continue;
                        count += stack.getCount();
                        player.inventory.removeOne(stack);
                        stack.setCount(0);
                        stack = ItemStack.EMPTY;
                        slot.setStack(stack);
                    }
                }
                getNBT(itemStack).putInt("countInBox", count);
                setNBT(itemStack);
                return;
            }
            ItemStack itemInBox = getStackInBox(itemStack);
            int count = getNBT(itemStack).getInt("countInBox");
            for (ItemStack stack : player.inventory.main) {
                if (stack.getItem() == itemInBox.getItem()) {
                    if (!StorageBoxSlot.canInsertStack(stack)) continue;
                    count += stack.getCount();
                    player.inventory.removeOne(stack);
                    stack.setCount(0);
                    stack = ItemStack.EMPTY;
                }
            }
            getNBT(itemStack).putInt("countInBox", count);
            setNBT(itemStack);
            return;
        }
        if (type == 3) {
            if (isAutoCollect(itemStack)) {
                getNBT(itemStack).putBoolean("autoCollect", false);
                player.sendMessage(new LiteralText("§7[StorageBox] §cAutoCorrect changed OFF"), false);
            } else {
                getNBT(itemStack).remove("autoCollect");
                player.sendMessage(new LiteralText("§7[StorageBox] §aAutoCorrect changed ON"), false);
            }
            setNBT(itemStack);
            return;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (hasNBT(stack)) {
            if (hasItemNBT(stack)) {
                ItemStack itemInBox = getStackInBox(stack);
                int count = getNBT(stack).getInt("countInBox");
                tooltip.add(new LiteralText("§7Name: " + itemInBox.getName().getString()));
                tooltip.add(new LiteralText("§7Unit: " + calcItemNumByUnit(count , false, itemInBox.getMaxCount()).toString()));
                tooltip.add(new LiteralText("§7Items: " + count));
                tooltip.add(new LiteralText("§7AutoCollect: " + (isAutoCollect(stack) ? "ON" : "OFF")));
                tooltip.add(new LiteralText("§7[Information]"));
            } else {
                if (getNBT(stack).contains("autoCollect")) {
                    tooltip.add(new LiteralText("§7AutoCollect: " + (isAutoCollect(stack) ? "ON" : "OFF")));
                    tooltip.add(new LiteralText("§7[Information]"));
                }
            }
        }
    }

    private StringBuilder calcItemNumByUnit(int count, boolean appendItemNum, int maxStackCount) {
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

    public static boolean isAutoCollect(ItemStack stack) {
        boolean autoCollect = true;
        if (getNBT(stack).contains("autoCollect")) {
            autoCollect = getNBT(stack).getBoolean("autoCollect");
        }
        return autoCollect;
    }

    public static boolean canGive(DefaultedList<ItemStack> inv) {
        for ( ItemStack stack : inv ) {
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
