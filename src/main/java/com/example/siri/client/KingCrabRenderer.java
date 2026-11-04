package com.example.siri.client;

import com.example.siri.SiriMod;
import com.example.siri.entity.KingCrabEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KingCrabRenderer extends MobRenderer<KingCrabEntity, KingCrabModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SiriMod.MODID, "textures/entity/king_crab.png");

    public KingCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new KingCrabModel(context.bakeLayer(KingCrabModel.LAYER)), 0.9f);
    }

    @Override
    public ResourceLocation getTextureLocation(KingCrabEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(KingCrabEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.2f, 1.2f, 1.2f);
    }
}
