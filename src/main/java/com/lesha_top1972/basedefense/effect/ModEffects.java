package com.lesha_top1972.basedefense.effect;

import com.lesha_top1972.basedefense.BaseDefenseInit;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {
    public static final Holder<MobEffect> CRUSHED = register("crushed", new CrushedEffect());
    public static final Holder<MobEffect> DISARMED = register("disarmed", new DisarmedEffect());

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(BaseDefenseInit.MOD_ID, name),
                effect);
    }

    /** Вызов нужен, чтобы эффекты зарегистрировались при старте. */
    public static void register() {
    }
}
