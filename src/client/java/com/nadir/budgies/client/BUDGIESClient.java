package com.nadir.budgies.client;

import com.nadir.budgies.client.renderer.BudgieRenderer;
import com.nadir.budgies.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BUDGIESClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.BUDGIE, BudgieRenderer::new);
	}
}