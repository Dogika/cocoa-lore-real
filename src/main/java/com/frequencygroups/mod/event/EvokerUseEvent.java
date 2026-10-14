package com.frequencygroups.mod.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public class EvokerUseEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final Level level;
    private final InteractionHand hand;
    private final Entity entity;
    @Nullable
    private final EntityHitResult hitResult;

    private InteractionResult interactionResult = InteractionResult.PASS;

    public EvokerUseEvent(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        this.player = player;
        this.level = level;
        this.hand = hand;
        this.entity = entity;
        this.hitResult = hitResult;
    }

    public Player getPlayer() { return player; }
    public Level getLevel() { return level; }
    public InteractionHand getHand() { return hand; }
    public Entity getEntity() { return entity; }
    @Nullable
    public EntityHitResult getHitResult() { return hitResult; }

    public void setCancellationResult(InteractionResult result) {
        this.interactionResult = result;
        if (result != InteractionResult.PASS) {
            this.setCanceled(true);
        }
    }

    public InteractionResult getInteractionResult() {
        return interactionResult;
    }
}