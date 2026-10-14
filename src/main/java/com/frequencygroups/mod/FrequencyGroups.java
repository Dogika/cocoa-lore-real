package com.frequencygroups.mod;

import com.frequencygroups.mod.effect.ModEffects;
import com.frequencygroups.mod.gui.ModMenuTypes;
import com.frequencygroups.mod.item.ModItems;
import com.frequencygroups.mod.registry.ModAttachments;
import com.frequencygroups.mod.registry.ModComponents;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(FrequencyGroups.MODID)
public class FrequencyGroups {

    public static final String MODID = "cl";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FrequencyGroups(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModMenuTypes.register(modEventBus);


    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
