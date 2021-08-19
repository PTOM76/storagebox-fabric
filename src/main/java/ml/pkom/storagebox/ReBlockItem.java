package ml.pkom.storagebox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

public class ReBlockItem extends BlockItem {

    public ReBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean canPlace(ItemPlacementContext context, BlockState state) {
        return super.canPlace(context, state);
    }
}
