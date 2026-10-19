package com.lesha_top1972.basedefense.client;

import com.lesha_top1972.basedefense.entity.LeaperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LeaperRenderer extends MobRenderer<LeaperEntity, LeaperRenderState, LeaperModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("basedefense", "textures/entity/leaper.png");

    public LeaperRenderer(EntityRendererProvider.Context context) {
        super(context, new LeaperModel(context.bakeLayer(LeaperModel.LAYER)), 0.55F);
    }

    @Override
    public LeaperRenderState createRenderState() {
        return new LeaperRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(LeaperRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(LeaperEntity entity, LeaperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        int phase = entity.getLeapPhase();
        state.leapTicks = phase > 0 ? phase + partialTick : 0.0F;
    }
}
