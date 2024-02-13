package io.github.reoseah.parts.core;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.parts.api.Parts;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PartContainerBlock extends BlockWithEntity {
    public static final MapCodec<PartContainerBlock> CODEC = createCodec(PartContainerBlock::new);

    public static final PartContainerBlock INSTANCE = new PartContainerBlock(Block.Settings.create() //
            .nonOpaque() //
            .luminance(state -> state.get(Parts.LUMINANCE)));

    protected PartContainerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(Parts.LUMINANCE, 0));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Parts.LUMINANCE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PartContainerBlockEntity(pos, state);
    }
}
