package com.frequencygroups.mod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BeggarsCrossbowItem extends CrossbowItem {

    public BeggarsCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isCharged(stack)) {
            // State: Primed. Instantly fire the loaded burst!
            shootSpread(level, player, hand, stack);
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            return InteractionResultHolder.consume(stack);
        } else if (!player.getProjectile(stack).isEmpty() || player.getAbilities().instabuild) {
            // State: Uncharged. Begin loading arrows into the chamber.
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide) return;

        // How long the user has held right-click
        int timeHeld = this.getUseDuration(stack, entity) - count;

        // Base charge time (Vanilla handles Quick Charge calculations automatically here)
        float baseCharge = getChargeDuration(stack, entity);

        ChargedProjectiles charge = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        List<ItemStack> currentProjectiles = new ArrayList<>(charge.getItems());
        int arrowsLoaded = currentProjectiles.size();

        // ---------------------------------------------------------
        // EXPONENTIAL SLOWDOWN MATH
        // Arrow 1: Base time
        // Arrow 2: Base time + (Base * 1.5)
        // Arrow 3: Base time + (Base * 1.5) + (Base * 2.25)
        // ---------------------------------------------------------
        float threshold = 0;
        for (int i = 0; i <= arrowsLoaded; i++) {
            threshold += baseCharge * Math.pow(1.5, i); // Rate slows down by 50% each arrow
        }

        // If we crossed the time threshold to load the next arrow
        if (timeHeld >= threshold) {
            ItemStack ammo = entity.getProjectile(stack);
            boolean isCreative = entity instanceof Player p && p.getAbilities().instabuild;

            if (!ammo.isEmpty() || isCreative) {
                if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);

                // Add exactly 1 arrow to the loaded chamber component
                ItemStack loadedAmmo = ammo.copyWithCount(1);
                currentProjectiles.add(loadedAmmo);
                stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(currentProjectiles));

                // Deduct from inventory
                if (!isCreative) {
                    ammo.shrink(1);
                    if (ammo.isEmpty() && entity instanceof Player p) {
                        p.getInventory().removeItem(ammo);
                    }
                }

                // Rising pitch sound effect as it gets more stuffed
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.PLAYERS,
                        0.5F, 1.0F + (arrowsLoaded * 0.15F));

                // ---------------------------------------------------------
                // OVERLOAD / MISFIRE MECHANIC (More than 3 arrows)
                // ---------------------------------------------------------
                if (currentProjectiles.size() > 3) {
                    // Play a distinct heavy "thunk" sound
                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 1.0F, 0.5F);

                    shootSpread(level, entity, entity.getUsedItemHand(), stack);
                    stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
                    entity.releaseUsingItem(); // Force them to stop drawing
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;

        ChargedProjectiles charge = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

        // If they let go before overloading, prime the crossbow to be fired later
        if (!charge.isEmpty() && charge.getItems().size() <= 3) {
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(arrowStack));
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS,
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        } else if (charge.isEmpty()) {
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    /**
     * Ejects all loaded arrows simultaneously in a randomized cone spread.
     */
    private void shootSpread(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
        ChargedProjectiles charge = weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (charge.isEmpty()) return;

        List<ItemStack> ammoList = charge.getItems();
        float velocity = 3.15F;
        float inaccuracy = 14.0F; // Adjust this float for a tighter/wider cone

        for (ItemStack ammo : ammoList) {
            Projectile projectile;

            if (ammo.getItem() instanceof ArrowItem arrowItem) {
                projectile = arrowItem.createArrow(level, ammo, shooter, weapon);
            } else {
                projectile = ((ArrowItem) Items.ARROW).createArrow(level, ammo, shooter, weapon);
            }

            // Applies the "spreadshot_clone" tag so your existing ProjectileImpactEvent
            // drops the immunity frames for multi-hit damage!
            projectile.addTag("spreadshot_clone");

            // Apply 3D Scatter Cone
            float pitchOffset = (shooter.getRandom().nextFloat() - 0.5F) * inaccuracy;
            float yawOffset = (shooter.getRandom().nextFloat() - 0.5F) * inaccuracy;

            float pitch = shooter.getXRot() + pitchOffset;
            float yaw = shooter.getYRot() + yawOffset;

            projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, velocity, 1.0F);

            // Set pickup behavior
            if (projectile instanceof AbstractArrow arrow) {
                if (shooter instanceof Player p && !p.getAbilities().instabuild) {
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                } else {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            level.addFreshEntity(projectile);
        }

        // Damage the weapon by the number of arrows fired at once
        weapon.hurtAndBreak(ammoList.size(), shooter, LivingEntity.getSlotForHand(hand));
        weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

        // Play the chaotic firing sound
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Must be artificially high so the player can hold it down long enough for the exponential math
        return 72000;
    }
}