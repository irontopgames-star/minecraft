package com.mimicrabbit.client;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.entity.MimicRabbit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MimicRabbitRenderer extends MobRenderer<MimicRabbit, MimicRabbitModel> {
    private static final ResourceLocation RABBIT_TEXTURE =
            new ResourceLocation(MimicRabbitMod.MOD_ID, "textures/entity/mimic_rabbit.png");
    private static final ResourceLocation MONSTER_TEXTURE =
            new ResourceLocation(MimicRabbitMod.MOD_ID, "textures/entity/mimic_rabbit_monster.png");

    public MimicRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new MimicRabbitModel(context.bakeLayer(MimicRabbitModel.LAYER_LOCATION)), 0.3F);
        this.addLayer(new MimicRabbitEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MimicRabbit entity) {
        return entity.shouldRenderMonsterModel() ? MONSTER_TEXTURE : RABBIT_TEXTURE;
    }

    /** Hace temblar al conejo durante la transformación (misma sacudida que usa el juego al congelarse). */
    @Override
    protected boolean isShaking(MimicRabbit entity) {
        return super.isShaking(entity) || entity.isTransforming();
    }

    /** Hinchazón del cuerpo durante la transformación + sombra según la forma. */
    @Override
    protected void scale(MimicRabbit entity, PoseStack poseStack, float partialTick) {
        this.shadowRadius = entity.shouldRenderMonsterModel() ? 0.5F : 0.3F;
        if (!entity.isTransforming()) {
            return;
        }
        float t = entity.getTransformTicks() + partialTick;
        float scale;
        if (t < MimicRabbit.TRANSFORM_SWAP_TICK) {
            // el conejo se hincha y se deforma
            float k = t / MimicRabbit.TRANSFORM_SWAP_TICK;
            scale = 1.0F + 0.9F * k * k + Mth.sin(t * 1.7F) * 0.06F * k;
        } else {
            // el monstruo surge desde pequeño hasta su tamaño real con un pequeño rebote
            float k = (t - MimicRabbit.TRANSFORM_SWAP_TICK)
                    / (float) (MimicRabbit.TRANSFORM_DURATION - MimicRabbit.TRANSFORM_SWAP_TICK);
            k = Mth.clamp(k, 0.0F, 1.0F);
            float eased = 1.0F - (1.0F - k) * (1.0F - k);
            scale = Mth.lerp(eased, 0.55F, 1.0F);
        }
        poseStack.scale(scale, scale, scale);
    }
}
