package io.github.reoseah.compartable.core;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import io.github.reoseah.compartable.api.Parts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartContainerBlock extends BlockWithEntity {
    public static final MapCodec<PartContainerBlock> CODEC = createCodec(PartContainerBlock::new);

    public static final PartContainerBlock INSTANCE = new PartContainerBlock(Block.Settings.create() //
            .nonOpaque() //
            .luminance(state -> state.get(Parts.LUMINANCE)));

    protected PartContainerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(Parts.LUMINANCE, 0).with(Parts.EMITS_REDSTONE, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Parts.LUMINANCE, Parts.EMITS_REDSTONE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PartContainerBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(Parts.EMITS_REDSTONE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            return container.getCollisionShape();
        }
        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            return container.getOutlineShape();
        }
        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            return container.getSidesShape();
        }
        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            int max = 0;
            for (BlockState part : container.parts.keySet()) {
                max = Math.max(max, part.getWeakRedstonePower(world, pos, direction));
            }
            return max;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            int max = 0;
            for (BlockState part : container.parts.keySet()) {
                max = Math.max(max, part.getStrongRedstonePower(world, pos, direction));
            }
            return max;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        BlockEntity entity = builder.get(LootContextParameters.BLOCK_ENTITY);
        if (entity instanceof PartContainerBlockEntity container) {
            List<ItemStack> stacks = new ArrayList<>();
            for (BlockState part : container.parts.keySet()) {
                stacks.addAll(part.getDroppedStacks(builder));
            }
            return stacks;
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;
        if (hit == null) {
            return ItemStack.EMPTY;
        }
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            @Nullable Map.Entry<BlockState, @Nullable BlockEntity> part = container.getPartAtPoint(hit.getPos());
            if (part != null) {
                return part.getKey().getBlock().getPickStack(world, pos, part.getKey());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            @Nullable Map.Entry<BlockState, @Nullable BlockEntity> part = container.getPartAtPoint(hit.getPos());
            if (part != null) {
                BlockState partState = part.getKey();
                ActionResult result = partState.getBlock().onUse(partState, world, pos, player, hand, hit);
                if (!world.getBlockState(pos).isOf(this)) {
                    container.repairAfterWorldMutation(partState);
                }
                return result;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            for (BlockState part : ImmutableSet.copyOf(container.parts.keySet())) {
                part.neighborUpdate(world, pos, sourceBlock, sourcePos, notify);
                if (!world.getBlockState(pos).isOf(this)) {
                    container.repairAfterWorldMutation(part);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            for (BlockState part : ImmutableSet.copyOf(container.parts.keySet())) {
                BlockState replacement = part.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos);
                if (!world.getBlockState(pos).isOf(this)) {
                    container.repairAfterWorldMutation(part);
                }
                if (replacement != part) {
                    container.replacePart(part, replacement);
                }
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PartContainerBlockEntity container) {
            for (BlockState part : ImmutableSet.copyOf(container.parts.keySet())) {
                part.scheduledTick(world, pos, random);
                if (!world.getBlockState(pos).isOf(this)) {
                    container.repairAfterWorldMutation(part);
                }
            }
        }
    }
}
