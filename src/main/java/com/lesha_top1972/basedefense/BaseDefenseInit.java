package com.lesha_top1972.basedefense;

import com.lesha_top1972.basedefense.effect.DisarmHandler;
import com.lesha_top1972.basedefense.effect.ModEffects;
import net.fabricmc.api.ModInitializer;

public class BaseDefenseInit implements ModInitializer {
    public static final String MOD_ID = "basedefense";

    @Override
    public void onInitialize() {
        ModEffects.register();
        ModEntities.register();
        DisarmHandler.register();
    }
}
