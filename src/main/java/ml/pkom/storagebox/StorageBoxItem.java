package ml.pkom.storagebox;

import ml.pkom.mcpitanlib.api.text.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class StorageBoxItem extends Item {



    public static StorageBoxItem instance = new StorageBoxItem(new Settings());

    public StorageBoxItem(Settings settings) {
        super(settings.group(ItemGroup.MISC).maxCount(1));
    }

    public static void showBar(PlayerEntity player, ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null) if (tag.contains("item")) {
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            player.sendMessage(TextUtil.literal(itemInBox.getName().getString() + "/" + instance.calcItemNumByUnit(tag.getInt("countInBox"), false, itemInBox.getMaxCount())), true);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound tag = itemStack.getNbt();
        if (tag != null) if (tag.contains("item")) {
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            boolean canUse = true;
            if (canUse) {
                int countInBox = tag.getInt("countInBox");
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
                    tag.remove("countInBox");
                    tag.remove("item");
                    if (tag.contains("autoCollect")) tag.remove("autoCollect");

                } else {
                    tag.putInt("countInBox", countInBox);
                    tag.put("item", itemInBox.writeNbt(new NbtCompound()));
                }
                itemStack.setNbt(tag);
            }
            return canUse ? TypedActionResult.success(itemStack) : TypedActionResult.pass(itemStack);
        }
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory((id, playerInv, player) -> new StorageBoxScreenHandler(id, playerInv, player), TextUtil.literal(""));
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
        NbtCompound tag = itemStack.getNbt();
        if (tag != null) if (tag.contains("item")) {
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            boolean canUse = true;
            // if (itemInBox.getItem() instanceof BlockItem) canUse = ((ReBlockItem) itemInBox.getItem()).canPlace(new ItemPlacementContext(context), context.getWorld().getBlockState(user.getBlockPos()));
            if (canUse) {
                int countInBox = tag.getInt("countInBox");
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
                    tag.remove("countInBox");
                    tag.remove("item");
                    if (tag.contains("autoCollect")) tag.remove("autoCollect");
                } else {
                    tag.putInt("countInBox", countInBox);
                    tag.put("item", itemInBox.writeNbt(new NbtCompound()));
                }
                itemStack.setNbt(tag);
            }
            return canUse ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return super.useOnBlock(context);
    }

    // 0 = 取り出し(インベントリオープン時はコンテナーへ収納) 1 = 取り出してドロップ 2 = ストレージボックスへ収納(インベントリオープン時はコンテナーからストレージボックスへ収納) 3 = AutoCollect切り替え
    public void keyboardEvent(int type, PlayerEntity player, ItemStack itemStack) {
        if (type == 0) {
            if (player.currentScreenHandler != null && !(player.currentScreenHandler instanceof PlayerScreenHandler) && !(player.currentScreenHandler.slots.size() <= 0)) {
                NbtCompound tag = itemStack.getNbt();
                ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
                int count = tag.getInt("countInBox");
                for (Slot slot : player.currentScreenHandler.slots) {
                    if (slot.inventory == player.getInventory()) continue;
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
                        tag.remove("countInBox");
                        tag.remove("item");
                        if (tag.contains("autoCollect")) tag.remove("autoCollect");
                        break;
                    }
                }
                if (tag.contains("item"))
                    tag.putInt("countInBox", count);
                itemStack.setNbt(tag);
                return;
            }
            NbtCompound tag = itemStack.getNbt();
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            int count = tag.getInt("countInBox");
            ItemStack giveStack = itemInBox.copy();
            if (count > 64) {
                giveStack.setCount(64);
                if (canGive(player.getInventory().main)) {
                    player.giveItemStack(giveStack);
                } else {
                    player.dropItem(giveStack, false);
                }
                tag.putInt("countInBox", count - 64);
            } else {
                giveStack.setCount(count);
                if (canGive(player.getInventory().main)) {
                    player.giveItemStack(giveStack);
                } else {
                    player.dropItem(giveStack, false);
                }                tag.remove("countInBox");
                tag.remove("item");
                if (tag.contains("autoCollect")) tag.remove("autoCollect");
            }
            itemStack.setNbt(tag);
            return;
        }
        if (type == 1) {
            NbtCompound tag = itemStack.getNbt();
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            int count = tag.getInt("countInBox");
            ItemStack dropStack = itemInBox.copy();
            if (count > 64) {
                dropStack.setCount(64);
                player.dropItem(dropStack, false);
                tag.putInt("countInBox", count - 64);
            } else {
                dropStack.setCount(count);
                player.dropItem(dropStack, false);
                tag.remove("countInBox");
                tag.remove("item");
                if (tag.contains("autoCollect")) tag.remove("autoCollect");
            }
            itemStack.setNbt(tag);
            return;
        }
        if (type == 2) {
            if (!(player.currentScreenHandler instanceof PlayerScreenHandler) && player.currentScreenHandler != null) {
                NbtCompound tag = itemStack.getNbt();
                ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
                int count = tag.getInt("countInBox");
                for (Slot slot : player.currentScreenHandler.slots) {
                    if (slot.inventory == player.getInventory()) continue;
                    ItemStack stack = slot.getStack();
                    if (stack.getItem() == itemInBox.getItem()) {
                        if (!StorageBoxSlot.canInsertStack(stack)) continue;
                        count += stack.getCount();
                        player.getInventory().removeOne(stack);
                        stack.setCount(0);
                        stack = ItemStack.EMPTY;
                        slot.setStack(stack);
                    }
                }
                tag.putInt("countInBox", count);
                itemStack.setNbt(tag);
                return;
            }
            NbtCompound tag = itemStack.getNbt();
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            int count = tag.getInt("countInBox");
            for (ItemStack stack : player.getInventory().main) {
                if (stack.getItem() == itemInBox.getItem()) {
                    if (!StorageBoxSlot.canInsertStack(stack)) continue;
                    count += stack.getCount();
                    player.getInventory().removeOne(stack);
                    stack.setCount(0);
                    stack = ItemStack.EMPTY;
                }
            }
            tag.putInt("countInBox", count);
            itemStack.setNbt(tag);
            return;
        }
        if (type == 3) {
            NbtCompound tag = itemStack.getNbt();
            if (isAutoCollect(itemStack)) {
                tag.putBoolean("autoCollect", false);
                player.sendMessage(TextUtil.literal("§7[StorageBox] §cAutoCollect changed OFF"), false);
            } else {
                tag.remove("autoCollect");
                player.sendMessage(TextUtil.literal("§7[StorageBox] §aAutoCollect changed ON"), false);
            }
            itemStack.setNbt(tag);
            return;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            ItemStack itemInBox = ItemStack.fromNbt(tag.getCompound("item"));
            int count = tag.getInt("countInBox");
            tooltip.add(TextUtil.literal("§7Name: " + itemInBox.getItem().getName().getString()));
            tooltip.add(TextUtil.literal("§7Unit: " + calcItemNumByUnit(count , false, itemInBox.getMaxCount()).toString()));
            tooltip.add(TextUtil.literal("§7Items: " + count));
            tooltip.add(TextUtil.literal("§7AutoCollect: " + (isAutoCollect(stack) ? "ON" : "OFF")));
            tooltip.add(TextUtil.literal("§7[Information]"));
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
        NbtCompound tag = stack.getNbt();
        boolean autoCollect = true;
        if (tag.contains("autoCollect")) {
            autoCollect = tag.getBoolean("autoCollect");
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
