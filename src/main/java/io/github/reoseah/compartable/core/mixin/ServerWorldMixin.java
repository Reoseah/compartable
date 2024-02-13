package io.github.reoseah.compartable.core.mixin;

import io.github.reoseah.compartable.core.PartContainerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "tickBlock", at = @At("HEAD"))
    private void tickBlock(BlockPos pos, Block block, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        BlockState state = world.getBlockState(pos);
        if (state.isOf(PartContainerBlock.INSTANCE)) {
            state.scheduledTick(world, pos, world.random);
        }
    }
}
