package com.mimicrabbit.registry;

import com.mimicrabbit.MimicRabbitMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Eventos de sonido del mod. Cada uno se define en assets/mimicrabbit/sounds.json.
 * Por defecto reutilizan sonidos vanilla (con otro tono); para usar audio propio ver el README.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MimicRabbitMod.MOD_ID);

    public static final RegistryObject<SoundEvent> TRANSFORM_START = register("entity.mimic_rabbit.transform_start");
    public static final RegistryObject<SoundEvent> TRANSFORM_CRACK = register("entity.mimic_rabbit.transform_crack");
    public static final RegistryObject<SoundEvent> TRANSFORM_SCREAM = register("entity.mimic_rabbit.transform_scream");
    public static final RegistryObject<SoundEvent> TRANSFORM_ROAR = register("entity.mimic_rabbit.transform_roar");
    public static final RegistryObject<SoundEvent> AMBIENT = register("entity.mimic_rabbit.ambient");
    public static final RegistryObject<SoundEvent> BREATH = register("entity.mimic_rabbit.breath");
    public static final RegistryObject<SoundEvent> ATTACK = register("entity.mimic_rabbit.attack");
    public static final RegistryObject<SoundEvent> HURT = register("entity.mimic_rabbit.hurt");
    public static final RegistryObject<SoundEvent> DEATH = register("entity.mimic_rabbit.death");
    public static final RegistryObject<SoundEvent> STEP = register("entity.mimic_rabbit.step");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MimicRabbitMod.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
