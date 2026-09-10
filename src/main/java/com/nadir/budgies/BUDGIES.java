package com.nadir.budgies;

import com.nadir.budgies.entity.BudgieEntity;
import com.nadir.budgies.registry.ModEntities;
import com.nadir.budgies.registry.ModItems;
import com.nadir.budgies.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BUDGIES implements ModInitializer {
	public static final String MOD_ID = "budgies";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModEntities.register();
		ModItems.register();
		FabricDefaultAttributeRegistry.register(ModEntities.BUDGIE, BudgieEntity.createAttributes());
		LOGGER.info("BUDGIES! mod initialized — let the chirping begin!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
