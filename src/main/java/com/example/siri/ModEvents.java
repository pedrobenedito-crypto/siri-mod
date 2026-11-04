package com.example.siri;

import com.example.siri.entity.SiriEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SiriMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SIRI.get(), SiriEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void spawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.SIRI.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SiriEntity::checkSiriSpawn,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    @SubscribeEvent
    public static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.SIRI_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SIRI_BUCKET);
        }
    }
}
