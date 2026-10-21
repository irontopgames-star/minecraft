package com.mimicrabbit.client;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registro de renderers y capas de modelo (solo cliente). */
@Mod.EventBusSubscriber(modid = MimicRabbitMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MimicRabbitModel.LAYER_LOCATION, MimicRabbitModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MIMIC_RABBIT.get(), MimicRabbitRenderer::new);
    }

    private ClientModEvents() {
    }
}
