package com.mimicrabbit.client;

import com.mimicrabbit.MimicRabbitMod;
import com.mimicrabbit.entity.MimicRabbit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Un solo modelo con las dos formas (partes "rabbit" y "monster"); se muestra una u otra según el estado.
 * Ambas texturas son de 128x64 px.
 *
 * Los offsets de textura (texOffs) coinciden con tools/generate_textures.py: si cambias uno, cambia el otro.
 */
public class MimicRabbitModel extends EntityModel<MimicRabbit> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(MimicRabbitMod.MOD_ID, "mimic_rabbit"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);

    private final ModelPart root;

    // --- forma conejo
    private final ModelPart rabbit;
    private final ModelPart rHead;
    private final ModelPart rEarL;
    private final ModelPart rEarR;
    private final ModelPart rFrontL;
    private final ModelPart rFrontR;
    private final ModelPart rHaunchL;
    private final ModelPart rHaunchR;

    // --- forma monstruo
    private final ModelPart monster;
    private final ModelPart mBody;
    private final ModelPart mHead;
    private final ModelPart mJaw;
    private final ModelPart mEarL;
    private final ModelPart mEarTipL;
    private final ModelPart mEarR;
    private final ModelPart mEarTipR;
    private final ModelPart mArmL;
    private final ModelPart mForearmL;
    private final ModelPart mArmR;
    private final ModelPart mForearmR;
    private final ModelPart mLegL;
    private final ModelPart mLegR;

    public MimicRabbitModel(ModelPart root) {
        this.root = root;

        this.rabbit = root.getChild("rabbit");
        this.rHead = this.rabbit.getChild("head");
        this.rEarL = this.rHead.getChild("ear_l");
        this.rEarR = this.rHead.getChild("ear_r");
        this.rFrontL = this.rabbit.getChild("front_l");
        this.rFrontR = this.rabbit.getChild("front_r");
        this.rHaunchL = this.rabbit.getChild("haunch_l");
        this.rHaunchR = this.rabbit.getChild("haunch_r");

        this.monster = root.getChild("monster");
        this.mBody = this.monster.getChild("body");
        this.mHead = this.mBody.getChild("head");
        this.mJaw = this.mHead.getChild("jaw");
        this.mEarL = this.mHead.getChild("ear_l");
        this.mEarTipL = this.mEarL.getChild("tip");
        this.mEarR = this.mHead.getChild("ear_r");
        this.mEarTipR = this.mEarR.getChild("tip");
        this.mArmL = this.mBody.getChild("arm_l");
        this.mForearmL = this.mArmL.getChild("forearm");
        this.mArmR = this.mBody.getChild("arm_r");
        this.mForearmR = this.mArmR.getChild("forearm");
        this.mLegL = this.monster.getChild("leg_l");
        this.mLegR = this.monster.getChild("leg_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // =========================================================== FORMA NORMAL (conejo)
        PartDefinition rabbit = root.addOrReplaceChild("rabbit", CubeListBuilder.create(), PartPose.ZERO);

        rabbit.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -5.0F, -5.0F, 6.0F, 5.0F, 9.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition rHead = rabbit.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(30, 0).addBox(-2.5F, -4.0F, -4.0F, 5.0F, 4.0F, 4.0F)
                        .texOffs(48, 0).addBox(-0.5F, -2.0F, -4.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, -5.0F));
        rHead.addOrReplaceChild("ear_l",
                CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(1.5F, -3.8F, -1.0F));
        rHead.addOrReplaceChild("ear_r",
                CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-1.5F, -3.8F, -1.0F));

        rabbit.addOrReplaceChild("front_l",
                CubeListBuilder.create().texOffs(58, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(2.0F, 21.0F, -3.5F));
        rabbit.addOrReplaceChild("front_r",
                CubeListBuilder.create().texOffs(58, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-2.0F, 21.0F, -3.5F));
        rabbit.addOrReplaceChild("haunch_l",
                CubeListBuilder.create().texOffs(66, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.offset(3.0F, 20.0F, 2.0F));
        rabbit.addOrReplaceChild("haunch_r",
                CubeListBuilder.create().texOffs(66, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.offset(-3.0F, 20.0F, 2.0F));
        rabbit.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(78, 0).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 17.5F, 4.0F));

        // =========================================================== FORMA MONSTRUO
        PartDefinition monster = root.addOrReplaceChild("monster", CubeListBuilder.create(), PartPose.ZERO);

        monster.addOrReplaceChild("leg_l",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F)
                        .texOffs(36, 17).addBox(-1.5F, 12.0F, -4.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(2.5F, 11.0F, 0.0F));
        monster.addOrReplaceChild("leg_r",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F)
                        .texOffs(36, 17).addBox(-1.5F, 12.0F, -4.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(-2.5F, 11.0F, 0.0F));

        PartDefinition body = monster.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(82, 0).addBox(-3.0F, -12.0F, -2.0F, 6.0F, 12.0F, 4.0F)
                        .texOffs(48, 17).addBox(-0.5F, -11.0F, 2.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(48, 17).addBox(-0.5F, -7.0F, 2.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(48, 17).addBox(-0.5F, -3.0F, 2.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.35F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        // cráneo grande
                        .texOffs(0, 0).addBox(-6.0F, -11.0F, -5.0F, 12.0F, 7.0F, 10.0F)
                        // cuello
                        .texOffs(102, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                        // dientes superiores (cuelgan del borde inferior del cráneo)
                        .texOffs(118, 0).addBox(-5.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-3.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-1.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(0.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(2.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(4.5F, -4.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-5.5F, -4.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(4.5F, -4.0F, -1.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        // mandíbula: bisagra en la parte trasera de la cabeza
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(44, 0).addBox(-5.0F, 0.0F, -9.0F, 10.0F, 3.0F, 9.0F)
                        // dientes inferiores (apuntan hacia arriba)
                        .texOffs(118, 0).addBox(-4.5F, -2.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-2.5F, -2.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-0.5F, -2.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(1.5F, -2.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(3.5F, -2.0F, -8.5F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(-4.5F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(118, 0).addBox(3.5F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -4.0F, 4.0F));

        // orejas largas y torcidas (dos tramos cada una)
        PartDefinition earL = head.addOrReplaceChild("ear_l",
                CubeListBuilder.create().texOffs(58, 17).addBox(-1.5F, -9.0F, -0.5F, 3.0F, 9.0F, 1.0F),
                PartPose.offset(3.5F, -10.5F, 1.0F));
        earL.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(66, 17).addBox(-1.5F, -7.0F, -0.5F, 3.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition earR = head.addOrReplaceChild("ear_r",
                CubeListBuilder.create().texOffs(58, 17).addBox(-1.5F, -9.0F, -0.5F, 3.0F, 9.0F, 1.0F),
                PartPose.offset(-3.5F, -10.5F, 1.0F));
        earR.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(66, 17).addBox(-1.5F, -7.0F, -0.5F, 3.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));

        // brazos larguísimos con garras
        PartDefinition armL = body.addOrReplaceChild("arm_l",
                CubeListBuilder.create().texOffs(12, 17).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(4.5F, -11.0F, 0.0F));
        armL.addOrReplaceChild("forearm",
                CubeListBuilder.create()
                        .texOffs(24, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F)
                        .texOffs(54, 17).addBox(-1.5F, 12.0F, -1.5F, 1.0F, 5.0F, 1.0F)
                        .texOffs(54, 17).addBox(0.5F, 12.0F, -1.5F, 1.0F, 5.0F, 1.0F)
                        .texOffs(54, 17).addBox(-0.5F, 12.0F, 0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition armR = body.addOrReplaceChild("arm_r",
                CubeListBuilder.create().texOffs(12, 17).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-4.5F, -11.0F, 0.0F));
        armR.addOrReplaceChild("forearm",
                CubeListBuilder.create()
                        .texOffs(24, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F)
                        .texOffs(54, 17).addBox(-1.5F, 12.0F, -1.5F, 1.0F, 5.0F, 1.0F)
                        .texOffs(54, 17).addBox(0.5F, 12.0F, -1.5F, 1.0F, 5.0F, 1.0F)
                        .texOffs(54, 17).addBox(-0.5F, 12.0F, 0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    // ------------------------------------------------------------------ animación

    @Override
    public void setupAnim(MimicRabbit entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean showMonster = entity.shouldRenderMonsterModel();
        this.rabbit.visible = !showMonster;
        this.monster.visible = showMonster;

        float partial = ageInTicks - entity.tickCount;
        if (showMonster) {
            this.animateMonster(entity, limbSwing, limbSwingAmount, ageInTicks, partial, netHeadYaw, headPitch);
        } else {
            this.animateRabbit(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private void animateRabbit(MimicRabbit entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                               float netHeadYaw, float headPitch) {
        this.rHead.xRot = headPitch * DEG;
        this.rHead.yRot = netHeadYaw * DEG;
        this.rHead.zRot = 0.0F;
        this.rEarL.zRot = 0.12F;
        this.rEarR.zRot = -0.12F;
        this.rEarL.xRot = 0.0F;
        this.rEarR.xRot = 0.0F;

        if (!entity.onGround()) {
            // en el aire: patas traseras estiradas, delanteras recogidas
            this.rHaunchL.xRot = 0.9F;
            this.rHaunchR.xRot = 0.9F;
            this.rFrontL.xRot = -0.6F;
            this.rFrontR.xRot = -0.6F;
            this.rEarL.xRot = 0.5F;
            this.rEarR.xRot = 0.5F;
        } else {
            this.rHaunchL.xRot = 0.0F;
            this.rHaunchR.xRot = 0.0F;
            this.rFrontL.xRot = 0.0F;
            this.rFrontR.xRot = 0.0F;
            // movimiento sutil de orejas / nariz en reposo
            this.rEarL.zRot += Mth.sin(ageInTicks * 0.09F) * 0.04F;
            this.rEarR.zRot -= Mth.sin(ageInTicks * 0.09F + 1.0F) * 0.04F;
        }

        if (entity.isTransforming()) {
            // el conejo empieza a convulsionar antes de transformarse
            float k = entity.getTransformProgress(ageInTicks - entity.tickCount) * 2.0F;
            this.rHead.zRot = Mth.sin(ageInTicks * 2.3F) * 0.25F * k;
            this.rHead.xRot += Mth.sin(ageInTicks * 3.1F) * 0.2F * k;
            this.rEarL.zRot += Mth.sin(ageInTicks * 3.7F) * 0.5F * k;
            this.rEarR.zRot += Mth.cos(ageInTicks * 3.3F) * 0.5F * k;
            this.rHaunchL.xRot = Mth.sin(ageInTicks * 2.9F) * 0.5F * k;
            this.rHaunchR.xRot = Mth.cos(ageInTicks * 2.7F) * 0.5F * k;
        }
    }

    private void animateMonster(MimicRabbit entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                float partial, float netHeadYaw, float headPitch) {
        boolean aggressive = entity.isAggressive();
        float attack = entity.getAttackAnim(partial);
        float swing = limbSwing * 0.6662F;

        // Fase de aparición (segunda mitad de la transformación): 0 = recién aparecido, 1 = terminado
        float emerge = 1.0F;
        if (entity.isTransforming()) {
            emerge = Mth.clamp((entity.getTransformTicks() + partial - MimicRabbit.TRANSFORM_SWAP_TICK)
                    / (float) (MimicRabbit.TRANSFORM_DURATION - MimicRabbit.TRANSFORM_SWAP_TICK), 0.0F, 1.0F);
        }
        float hunch = Mth.lerp(emerge, -0.35F, 0.35F) + (aggressive ? 0.12F : 0.0F)
                + Mth.sin(ageInTicks * 0.12F) * 0.03F;
        this.mBody.xRot = hunch;

        // Cabeza: mira al objetivo, con inclinaciones y tics inquietantes
        this.mHead.xRot = headPitch * DEG - this.mBody.xRot * 0.6F;
        this.mHead.yRot = netHeadYaw * DEG * 0.8F;
        this.mHead.zRot = Mth.sin(ageInTicks * 0.35F) * 0.05F + Mth.sin(ageInTicks * 0.11F) * 0.12F;

        // Mandíbula: muy abierta al aparecer, luego entreabierta y castañeando al perseguir
        float jaw = 0.18F + Mth.sin(ageInTicks * 0.15F) * 0.04F;
        if (aggressive) {
            jaw = 0.5F + Mth.sin(ageInTicks * 0.9F) * 0.12F;
        }
        jaw += attack * 0.5F;
        if (entity.isTransforming()) {
            jaw = Mth.lerp(emerge, 1.25F, 0.3F);
        }
        this.mJaw.xRot = jaw;

        // Orejas asimétricas: una doblada hacia fuera, la otra rota y retorcida
        float sway = Mth.sin(ageInTicks * 0.15F) * 0.04F;
        this.mEarL.zRot = 0.25F + sway;
        this.mEarL.xRot = -0.1F;
        this.mEarTipL.zRot = 0.9F * emerge;
        this.mEarTipL.xRot = -0.3F;
        this.mEarR.zRot = -0.35F - sway;
        this.mEarR.xRot = 0.15F;
        this.mEarR.yRot = 0.2F;
        this.mEarTipR.zRot = -0.4F * emerge;
        this.mEarTipR.yRot = 0.5F;
        this.mEarTipR.xRot = 0.4F;

        // Piernas
        this.mLegL.xRot = Mth.cos(swing) * 1.3F * limbSwingAmount;
        this.mLegR.xRot = Mth.cos(swing + (float) Math.PI) * 1.3F * limbSwingAmount;

        // Brazos colgando hacia delante, balanceándose en sentido contrario a las piernas
        float armForward = aggressive ? -0.55F : -0.15F;
        this.mArmL.xRot = armForward + Mth.cos(swing + (float) Math.PI) * 0.9F * limbSwingAmount;
        this.mArmR.xRot = armForward + Mth.cos(swing) * 0.9F * limbSwingAmount;
        this.mArmL.zRot = -0.06F - Mth.sin(ageInTicks * 0.09F) * 0.03F;
        this.mArmR.zRot = 0.06F + Mth.sin(ageInTicks * 0.09F + 1.5F) * 0.03F;
        this.mForearmL.xRot = -0.6F - (aggressive ? 0.4F : 0.0F);
        this.mForearmR.xRot = -0.6F - (aggressive ? 0.4F : 0.0F);

        // Zarpazo: ambos brazos barren hacia delante al atacar
        if (attack > 0.0F) {
            float slash = Mth.sin(attack * (float) Math.PI);
            this.mArmL.xRot -= slash * 1.7F;
            this.mArmR.xRot -= slash * 1.7F;
            this.mForearmL.xRot -= slash * 0.5F;
            this.mForearmR.xRot -= slash * 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
