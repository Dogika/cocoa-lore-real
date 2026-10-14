package com.frequencygroups.mod.mixin.minecraft;

import com.frequencygroups.mod.registry.ModAttachments;
import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public class VillagersFearEffectMixin {

    @Inject(
            method = "isHostile",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fearPlayersWithModdedEffect(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && !player.getData(ModAttachments.HAS_KILLED_VILLAGER)) cir.setReturnValue(false);
    }

    @ModifyReceiver(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"),
            remap = false
    )
    private static ImmutableMap.Builder<EntityType<?>, Float> addPlayerToSquaredDistancesForDangerMap(ImmutableMap.Builder<EntityType<?>, Float> instance) {
        instance.put(EntityType.PLAYER, 24.0F);
        return instance;
    }
}