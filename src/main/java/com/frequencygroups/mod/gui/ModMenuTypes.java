package com.frequencygroups.mod.gui;

import com.frequencygroups.mod.FrequencyGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FrequencyGroups.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EvokerMenu>> EVOKER_MENU = MENUS.register(
            "evoker",
            () -> new MenuType<>(EvokerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}