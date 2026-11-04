package com.example.siri.client;

import com.example.siri.SiriMod;
import com.example.siri.entity.SiriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SiriRenderer extends MobRenderer<SiriEntity, SiriModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SiriMod.MODID, "textures/entity/siri.png");

    public SiriRenderer(EntityRendererProvider.Context context) {
        super(context, new SiriModel(context.bakeLayer(SiriModel.LAYER)), 0.35f);
    }

    @Override
    public ResourceLocation getTextureLocation(SiriEntity entity) {
        return TEXTURE;
    }

    /** Afunda o modelo na areia conforme o progresso do "burrow". (Eixo Y invertido aqui: + = para baixo.) */
    @Override
    protected void scale(SiriEntity entity, PoseStack poseStack, float partialTick) {
        float p = entity.getBurrowProgress(partialTick);
        poseStack.translate(0.0f, 0.8f * p, 0.0f);
    }

    @Override
    public void render(SiriEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float p = entity.getBurrowProgress(partialTicks);
        if (p >= 0.99f) return;               // totalmente enterrado: nao desenha nada
        this.shadowRadius = 0.35f * (1.0f - p); // sombra some junto com ele
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected boolean shouldShowName(SiriEntity entity) {
        return !entity.isBurrowed() && super.shouldShowName(entity);
    }
}
