package com.frequencygroups.mod.client;

import com.frequencygroups.mod.FrequencyGroups;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = FrequencyGroups.MODID, value = Dist.CLIENT)
public class DamageTooltipModifier {

    private static final DecimalFormat FORMATTER = new DecimalFormat("0.##");

    @SubscribeEvent
    public static void onTooltipModified(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        double[] damages = calculateDamages(stack);
        double originalDamage = damages[0];
        double finalDamage = damages[1];
        double smiteBonus = damages[2];
        double baneBonus = damages[3];

        if (originalDamage <= 1.0) return;

        int insertIndex = -1;
        Iterator<Component> iterator = tooltip.iterator();
        int currentIndex = 0;

        while (iterator.hasNext()) {
            Component line = iterator.next();
            String rawText = line.getString();

            if (rawText.contains("Attack Damage")) {
                iterator.remove();
                if (insertIndex == -1) {
                    insertIndex = currentIndex;
                }
            } else {
                currentIndex++;
            }
        }

        if (insertIndex != -1) {
            MutableComponent newLabel = Component.literal(" ");

            if (Math.abs(originalDamage - finalDamage) > 0.01) {
                newLabel.append(Component.literal(FORMATTER.format(originalDamage))
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH));
                newLabel.append(Component.literal(" " + FORMATTER.format(finalDamage) + " ").withStyle(originalDamage < finalDamage ? ChatFormatting.GOLD : ChatFormatting.RED));

            } else {
                newLabel.append(Component.literal(FORMATTER.format(originalDamage) + " ")
                        .withStyle(ChatFormatting.DARK_GREEN));
            }

            if (smiteBonus > 0) {
                newLabel.append(Component.literal("+" + FORMATTER.format(smiteBonus) + " ")
                        .withStyle(ChatFormatting.GRAY));
            }

            if (baneBonus > 0) {
                newLabel.append(Component.literal("+" + FORMATTER.format(baneBonus) + " ")
                        .withStyle(ChatFormatting.GRAY));
            }

            newLabel.append(Component.translatable("attribute.name.generic.attack_damage")
                    .withStyle(ChatFormatting.DARK_GREEN));

            tooltip.add(insertIndex, newLabel);
        }
    }

    private static double[] calculateDamages(ItemStack stack) {
        double base = 1.0;
        double[] addValue = {0.0};
        double[] addMultipliedBase = {0.0};
        double[] addMultipliedTotal = {0.0};

        stack.forEachModifier(EquipmentSlotGroup.MAINHAND, (attributeHolder, modifier) -> {
            if (attributeHolder.is(Attributes.ATTACK_DAMAGE)) {
                switch (modifier.operation()) {
                    case ADD_VALUE -> addValue[0] += modifier.amount();
                    case ADD_MULTIPLIED_BASE -> addMultipliedBase[0] += modifier.amount();
                    case ADD_MULTIPLIED_TOTAL -> addMultipliedTotal[0] += modifier.amount();
                }
            }
        });

        double originalDamage = (base + addValue[0]) * (1.0 + addMultipliedBase[0]);
        double finalDamage = originalDamage * (1.0 + addMultipliedTotal[0]);

        double sharpnessBonus = 0;
        double smiteBonus = 0;
        double baneBonus = 0;

        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> ench = entry.getKey();
            int level = entry.getIntValue();

            if (ench.is(Enchantments.SHARPNESS)) {
                sharpnessBonus += 0.5 + (0.5 * level);
            } else if (ench.is(Enchantments.SMITE)) {
                smiteBonus += 2.5 * level;
            } else if (ench.is(Enchantments.BANE_OF_ARTHROPODS)) {
                baneBonus += 2.5 * level;
            }
        }

        finalDamage += sharpnessBonus;

        return new double[]{originalDamage, finalDamage, smiteBonus, baneBonus};
    }
}