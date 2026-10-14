package com.frequencygroups.mod.event;

import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.effect.ModEffects;
import com.frequencygroups.mod.gui.EvokerMenu;
import com.frequencygroups.mod.item.ModItems;
import com.frequencygroups.mod.registry.ModAttachments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

@EventBusSubscriber(modid = FrequencyGroups.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onEvokerUse(EvokerUseEvent event) {
        var player = event.getPlayer();
        var level = event.getLevel();
        var entity = event.getEntity();

        if (!level.isClientSide()) {
            player.openMenu(
                    new SimpleMenuProvider((syncId, playerInventory, playerEntity) ->
                            new EvokerMenu(
                                    syncId,
                                    playerInventory,
                                    ContainerLevelAccess.create(level, entity.blockPosition())
                            ), entity.getDisplayName()
                    )
            );
        }

        event.setCancellationResult(InteractionResult.PASS);
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();
        Level level = entity.level();

        if (!level.isClientSide()) {

            boolean hasGoldFever = entity.hasEffect(ModEffects.GOLD_FEVER);
            boolean hasEmeraldFever = entity.hasEffect(ModEffects.EMERALD_FEVER);

            boolean isGold = isGold(stack);
            boolean isEmerald = isEmerald(stack);

            if ((hasGoldFever && isGold) || (hasEmeraldFever && isEmerald)) {

                Vec3 pos = entity.position();

                level.explode(
                        null,
                        level.damageSources().badRespawnPointExplosion(pos),
                        null,
                        pos.x, pos.y, pos.z,
                        5.0F,
                        true,
                        Level.ExplosionInteraction.BLOCK
                );
            }

            if (isGold) {
                entity.addEffect(new MobEffectInstance(ModEffects.GOLD_FEVER, 1200, 0, false, false, true));
            }
        }
    }

    private static boolean isEmerald(ItemStack stack) {
        return stack.is(ModItems.EMERALD_CARROT.get()) ||
                stack.is(ModItems.EMERALD_APPLE.get()) ||
                stack.is(ModItems.ENCHANTED_EMERALD_APPLE.get());
    }

    private static boolean isGold(ItemStack stack) {
        return stack.is(Items.GOLDEN_CARROT) ||
                stack.is(Items.GOLDEN_APPLE) ||
                stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    @SubscribeEvent
    public static void onMobInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();

        if (entity instanceof Villager villager) {
            if (player.getData(ModAttachments.HAS_KILLED_VILLAGER)) {
                ItemStack itemStack = player.getItemInHand(event.getHand());
                if (!itemStack.is(Items.VILLAGER_SPAWN_EGG)
                    && villager.isAlive()
                    && !villager.isTrading()
                    && !villager.isSleeping()
                    && !player.isSecondaryUseActive()
                ) {
                    villager.setUnhappyCounter(40);
                    if (!event.getLevel().isClientSide) {
                        villager.makeSound(SoundEvents.VILLAGER_NO);
                    }

                    event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
                    event.setCanceled(true);
                }
            }
        }

        if (entity instanceof Evoker evoker && (player.hasEffect(ModEffects.HERO_OF_THE_PILLAGE) || player.isCreative())) {

            EvokerUseEvent customEvent = new EvokerUseEvent(
                    player,
                    event.getLevel(),
                    event.getHand(),
                    evoker,
                    null
            );

            NeoForge.EVENT_BUS.post(customEvent);

            if (customEvent.isCanceled()) {
                event.setCancellationResult(customEvent.getInteractionResult());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager
                && !villager.level().isClientSide()
                && event.getSource().getEntity() instanceof Player player
                && villager.level() instanceof ServerLevel serverLevel
        ) {
            Raid raid = serverLevel.getRaidAt(villager.blockPosition());
            if (raid != null) {
                player.setData(ModAttachments.HAS_KILLED_VILLAGER, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurtIllager(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Raider raider
                && !raider.hasData(ModAttachments.VILLAGER_PAINTED)
                && event.getSource().getEntity() instanceof LivingEntity attacker
        ) {
            attacker.removeEffect(ModEffects.HERO_OF_THE_PILLAGE);
        }
    }

    @SubscribeEvent
    public static void onIllagerTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Raider) {
            LivingEntity target = event.getNewAboutToBeSetTarget();

            if (target instanceof Player player) {
                if (player.hasEffect(ModEffects.HERO_OF_THE_PILLAGE)) {
                    event.setCanceled(true);
                }
                return;
            }

            if (target instanceof AbstractIllager illager) {
                boolean painted = illager.getData(ModAttachments.VILLAGER_PAINTED);
                if (!painted) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.level().isClientSide()) return;

            if (entity.hasEffect(ModEffects.GLUED)) {
                AABB boundingBox = entity.getBoundingBox().inflate(0.1D);
                List<LivingEntity> nearby = entity.level().getEntitiesOfClass(LivingEntity.class, boundingBox);

                for (LivingEntity target : nearby) {
                    if (target != entity && !target.hasEffect(ModEffects.GLUED)) {
                        target.addEffect(new MobEffectInstance(ModEffects.GLUED, 200, 0));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.level().isClientSide()) return;

            if (attacker.hasEffect(ModEffects.GLUED) && !target.hasEffect(ModEffects.GLUED)) {
                target.addEffect(new MobEffectInstance(ModEffects.GLUED, 200, 0));
            }

            if (target.hasEffect(ModEffects.GLUED) && !attacker.hasEffect(ModEffects.GLUED)) {
                attacker.addEffect(new MobEffectInstance(ModEffects.GLUED, 200, 0));
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();
        LivingEntity entity = event.getEntity();

        if (!entity.level().isClientSide() && instance != null) {

            if (instance.getEffect().value() == ModEffects.GLUED.value()) {
                if (instance.getDuration() <= 1) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 255, false, true));
                    entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 250, false, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        RandomSource random = entity.getRandom();
        Registry<Enchantment> enchantRegistry = entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        if (entity instanceof Pillager pillager) {
            if (pillager.getPersistentData().getBoolean("AlreadyEnchanted")) return;

            if (random.nextFloat() < 1.0f) {
                ItemStack weaponCopy = pillager.getMainHandItem().copy();

                if (weaponCopy.is(Items.CROSSBOW)) {
                    var quickCharge = enchantRegistry.getHolder(Enchantments.QUICK_CHARGE);
                    var spreadshot = enchantRegistry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("spreadshot")));

                    quickCharge.ifPresent(enchant -> weaponCopy.enchant(enchant, 1 + random.nextInt(2)));
                    spreadshot.ifPresent(enchant -> weaponCopy.enchant(enchant, 1 + random.nextInt(2)));

                    pillager.setItemSlot(EquipmentSlot.MAINHAND, weaponCopy);
                    pillager.setDropChance(EquipmentSlot.MAINHAND, 0.085F);

                    pillager.getPersistentData().putBoolean("AlreadyEnchanted", true);
                }
            }
        }

        if (entity instanceof Vindicator vindicator) {
            if (vindicator.getPersistentData().getBoolean("AlreadyEnchanted")) return;

            if (random.nextFloat() < 1.0f) {
                ItemStack weaponCopy = vindicator.getMainHandItem().copy();

                if (weaponCopy.is(Items.IRON_AXE)) {
                    var bleeding = enchantRegistry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, FrequencyGroups.id("bleeding")));

                    bleeding.ifPresent(enchant -> weaponCopy.enchant(enchant, 3 + random.nextInt(3)));

                    vindicator.setItemSlot(EquipmentSlot.MAINHAND, weaponCopy);
                    vindicator.setDropChance(EquipmentSlot.MAINHAND, 0.085F);

                    vindicator.getPersistentData().putBoolean("AlreadyEnchanted", true);
                }
            }
        }
    }
}