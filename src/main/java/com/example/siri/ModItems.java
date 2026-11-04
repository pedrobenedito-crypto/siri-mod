package com.example.siri;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SiriMod.MODID);

    public static final RegistryObject<Item> SIRI_BUCKET = ITEMS.register("siri_bucket",
            () -> new MobBucketItem(ModEntities.SIRI, () -> Fluids.WATER,
                    () -> SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SIRI_SPAWN_EGG = ITEMS.register("siri_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SIRI, 0xD9532E, 0x2B1A12, new Item.Properties()));
}
