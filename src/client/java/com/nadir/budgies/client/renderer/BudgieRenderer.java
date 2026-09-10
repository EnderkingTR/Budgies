package com.nadir.budgies.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nadir.budgies.client.model.BudgieModel;
import com.nadir.budgies.entity.BudgieEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BudgieRenderer extends GeoEntityRenderer<BudgieEntity> {

    public BudgieRenderer(EntityRendererProvider.Context context) {
        super(context, new BudgieModel());
    }

    @Override
    public void render(BudgieEntity entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {
        // Scale babies to half size
        if (entity.isBaby()) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
