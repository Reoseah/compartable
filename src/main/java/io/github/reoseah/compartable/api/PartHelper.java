package io.github.reoseah.compartable.api;

import io.github.reoseah.compartable.core.PartContainerBlock;
import io.github.reoseah.compartable.core.PartContainerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public abstract class PartHelper {
    /**
     * Returns the block state at the given position, or the first matching part state if the block at the position is
     * a part container.
     */
    public static BlockState getPartOrBlockState(World world, BlockPos pos, Block block) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == PartContainerBlock.INSTANCE) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PartContainerBlockEntity container) {
                Map<BlockState, ?> parts = container.getParts();
                if (parts != null) {
                    BlockState found = state;
                    for (BlockState s : parts.keySet()) {
                        if (s.isOf(block)) {
                            found = s;
                            break;
                        }
                    }
                    state = found;
                }
            }
        }
        return state;
    }

    /**
     * Returns the block state at the given position, or the first matching part state if the block at the position is
     * a part container.
     */
    public static BlockState getPartOrBlockState(World world, BlockPos pos, TagKey<Block> tag) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == PartContainerBlock.INSTANCE) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PartContainerBlockEntity container) {
                Map<BlockState, ?> parts = container.getParts();
                if (parts != null) {
                    BlockState found = state;
                    for (BlockState s : parts.keySet()) {
                        if (s.isIn(tag)) {
                            found = s;
                            break;
                        }
                    }
                    state = found;
                }
            }
        }
        return state;
    }

    private PartHelper() {
    }
}
