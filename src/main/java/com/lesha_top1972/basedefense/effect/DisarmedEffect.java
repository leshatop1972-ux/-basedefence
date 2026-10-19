package com.lesha_top1972.basedefense.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** «Обезоружен»: нельзя бить и стрелять/бросать. Логика в DisarmHandler. */
public class DisarmedEffect extends MobEffect {
    public DisarmedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A2B2B);
    }
}
