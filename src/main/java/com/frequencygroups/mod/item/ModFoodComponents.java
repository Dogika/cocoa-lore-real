package com.frequencygroups.mod.item;

import com.frequencygroups.mod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodComponents {

    public static final FoodProperties ENCHANTED_EMERALD_APPLE = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 6000, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F)
            .effect(() -> new MobEffectInstance(ModEffects.EMERALD_FEVER, 1200, 0, false, false, true), 0.5F)
            .alwaysEdible()
            .build();

    public static final FoodProperties EMERALD_APPLE = new FoodProperties.Builder()
            .nutrition(5)
            .saturationModifier(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 6000, 0), 0.05F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
            .effect(() -> new MobEffectInstance(ModEffects.EMERALD_FEVER, 1200, 0, false, false, true), 1.0F)
            .alwaysEdible()
            .build();

    public static final FoodProperties EMERALD_CARROT = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(1.2F)
            .effect(() -> new MobEffectInstance(ModEffects.EMERALD_FEVER, 1200, 0, false, false, true), 1.0F)
            .build();
}