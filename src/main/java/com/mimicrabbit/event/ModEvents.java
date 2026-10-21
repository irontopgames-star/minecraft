package com.mimicrabbit.event;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.entity.MimicRabbit;
import com.mimicrabbit.registry.ModEntities;
import com.mimicrabbit.registry.ModItems;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos del bus del mod (lado común: cliente y servidor). */
@Mod.EventBusSubscriber(modid = MimicRabbitMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEvents {

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MIMIC_RABBIT.get(), MimicRabbit.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.MIMIC_RABBIT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MimicRabbit::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.MIMIC_RABBIT_SPAWN_EGG.get());
        }
    }

    private ModEvents() {
    }
}
