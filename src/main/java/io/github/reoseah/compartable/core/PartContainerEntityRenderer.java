package io.github.reoseah.compartable.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class PartContainerEntityRenderer implements BlockEntityRenderer<PartContainerBlockEntity> {
    public PartContainerEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(PartContainerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockManager = client.getBlockRenderManager();
        BlockModelRenderer blockRenderer = blockManager.getModelRenderer();

        World world = entity.getWorld();
        BlockPos pos = entity.getPos();

        for (BlockState partState : entity.parts.keySet()) {
            switch (partState.getRenderType()) {
                case MODEL: {
                    // TODO use Fabric rendering API for static models for performance
                    BakedModel model = blockManager.getModel(partState);
                    matrices.push();
                    blockRenderer.render(world, model, partState, pos, matrices, vertexConsumers.getBuffer(RenderLayer.getCutout()), false, world.getRandom(), 1, 0xFFFFFF);
                    matrices.pop();
                }
                default: {
                    // FIXME invoke block entity renderers of parts
                }
            }
        }
    }
}
