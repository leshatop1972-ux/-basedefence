package com.lesha_top1972.basedefense.client;

import com.lesha_top1972.basedefense.entity.AbstractBruteEntity;
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
 * Модель громилы: широкий торс, наплечники, булава в правой руке.
 * Текстура 128x64.
 */
public class BruteModel extends EntityModel<BruteRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("basedefense", "brute"), "main");

    private static final float DEG = (float) Math.PI / 180.0F;

    // позы: {наклон торса, правая рука, левая рука, наклон головы, широта стойки}
    private static final float[] IDLE   = {0.10F, -0.55F,  0.05F,  0.00F, 0.0F};
    private static final float[] RAISED = {-0.30F, -3.15F, -2.95F, -0.45F, 0.5F};  // булава над головой
    private static final float[] HIT    = {0.65F, -1.00F, -0.90F,  0.45F, 1.0F};  // удар в землю

    private final ModelPart modelRoot;
    private final ModelPart upper;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public BruteModel(ModelPart root) {
        super(root);
        this.modelRoot = root;
        this.upper = root.getChild("upper");
        this.head = this.upper.getChild("head");
        this.rightArm = this.upper.getChild("right_arm");
        this.leftArm = this.upper.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // торс целиком (наклоняется при беге и ударе), ось в районе пояса
        PartDefinition upper = root.addOrReplaceChild("upper", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        upper.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        // голова + шлем (второй слой чуть больше)
        upper.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)),
                PartPose.offset(0.0F, -11.0F, -1.0F));

        // правая рука + наплечник + булава
        upper.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(32, 16).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F)
                        .texOffs(72, 16).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 3.0F, 7.0F)
                        .texOffs(40, 34).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 8.0F, 2.0F)      // рукоять
                        .texOffs(48, 34).addBox(-3.0F, 14.0F, -3.0F, 6.0F, 6.0F, 6.0F)     // навершие
                        .texOffs(72, 34).addBox(-0.5F, 20.0F, -0.5F, 1.0F, 1.0F, 1.0F)     // шипы
                        .addBox(-4.0F, 16.5F, -0.5F, 1.0F, 1.0F, 1.0F)
                        .addBox(3.0F, 16.5F, -0.5F, 1.0F, 1.0F, 1.0F)
                        .addBox(-0.5F, 16.5F, -4.0F, 1.0F, 1.0F, 1.0F)
                        .addBox(-0.5F, 16.5F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-7.5F, -10.0F, 0.0F));

        // левая рука + наплечник
        upper.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(52, 16).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F)
                        .texOffs(100, 16).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 3.0F, 7.0F),
                PartPose.offset(7.5F, -10.0F, 0.0F));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F),
                PartPose.offset(-2.6F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(20, 34).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F),
                PartPose.offset(2.6F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BruteRenderState state) {
        super.setupAnim(state); // сброс позы

        final int w = AbstractBruteEntity.WINDUP_TICKS;
        final int end = w + AbstractBruteEntity.RECOVERY_TICKS;
        final float swingEnd = w + 3;
        final float holdEnd = w + 7;

        float s = state.smashTicks;
        float[] cur;
        float runLegR = 0.0F;
        float runLegL = 0.0F;

        if (s > 0.0F) {
            if (s <= w) {                       // медленный замах: булава вверх и назад
                float p = Mth.clamp(s / w, 0.0F, 1.0F);
                cur = mix(IDLE, RAISED, p * p * (3.0F - 2.0F * p));
                cur[1] += Mth.sin(s * 1.7F) * 0.02F * p; // лёгкая дрожь от напряжения
            } else if (s <= swingEnd) {         // быстрый удар вниз
                float p = (s - w) / 3.0F;
                cur = mix(RAISED, HIT, p * p);
            } else if (s <= holdEnd) {          // булава в земле
                cur = mix(HIT, HIT, 0.0F);
                cur[0] += Mth.sin(s * 3.0F) * 0.02F;
            } else {                            // возвращается в стойку
                float p = Mth.clamp((s - holdEnd) / (end - holdEnd), 0.0F, 1.0F);
                cur = mix(HIT, IDLE, p);
            }
        } else {
            // простая анимация бега: широкий шаг, наклон вперёд, подпрыгивание, махи руками
            cur = mix(IDLE, IDLE, 0.0F);
            float speed = Math.min(state.walkAnimationSpeed * 1.5F, 1.0F);
            float c = Mth.cos(state.walkAnimationPos * 0.6662F);
            runLegR = c * 1.3F * speed;
            runLegL = -c * 1.3F * speed;
            cur[0] += 0.28F * speed;
            cur[1] += -c * 0.45F * speed;
            cur[2] += c * 1.1F * speed;
            this.modelRoot.y = -Mth.abs(c) * 1.1F * speed;
        }

        this.upper.xRot = cur[0];
        this.rightArm.xRot = cur[1];
        this.leftArm.xRot = cur[2];
        this.head.xRot = state.xRot * DEG + cur[3] - cur[0] * 0.8F;
        this.head.yRot = state.yRot * DEG;
        this.rightLeg.xRot = runLegR - cur[4] * 0.35F;
        this.leftLeg.xRot = runLegL + cur[4] * 0.35F;

        // лёгкое дыхание руками
        float breathe = Mth.cos(state.ageInTicks * 0.09F) * 0.03F + 0.04F;
        this.rightArm.zRot += breathe;
        this.leftArm.zRot -= breathe;
    }

    private static float[] mix(float[] a, float[] b, float t) {
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] + (b[i] - a[i]) * t;
        }
        return r;
    }
}
