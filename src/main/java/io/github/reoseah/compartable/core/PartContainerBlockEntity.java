package io.github.reoseah.compartable.core;

import com.google.common.collect.ImmutableSet;
import io.github.reoseah.compartable.api.Part;
import io.github.reoseah.compartable.api.PartContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PartContainerBlockEntity extends BlockEntity implements PartContainer {
    public static final BlockEntityType<PartContainerBlockEntity> TYPE = new BlockEntityType<>(PartContainerBlockEntity::new, ImmutableSet.of(PartContainerBlock.INSTANCE), null);

    protected final Map<BlockState, @Nullable BlockEntity> parts = new HashMap<>();

    public @Nullable VoxelShape collisionShape;
    public @Nullable VoxelShape outlineShape;
    public @Nullable VoxelShape sidesShape;

    public PartContainerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    {
        // FIXME testing code
        this.parts.put(Blocks.REDSTONE_TORCH.getDefaultState(), null);
        this.parts.put(Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.CEILING), null);
    }

    @Override
    public Map<BlockState, @Nullable BlockEntity> getParts() {
        return this.parts;
    }

    @Override
    public @Nullable Map.Entry<BlockState, @Nullable BlockEntity> getPartAtPoint(double x, double y, double z) {
        x -= this.pos.getX();
        y -= this.pos.getY();
        z -= this.pos.getZ();
        for (Map.Entry<BlockState, @Nullable BlockEntity> entry : this.parts.entrySet()) {
            for (Box box : entry.getKey().getOutlineShape(this.world, this.pos).getBoundingBoxes()) {
                if (box.expand(0.01).contains(x, y, z)) {
                    return entry;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity getPartEntity(BlockState part) {
        return this.parts.get(part);
    }

    @Override
    public boolean canInsert(BlockState state, @Nullable BlockEntity entity) {
        // TODO check for collision with other parts
        return state.getBlock() instanceof Part;
    }

    @Override
    public boolean insert(BlockState state, @Nullable BlockEntity entity, @Nullable PlayerEntity syncUnneeded) {
        if (this.canInsert(state, entity)) {
            this.parts.put(state, entity);

            if (!this.world.isClient) {
                PartContainerPackets.syncPartInsertion(this, state, entity);
            }

            this.collisionShape = null;
            this.outlineShape = null;
            this.sidesShape = null;

            return true;
        }
        return false;
    }

    @Override
    public void repairAfterWorldMutation(BlockState causer) {
        BlockState newState = this.world.getBlockState(this.pos);

        this.world.setBlockState(this.pos, this.getCachedState());
        this.world.getWorldChunk(this.pos).setBlockEntity(this);

        this.replacePart(causer, newState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void replacePart(BlockState part, BlockState replacement) {
        @Nullable BlockEntity entity = this.parts.remove(part);
        this.parts.put(replacement, entity);
        if (entity != null) {
            entity.setCachedState(replacement);
        }

        part.onStateReplaced(this.world, this.pos, replacement, false);

        if (!this.world.isClient) {
            PartContainerPackets.syncPartReplacement(this, part, replacement, null);
        }

        this.collisionShape = null;
        this.outlineShape = null;
        this.sidesShape = null;
    }

    public VoxelShape getCollisionShape() {
        if (this.collisionShape == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (BlockState part : this.parts.keySet()) {
                shape = VoxelShapes.union(shape, part.getCollisionShape(this.world, this.pos));
            }
            this.collisionShape = shape;
        }
        return this.collisionShape;
    }

    public VoxelShape getOutlineShape() {
        if (this.outlineShape == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (BlockState part : this.parts.keySet()) {
                shape = VoxelShapes.union(shape, part.getOutlineShape(this.world, this.pos));
            }
            this.outlineShape = shape;
        }
        return this.outlineShape;
    }

    public VoxelShape getSidesShape() {
        if (this.sidesShape == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (BlockState state : this.parts.keySet()) {
                shape = VoxelShapes.union(shape, state.getSidesShape(this.world, this.pos));
            }
            this.sidesShape = shape;
        }
        return this.sidesShape;
    }
}
