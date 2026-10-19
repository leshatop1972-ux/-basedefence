package com.lesha_top1972.basedefense.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class BruteRenderState extends LivingEntityRenderState {
    /** 0 = не бьёт; иначе номер тика замаха/удара (с долей тика). */
    public float smashTicks;
}
