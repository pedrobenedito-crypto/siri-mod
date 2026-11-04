package com.example.siri.client;

import com.example.siri.ModEntities;
import com.example.siri.SiriMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SiriMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SIRI.get(), SiriRenderer::new);
        event.registerEntityRenderer(ModEntities.KING_CRAB.get(), KingCrabRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SiriModel.LAYER, SiriModel::createBodyLayer);
        event.registerLayerDefinition(KingCrabModel.LAYER, KingCrabModel::createBodyLayer);
    }
}
