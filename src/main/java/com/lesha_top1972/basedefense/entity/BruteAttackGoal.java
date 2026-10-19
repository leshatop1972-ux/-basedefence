package com.lesha_top1972.basedefense.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Идёт к цели; когда пора, запускает замах (сам удар обрабатывается в AbstractBruteEntity). */
public class BruteAttackGoal extends Goal {
    private final AbstractBruteEntity brute;
    private int repathDelay;

    public BruteAttackGoal(AbstractBruteEntity brute) {
        this.brute = brute;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = brute.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        brute.setAggressive(true);
        repathDelay = 0;
    }

    @Override
    public void stop() {
        brute.setAggressive(false);
        if (!brute.isSmashing()) {
            brute.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = brute.getTarget();
        if (target == null) {
            return;
        }
        if (brute.isSmashing()) {
            brute.getNavigation().stop(); // замах и удар: стоит на месте
            return;
        }
        brute.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--repathDelay <= 0) {
            repathDelay = 4 + brute.getRandom().nextInt(7);
            brute.getNavigation().moveTo(target, 1.0D);
        }
        if (brute.canStartSmash(target)) {
            brute.startSmash(target);
        }
    }
}
