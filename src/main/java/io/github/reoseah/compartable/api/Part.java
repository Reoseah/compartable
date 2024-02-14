package io.github.reoseah.compartable.api;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A {@link Block} that can share its block space with other blocks.
 * It's a marker interface, it doesn't require you to implement any new methods on your block,
 * but it requires block to be implemented in a way that doesn't break part container.
 * <p>
 * Contrast it to libraries like LibMultipart that have part code that looks similar to vanilla blocks
 * and because of it, they lead to some code duplication in consumers of that library.
 * <p>
 * When your block is being used as a part, the actual block present in world will be "compartable:part_container",
 * and the part will be stored in a block entity. This means that you can't rely on your block and/or block entity
 * being in the world, and you need you use {@link PartContainer} or helper methods provided instead of interacting
 * with the world directly.
 * <p>
 * There are a few common cases that are handled by the library:
 * <ul>
 *     <li>calling {@link World#setBlockState} at your position inside block methods will be corrected into
 *     replacing the part in the container</li>
 *     <li>{@link World#scheduleBlockTick} will work as expected, except when there are multiple parts
 *     with the same block - they all will receive the tick</li>
 * </ul>
 * <p>
 * Common cases that will need to be coded differently:
 * <ul>
 *     <li>calling {@link World#getBlockEntity(BlockPos)} will return corresponding {@link PartContainer},
 *     use {@link PartContainer#getPartEntity} to get your entity</li>
 *     <li>in blocks that connect to other blocks, like fences, using {@link World#getBlockState} to get neighbor state
 *     won't work when neighbors are parts in a container, use {@link PartHelper#getPartOrBlockState} instead</li>
 * </ul>
 *
 * @see PartContainer
 */
public interface Part {
}
