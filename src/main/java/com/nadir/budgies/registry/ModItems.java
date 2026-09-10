package com.nadir.budgies.registry;

import com.nadir.budgies.BUDGIES;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {

    public static final Item BUDGIE_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            BUDGIES.id("budgie_spawn_egg"),
            new SpawnEggItem(ModEntities.BUDGIE, 0x6FBF4A, 0xF5E04E,
                    new Item.Properties())
    );

    public static void register() {
        BUDGIES.LOGGER.debug("Registering ModItems for " + BUDGIES.MOD_ID);
    }
}
