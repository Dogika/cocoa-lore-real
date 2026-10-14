package com.frequencygroups.mod.entity;

import com.frequencygroups.mod.registry.ModAttachments;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.SpellcasterIllager;

import java.util.List;

public class EvokerCleansePaintGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {

    private final Evoker evoker;
    private AbstractIllager cleanseTarget;

    public EvokerCleansePaintGoal(Evoker evoker) {
        evoker.super();
        this.evoker = evoker;
    }

    @Override
    public boolean canUse() {
        if (this.evoker.getTarget() != null) {
            return false;
        }

        if (this.evoker.getData(ModAttachments.VILLAGER_PAINTED)) {
            this.cleanseTarget = null;
            return true;
        }

        List<AbstractIllager> nearby = this.evoker.level().getEntitiesOfClass(
                AbstractIllager.class,
                this.evoker.getBoundingBox().inflate(16.0D, 4.0D, 16.0D),
                illager -> illager != this.evoker && illager.getData(ModAttachments.VILLAGER_PAINTED)
        );

        this.cleanseTarget = nearby.isEmpty() ? null : nearby.get(0);
        return this.cleanseTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        AbstractIllager subject = this.cleanseTarget != null ? this.cleanseTarget : this.evoker;
        return subject.isAlive() && subject.getData(ModAttachments.VILLAGER_PAINTED);
    }

    @Override
    public void stop() {
        super.stop();
        this.cleanseTarget = null;
    }

    @Override
    protected int getCastingTime() {
        return 40;
    }

    @Override
    protected int getCastingInterval() {
        return 100;
    }

    @Override
    protected SoundEvent getSpellPrepareSound() {
        return SoundEvents.EVOKER_PREPARE_WOLOLO;
    }

    @Override
    protected SpellcasterIllager.IllagerSpell getSpell() {
        return SpellcasterIllager.IllagerSpell.WOLOLO;
    }

    @Override
    protected void performSpellCasting() {
        AbstractIllager subject = this.cleanseTarget != null ? this.cleanseTarget : this.evoker;
        subject.setData(ModAttachments.VILLAGER_PAINTED, false);
    }
}