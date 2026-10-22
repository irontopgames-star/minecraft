package com.mimicrabbit.entity;

import com.mimicrabbit.registry.ModSounds;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Conejo con dos formas:
 * <ul>
 *   <li>Forma normal: conejo inofensivo (pequeño, salta, no ataca).</li>
 *   <li>Forma monstruosa: aparece tras ser golpeado por un jugador. Es alto, rápido y persigue
 *       a ese jugador hasta morir (o hasta perderlo durante mucho tiempo).</li>
 * </ul>
 *
 * Máquina de estados (ver {@link State}):
 * NORMAL -> TRANSFORMING -> HOSTILE <-> SEARCHING, y DYING al morir.
 * La transformación solo puede ocurrir una vez: el flag {@code Transformed} es permanente.
 */
public class MimicRabbit extends PathfinderMob {

    public enum State {
        /** Conejo común, no ataca. */
        NORMAL,
        /** Animación de transformación (el mob es inmóvil e invulnerable). */
        TRANSFORMING,
        /** Monstruo con el objetivo a la vista: persigue y ataca. */
        HOSTILE,
        /** Monstruo que perdió de vista al objetivo: va a su última posición conocida y rastrea. */
        SEARCHING,
        /** Muriendo. */
        DYING
    }

    // ------------------------------------------------------------------ constantes

    /** Duración total de la transformación en ticks (50 = 2,5 s). */
    public static final int TRANSFORM_DURATION = 50;
    /** Tick en el que el modelo cambia de conejo a monstruo y suena el grito. */
    public static final int TRANSFORM_SWAP_TICK = 28;

    private static final double NORMAL_HEALTH = 3.0D;
    private static final double NORMAL_SPEED = 0.22D;
    private static final double NORMAL_ATTACK = 1.0D;
    private static final double NORMAL_FOLLOW = 16.0D;

    private static final double MONSTER_HEALTH = 40.0D;
    private static final double MONSTER_SPEED = 0.27D;
    /** 6 puntos = 3 corazones (en Normal; la dificultad lo escala igual que a cualquier mob). */
    private static final double MONSTER_ATTACK = 6.0D;
    private static final double MONSTER_FOLLOW = 64.0D;
    private static final double MONSTER_KNOCKBACK_RESISTANCE = 0.4D;

    private static final EntityDimensions MONSTER_DIMENSIONS = EntityDimensions.fixed(0.8F, 2.1F);

    /** Distancia a la que "huele"/oye al atacante aunque no lo vea. */
    private static final double SENSE_RANGE_SQR = 16.0D * 16.0D;
    /** Distancia máxima a la que lo ve (con línea de visión) o lo sigue. */
    private static final double SIGHT_RANGE_SQR = 64.0D * 64.0D;
    /** Ticks sin ver al objetivo (y lejos) antes de dejar de perseguir y pasar a rastrear. */
    private static final int LOSE_TRACK_TICKS = 100;
    /** Ticks que rastrea la última posición conocida (20 s). */
    private static final int SEARCH_DURATION = 400;

    private static final EntityDataAccessor<Boolean> DATA_TRANSFORMED =
            SynchedEntityData.defineId(MimicRabbit.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TRANSFORM_TICKS =
            SynchedEntityData.defineId(MimicRabbit.class, EntityDataSerializers.INT);

    // ------------------------------------------------------------------ estado (solo servidor)

    @Nullable
    private UUID attackerId;
    @Nullable
    private Vec3 lastKnownTargetPos;
    private int searchTicks;

    public MimicRabbit(EntityType<? extends MimicRabbit> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, NORMAL_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, NORMAL_SPEED)
                .add(Attributes.ATTACK_DAMAGE, NORMAL_ATTACK)
                .add(Attributes.FOLLOW_RANGE, NORMAL_FOLLOW);
    }

    /** Aparece sobre los mismos bloques que el conejo vanilla y con luz suficiente. */
    public static boolean checkSpawnRules(EntityType<MimicRabbit> type, LevelAccessor level, MobSpawnType reason,
                                          BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 8;
    }

    // ------------------------------------------------------------------ datos sincronizados

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRANSFORMED, false);
        this.entityData.define(DATA_TRANSFORM_TICKS, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_TRANSFORMED.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.isTransformed() ? MONSTER_DIMENSIONS : super.getDimensions(pose);
    }

    public boolean isTransformed() {
        return this.entityData.get(DATA_TRANSFORMED);
    }

    public int getTransformTicks() {
        return this.entityData.get(DATA_TRANSFORM_TICKS);
    }

    private void setTransformTicks(int ticks) {
        this.entityData.set(DATA_TRANSFORM_TICKS, ticks);
    }

    /** true mientras dura la animación de transformación. */
    public boolean isTransforming() {
        return !this.isTransformed() && this.getTransformTicks() > 0;
    }

    /** true cuando hay que dibujar el modelo/textura del monstruo (a mitad de la transformación ya cambia). */
    public boolean shouldRenderMonsterModel() {
        return this.isTransformed() || this.getTransformTicks() >= TRANSFORM_SWAP_TICK;
    }

    /** Progreso 0..1 de la transformación (para animaciones del cliente). */
    public float getTransformProgress(float partialTick) {
        if (this.isTransformed()) {
            return 1.0F;
        }
        return Mth.clamp((this.getTransformTicks() + partialTick) / TRANSFORM_DURATION, 0.0F, 1.0F);
    }

    /** Estado actual de la máquina de estados. */
    public State getState() {
        if (this.isDeadOrDying()) {
            return State.DYING;
        }
        if (this.isTransforming()) {
            return State.TRANSFORMING;
        }
        if (!this.isTransformed()) {
            return State.NORMAL;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.hasLineOfSight(target)) {
            return State.HOSTILE;
        }
        return State.SEARCHING;
    }

    // ------------------------------------------------------------------ IA

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Forma normal: huye si algo que no sea un jugador la daña (p. ej. un zombi, el fuego...).
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D) {
            @Override
            public boolean canUse() {
                return !MimicRabbit.this.isTransformed() && super.canUse();
            }
        });
        // Forma monstruosa: ataque cuerpo a cuerpo persistente (sigue al objetivo aunque no lo vea).
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true) {
            @Override
            public boolean canUse() {
                return MimicRabbit.this.isTransformed() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return MimicRabbit.this.isTransformed() && super.canContinueToUse();
            }
        });
        // Saltos de acecho para abalanzarse sobre el jugador.
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.5F) {
            @Override
            public boolean canUse() {
                return MimicRabbit.this.isTransformed() && !MimicRabbit.this.isTransforming() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new SearchLastKnownPositionGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new FocusAttackerGoal());
    }

    /**
     * Elige como objetivo al jugador que golpeó al conejo. Lo mantiene mientras lo vea (o esté cerca)
     * y lo pierde si pasan {@link #LOSE_TRACK_TICKS} sin verlo estando lejos.
     */
    private class FocusAttackerGoal extends Goal {
        private int unseenTicks;

        FocusAttackerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!isTransformed() || isTransforming()) {
                return false;
            }
            Player attacker = getValidAttacker();
            if (attacker == null) {
                return false;
            }
            LivingEntity current = getTarget();
            if (current != null && current.isAlive() && current != attacker) {
                return false;
            }
            double dist = distanceToSqr(attacker);
            return dist <= SENSE_RANGE_SQR || (dist <= SIGHT_RANGE_SQR && hasLineOfSight(attacker));
        }

        @Override
        public void start() {
            setTarget(getValidAttacker());
            this.unseenTicks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            Player attacker = getValidAttacker();
            if (attacker == null || getTarget() != attacker) {
                return false;
            }
            double dist = distanceToSqr(attacker);
            if (dist > SIGHT_RANGE_SQR) {
                return false;
            }
            if (dist <= SENSE_RANGE_SQR || hasLineOfSight(attacker)) {
                this.unseenTicks = 0;
            } else {
                this.unseenTicks++;
            }
            return this.unseenTicks < LOSE_TRACK_TICKS;
        }

        @Override
        public void stop() {
            if (getTarget() == getValidAttacker()) {
                setTarget(null);
            }
        }
    }

    /** ESTADO DE BÚSQUEDA: sin objetivo, va a la última posición conocida y merodea por allí. */
    private class SearchLastKnownPositionGoal extends Goal {
        private int repathCooldown;

        SearchLastKnownPositionGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return isTransformed() && !isTransforming() && getTarget() == null
                    && lastKnownTargetPos != null && searchTicks > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            searchTicks--;
            if (--this.repathCooldown > 0 || lastKnownTargetPos == null) {
                return;
            }
            this.repathCooldown = 20 + getRandom().nextInt(20);
            Vec3 dest = lastKnownTargetPos;
            if (getNavigation().isDone() && position().distanceToSqr(dest) < 9.0D) {
                Vec3 around = LandRandomPos.getPos(MimicRabbit.this, 10, 4);
                if (around != null) {
                    dest = around;
                }
            }
            getNavigation().moveTo(dest.x, dest.y, dest.z, 1.0D);
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }

    // ------------------------------------------------------------------ movimiento

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (!this.isTransformed()) {
            // Forma normal: saltitos mientras sigue una ruta (como un conejo vanilla).
            if (this.onGround() && this.getNavigation().isInProgress()) {
                this.getJumpControl().jump();
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.hasLineOfSight(target)) {
            this.lastKnownTargetPos = target.position();
            this.searchTicks = SEARCH_DURATION;
        }
        // Respiración inquietante durante la persecución.
        if (target != null && this.tickCount % 50 == 0) {
            this.playSound(ModSounds.BREATH.get(), 0.9F, 0.85F + this.getRandom().nextFloat() * 0.3F);
        }
    }

    /** En la forma normal solo se desplaza saltando: en el suelo no hay empuje horizontal continuo. */
    @Override
    public void travel(Vec3 travelVector) {
        if (!this.isTransformed() && this.onGround() && !this.isInWater()) {
            super.travel(Vec3.ZERO);
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    protected float getJumpPower() {
        return this.isTransformed() ? super.getJumpPower() : 0.32F * this.getBlockJumpFactor();
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!this.isTransformed()) {
            if (this.moveControl.getSpeedModifier() > 0.0D
                    && this.getDeltaMovement().horizontalDistanceSqr() < 0.04D) {
                this.moveRelative(0.16F, new Vec3(0.0D, 0.0D, 1.0D));
            }
            this.playSound(SoundEvents.RABBIT_JUMP, 0.4F, 1.0F);
        }
    }

    /** Durante la transformación el mob se queda quieto (no ejecuta IA). */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isTransforming();
    }

    // ------------------------------------------------------------------ daño y transformación

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return super.hurt(source, amount);
        }
        if (this.isTransforming()) {
            // Invulnerable durante la transformación (salvo /kill y vacío).
            return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
        }
        if (!this.isTransformed()) {
            if (source.getEntity() instanceof Player player) {
                // El golpe inicial no hace daño: dispara la transformación una sola vez.
                this.startTransformation(player);
                return false;
            }
        } else if (source.is(DamageTypes.IN_WALL)) {
            // La forma grande puede quedar encajada en bloques: no la matamos por eso.
            return false;
        }
        return super.hurt(source, amount);
    }

    private void startTransformation(Player attacker) {
        this.attackerId = attacker.getUUID();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setTransformTicks(1);
        this.playSound(ModSounds.TRANSFORM_START.get(), 1.4F, 1.0F);
        // Efecto de terror: la pantalla del jugador se oscurece unos segundos.
        attacker.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 90, 0, false, false));
    }

    private void tickTransformation(ServerLevel level) {
        int t = this.getTransformTicks() + 1;

        this.getNavigation().stop();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, motion.y, 0.0D);

        Player attacker = this.getValidAttacker();
        if (attacker != null) {
            double dx = attacker.getX() - this.getX();
            double dz = attacker.getZ() - this.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(yaw);
            this.setYBodyRot(yaw);
            this.setYHeadRot(yaw);
        }

        double px = this.getX();
        double py = this.getY() + this.getBbHeight() * 0.5D;
        double pz = this.getZ();

        if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 3, 0.25D, 0.3D, 0.25D, 0.02D);
        }
        if (t % 3 == 0) {
            level.sendParticles(new DustParticleOptions(new Vector3f(0.45F, 0.0F, 0.0F), 1.6F),
                    px, py, pz, 6, 0.3D, 0.4D, 0.3D, 0.0D);
        }
        if (t < TRANSFORM_SWAP_TICK && t % 6 == 0) {
            // Crujidos de huesos mientras el cuerpo se deforma.
            this.playSound(ModSounds.TRANSFORM_CRACK.get(), 0.9F, 0.6F + t * 0.01F);
        }
        if (t == TRANSFORM_SWAP_TICK) {
            this.playSound(ModSounds.TRANSFORM_SCREAM.get(), 1.6F, 0.8F);
            this.playSound(ModSounds.TRANSFORM_ROAR.get(), 1.6F, 0.9F);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 25, 0.4D, 0.5D, 0.4D, 0.05D);
        }

        if (t >= TRANSFORM_DURATION) {
            this.finishTransformation(level);
        } else {
            this.setTransformTicks(t);
        }
    }

    private void finishTransformation(ServerLevel level) {
        this.setTransformTicks(0);
        this.entityData.set(DATA_TRANSFORMED, true);
        this.refreshDimensions();
        this.applyFormAttributes();
        this.setHealth(this.getMaxHealth());
        this.makeRoomForMonsterForm();

        double px = this.getX();
        double py = this.getY() + this.getBbHeight() * 0.5D;
        double pz = this.getZ();
        level.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 50, 0.5D, 0.8D, 0.5D, 0.08D);
        level.sendParticles(ParticleTypes.SOUL, px, py, pz, 15, 0.4D, 0.8D, 0.4D, 0.03D);
        this.playSound(ModSounds.AMBIENT.get(), 1.4F, 0.7F);

        Player attacker = this.getValidAttacker();
        if (attacker != null) {
            this.setTarget(attacker);
            this.lastKnownTargetPos = attacker.position();
            this.searchTicks = SEARCH_DURATION;
        }
    }

    /** La forma grande es más alta: si queda dentro de bloques, busca un hueco cercano libre. */
    private void makeRoomForMonsterForm() {
        if (this.level().noCollision(this)) {
            return;
        }
        for (int r = 0; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (double dy : new double[] {0.0D, 0.5D, 1.0D}) {
                        AABB moved = this.getBoundingBox().move(dx * 0.5D, dy, dz * 0.5D);
                        if (this.level().noCollision(this, moved)) {
                            this.setPos(this.getX() + dx * 0.5D, this.getY() + dy, this.getZ() + dz * 0.5D);
                            return;
                        }
                    }
                }
            }
        }
    }

    /** Aplica los atributos de la forma actual (también al cargar el mundo). */
    private void applyFormAttributes() {
        boolean monster = this.isTransformed();
        this.setBase(Attributes.MAX_HEALTH, monster ? MONSTER_HEALTH : NORMAL_HEALTH);
        this.setBase(Attributes.MOVEMENT_SPEED, monster ? MONSTER_SPEED : NORMAL_SPEED);
        this.setBase(Attributes.ATTACK_DAMAGE, monster ? MONSTER_ATTACK : NORMAL_ATTACK);
        this.setBase(Attributes.FOLLOW_RANGE, monster ? MONSTER_FOLLOW : NORMAL_FOLLOW);
        this.setBase(Attributes.KNOCKBACK_RESISTANCE, monster ? MONSTER_KNOCKBACK_RESISTANCE : 0.0D);
        this.xpReward = monster ? 20 : 1;
    }

    private void setBase(Attribute attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Nullable
    private Player getValidAttacker() {
        if (this.attackerId == null) {
            return null;
        }
        Player player = this.level().getPlayerByUUID(this.attackerId);
        return (player != null && player.isAlive() && !player.isSpectator()) ? player : null;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.isTransforming()) {
                this.tickTransformation(serverLevel);
            } else if (this.isTransformed() && this.isAlive() && this.tickCount % 6 == 0) {
                // Rastro de humo oscuro a su alrededor.
                serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                        2, 0.3D, 0.6D, 0.3D, 0.01D);
            }
        }
    }

    // ------------------------------------------------------------------ muerte y botín

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.isTransformed() && this.level() instanceof ServerLevel serverLevel && this.deathTime % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.5D, this.getZ(),
                    2, 0.3D, 0.4D, 0.3D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.8D, this.getZ(),
                    3, 0.4D, 0.6D, 0.4D, 0.02D);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        RandomSource random = this.getRandom();
        if (this.isTransformed()) {
            int hide = 1 + random.nextInt(3);
            this.spawnAtLocation(new ItemStack(Items.RABBIT_HIDE, hide));
            if (random.nextFloat() < 0.35F + 0.1F * looting) {
                this.spawnAtLocation(new ItemStack(Items.RABBIT_FOOT));
            }
            if (random.nextFloat() < 0.5F) {
                this.spawnAtLocation(new ItemStack(Items.RABBIT, 1 + random.nextInt(2)));
            }
        } else {
            if (random.nextInt(2) == 0) {
                this.spawnAtLocation(new ItemStack(Items.RABBIT_HIDE));
            }
            if (random.nextInt(2) == 0) {
                this.spawnAtLocation(new ItemStack(Items.RABBIT));
            }
        }
    }

    // ------------------------------------------------------------------ combate

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.isTransformed()) {
            this.playSound(ModSounds.ATTACK.get(), 1.0F, 0.9F + this.getRandom().nextFloat() * 0.2F);
        }
        return hit;
    }

    // ------------------------------------------------------------------ sonidos

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.isTransformed() ? ModSounds.AMBIENT.get() : SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isTransformed() ? ModSounds.HURT.get() : SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTransformed() ? ModSounds.DEATH.get() : SoundEvents.RABBIT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isTransformed()) {
            this.playSound(ModSounds.STEP.get(), 0.25F, 0.8F + this.getRandom().nextFloat() * 0.3F);
        }
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Transformed", this.isTransformed());
        tag.putInt("TransformTicks", this.getTransformTicks());
        if (this.attackerId != null) {
            tag.putUUID("Attacker", this.attackerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_TRANSFORMED, tag.getBoolean("Transformed"));
        this.setTransformTicks(tag.getInt("TransformTicks"));
        if (tag.hasUUID("Attacker")) {
            this.attackerId = tag.getUUID("Attacker");
        }
        this.applyFormAttributes();
    }
}
