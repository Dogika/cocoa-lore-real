package com.frequencygroups.mod.event;
import com.frequencygroups.mod.FrequencyGroups;
import com.frequencygroups.mod.registry.ModAttachments;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = FrequencyGroups.MODID)
public class TickEvents {

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            Level level = entity.level();
            if (level.isClientSide) return;

            int fwTicks = entity.getData(ModAttachments.FIREWALKER_TICKS);
            if (fwTicks > 0) {
                entity.setData(ModAttachments.FIREWALKER_TICKS, fwTicks - 1);
                var pos = entity.blockPosition();
                if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                    level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                }
            }

            int bleedStacks = entity.getData(ModAttachments.BLEEDING_STACKS);
            if (bleedStacks > 0) {
                entity.setData(ModAttachments.BLEEDING_STACKS, bleedStacks - 1);
            }
        }
    }
}