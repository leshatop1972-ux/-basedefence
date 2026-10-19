package com.lesha_top1972.basedefense.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Общая логика громил: булава, замах 1 с, удар раз в 5 с, разрушение блоков 3x3 на глубину 2.
 * Различия между Громилой 1 и 2 (хп, урон, прочность ломаемых блоков, звук) задаются в наследниках.
 */
public abstract class AbstractBruteEntity extends Monster {

    // ================= НАСТРОЙКИ (можно менять) =================
    public static final int WINDUP_TICKS = 20;            // замах: 1 с
    public static final int RECOVERY_TICKS = 12;          // «отдышка» после удара (для анимации)
    public static final int ATTACK_INTERVAL_TICKS = 100;  // 5 с между ударами (от удара до удара)

    private static final double HIT_REACH_FRONT = 2.0D;   // урон: на сколько блоков вперёд от тела
    private static final double HIT_REACH_SIDE = 1.0D;    // урон: на сколько блоков в каждую сторону от тела
    private static final double TRIGGER_RANGE = 2.5D;     // с какого расстояния до цели начинает замах
    private static final double SIEGE_RANGE = 40.0D;      // на каком расстоянии от цели бьёт по преградам
    private static final int BREAK_DEPTH = 2;             // сколько слоёв блоков ломает вперёд (каждый 3x3)
    private static final boolean DROP_BROKEN_BLOCKS = true; // выпадают ли сломанные блоки
    // ============================================================

    // 0 = не бьёт; 1..WINDUP = замах; дальше = удар и восстановление
    private static final EntityDataAccessor<Integer> DATA_SMASH_PHASE =
            SynchedEntityData.defineId(AbstractBruteEntity.class, EntityDataSerializers.INT);

    private int smashCooldown = 0;
    private float lockedYaw = 0.0F;

    protected AbstractBruteEntity(EntityType<? extends AbstractBruteEntity> type, Level level) {
        super(type, level);
        this.refreshDimensions(); // применить размер из атрибута scale
    }

    /** Блоки с твёрдостью выше этого значения громила не ломает. */
    protected abstract float maxBreakHardness();

    /** Звук удара булавой по земле. */
    protected abstract SoundEvent smashSound();

    protected static AttributeSupplier.Builder bruteAttributes(double health, double damage) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)   // зомби 0.23 -> +30%
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.SCALE, 1.385D);          // 1.95 * 1.385 = 2.7 блока (1.5 роста игрока)
    }

    // ---------------------- ИИ ----------------------
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BruteAttackGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SMASH_PHASE, 0);
    }

    public int getSmashPhase() {
        return this.entityData.get(DATA_SMASH_PHASE);
    }

    private void setSmashPhase(int phase) {
        this.entityData.set(DATA_SMASH_PHASE, phase);
    }

    public boolean isSmashing() {
        return getSmashPhase() > 0;
    }

    // ---------------------- Когда бить ----------------------
    public boolean canStartSmash(LivingEntity target) {
        if (isSmashing() || smashCooldown > 0) {
            return false;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double distSqr = dx * dx + dz * dz;

        // 1) цель в зоне досягаемости булавы
        if (distSqr <= TRIGGER_RANGE * TRIGGER_RANGE && Math.abs(target.getY() - this.getY()) <= 2.5D) {
            return true;
        }
        // 2) путь к цели преграждён блоками (стена, дом): бьём по ним
        if (distSqr <= SIEGE_RANGE * SIEGE_RANGE) {
            return findObstacle(serverLevel, yawTo(dx, dz), 2.0D) != null;
        }
        return false;
    }

    public void startSmash(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        this.lockedYaw = yawTo(dx, dz);
        this.getNavigation().stop();
        setSmashPhase(1);
    }

    private static float yawTo(double dx, double dz) {
        return (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
    }

    // ---------------------- Ход удара ----------------------
    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (smashCooldown > 0) {
            smashCooldown--;
        }

        int phase = getSmashPhase();
        if (phase <= 0) {
            return;
        }

        // во время замаха и удара стоит на месте и смотрит в сторону удара
        this.getNavigation().stop();
        this.setYRot(lockedYaw);
        this.setYBodyRot(lockedYaw);
        this.setYHeadRot(lockedYaw);

        phase++;
        if (phase == WINDUP_TICKS - 4) {
            this.playSound(SoundEvents.MACE_SMASH_AIR, 1.5F, 0.8F); // свист опускающейся булавы
        }
        if (phase == WINDUP_TICKS + 1) {
            smash(serverLevel);
            smashCooldown = ATTACK_INTERVAL_TICKS - WINDUP_TICKS;
        }
        if (phase > WINDUP_TICKS + RECOVERY_TICKS) {
            phase = 0;
        }
        setSmashPhase(phase);
    }

    private void smash(ServerLevel level) {
        this.playSound(smashSound(), 2.0F, 1.0F);
        damageInFront(level);
        if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            breakInFront(level);
        }
    }

    /**
     * Урон всем живым (кроме монстров) в зоне: HIT_REACH_FRONT блоков вперёд от тела
     * и HIT_REACH_SIDE блок в каждую сторону от тела. Зона повёрнута точно по направлению удара.
     */
    private void damageInFront(ServerLevel level) {
        float yawRad = lockedYaw * ((float) Math.PI / 180.0F);
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double halfWidth = this.getBbWidth() / 2.0D;

        AABB search = this.getBoundingBox().inflate(HIT_REACH_FRONT + 2.0D, 2.0D, HIT_REACH_FRONT + 2.0D);
        for (LivingEntity victim : level.getEntitiesOfClass(
                LivingEntity.class, search,
                entity -> entity != this && entity.isAlive() && !(entity instanceof Enemy))) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double forward = dx * fx + dz * fz;
            double lateral = Math.abs(dx * fz - dz * fx);
            double victimHalf = victim.getBbWidth() / 2.0D;

            boolean inFront = forward >= -0.3D && forward <= halfWidth + HIT_REACH_FRONT + victimHalf;
            boolean inSide = lateral <= halfWidth + HIT_REACH_SIDE + victimHalf;
            boolean sameHeight = victim.getBoundingBox().maxY > this.getY() - 1.0D
                    && victim.getY() < this.getY() + this.getBbHeight();
            if (inFront && inSide && sameHeight) {
                this.doHurtTarget(level, victim);
            }
        }
    }

    /**
     * Ломает BREAK_DEPTH слоёв блоков 3x3 (3 в ширину, 3 в высоту от уровня ног).
     * Участок центрируется на первом блоке, в который упёрся громила, поэтому ломается ровно
     * по сетке, даже если он стоит чуть сбоку от стены.
     */
    private void breakInFront(ServerLevel level) {
        Direction dir = Direction.fromYRot(lockedYaw);
        Direction side = dir.getClockWise();
        int baseY = this.blockPosition().getY();

        BlockPos hit = findObstacle(level, lockedYaw, 2.6D);
        BlockPos origin = (hit != null)
                ? new BlockPos(hit.getX(), baseY, hit.getZ())
                : this.blockPosition().relative(dir);

        for (int d = 0; d < BREAK_DEPTH; d++) {
            for (int l = -1; l <= 1; l++) {
                for (int h = 0; h < 3; h++) {
                    BlockPos pos = origin.relative(dir, d).relative(side, l).above(h);
                    if (canBreak(level, pos)) {
                        level.destroyBlock(pos, DROP_BROKEN_BLOCKS, this);
                    }
                }
            }
        }
    }

    /** Первый твёрдый блок, который можно сломать, на линии удара (ноги, грудь, голова). */
    private BlockPos findObstacle(ServerLevel level, float yaw, double maxDist) {
        float yawRad = yaw * ((float) Math.PI / 180.0F);
        double fx = -Mth.sin(yawRad);
        double fz = Mth.cos(yawRad);
        double[] heights = {0.4D, 1.3D, 2.2D};
        for (double t = 0.3D; t <= maxDist; t += 0.2D) {
            for (double h : heights) {
                BlockPos pos = BlockPos.containing(
                        this.getX() + fx * t, this.getY() + h, this.getZ() + fz * t);
                BlockState state = level.getBlockState(pos);
                if (!state.getCollisionShape(level, pos).isEmpty() && canBreak(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean canBreak(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof LiquidBlock) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0.0F && hardness <= maxBreakHardness();
    }

    // ---------------------- Звуки ----------------------
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.8F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
