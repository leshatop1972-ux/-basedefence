package com.lesha_top1972.basedefense.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class Brute1Entity extends AbstractBruteEntity {

    public Brute1Entity(EntityType<? extends AbstractBruteEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createBrute1Attributes() {
        // здоровье 40 (у зомби 20, x2)
        // урон 11.2: полный железный комплект оставляет игроку ~7 урона = 35% от 20 хп
        return bruteAttributes(40.0D, 11.2D);
    }

    @Override
    protected float maxBreakHardness() {
        return 4.9F; // всё, что слабее железного блока (твёрдость 5)
    }

    @Override
    protected SoundEvent smashSound() {
        return SoundEvents.MACE_SMASH_GROUND;
    }
}
