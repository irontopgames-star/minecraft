package com.mimicrabbit.registry;

import com.mimicrabbit.MimicRabbitMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MimicRabbitMod.MOD_ID);

    /** Huevo de aparición: solo para poder probar el mod fácilmente. */
    public static final RegistryObject<Item> MIMIC_RABBIT_SPAWN_EGG = ITEMS.register(
            "mimic_rabbit_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MIMIC_RABBIT, 0x8B6B4A, 0x7A0A0A, new Item.Properties()));

    private ModItems() {
    }
}
