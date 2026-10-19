package com.lesha_top1972.basedefense.client;

import com.lesha_top1972.basedefense.entity.LeaperEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Горбатый двуногий: торс наклонён вперёд, длинные руки с когтями, ноги из двух сегментов (бедро + голень).
 * Текстура 128x64.
 */
public class LeaperModel extends EntityModel<LeaperRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("basedefense", "leaper"), "main");

    private static final float DEG = (float) Math.PI / 180.0F;

    // позы: {наклон торса, руки (относительно торса), бедро, голень, смещение вниз (присед), голова}
    private static final float[] IDLE = {0.95F, -1.05F, -0.35F, 0.60F, 0.6F, -0.65F};
    private static final float[] WIND = {1.15F,  0.80F, -1.00F, 1.70F, 4.2F, -0.95F};  // присел, руки назад
    private static final float[] FLY  = {1.30F, -3.50F,  0.40F, 0.30F, 0.0F, -1.10F};  // вытянулся, руки вперёд
    private static final float[] LAND = {1.10F, -1.90F, -1.00F, 1.70F, 4.2F, -0.90F};  // приземление в приседе

    private final ModelPart modelRoot;
    private final ModelPart upper;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightThigh;
    private final ModelPart rightShin;
    private final ModelPart leftThigh;
    private final ModelPart leftShin;

    public LeaperModel(ModelPart root) {
        super(root);
        this.modelRoot = root;
        this.upper = root.getChild("upper");
        this.head = this.upper.getChild("head");
        this.rightArm = this.upper.getChild("right_arm");
        this.leftArm = this.upper.getChild("left_arm");
        this.rightThigh = root.getChild("right_thigh");
        this.rightShin = this.rightThigh.getChild("right_shin");
        this.leftThigh = root.getChild("left_thigh");
        this.leftShin = this.leftThigh.getChild("left_shin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // торс целиком, ось в районе бёдер
        PartDefinition upper = root.addOrReplaceChild("upper", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        upper.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                        .texOffs(24, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)) // лохмотья
                        .texOffs(48, 16).addBox(-4.5F, -13.5F, 1.0F, 9.0F, 7.0F, 5.0F),                           // горб
                PartPose.offset(0.0F, 0.0F, 0.0F));

        upper.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),   // повязка
                PartPose.offset(0.0F, -11.5F, -1.0F));

        upper.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(76, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F)
                        .texOffs(64, 34).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 3.0F, 4.0F)         // кисть
                        .texOffs(96, 34).addBox(-1.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F)        // когти
                        .addBox(-0.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                        .addBox(0.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-6.0F, -10.0F, 0.0F));

        upper.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(88, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F)
                        .texOffs(80, 34).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(96, 34).addBox(-1.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                        .addBox(-0.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                        .addBox(0.5F, 14.0F, -1.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(6.0F, -10.0F, 0.0F));

        PartDefinition rightThigh = root.addOrReplaceChild("right_thigh", CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-2.5F, 12.0F, 0.0F));
        rightThigh.addOrReplaceChild("right_shin", CubeListBuilder.create()
                        .texOffs(32, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition leftThigh = root.addOrReplaceChild("left_thigh", CubeListBuilder.create()
                        .texOffs(16, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(2.5F, 12.0F, 0.0F));
        leftThigh.addOrReplaceChild("left_shin", CubeListBuilder.create()
                        .texOffs(48, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(LeaperRenderState state) {
        super.setupAnim(state); // сброс позы

        final int w = LeaperEntity.WINDUP_TICKS;
        final int f = LeaperEntity.FLIGHT_TICKS;
        final int r = LeaperEntity.RECOVERY_TICKS;
        float t = state.leapTicks;

        float[] cur;
        if (t > 0.0F) {
            if (t <= w) {                                   // отводит руки назад, приседает
                float p = Mth.clamp(t / w, 0.0F, 1.0F);
                cur = mix(IDLE, WIND, smooth(p));
                cur[1] += Mth.sin(t * 2.2F) * 0.05F * p;    // дрожь перед прыжком
            } else if (t <= w + f) {                        // полёт
                float p = (t - w) / f;
                if (p < 0.15F) {
                    cur = mix(WIND, FLY, smooth(p / 0.15F));
                } else if (p > 0.8F) {
                    cur = mix(FLY, LAND, smooth((p - 0.8F) / 0.2F));
                } else {
                    cur = mix(FLY, FLY, 0.0F);
                }
            } else {                                        // приземление и восстановление
                float p = Mth.clamp((t - (w + f)) / r, 0.0F, 1.0F);
                cur = mix(LAND, IDLE, smooth(p));
            }
        } else {
            cur = mix(IDLE, IDLE, 0.0F);
        }

        float rArm = cur[1];
        float lArm = cur[1];
        float rThigh = cur[2];
        float lThigh = cur[2];
        float rShin = cur[3];
        float lShin = cur[3];
        float rootY = cur[4];
        float sway = 0.0F;

        if (t <= 0.0F) {
            // шаркающая горбатая походка
            float speed = Math.min(state.walkAnimationSpeed * 1.5F, 1.0F);
            float c = Mth.cos(state.walkAnimationPos * 0.6662F);
            rThigh += c * 0.9F * speed;
            lThigh -= c * 0.9F * speed;
            rShin += Math.max(0.0F, -c) * 1.0F * speed;
            lShin += Math.max(0.0F, c) * 1.0F * speed;
            rArm += -c * 0.5F * speed;
            lArm += c * 0.5F * speed;
            sway = c * 0.07F * speed;
            rootY += -Mth.abs(c) * 0.8F * speed;
            cur[0] += Mth.abs(c) * 0.05F * speed;
        }

        this.modelRoot.y = rootY;
        this.upper.xRot = cur[0] + Mth.cos(state.ageInTicks * 0.1F) * 0.015F;
        this.upper.zRot = sway;
        this.head.xRot = state.xRot * DEG + cur[5];
        this.head.yRot = state.yRot * DEG;
        this.rightArm.xRot = rArm;
        this.leftArm.xRot = lArm;
        this.rightThigh.xRot = rThigh;
        this.leftThigh.xRot = lThigh;
        this.rightShin.xRot = rShin;
        this.leftShin.xRot = lShin;

        float breathe = Mth.cos(state.ageInTicks * 0.09F) * 0.03F + 0.04F;
        this.rightArm.zRot += breathe;
        this.leftArm.zRot -= breathe;
    }

    private static float smooth(float p) {
        p = Mth.clamp(p, 0.0F, 1.0F);
        return p * p * (3.0F - 2.0F * p);
    }

    private static float[] mix(float[] a, float[] b, float t) {
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] + (b[i] - a[i]) * t;
        }
        return r;
    }
}
