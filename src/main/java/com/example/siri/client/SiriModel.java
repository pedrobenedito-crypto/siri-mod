package com.example.siri.client;

import com.example.siri.SiriMod;
import com.example.siri.entity.SiriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SiriModel extends EntityModel<SiriEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(SiriMod.MODID, "siri"), "main");

    private final ModelPart root;
    private final ModelPart body, leftClaw, rightClaw, leftEye, rightEye;
    private final ModelPart[] rightLegs = new ModelPart[4];
    private final ModelPart[] leftLegs = new ModelPart[4];

    public SiriModel(ModelPart root) {
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
                CubeListBuilder.create().texOffs(0, 0).addBox(-5f, -3f, -3.5f, 10, 3, 7),
                PartPose.offset(0f, 21f, 0f));

        CubeListBuilder claw = CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1.5f, -1.5f, -4f, 3, 3, 4)
                .texOffs(20, 12).addBox(-1f, -1.5f, -7f, 2, 1, 3)
                .addBox(-1f, 0.5f, -7f, 2, 1, 3);
        body.addOrReplaceChild("right_claw", claw, PartPose.offsetAndRotation(6.5f, -1.5f, -3f, 0f, -0.3f, 0f));
        body.addOrReplaceChild("left_claw", claw, PartPose.offsetAndRotation(-6.5f, -1.5f, -3f, 0f, 0.3f, 0f));

        CubeListBuilder eye = CubeListBuilder.create().texOffs(0, 20).addBox(-1f, -2f, -1f, 2, 2, 2);
        body.addOrReplaceChild("right_eye", eye, PartPose.offset(2f, -3f, -2.5f));
        body.addOrReplaceChild("left_eye", eye, PartPose.offset(-2f, -3f, -2.5f));

        float[] zs = {-2.4f, -0.8f, 0.8f, 2.4f};
        for (int i = 0; i < 4; i++) {
            body.addOrReplaceChild("right_leg_" + i,
                    CubeListBuilder.create().texOffs(0, 26).addBox(0f, -0.5f, -0.5f, 4, 1, 1),
                    PartPose.offsetAndRotation(4.5f, -0.5f, zs[i], 0f, 0f, 0.8f));
            body.addOrReplaceChild("left_leg_" + i,
                    CubeListBuilder.create().texOffs(0, 26).addBox(-4f, -0.5f, -0.5f, 4, 1, 1),
                    PartPose.offsetAndRotation(-4.5f, -0.5f, zs[i], 0f, 0f, -0.8f));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SiriEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partial = ageInTicks - entity.tickCount;
        float p = entity.getBurrowProgress(partial);
        boolean transition = p > 0.0f && p < 1.0f;

        // corpo treme enquanto se enterra / sai da areia
        body.x = transition ? Mth.sin(ageInTicks * 2.5f) * 0.8f : 0f;

        // pernas
        for (int i = 0; i < 4; i++) {
            float phase = i * 0.9f;
            float swingR = Mth.cos(limbSwing * 1.4f + phase) * limbSwingAmount * 0.7f;
            float liftR = Math.max(0f, Mth.sin(limbSwing * 1.4f + phase)) * limbSwingAmount * 0.5f;
            float swingL = Mth.cos(limbSwing * 1.4f + phase + Mth.PI) * limbSwingAmount * 0.7f;
            float liftL = Math.max(0f, Mth.sin(limbSwing * 1.4f + phase + Mth.PI)) * limbSwingAmount * 0.5f;
            float yawBase = (i - 1.5f) * 0.15f;

            rightLegs[i].zRot = Mth.lerp(p, 0.8f - liftR, 1.3f);
            rightLegs[i].yRot = yawBase + swingR;
            leftLegs[i].zRot = -Mth.lerp(p, 0.8f - liftL, 1.3f);
            leftLegs[i].yRot = -yawBase + swingL;
        }

        // garras: balancam parado, levantam ao se enterrar
        float idle = Mth.sin(ageInTicks * 0.09f) * 0.06f;
        float walk = Mth.cos(limbSwing * 0.7f) * limbSwingAmount * 0.4f;
        rightClaw.xRot = Mth.lerp(p, idle + walk, -0.9f);
        leftClaw.xRot = Mth.lerp(p, -idle - walk, -0.9f);
        rightClaw.yRot = -0.3f + Mth.sin(ageInTicks * 0.07f) * 0.05f;
        leftClaw.yRot = 0.3f - Mth.sin(ageInTicks * 0.07f) * 0.05f;

        // olhinhos
        float bob = Mth.sin(ageInTicks * 0.15f) * 0.3f;
        rightEye.y = -3f + bob;
        leftEye.y = -3f - bob;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(poseStack, buffer, light, overlay, r, g, b, a);
    }
}
