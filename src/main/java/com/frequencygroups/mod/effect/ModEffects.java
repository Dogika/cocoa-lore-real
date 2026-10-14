package com.frequencygroups.mod.effect;

import com.frequencygroups.mod.FrequencyGroups;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, FrequencyGroups.MODID);

    public static final Holder<MobEffect> HERO_OF_THE_PILLAGE = EFFECTS.register("hero_of_the_pillage", () -> new MobEffect(
            MobEffectCategory.HARMFUL, 0xbb00bb
    ) {});
    public static final Holder<MobEffect> GOLD_FEVER = EFFECTS.register("gold_fever", () -> new MobEffect(
            MobEffectCategory.HARMFUL, 0xffd700
    ) {});
    public static final Holder<MobEffect> EMERALD_FEVER = EFFECTS.register("emerald_fever", () -> new MobEffect(
            MobEffectCategory.HARMFUL, 0x50c878
    ) {});
    public static final Holder<MobEffect> GLUED = EFFECTS.register("glued", GluedEffect::new);

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}