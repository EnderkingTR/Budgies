package com.nadir.budgies.client.model;

import com.nadir.budgies.entity.BudgieEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BudgieModel extends GeoModel<BudgieEntity> {

    private static final ResourceLocation MODEL      = ResourceLocation.fromNamespaceAndPath("budgies", "geo/budgie.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("budgies", "animations/budgie.animation.json");

    private static final ResourceLocation[] TEXTURES_MALE = {
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_0_male.png"),
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_1_male.png"),
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_2_male.png")
    };

    private static final ResourceLocation[] TEXTURES_FEMALE = {
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_0_female.png"),
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_1_female.png"),
            ResourceLocation.fromNamespaceAndPath("budgies", "textures/entity/variant_2_female.png")
    };

    @Override
    public ResourceLocation getModelResource(BudgieEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BudgieEntity animatable) {
        int variant = Math.max(0, Math.min(2, animatable.getVariant()));
        return animatable.isMale() ? TEXTURES_MALE[variant] : TEXTURES_FEMALE[variant];
    }

    @Override
    public ResourceLocation getAnimationResource(BudgieEntity animatable) {
        return ANIMATIONS;
    }
}
