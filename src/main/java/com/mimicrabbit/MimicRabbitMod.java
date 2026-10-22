package com.mimicrabbit;

import com.mimicrabbit.registry.ModEntities;
import com.mimicrabbit.registry.ModItems;
import com.mimicrabbit.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Punto de entrada del mod. Solo registra los registros diferidos (entidades, sonidos, objetos).
 * Los eventos del bus del mod se manejan en {@link com.mimicrabbit.event.ModEvents}
 * y {@link com.mimicrabbit.client.ClientModEvents}.
 */
@Mod(MimicRabbitMod.MOD_ID)
public class MimicRabbitMod {
    public static final String MOD_ID = "mimicrabbit";

    public MimicRabbitMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
    }
}
