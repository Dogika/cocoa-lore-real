package com.frequencygroups.mod.mixin.minecraft;

import com.frequencygroups.mod.effect.ModEffects;
import com.frequencygroups.mod.registry.ModAttachments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Raid.class)
public abstract class RaidMixin {

    @Shadow @Final private ServerLevel level;

    @Shadow protected abstract Predicate<ServerPlayer> validPlayer();

    @Inject(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/raid/Raid$RaidStatus;LOSS:Lnet/minecraft/world/entity/raid/Raid$RaidStatus;")
    )
    private void giveNegativeRaidEffects(CallbackInfo ci) {
        List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());
        for (ServerPlayer serverPlayer : list) {
            if (!serverPlayer.isSpectator() && serverPlayer.getData(ModAttachments.HAS_KILLED_VILLAGER)) {
                serverPlayer.displayClientMessage(Component.literal("You're getting the modded effect."), false);
                serverPlayer.addEffect(new MobEffectInstance(ModEffects.HERO_OF_THE_PILLAGE, 48000, 1, false, false, true));
            }
        }
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerBossEvent;setName(Lnet/minecraft/network/chat/Component;)V", ordinal = 5)
    )
    private void additionalConditions(ServerBossEvent instance, Component name, Operation<Void> original) {
        List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());
        for (ServerPlayer serverPlayer : list) {
            if (serverPlayer.hasEffect(ModEffects.HERO_OF_THE_PILLAGE)) {
                original.call(instance, name.copy().append("?"));
                return;
            }
        }
        original.call(instance, name);
    }

    @Inject(
            method = "playSound",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V")
    )
    private void resetPlayerKilledVillager(BlockPos pos, CallbackInfo ci, @Local(ordinal = 0) ServerPlayer serverPlayer) {
        serverPlayer.setData(ModAttachments.HAS_KILLED_VILLAGER, false);
    }
}