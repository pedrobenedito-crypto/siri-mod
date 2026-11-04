package com.example.siri.client;

import com.example.siri.SiriMod;
import com.example.siri.entity.SiriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
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
        poseStack.translate(0.0f, 0.55f * p, 0.0f);
    }

    @Override
    protected boolean shouldShowName(SiriEntity entity) {
        return !entity.isBurrowed() && super.shouldShowName(entity);
    }
}
