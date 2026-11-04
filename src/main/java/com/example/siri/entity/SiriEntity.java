package com.example.siri.entity;

import com.example.siri.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SiriEntity extends PathfinderMob implements Bucketable {

    private static final EntityDataAccessor<Boolean> BURROWED =
            SynchedEntityData.defineId(SiriEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET =
            SynchedEntityData.defineId(SiriEntity.class, EntityDataSerializers.BOOLEAN);

    private int burrowTimer;
    private int burrowCooldown;
    private float burrowProgress;
    private float burrowProgressO;

    public SiriEntity(EntityType<? extends SiriEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D);
    }

    public static boolean checkSiriSpawn(EntityType<SiriEntity> type, ServerLevelAccessor level,
                                         MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.SAND) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BURROWED, false);
        this.entityData.define(FROM_BUCKET, false);
    }

    // ---------------------------------------------------------------- Burrow (esconder na areia)

    public boolean isBurrowed() {
        return this.entityData.get(BURROWED);
    }

    private void setBurrowed(boolean value) {
        this.entityData.set(BURROWED, value);
    }

    public float getBurrowProgress(float partialTick) {
        return Mth.lerp(partialTick, burrowProgressO, burrowProgress);
    }

    private boolean isSandBelow() {
        return this.level().getBlockState(this.blockPosition().below()).is(BlockTags.SAND);
    }

    private void startBurrow(int ticks) {
        setBurrowed(true);
        this.burrowTimer = ticks;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(SoundEvents.SAND_BREAK, 1.0F, 0.8F);
    }

    private void emerge() {
        setBurrowed(false);
        this.burrowCooldown = 60 + this.random.nextInt(100);
        this.setDeltaMovement(0.0D, 0.28D, 0.0D); // pulinho ao sair da areia
        this.playSound(SoundEvents.SAND_BREAK, 1.0F, 1.2F);
    }

    @Override
    public void tick() {
        super.tick();

        this.burrowProgressO = this.burrowProgress;
        if (isBurrowed()) {
            this.burrowProgress = Math.min(1.0F, this.burrowProgress + 0.08F);
        } else {
            this.burrowProgress = Math.max(0.0F, this.burrowProgress - 0.08F);
        }

        if (this.level().isClientSide) {
            if (this.burrowProgress != this.burrowProgressO) {
                spawnSandParticles();
            }
        } else {
            serverBurrowLogic();
        }
    }

    private void serverBurrowLogic() {
        if (this.burrowCooldown > 0) this.burrowCooldown--;

        Player near = this.level().getNearestPlayer(this, 6.0D);
        boolean threat = false;
        if (near != null) {
            double r = near.isCrouching() ? 2.5D : 5.5D; // agachado = consegue chegar mais perto
            threat = near.distanceToSqr(this) < r * r;
        }

        if (isBurrowed()) {
            this.burrowTimer--;
            if (!isSandBelow()) {
                emerge();
            } else if (this.burrowTimer <= 0 && !threat) {
                emerge();
            }
        } else if (this.burrowCooldown == 0 && this.onGround() && isSandBelow() && !this.isInWaterOrBubble()) {
            if (threat || this.random.nextInt(600) == 0) {
                startBurrow(100 + this.random.nextInt(140));
            }
        }
    }

    private void spawnSandParticles() {
        BlockState state = this.level().getBlockState(this.blockPosition().below());
        if (state.isAir()) return;
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                    this.getY() + 0.05D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                    (this.random.nextDouble() - 0.5D) * 0.1D, 0.15D, (this.random.nextDouble() - 0.5D) * 0.1D);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isBurrowed();
    }

    @Override
    public boolean isPickable() {
        return !isBurrowed() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !isBurrowed() && super.isPushable();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (isBurrowed() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && !isBurrowed() && this.isAlive()
                && this.onGround() && isSandBelow()) {
            startBurrow(120); // foge se enterrando
        }
        return result;
    }

    // ---------------------------------------------------------------- Balde

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if ((held.is(Items.WATER_BUCKET) || held.is(Items.BUCKET)) && this.isAlive() && !isBurrowed()) {
            this.playSound(this.getPickupSound(), 1.0F, 1.0F);
            ItemStack bucket = this.getBucketItemStack();
            this.saveToBucketTag(bucket);
            ItemStack result = ItemUtils.createFilledResult(held, player, bucket, false);
            player.setItemInHand(hand, result);
            if (!this.level().isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, bucket);
            }
            this.discard();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override public boolean fromBucket() { return this.entityData.get(FROM_BUCKET); }
    @Override public void setFromBucket(boolean value) { this.entityData.set(FROM_BUCKET, value); }
    @Override public void saveToBucketTag(ItemStack stack) { Bucketable.saveDefaultDataToBucketTag(this, stack); }
    @Override public void loadFromBucketTag(CompoundTag tag) { Bucketable.loadDefaultDataFromBucketTag(this, tag); }
    @Override public ItemStack getBucketItemStack() { return new ItemStack(ModItems.SIRI_BUCKET.get()); }
    @Override public SoundEvent getPickupSound() { return SoundEvents.BUCKET_FILL_FISH; }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    // ---------------------------------------------------------------- NBT / sons

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("FromBucket", this.fromBucket());
        tag.putBoolean("Burrowed", this.isBurrowed());
        tag.putInt("BurrowTimer", this.burrowTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFromBucket(tag.getBoolean("FromBucket"));
        this.setBurrowed(tag.getBoolean("Burrowed"));
        this.burrowTimer = tag.getInt("BurrowTimer");
        this.burrowProgress = this.burrowProgressO = isBurrowed() ? 1.0F : 0.0F;
    }

    @Override protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dims) { return 0.3F; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.TURTLE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.TURTLE_DEATH; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.4F); }
}
