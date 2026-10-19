package com.lesha_top1972.basedefense.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class Brute2Entity extends AbstractBruteEntity {

    public Brute2Entity(EntityType<? extends AbstractBruteEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createBrute2Attributes() {
        // здоровье 80 (в 2 раза больше, чем у Громилы 1), урон 16.8 (в 1.5 раза больше)
        return bruteAttributes(80.0D, 16.8D);
    }

    @Override
    protected float maxBreakHardness() {
        return 50.0F; // вплоть до обсидиана включительно (бедрок и подобное не ломается)
    }

    @Override
    protected SoundEvent smashSound() {
        return SoundEvents.MACE_SMASH_GROUND_HEAVY;
    }
}
