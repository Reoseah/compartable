package io.github.reoseah.compartable.core;

import com.google.common.collect.ImmutableSet;
import io.github.reoseah.compartable.api.Part;
import io.github.reoseah.compartable.api.PartContainer;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        this.parts.put(Blocks.OAK_FENCE.getDefaultState(), null);
        this.parts.put(Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.WALL).with(LeverBlock.FACING, Direction.NORTH), null);
        this.parts.put(Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(WallRedstoneTorchBlock.FACING, Direction.SOUTH), null);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList partsNbt = new NbtList();
        for (Map.Entry<BlockState, @Nullable BlockEntity> entry : this.parts.entrySet()) {
            NbtCompound partNbt = new NbtCompound();
            partNbt.put("state", NbtHelper.fromBlockState(entry.getKey()));
            if (entry.getValue() != null) {
                NbtCompound entityNbt = entry.getValue().createNbt();
                partNbt.put("entity", entityNbt);
            }
            partsNbt.add(partNbt);
        }
        nbt.put("parts", partsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        RegistryEntryLookup<Block> lookup = new RegistryEntryLookup<>() {
            @Override
            public Optional<RegistryEntry.Reference<Block>> getOptional(RegistryKey<Block> key) {
                return Registries.BLOCK.getEntry(key);
            }

            @Override
            public Optional<RegistryEntryList.Named<Block>> getOptional(TagKey<Block> tag) {
                return Registries.BLOCK.getTagCreatingWrapper().getOptional(tag);
            }
        };

        NbtList partsNbt = nbt.getList("parts", 10);
        this.parts.clear();
        for (int i = 0; i < partsNbt.size(); i++) {
            NbtCompound partNbt = partsNbt.getCompound(i);
            BlockState state = NbtHelper.toBlockState(lookup, partNbt.getCompound("state"));
            @Nullable BlockEntity entity = null;
            if (partNbt.contains("entity", NbtElement.COMPOUND_TYPE)) {
                NbtCompound entityNbt = partNbt.getCompound("entity");
                entity = BlockEntity.createFromNbt(this.pos, state, entityNbt);
            }
            this.parts.put(state, entity);
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = super.toInitialChunkDataNbt();
        // TODO reduce amount of data sent
        this.writeNbt(tag);
        return tag;
    }

    public @Nullable BlockState getFirstMatchingPart(Block block) {
        for (BlockState state : this.getParts().keySet()) {
            if (state.isOf(block)) {
                return state;
            }
        }
        return null;
    }

    public @Nullable BlockState getFirstMatchingPart(TagKey<Block> tag) {
        for (BlockState state : this.getParts().keySet()) {
            if (state.isIn(tag)) {
                return state;
            }
        }
        return null;
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

            this.markDirty();

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

        this.markDirty();
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
