package com.frequencygroups.mod.client;

import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.gui.EvokerScreen;
import com.frequencygroups.mod.gui.ModMenuTypes;
import com.frequencygroups.mod.registry.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = FrequencyGroups.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EVOKER_MENU.get(), EvokerScreen::new);
    }

    private static final ResourceLocation BAR_BG = ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, "textures/gui/bleed_bar_bg.png");
    private static final ResourceLocation BAR_FG = ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, "textures/gui/bleed_bar_fg.png");

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {

        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, "bleed_buildup_bar"), (guiGraphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            int currentBuildup = player.getData(ModAttachments.BLEEDING_STACKS);
            int maxBuildup = 200;

            if (currentBuildup > 0) {
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();

                int texWidth = 100;
                int texHeight = 5;

                int x = (screenWidth / 2) - (texWidth / 2);
                int y = screenHeight - 65;

                guiGraphics.blit(BAR_BG, x, y, 0, 0, texWidth, texHeight, texWidth, texHeight);

                float fillRatio = Math.min((float) currentBuildup / maxBuildup, 1.0f);
                int fillWidth = (int) (texWidth * fillRatio);

                if (fillWidth > 0) {
                        guiGraphics.blit(BAR_FG, x, y, 0, 0, fillWidth, texHeight, texWidth, texHeight);
                }
            }
        });
    }
}