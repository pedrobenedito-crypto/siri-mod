package com.example.siri.client;

import com.example.siri.SiriMod;
import com.example.siri.entity.KingCrabEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class KingCrabModel extends EntityModel<KingCrabEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(SiriMod.MODID, "king_crab"), "main");

    private final ModelPart root, body, leftClaw, rightClaw, leftEye, rightEye;
    private final ModelPart[] rightLegs = new ModelPart[4];
    private final ModelPart[] leftLegs = new ModelPart[4];

    public KingCrabModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.leftClaw = body.getChild("left_claw");
        this.rightClaw = body.getChild("right_claw");
        this.leftEye = body.getChild("left_eye");
        this.rightEye = body.getChild("right_eye");
        for (int i = 0; i < 4; i++) {
            rightLegs[i] = body.getChild("right_leg_" + i);
            leftLegs[i] = body.getChild("left_leg_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition body = rootDef.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8f, -6f, -6f, 16, 6, 12),
                PartPose.offset(0f, 18f, 0f));

        // espinhos no casco
        CubeListBuilder spike = CubeListBuilder.create().texOffs(0, 20).addBox(-1f, -2f, -1f, 2, 2, 2);
        float[][] spikes = {{-4, -6, -3}, {4, -6, -3}, {0, -6, 0}, {-4, -6, 3}, {4, -6, 3}};
        for (int i = 0; i < spikes.length; i++) {
            body.addOrReplaceChild("spike_" + i, spike, PartPose.offset(spikes[i][0], spikes[i][1], spikes[i][2]));
        }

        CubeListBuilder eye = CubeListBuilder.create().texOffs(10, 20).addBox(-1f, -3f, -1f, 2, 3, 2);
        body.addOrReplaceChild("right_eye", eye, PartPose.offset(3f, -6f, -5.5f));
        body.addOrReplaceChild("left_eye", eye, PartPose.offset(-3f, -6f, -5.5f));

        CubeListBuilder claw = CubeListBuilder.create()
                .texOffs(0, 28).addBox(-3f, -3f, -8f, 6, 6, 8)
                .texOffs(30, 28).addBox(-2f, -3f, -14f, 4, 2, 6)
                .addBox(-2f, 1f, -14f, 4, 2, 6);
        body.addOrReplaceChild("right_claw", claw, PartPose.offsetAndRotation(11f, -3f, -5f, 0f, -0.35f, 0f));
        body.addOrReplaceChild("left_claw", claw, PartPose.offsetAndRotation(-11f, -3f, -5f, 0f, 0.35f, 0f));

        float[] zs = {-4.5f, -1.5f, 1.5f, 4.5f};
        for (int i = 0; i < 4; i++) {
            body.addOrReplaceChild("right_leg_" + i,
                    CubeListBuilder.create().texOffs(0, 44).addBox(0f, -1f, -1f, 8, 2, 2),
                    PartPose.offsetAndRotation(8f, -1f, zs[i], 0f, 0f, 0.75f));
            body.addOrReplaceChild("left_leg_" + i,
                    CubeListBuilder.create().texOffs(0, 44).addBox(-8f, -1f, -1f, 8, 2, 2),
                    PartPose.offsetAndRotation(-8f, -1f, zs[i], 0f, 0f, -0.75f));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(KingCrabEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partial = ageInTicks - entity.tickCount;
        float c = entity.getChargeProgress(partial);
        boolean angry = entity.isAggressive();

        body.x = Mth.sin(ageInTicks * 3.0f) * 0.6f * c; // treme enquanto carrega a pancada

        for (int i = 0; i < 4; i++) {
            float phase = i * 0.9f;
            float swingR = Mth.cos(limbSwing * 1.2f + phase) * limbSwingAmount * 0.6f;
            float liftR = Math.max(0f, Mth.sin(limbSwing * 1.2f + phase)) * limbSwingAmount * 0.4f;
            float swingL = Mth.cos(limbSwing * 1.2f + phase + Mth.PI) * limbSwingAmount * 0.6f;
            float liftL = Math.max(0f, Mth.sin(limbSwing * 1.2f + phase + Mth.PI)) * limbSwingAmount * 0.4f;
            float yawBase = (i - 1.5f) * 0.12f;
            rightLegs[i].zRot = 0.75f - liftR;
            rightLegs[i].yRot = yawBase + swingR;
            leftLegs[i].zRot = -(0.75f - liftL);
            leftLegs[i].yRot = -yawBase + swingL;
        }

        float idle = Mth.sin(ageInTicks * 0.08f) * 0.05f;
        float walk = Mth.cos(limbSwing * 0.6f) * limbSwingAmount * 0.3f;
        // bravo: garras erguidas e batendo uma na outra; carregando: garras bem acima da cabeca
        float guard = angry ? -0.7f + Mth.sin(ageInTicks * 0.5f) * 0.12f : 0f;

        rightClaw.xRot = Mth.lerp(c, guard + idle + walk, -2.3f);
        leftClaw.xRot = Mth.lerp(c, guard - idle - walk, -2.3f);
        rightClaw.yRot = -0.35f + (angry ? 0.25f : 0f);
        leftClaw.yRot = 0.35f - (angry ? 0.25f : 0f);

        float bob = Mth.sin(ageInTicks * 0.15f) * 0.4f;
        rightEye.y = -6f + bob;
        leftEye.y = -6f - bob;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(poseStack, buffer, light, overlay, r, g, b, a);
    }
}
