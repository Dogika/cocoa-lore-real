package com.frequencygroups.mod.effect;

import com.frequencygroups.mod.FrequencyGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class GluedEffect extends MobEffect {

    public GluedEffect() {
        super(MobEffectCategory.HARMFUL, 0x82c873);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, "glued_slowness"),
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        this.addAttributeModifier(
                Attributes.JUMP_STRENGTH,
                ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, "glued_no_jump"),
                -1.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}