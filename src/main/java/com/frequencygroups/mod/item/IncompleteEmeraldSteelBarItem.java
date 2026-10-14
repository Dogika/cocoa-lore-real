package com.frequencygroups.mod.item;

import com.frequencygroups.mod.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class IncompleteEmeraldSteelBarItem extends Item {

    public static final int REQUIRED_HITS = 50;

    public IncompleteEmeraldSteelBarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int hits = stack.getOrDefault(ModComponents.HITS.get(), 0);
        tooltip.add(Component.translatable("item.experienceforging.incomplete_emerald_steel_bar.progress", hits, REQUIRED_HITS).withStyle(ChatFormatting.GRAY));
    }
}