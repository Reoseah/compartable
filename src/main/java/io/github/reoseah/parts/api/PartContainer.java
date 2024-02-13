package io.github.reoseah.parts.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A {@link net.minecraft.block.entity.BlockEntity} that manages parts sharing the same block space.
 */
public interface PartContainer {
    /**
     * @return the parts managed by this container
     */
    Map<BlockState, @Nullable BlockEntity> getParts();

    void forEachPart(BiConsumer<BlockState, @Nullable BlockEntity> action);

    /**
     * @see #getPartAtPoint(double, double, double)
     */
    default @Nullable Map.Entry<BlockState, @Nullable BlockEntity> getPartAtPoint(Vec3d point) {
        return this.getPartAtPoint(point.x, point.y, point.z);
    }

    /**
     * @return the part at the given point, or null if there is no part at that point.
     */
    @Nullable
    Map.Entry<BlockState, @Nullable BlockEntity> getPartAtPoint(double x, double y, double z);
}
