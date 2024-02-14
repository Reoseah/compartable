package io.github.reoseah.compartable.api;

import io.github.reoseah.compartable.core.PartContainerBlock;
import io.github.reoseah.compartable.core.PartContainerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class PartHelper {
    /**
     * Returns the block state at the given position, or the first matching part state if the block at the position is
     * a part container.
     */
    public static BlockState getPartOrBlockState(BlockView world, BlockPos pos, Block block) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == PartContainerBlock.INSTANCE) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PartContainerBlockEntity container) {
                BlockState part = container.getFirstMatchingPart(block);
                if (part != null) {
                    return part;
                }
            }
        }
        return state;
    }

    /**
     * Returns the block state at the given position, or the first matching part state if the block at the position is
     * a part container.
     */
    public static BlockState getPartOrBlockState(BlockView world, BlockPos pos, TagKey<Block> tag) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == PartContainerBlock.INSTANCE) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PartContainerBlockEntity container) {
                BlockState part = container.getFirstMatchingPart(tag);
                if (part != null) {
                    return part;
                }
            }
        }
        return state;
    }

    private PartHelper() {
    }
}
