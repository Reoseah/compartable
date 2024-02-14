package io.github.reoseah.compartable.vanillaparts.mixin;

import io.github.reoseah.compartable.api.Part;
import io.github.reoseah.compartable.api.PartHelper;
import io.github.reoseah.compartable.core.PartContainerBlock;
import io.github.reoseah.compartable.core.PartContainerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public abstract class FenceBlockMixin extends HorizontalConnectingBlock implements Part {
    private FenceBlockMixin(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, Settings settings) {
        super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
    }

    @Redirect(method = "getPlacementState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState getBlockState(BlockView world, BlockPos pos) {
        return PartHelper.getPartOrBlockState(world, pos, BlockTags.FENCES);
    }

    @Shadow
    public abstract boolean canConnect(BlockState state, boolean neighborIsFullCube, Direction dir);

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom, CallbackInfoReturnable<BlockState> info) {
        if (direction.getAxis().getType() == Direction.Type.HORIZONTAL && newState.getBlock() == PartContainerBlock.INSTANCE) {
            BlockEntity entity = world.getBlockEntity(posFrom);
            if (entity instanceof PartContainerBlockEntity container) {
                BlockState wrappedFence = container.getFirstMatchingPart(BlockTags.FENCES);
                if (wrappedFence != null) {
                    info.setReturnValue(state.with(FACING_PROPERTIES.get(direction), //
                            this.canConnect(wrappedFence, wrappedFence.isFullCube(world, posFrom), direction)));
                }
            }
        }
    }
}
