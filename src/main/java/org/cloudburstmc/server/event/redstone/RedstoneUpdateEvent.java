package org.cloudburstmc.server.event.redstone;

import org.cloudburstmc.server.block.Block;
import org.cloudburstmc.server.event.block.BlockUpdateEvent;

public class RedstoneUpdateEvent extends BlockUpdateEvent {

    public RedstoneUpdateEvent(Block source) {
        super(source);
    }

}

