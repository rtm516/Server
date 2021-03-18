package org.cloudburstmc.server.block.behavior;

import com.nukkitx.math.vector.Vector3f;
import org.cloudburstmc.api.block.*;
import org.cloudburstmc.api.block.behavior.BlockBehavior;
import org.cloudburstmc.api.blockentity.Bed;
import org.cloudburstmc.api.blockentity.BlockEntity;
import org.cloudburstmc.api.blockentity.BlockEntityTypes;
import org.cloudburstmc.api.item.ItemStack;
import org.cloudburstmc.api.level.Location;
import org.cloudburstmc.api.player.Player;
import org.cloudburstmc.api.util.Direction;
import org.cloudburstmc.api.util.Direction.Plane;
import org.cloudburstmc.api.util.data.BlockColor;
import org.cloudburstmc.api.util.data.DyeColor;
import org.cloudburstmc.server.item.ItemTypes;
import org.cloudburstmc.server.level.CloudLevel;
import org.cloudburstmc.server.level.chunk.CloudChunk;
import org.cloudburstmc.server.locale.TranslationContainer;
import org.cloudburstmc.server.player.CloudPlayer;
import org.cloudburstmc.server.registry.BlockEntityRegistry;
import org.cloudburstmc.server.registry.BlockRegistry;
import org.cloudburstmc.server.registry.CloudItemRegistry;
import org.cloudburstmc.server.utils.TextFormat;

public class BlockBehaviorBed extends BlockBehaviorTransparent {

    @Override
    public boolean canBeActivated(Block block) {
        return true;
    }


    @Override
    public boolean onActivate(Block block, ItemStack item, Player player) {
        BlockState state = block.getState();

        BlockState head = null;

        if (state.ensureTrait(BlockTraits.IS_HEAD_PIECE)) {
            head = state;
        } else {
            for (Direction face : Plane.HORIZONTAL) {
                BlockState side = block.getSide(face).getState();

                if (side.getType() == BlockTypes.BED && side.ensureTrait(BlockTraits.IS_HEAD_PIECE)) {
                    head = side;
                    break;
                }
            }
        }

        if (head == null) {
            if (player != null) {
                ((CloudPlayer) player).sendMessage(new TranslationContainer("tile.bed.notValid"));
            }

            return true;
        }

        if (player != null) {
            Location spawn = Location.from(block.getPosition().toFloat().add(0.5, 0.5, 0.5), player.getYaw(), player.getPitch(), player.getLevel());

            if (!player.getSpawn().equals(spawn)) {
                player.setSpawn(spawn);
                ((CloudPlayer) player).sendMessage(new TranslationContainer(TextFormat.GRAY + "%tile.bed.respawnSet"));
            }
        }

        int time = block.getLevel().getTime() % CloudLevel.TIME_FULL;

        boolean isNight = (time >= CloudLevel.TIME_NIGHT && time < CloudLevel.TIME_SUNRISE);

        if (player != null && !isNight) {
            ((CloudPlayer) player).sendMessage(new TranslationContainer(TextFormat.GRAY + "%tile.bed.noSleep"));
            return true;
        }

        if (player != null && !player.sleepOn(block.getPosition())) {
            ((CloudPlayer) player).sendMessage(new TranslationContainer(TextFormat.GRAY + "%tile.bed.occupied"));
        }


        return true;
    }

    @Override
    public boolean place(ItemStack item, Block block, Block target, Direction face, Vector3f clickPos, Player player) {
        BlockState down = block.down().getState();
        BlockRegistry registry = BlockRegistry.get();

        if (!down.inCategory(BlockCategory.TRANSPARENT)) {
            Block next = block.getSide(player.getHorizontalDirection());
            BlockBehavior nextBehavior = next.getState().getBehavior();

            BlockState downNext = next.down().getState();

            if (nextBehavior.canBeReplaced(next) && !downNext.inCategory(BlockCategory.TRANSPARENT)) {
                BlockState bed = registry.getBlock(BlockTypes.BED)
                        .withTrait(BlockTraits.DIRECTION, player.getDirection());

                placeBlock(block, bed);
                placeBlock(next, bed.withTrait(BlockTraits.IS_HEAD_PIECE, true));

                createBlockEntity(block, item.getMetadata(DyeColor.class));
                createBlockEntity(next, item.getMetadata(DyeColor.class));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onBreak(Block block, ItemStack item) {
        BlockState state = block.getState();
        boolean head = state.ensureTrait(BlockTraits.IS_HEAD_PIECE);
        Direction facing = state.ensureTrait(BlockTraits.DIRECTION);

        Block otherPart = null;
        for (Direction direction : Plane.HORIZONTAL) {
            Block side = block.getSide(direction);
            BlockState face = side.getState();

            if (face.getType() == BlockTypes.BED && face.ensureTrait(BlockTraits.IS_HEAD_PIECE) != head && face.ensureTrait(BlockTraits.DIRECTION) == facing) {
                otherPart = side;
                break;
            }
        }

        if (otherPart != null) {
            otherPart.getState().getBehavior().removeBlock(otherPart, false);
        }

        removeBlock(block, true);
        return true;
    }

    private void createBlockEntity(Block block, DyeColor color) {
        Bed bed = BlockEntityRegistry.get().newEntity(BlockEntityTypes.BED, (CloudChunk) block.getChunk(), block.getPosition());
        bed.setColor(color);
    }

    @Override
    public ItemStack toItem(Block block) {
        return CloudItemRegistry.get().getItem(ItemTypes.BED, 1, this.getDyeColor(block));
    }

    @Override
    public BlockColor getColor(Block block) {
        return this.getDyeColor(block).getColor();
    }

    public DyeColor getDyeColor(Block block) {
        BlockEntity blockEntity = block.getLevel().getBlockEntity(block.getPosition());

        if (blockEntity instanceof Bed) {
            return ((Bed) blockEntity).getColor();
        }

        return DyeColor.WHITE;
    }


}
