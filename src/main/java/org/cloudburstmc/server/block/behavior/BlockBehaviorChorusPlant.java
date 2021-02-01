package org.cloudburstmc.server.block.behavior;

import org.cloudburstmc.api.item.ItemStack;
import org.cloudburstmc.server.block.Block;
import org.cloudburstmc.server.utils.BlockColor;

public class BlockBehaviorChorusPlant extends BlockBehaviorTransparent {


    @Override
    public ItemStack[] getDrops(Block block, ItemStack hand) {
        return new ItemStack[0];
    }

    @Override
    public BlockColor getColor(Block block) {
        return BlockColor.PURPLE_BLOCK_COLOR;
    }
}
