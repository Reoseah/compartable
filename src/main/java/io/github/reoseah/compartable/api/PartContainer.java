package io.github.reoseah.compartable.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A {@link net.minecraft.block.entity.BlockEntity} that manages parts sharing the same block space.
 */
public interface PartContainer {
    /** @return the parts managed by this container */
    Map<BlockState, @Nullable BlockEntity> getParts();

    /** @return the part at the given point, or null if there is no part at that point. */
    @Nullable
    Map.Entry<BlockState, @Nullable BlockEntity> getPartAtPoint(double x, double y, double z);

    /** @return the entity of this part, if present. */
    @Nullable BlockEntity getPartEntity(BlockState part);

    /**
     * Returns whether the given part can be inserted into this container.
     *
     * @see #insert(BlockState, BlockEntity, PlayerEntity)
     */
    boolean canInsert(BlockState state, @Nullable BlockEntity entity);

    /**
     * Inserts the given part into this container.
     * <p>
     * Fails when:
     * <ul>
     *     <li>outline shape collides with another part
     *     <li>part is already present
     *     <li>container is waterlogged and part is not waterloggable
     * </ul>
     */
    boolean insert(BlockState state, @Nullable BlockEntity entity, @Nullable PlayerEntity syncUnnecessary);

    /**
     * Replace the given part with the replacement part. If part has a block entity,
     * it's assigned to the replacement part and {@link BlockEntity#setCachedState(BlockState)} is called with it.
     */
    void replacePart(BlockState part, BlockState replacement);

    /**
     * Restores container after a part directly modified block state or block entity.
     * The 'causer' is replaced by the new block state at the same position.
     */
    void repairAfterWorldMutation(BlockState causer);
}
