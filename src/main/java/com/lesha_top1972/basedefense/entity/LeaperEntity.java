package com.lesha_top1972.basedefense.entity;

import com.lesha_top1972.basedefense.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Прыгун: горбатый двуногий. Отводит руки назад (1.2 с), затем прыгает на игрока.
 * Весь цикл от начала замаха до приземления: 1.2 с + 0.9 с = 2.1 с.
 */
public class LeaperEntity extends Monster {

    // ================= НАСТРОЙКИ (можно менять) =================
    public static final int WINDUP_TICKS = 24;       // отвод рук назад: 1.2 с
    public static final int FLIGHT_TICKS = 18;       // полёт: 0.9 с  (вместе с замахом 2.1 с)
    public static final int RECOVERY_TICKS = 10;     // после приземления

    private static final double MAX_LEAP_RANGE = 9.0D;   // с какой дистанции прыгает
    private static final double MIN_APEX = 1.0D;         // высота прыжка вблизи
    private static final double MAX_APEX = 3.0D;         // высота прыжка на максимальной дистанции
    private static final double DODGE_RADIUS = 2.0D;     // чтобы увернуться, нужно быть дальше 2 блоков от точки приземления

    private static final int COOLDOWN_AFTER_HIT = 140;   // 7 с, если попал
    private static final int COOLDOWN_AFTER_MISS = 80;   // 4 с, если промахнулся
    private static final int SLOW_TICKS = 80;            // «Придавлен»: -80% скорости на 4 с
    private static final int DISARM_TICKS = 40;          // «Обезоружен»: 2 с без атак
    // ============================================================

    // 0 = не прыгает; 1..24 замах; 25..42 полёт; 43..52 восстановление
    private static final EntityDataAccessor<Integer> DATA_LEAP_PHASE =
            SynchedEntityData.defineId(LeaperEntity.class, EntityDataSerializers.INT);

    private int leapCooldown = 0;
    private float lockedYaw = 0.0F;
    private Vec3 leapStart = Vec3.ZERO;
    private Vec3 leapEnd = Vec3.ZERO;
    private double leapApex = MIN_APEX;

    public LeaperEntity(EntityType<? extends LeaperEntity> type, Level level) {
        super(type, level);
        this.refreshDimensions();
    }

    public static AttributeSupplier.Builder createLeaperAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)        // у зомби 20 (x2)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)     // 50% от 20 хп игрока без брони
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.SCALE, 1.2D);             // 1.5 * 1.2 = 1.8 блока (рост игрока)
    }

    // ---------------------- ИИ ----------------------
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeaperAttackGoal(this));
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
        builder.define(DATA_LEAP_PHASE, 0);
    }

    public int getLeapPhase() {
        return this.entityData.get(DATA_LEAP_PHASE);
    }

    private void setLeapPhase(int phase) {
        this.entityData.set(DATA_LEAP_PHASE, phase);
    }

    public boolean isLeaping() {
        return getLeapPhase() > 0;
    }

    // ---------------------- Когда прыгать ----------------------
    public boolean canStartLeap(LivingEntity target) {
        if (isLeaping() || leapCooldown > 0 || !this.onGround()) {
            return false;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz > MAX_LEAP_RANGE * MAX_LEAP_RANGE) {
            return false;
        }
        if (Math.abs(target.getY() - this.getY()) > 4.0D) {
            return false;
        }
        return this.hasLineOfSight(target);
    }

    public void startLeap(LivingEntity target) {
        this.lockedYaw = yawTo(target.getX() - this.getX(), target.getZ() - this.getZ());
        this.getNavigation().stop();
        setLeapPhase(1);
    }

    private static float yawTo(double dx, double dz) {
        return (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
    }

    private void faceYaw(float yaw) {
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    // ---------------------- Ход прыжка ----------------------
    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel serverLevel) {
            leapServerTick(serverLevel); // задаёт движение до обычной физики
        }
        super.tick();
    }

    private void leapServerTick(ServerLevel level) {
        if (leapCooldown > 0) {
            leapCooldown--;
        }
        int phase = getLeapPhase();
        if (phase <= 0) {
            return;
        }
        phase++;

        if (phase <= WINDUP_TICKS) {
            // замах: стоит на месте, следит за целью
            this.getNavigation().stop();
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                lockedYaw = yawTo(target.getX() - this.getX(), target.getZ() - this.getZ());
            }
            faceYaw(lockedYaw);
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            if (phase == 2) {
                this.playSound(SoundEvents.ZOMBIE_VILLAGER_AMBIENT, 1.4F, 0.5F); // низкое рычание
            }
        } else if (phase <= WINDUP_TICKS + FLIGHT_TICKS) {
            int k = phase - WINDUP_TICKS;
            if (k == 1 && !beginFlight()) {
                // цель пропала: отменяем прыжок
                leapCooldown = 40;
                setLeapPhase(0);
                return;
            }
            // летим по дуге: каждый тик двигаемся к следующей точке параболы
            Vec3 next = arcPoint(k / (double) FLIGHT_TICKS);
            this.setDeltaMovement(next.subtract(this.position()));
            faceYaw(lockedYaw);
            this.resetFallDistance();
        } else if (phase == WINDUP_TICKS + FLIGHT_TICKS + 1) {
            land(level);
            this.setDeltaMovement(Vec3.ZERO);
        } else if (phase > WINDUP_TICKS + FLIGHT_TICKS + RECOVERY_TICKS) {
            phase = 0;
        } else {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
        setLeapPhase(phase);
    }

    /** Фиксирует точку приземления в момент прыжка. */
    private boolean beginFlight() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        leapStart = this.position();
        Vec3 end = target.position();
        double fx = end.x - leapStart.x;
        double fz = end.z - leapStart.z;
        double dist = Math.sqrt(fx * fx + fz * fz);

        double maxDist = MAX_LEAP_RANGE + 1.0D;
        if (dist > maxDist) {
            fx *= maxDist / dist;
            fz *= maxDist / dist;
            dist = maxDist;
            end = new Vec3(leapStart.x + fx, end.y, leapStart.z + fz);
        }
        leapEnd = end;
        leapApex = Mth.clamp(MIN_APEX + (MAX_APEX - MIN_APEX) * (dist / MAX_LEAP_RANGE), MIN_APEX, MAX_APEX);
        if (dist > 0.01D) {
            lockedYaw = yawTo(fx, fz);
        }
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 1.4F); // звук прыжка
        return true;
    }

    /** Точка параболы: a = 0 старт, a = 1 цель; в середине добавляется высота leapApex. */
    private Vec3 arcPoint(double a) {
        Vec3 line = leapStart.add(leapEnd.subtract(leapStart).scale(a));
        return line.add(0.0D, 4.0D * leapApex * a * (1.0D - a), 0.0D);
    }

    private void land(ServerLevel level) {
        this.resetFallDistance();

        Vec3 center = this.position().distanceTo(leapEnd) <= 1.5D ? leapEnd : this.position();
        boolean hitAny = false;

        AABB box = new AABB(center.x, center.y, center.z, center.x, center.y, center.z)
                .inflate(DODGE_RADIUS + 1.0D, 3.0D, DODGE_RADIUS + 1.0D);
        for (LivingEntity victim : level.getEntitiesOfClass(
                LivingEntity.class, box,
                entity -> entity != this && entity.isAlive() && !(entity instanceof Enemy))) {
            double dx = victim.getX() - center.x;
            double dz = victim.getZ() - center.z;
            boolean close = dx * dx + dz * dz <= DODGE_RADIUS * DODGE_RADIUS
                    && Math.abs(victim.getY() - center.y) <= 2.5D;
            // эффекты накладываются только если урон прошёл (блок щитом их отменяет)
            if (close && this.doHurtTarget(level, victim)) {
                victim.addEffect(new MobEffectInstance(ModEffects.CRUSHED, SLOW_TICKS, 0));
                victim.addEffect(new MobEffectInstance(ModEffects.DISARMED, DISARM_TICKS, 0));
                if (victim instanceof Player player) {
                    player.stopUsingItem(); // сбрасывает натянутый лук
                }
                hitAny = true;
            }
        }

        if (hitAny) {
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.9F);
            this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.2F, 0.7F);
            leapCooldown = COOLDOWN_AFTER_HIT;
        } else {
            this.playSound(SoundEvents.RAVAGER_STEP, 1.5F, 0.7F);
            leapCooldown = COOLDOWN_AFTER_MISS;
        }
    }

    // ---------------------- Звуки ----------------------
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_VILLAGER_STEP, 0.15F, 1.0F);
    }
}
