package com.frequencygroups.mod.event;

import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.registry.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = FrequencyGroups.MODID)
public class CombatEvents {
    static int getEnchLevel(ItemStack stack, LivingEntity entity, String name) {
        if (stack.isEmpty()) return 0;
        var registry = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Enchantment ench = registry.get(ResourceLocation.fromNamespaceAndPath(FrequencyGroups.MODID, name));
        if (ench == null) return 0;
        Holder<Enchantment> holder = registry.wrapAsHolder(ench);
        return stack.getEnchantmentLevel(holder);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            LivingEntity victim = event.getEntity();
            int fwLevel = getEnchLevel(victim.getItemBySlot(EquipmentSlot.FEET), victim, "firewalker");
            if (fwLevel > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, fwLevel - 1)); // 5 seconds of speed
                victim.setData(ModAttachments.FIREWALKER_TICKS, 100);
            }

            int barrierLevel = 0;
            for (ItemStack armor : victim.getArmorSlots()) {
                barrierLevel += getEnchLevel(armor, victim, "barrier");
            }
            if (barrierLevel > 0 && victim.getRandom().nextFloat() < (barrierLevel * 0.083f)) {
                double knockbackStrength = 0.2 + (barrierLevel * 0.075);

                attacker.knockback(knockbackStrength, victim.getX() - attacker.getX(), victim.getZ() - attacker.getZ());
            }

            ItemStack weapon = attacker.getMainHandItem();

            int dullnessLevel = getEnchLevel(weapon, attacker, "dullness");
            if (dullnessLevel > 0) {
                int randomMod = attacker.getRandom().nextInt(10) - 3; // Rolls -3 to 6
                event.setNewDamage(Math.max(0, event.getNewDamage() + randomMod));

                if (randomMod < 0) {
                    attacker.hurt(attacker.damageSources().magic(), Math.abs(randomMod));
                }
            }

            int bleedLevel = getEnchLevel(weapon, attacker, "bleeding");
            if (bleedLevel > 0) {
                event.setNewDamage(event.getNewDamage() * 0.7f);

                int bleedBuildup = bleedLevel*14+12;

                if (attacker instanceof Player player) {
                    bleedBuildup = (int) (bleedBuildup * player.getAttackStrengthScale(0.0f));
                }

                int stacks = victim.getData(ModAttachments.BLEEDING_STACKS) + bleedBuildup;
                if (stacks >= 200) {
                    victim.setData(ModAttachments.BLEEDING_STACKS, 0);
                    victim.hurt(victim.damageSources().magic(), victim.getMaxHealth() * 0.06f * bleedLevel);

                    if (victim.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 1.2f);

                        for (int i = 0; i < 360; i += (int) (23*serverLevel.random.nextFloat()+23)) {
                            double rad = Math.toRadians(i);
                            for (int j = 0; j < 3; j++) {
                                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                        victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
                                        0, Math.cos(rad) * 0.3 * (j + 1), 0.1, Math.sin(rad) * 0.3 * (j + 1), 1.0);
                            }
                        }
                    }
                } else {
                    victim.setData(ModAttachments.BLEEDING_STACKS, stacks);
                }
            }
        }
    }
}
