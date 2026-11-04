package com.example.siri;

import com.example.siri.entity.SiriEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SiriMod.MODID);

    public static final RegistryObject<EntityType<SiriEntity>> SIRI = ENTITIES.register("siri",
            () -> EntityType.Builder.of(SiriEntity::new, MobCategory.CREATURE)
                    .sized(0.7f, 0.5f)
                    .clientTrackingRange(8)
                    .build(new ResourceLocation(SiriMod.MODID, "siri").toString()));
}
