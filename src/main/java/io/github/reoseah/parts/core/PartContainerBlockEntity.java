package io.github.reoseah.parts.core;

import com.google.common.collect.ImmutableSet;
import io.github.reoseah.parts.api.PartContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PartContainerBlockEntity extends BlockEntity implements PartContainer {
    public static final BlockEntityType<PartContainerBlockEntity> TYPE = new BlockEntityType<>(PartContainerBlockEntity::new, ImmutableSet.of(PartContainerBlock.INSTANCE), null);

    protected final Map<BlockState, @Nullable BlockEntity> parts = new HashMap<>();

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
    public void forEachPart(BiConsumer<BlockState, @Nullable BlockEntity> action) {
        this.parts.forEach(action);
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
}
