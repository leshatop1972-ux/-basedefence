package com.lesha_top1972.basedefense.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Подходит к цели; как только цель ближе 9 блоков и всё готово, начинает замах и прыжок. */
public class LeaperAttackGoal extends Goal {
    private final LeaperEntity leaper;
    private int repathDelay;

    public LeaperAttackGoal(LeaperEntity leaper) {
        this.leaper = leaper;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = leaper.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || leaper.isLeaping();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        leaper.setAggressive(true);
        repathDelay = 0;
    }

    @Override
    public void stop() {
        leaper.setAggressive(false);
        if (!leaper.isLeaping()) {
            leaper.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = leaper.getTarget();
        if (leaper.isLeaping()) {
            leaper.getNavigation().stop(); // замах, полёт и восстановление ведёт сама сущность
            return;
        }
        if (target == null) {
            return;
        }
        leaper.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (leaper.canStartLeap(target)) {
            leaper.startLeap(target);
            return;
        }

        // пока не готов прыгать: подходит, вплотную не лезет
        if (leaper.distanceToSqr(target) > 4.0D) {
            if (--repathDelay <= 0) {
                repathDelay = 4 + leaper.getRandom().nextInt(7);
                leaper.getNavigation().moveTo(target, 1.0D);
            }
        } else {
            leaper.getNavigation().stop();
        }
    }
}
