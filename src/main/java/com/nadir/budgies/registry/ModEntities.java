package com.nadir.budgies.registry;

import com.nadir.budgies.BUDGIES;
import com.nadir.budgies.entity.BudgieEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<BudgieEntity> BUDGIE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            BUDGIES.id("budgie"),
            FabricEntityTypeBuilder.<BudgieEntity>create(MobCategory.CREATURE, BudgieEntity::new)
                    .dimensions(EntityDimensions.scalable(0.4F, 0.5F))
                    .trackRangeBlocks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    /** Call from BUDGIES#onInitialize to trigger static field init. */
    public static void register() {
        BUDGIES.LOGGER.debug("Registering ModEntities for " + BUDGIES.MOD_ID);
    }
}
