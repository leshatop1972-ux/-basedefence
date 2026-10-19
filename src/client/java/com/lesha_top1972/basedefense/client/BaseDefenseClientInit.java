package com.lesha_top1972.basedefense.client;

import com.lesha_top1972.basedefense.ModEntities;
import com.lesha_top1972.basedefense.entity.AbstractBruteEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.ResourceLocation;

public class BaseDefenseClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.IRON_ZOMBIE, IronZombieRenderer::new);

        // громилы
        EntityModelLayerRegistry.registerModelLayer(BruteModel.LAYER, BruteModel::createBodyLayer);
        ResourceLocation tex1 = ResourceLocation.fromNamespaceAndPath("basedefense", "textures/entity/brute_1.png");
        ResourceLocation tex2 = ResourceLocation.fromNamespaceAndPath("basedefense", "textures/entity/brute_2.png");
        EntityRendererRegistry.<AbstractBruteEntity>register(ModEntities.BRUTE_1, ctx -> new BruteRenderer(ctx, tex1));
        EntityRendererRegistry.<AbstractBruteEntity>register(ModEntities.BRUTE_2, ctx -> new BruteRenderer(ctx, tex2));

        // прыгун
        EntityModelLayerRegistry.registerModelLayer(LeaperModel.LAYER, LeaperModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.LEAPER, LeaperRenderer::new);
    }
}
