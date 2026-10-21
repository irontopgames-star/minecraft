package com.mimicrabbit.client;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.entity.MimicRabbit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos rojos que brillan en la oscuridad (se dibujan a brillo máximo, como los de la araña). */
public class MimicRabbitEyesLayer extends EyesLayer<MimicRabbit, MimicRabbitModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(MimicRabbitMod.MOD_ID, "textures/entity/mimic_rabbit_monster_eyes.png"));

    public MimicRabbitEyesLayer(RenderLayerParent<MimicRabbit, MimicRabbitModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MimicRabbit entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.shouldRenderMonsterModel()) {
            super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick,
                    ageInTicks, netHeadYaw, headPitch);
        }
    }
}
