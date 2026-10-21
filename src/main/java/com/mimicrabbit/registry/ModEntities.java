package com.mimicrabbit.registry;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.entity.MimicRabbit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MimicRabbitMod.MOD_ID);

    /** Tamaño base = conejo vanilla (0.4 x 0.5). La forma monstruosa cambia sus dimensiones en la propia entidad. */
    public static final RegistryObject<EntityType<MimicRabbit>> MIMIC_RABBIT = ENTITY_TYPES.register(
            "mimic_rabbit",
            () -> EntityType.Builder.of(MimicRabbit::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.5F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(MimicRabbitMod.MOD_ID, "mimic_rabbit").toString()));

    private ModEntities() {
    }
}
