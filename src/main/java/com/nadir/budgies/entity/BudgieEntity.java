package com.nadir.budgies.entity;

import com.nadir.budgies.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BudgieEntity extends net.minecraft.world.entity.TamableAnimal
        implements GeoEntity, FlyingAnimal {

    // ── Synced Data ──────────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> IS_MALE =
            SynchedEntityData.defineId(BudgieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BudgieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING =
            SynchedEntityData.defineId(BudgieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_POOPING =
            SynchedEntityData.defineId(BudgieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RELAXING =
            SynchedEntityData.defineId(BudgieEntity.class, EntityDataSerializers.BOOLEAN);

    // ── GeckoLib ──────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ── Timers ────────────────────────────────────────────────────────────────
    /** Ticks until next poop (1200-2400). */
    private int poopTimer;
    /** Ticks until next idle/calling sound attempt. */
    private int idleSoundTimer;
    /** Cooldown before this bird can call again (prevents answer loops). */
    private int callingCooldown;
    /** Ticks until this bird answers a call (0 = no pending answer). */
    private int answerTimer;
    /** Ticks spent in RELAX state. */
    private int relaxTimer;
    /** Tick count when this budgie mounted a player. */
    private int mountTime;

    // ── Constructor ───────────────────────────────────────────────────────────
    public BudgieEntity(EntityType<? extends net.minecraft.world.entity.TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        resetPoopTimer();
        resetIdleSoundTimer();
    }

    // ── Attributes ────────────────────────────────────────────────────────────
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (this.isFlying() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }
        super.travel(travelVector);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
    }

    // ── Synched Data ──────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_MALE, false);
        builder.define(VARIANT, 0);
        builder.define(IS_SLEEPING, false);
        builder.define(IS_POOPING, false);
        builder.define(IS_RELAXING, false);
    }

    public boolean isMale() { return this.getEntityData().get(IS_MALE); }
    public void setMale(boolean male) { this.getEntityData().set(IS_MALE, male); }

    public int getVariant() { return this.getEntityData().get(VARIANT); }
    public void setVariant(int variant) { this.getEntityData().set(VARIANT, variant); }

    public boolean isSleepingBudgie() { return this.getEntityData().get(IS_SLEEPING); }
    public void setSleepingBudgie(boolean sleeping) { this.getEntityData().set(IS_SLEEPING, sleeping); }

    public boolean isPooping() { return this.getEntityData().get(IS_POOPING); }
    public void setPooping(boolean pooping) { this.getEntityData().set(IS_POOPING, pooping); }

    public boolean isRelaxingBudgie() { return this.getEntityData().get(IS_RELAXING); }
    public void setRelaxingBudgie(boolean relaxing) { this.getEntityData().set(IS_RELAXING, relaxing); }

    // ── Spawn Initialisation ──────────────────────────────────────────────────
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                   MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        RandomSource rng = level.getRandom();
        // 50/50 gender
        setMale(rng.nextBoolean());
        // Random variant (0, 1, or 2)
        setVariant(rng.nextInt(3));
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    // ── Goals ─────────────────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        // Flee from non-tamed players approaching within 4 blocks
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 4.0F, 1.6D, 1.4D,
                e -> !this.isTame()) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
        });
        // Tempt with seeds (taming)
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, stack ->
                stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS), false) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !BudgieEntity.this.isSleepingBudgie() && super.canUse();
            }
        });
    }

    // ── Taming & Interaction ──────────────────────────────────────────────────
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // If not tamed, seeds will try to tame
        if (!this.isTame() && this.isFood(stack)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (this.getRandom().nextInt(3) == 0) {
                this.tame(player);
                this.getNavigation().stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 7); // heart particles
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6); // smoke particles
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        
        // If tamed and holding food, let super handle feeding/breeding
        if (this.isTame() && this.isFood(stack)) {
            return super.mobInteract(player, hand);
        }
        
        // If tamed and owned, and NOT holding food, toggle sitting or ride
        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                if (player.isShiftKeyDown()) {
                    if (!this.isPassenger()) {
                        this.startRiding(player, true);
                        this.mountTime = this.tickCount;
                    }
                } else {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.getNavigation().stop();
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // ── Food / Breeding ───────────────────────────────────────────────────────
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS);
    }

    @Override
    public boolean canMate(Animal other) {
        if (other == this) return false;
        if (!(other instanceof BudgieEntity partner)) return false;
        if (!this.isInLove() || !partner.isInLove()) return false;
        // One must be male, the other female
        return this.isMale() != partner.isMale();
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        BudgieEntity baby = new BudgieEntity(
                com.nadir.budgies.registry.ModEntities.BUDGIE, level);
        if (this.getOwner() instanceof Player ownerPlayer) {
            baby.tame(ownerPlayer);
        }
        // Inherit variant from one of the parents, with a 10% chance to mutate
        if (this.getRandom().nextFloat() < 0.10f) {
            baby.setVariant(this.getRandom().nextInt(3));
        } else {
            baby.setVariant(this.getRandom().nextBoolean() ? this.getVariant() : ((BudgieEntity) partner).getVariant());
        }
        baby.setMale(this.getRandom().nextBoolean());
        return baby;
    }

    // ── Main Tick ─────────────────────────────────────────────────────────────
    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof Player player) {
            // Dismount if the player sneaks (with a 1-second cooldown to prevent instant dismount on mount)
            if (player.isShiftKeyDown() && (this.tickCount - this.mountTime) > 20) {
                this.stopRiding();
            }
            // Sync animation state: while riding, look idle or relax
            if (!this.level().isClientSide) {
                this.yBodyRot = player.yBodyRot;
                this.setYRot(player.getYRot());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return; // Server only logic below

        // ── Sleep logic ───────────────────────────────────────────────────────
        boolean shouldSleep = shouldSleep();
        if (shouldSleep != isSleepingBudgie()) {
            setSleepingBudgie(shouldSleep);
            if (shouldSleep) setRelaxingBudgie(false);
        }

        if (isSleepingBudgie()) {
            // Occasionally murmur sleeping sound (1 in 200 ticks = ~10 seconds)
            if (this.getRandom().nextInt(200) == 0) {
                this.playSound(ModSounds.BUDGIE_SLEEPING, 0.3f,
                        0.9f + this.getRandom().nextFloat() * 0.2f);
            }
            // Clear pending answers if we went to sleep
            this.answerTimer = 0;
            return;
        }

        // ── Calling cooldown tick ─────────────────────────────────────────────
        if (callingCooldown > 0) callingCooldown--;

        // ── Pending answer tick ───────────────────────────────────────────────
        if (answerTimer > 0) {
            answerTimer--;
            if (answerTimer == 0) {
                this.playSound(ModSounds.BUDGIE_ANSWERING, 0.7f,
                        0.9f + this.getRandom().nextFloat() * 0.2f);
                callingCooldown = 200;
            }
        }

        // ── Poop timer ────────────────────────────────────────────────────────
        poopTimer--;
        if (poopTimer <= 0) {
            triggerAnim("poop_controller", "poop");
            this.playSound(SoundEvents.CHICKEN_EGG, 0.5f,
                    1.0f + this.getRandom().nextFloat() * 0.2f);
            resetPoopTimer();
        }

        // ── Relax timer ───────────────────────────────────────────────────────
        if (!isFlying()) {
            if (isRelaxingBudgie()) {
                relaxTimer--;
                if (relaxTimer <= 0) {
                    setRelaxingBudgie(false);
                }
            } else if (!isSleepingBudgie() && this.getRandom().nextInt(200) == 0) {
                setRelaxingBudgie(true);
                relaxTimer = 100 + this.getRandom().nextInt(100); // 5-10 seconds
            }
        } else {
            setRelaxingBudgie(false);
        }

        // ── Idle sound / calling system ───────────────────────────────────────
        if (!isRelaxingBudgie()) {
            idleSoundTimer--;
            if (idleSoundTimer <= 0) {
                resetIdleSoundTimer();
                if (this.getRandom().nextInt(10) < 7) {
                    // 70%: normal idle chirp
                    this.playSound(ModSounds.BUDGIE_IDLE, 0.6f,
                            0.9f + this.getRandom().nextFloat() * 0.2f);
                } else if (callingCooldown <= 0) {
                    // 30%: call out and signal nearby budgies
                    this.playSound(ModSounds.BUDGIE_CALLING, 0.8f,
                            0.9f + this.getRandom().nextFloat() * 0.2f);
                    callingCooldown = 200;
                    signalNearbyBudgies();
                }
            }
        }
    }

    /** Broadcast a "you've been called" signal to nearby budgies within 10 blocks. */
    private void signalNearbyBudgies() {
        List<BudgieEntity> nearby = this.level().getEntitiesOfClass(
                BudgieEntity.class, this.getBoundingBox().inflate(10.0),
                e -> e != this);
        for (BudgieEntity other : nearby) {
            other.receiveCall();
        }
    }

    /** Called when another budgie has called out to this one. */
    public void receiveCall() {
        if (this.level().isClientSide) return;
        if (this.isSleepingBudgie()) return; // Don't wake up or answer if sleeping
        // Schedule an answer in 10-30 ticks, but only if not already answering
        if (answerTimer <= 0 && callingCooldown <= 0) {
            answerTimer = 10 + this.getRandom().nextInt(21);
        }
    }

    // ── Sleep detection ───────────────────────────────────────────────────────
    private boolean shouldSleep() {
        if (isFlying()) return false;
        Level lv = this.level();
        return lv.isNight() || lv.getMaxLocalRawBrightness(this.blockPosition()) == 0;
    }

    // ── NBT Persistence ───────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsMale", this.isMale());
        tag.putInt("Variant", this.getVariant());
        tag.putInt("PoopTimer", this.poopTimer);
        tag.putInt("CallingCooldown", this.callingCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setMale(tag.getBoolean("IsMale"));
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        } else if (tag.contains("FeatherColor")) {
            this.setVariant(this.getRandom().nextInt(3));
        }
        this.poopTimer = tag.contains("PoopTimer") ? tag.getInt("PoopTimer") : getNewPoopTimer();
        this.callingCooldown = tag.contains("CallingCooldown") ? tag.getInt("CallingCooldown") : 0;
    }

    // ── Sounds ────────────────────────────────────────────────────────────────
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null; // Handled manually in tick()
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BUDGIE_IDLE;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return ModSounds.BUDGIE_IDLE;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void resetPoopTimer() {
        this.poopTimer = getNewPoopTimer();
    }

    private int getNewPoopTimer() {
        return 1200 + this.getRandom().nextInt(1201); // 1200-2400 ticks
    }

    private void resetIdleSoundTimer() {
        this.idleSoundTimer = 80 + this.getRandom().nextInt(120); // 4-10 seconds
    }

    // ── GeckoLib ─────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        // Main locomotion + state controller
        controllers.add(new AnimationController<>(this, "main_controller", 5, state -> {
            if (isFlying()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("fly"));
            }
            if (isSleepingBudgie()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("sleep"));
            }
            if (isRelaxingBudgie()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("relax"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("idle"));
        }));

        // High-priority one-shot poop controller
        controllers.add(new AnimationController<>(this, "poop_controller", 0, state ->
                PlayState.STOP
        ).triggerableAnim("poop", RawAnimation.begin().thenPlay("poop")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
