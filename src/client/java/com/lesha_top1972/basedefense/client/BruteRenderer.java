package com.lesha_top1972.basedefense.client;

import com.lesha_top1972.basedefense.entity.AbstractBruteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BruteRenderer extends MobRenderer<AbstractBruteEntity, BruteRenderState, BruteModel> {
    private final ResourceLocation texture;

    public BruteRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new BruteModel(context.bakeLayer(BruteModel.LAYER)), 0.9F);
        this.texture = texture;
    }

    @Override
    public BruteRenderState createRenderState() {
        return new BruteRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(BruteRenderState state) {
        return texture;
    }

    @Override
    public void extractRenderState(AbstractBruteEntity entity, BruteRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        int phase = entity.getSmashPhase();
        state.smashTicks = phase > 0 ? phase + partialTick : 0.0F;
    }
}
