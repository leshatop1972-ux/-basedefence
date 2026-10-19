package com.lesha_top1972.basedefense.effect;

import com.lesha_top1972.basedefense.BaseDefenseInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** «Придавлен»: скорость движения -80%. */
public class CrushedEffect extends MobEffect {
    public CrushedEffect() {
        super(MobEffectCategory.HARMFUL, 0x6B4A2B);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(BaseDefenseInit.MOD_ID, "effect.crushed"),
                -0.8D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
