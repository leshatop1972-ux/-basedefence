package com.lesha_top1972.basedefense.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class LeaperRenderState extends LivingEntityRenderState {
    /** 0 = не прыгает; иначе номер тика замаха/полёта/приземления (с долей тика). */
    public float leapTicks;
}
