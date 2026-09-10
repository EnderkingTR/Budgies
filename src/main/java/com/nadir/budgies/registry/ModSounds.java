package com.nadir.budgies.registry;

import com.nadir.budgies.BUDGIES;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent BUDGIE_IDLE     = register("budgie.idle");
    public static final SoundEvent BUDGIE_CALLING  = register("budgie.calling");
    public static final SoundEvent BUDGIE_ANSWERING = register("budgie.answering");
    public static final SoundEvent BUDGIE_SLEEPING = register("budgie.sleeping");

    private static SoundEvent register(String name) {
        ResourceLocation id = BUDGIES.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    /** Call from BUDGIES#onInitialize to trigger static field init. */
    public static void register() {
        BUDGIES.LOGGER.debug("Registering ModSounds for " + BUDGIES.MOD_ID);
    }
}
