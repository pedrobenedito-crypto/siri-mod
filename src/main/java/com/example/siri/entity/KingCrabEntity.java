package com.example.siri.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;

/** Caranguejo-Rei: hostil, territorial, e quebra a areia ao redor com as garras. */
public class KingCrabEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(KingCrabEntity.class, EntityDataSerializers.BOOLEAN);

    private int smashCooldown = 60;
    private float chargeProgress;
    private float chargeProgressO;

    public KingCrabEntity(EntityType<? extends KingCrabEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.ARMOR, 4.0D);
    }

    public static boolean checkKingSpawn(EntityType<KingCrabEntity> type, ServerLevelAccessor level,
                                         MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getBlockState(pos.below()).is(BlockTags.SAND)
                && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SandSmashGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // territorial: ataca qualquer jogador que chegue a menos de 10 blocos
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> target.distanceToSqr(this) < 100.0D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGING, false);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public float getChargeProgress(float partialTick) {
        return Mth.lerp(partialTick, chargeProgressO, chargeProgress);
    }

    @Override
    public void tick() {
        super.tick();
        this.chargeProgressO = this.chargeProgress;
        if (isCharging()) {
            this.chargeProgress = Math.min(1.0F, this.chargeProgress + 0.12F);
        } else {
            this.chargeProgress = Math.max(0.0F, this.chargeProgress - 0.5F); // desce rapido = "pancada"
        }
        if (!this.level().isClientSide && this.smashCooldown > 0) {
            this.smashCooldown--;
        }
    }

    private boolean isSandAt(BlockPos pos) {
        return this.level().getBlockState(pos).is(BlockTags.SAND);
    }

    /** Habilidade: a pancada quebra a areia ao redor, solta particulas de areia e empurra quem estiver perto. */
    private void smash() {
        if (!(this.level() instanceof ServerLevel server)) return;

        boolean grief = ForgeEventFactory.getMobGriefingEvent(server, this);
        BlockPos origin = this.blockPosition();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double d2 = dx * dx + dz * dz;
                if (d2 < 2.0D || d2 > 12.5D) continue; // anel ao redor, sem quebrar embaixo dele
                for (int dy = -1; dy <= 0; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = server.getBlockState(pos);
                    if (state.is(BlockTags.SAND)) {
                        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                                12, 0.3D, 0.2D, 0.3D, 0.18D);
                        if (grief) {
                            server.destroyBlock(pos, this.random.nextFloat() < 0.25F);
                        }
                    }
                }
            }
        }

        // nuvem de areia ao redor das garras
        BlockState ground = server.getBlockState(origin.below());
        if (ground.isAir()) ground = Blocks.SAND.defaultBlockState();
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                this.getX(), this.getY() + 0.1D, this.getZ(), 50, 1.4D, 0.1D, 1.4D, 0.25D);

        this.playSound(SoundEvents.GRAVEL_BREAK, 2.0F, 0.6F);
        this.playSound(SoundEvents.ANVIL_LAND, 0.5F, 0.5F);

        // onda de choque
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(3.0D, 1.0D, 3.0D),
                t -> t != this && !(t instanceof KingCrabEntity) && !(t instanceof SiriEntity))) {
            if (e.hurt(this.damageSources().mobAttack(this), 4.0F)) {
                Vec3 dir = e.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                e.push(dir.x * 0.8D, 0.45D, dir.z * 0.8D);
                e.hurtMarked = true;
            }
        }
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.TURTLE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.TURTLE_DEATH; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.3F, 0.7F);
    }

    // ------------------------------------------------------------------ goal da habilidade

    private static class SandSmashGoal extends Goal {
        private final KingCrabEntity crab;
        private int chargeTicks;
        private boolean done;

        SandSmashGoal(KingCrabEntity crab) {
            this.crab = crab;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = crab.getTarget();
            return t != null && t.isAlive() && crab.smashCooldown <= 0 && crab.onGround()
                    && crab.distanceToSqr(t) < 36.0D
                    && (crab.isSandAt(crab.blockPosition().below()) || crab.isSandAt(t.blockPosition().below()));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = crab.getTarget();
            return !done && t != null && t.isAlive();
        }

        @Override
        public void start() {
            chargeTicks = 0;
            done = false;
            crab.getNavigation().stop();
            crab.entityData.set(CHARGING, true);
        }

        @Override
        public void tick() {
            LivingEntity t = crab.getTarget();
            if (t != null) crab.getLookControl().setLookAt(t, 30.0F, 30.0F);
            if (++chargeTicks >= 20) {
                crab.smash();
                done = true;
            }
        }

        @Override
        public void stop() {
            crab.entityData.set(CHARGING, false);
            crab.smashCooldown = 100 + crab.random.nextInt(60);
        }
    }
}
