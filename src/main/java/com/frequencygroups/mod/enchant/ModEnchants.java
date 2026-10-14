package com.frequencygroups.mod.enchant;

import com.frequencygroups.mod.FrequencyGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchants {

    public static final ResourceKey<Enchantment> BARRIER = createKey("barrier");
    public static final ResourceKey<Enchantment> BLEEDING = createKey("bleeding");
    public static final ResourceKey<Enchantment> DULLNESS = createKey("dullness");
    public static final ResourceKey<Enchantment> HOT_FOOTED = createKey("hot_footed");
    public static final ResourceKey<Enchantment> FREEZING = createKey("freezing");
    public static final ResourceKey<Enchantment> SPREADSHOT = createKey("spreadshot");

    private static ResourceKey<Enchantment> createKey(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, name));
    }
}