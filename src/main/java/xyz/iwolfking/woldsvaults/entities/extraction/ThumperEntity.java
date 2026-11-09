package xyz.iwolfking.woldsvaults.entities.extraction;

import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;
import xyz.iwolfking.woldsvaults.objectives.extraction.ExtractionSpawnManager;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A thumper: one of the two secondary rigs of an extraction site room. It stands on a thumper pad, takes its pools as
 * fractions of the keyed extractor's stats, and is inert and invulnerable until the main rig drills. Shield then
 * armor-mitigated hull, like the main rig, but hull damage never wears the extractor; when its hull runs out it does
 * not die but becomes a wreck that keeps marking the pad, and the objective charges the loss to the main rig.
 */
public class ThumperEntity extends LivingEntity implements IAnimatable, RigTarget {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(ThumperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_WRECKED = SynchedEntityData.defineId(ThumperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int SHIELD_REACTIVATION_TICKS = 40;
    private static final int THUMP_PERIOD_TICKS = 30;

    private final AnimationFactory factory = new AnimationFactory(this);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private AnimationBuilder chain;
    private int chainState = -1;
    private float shield;
    private float maxShield;
    private float shieldRegenPerTick;
    private float healthRegenPerTick;
    private float armor;
    private float shieldToughness;
    private int shieldDownTicks;
    private int siteIndex = -1;
    private int padIndex = -1;
    private UUID rigKey;

    public ThumperEntity(EntityType<? extends ThumperEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTIVE, false);
        this.entityData.define(DATA_WRECKED, false);
    }

    /** Snapshots the keyed extractor's pools, scaled by the THUMPER fractions. */
    public void initFromItem(ItemStack stack, ExtractionObjectiveConfig.ThumperEntry cfg, int siteIndex, int padIndex, UUID rigKey) {
        CompoundTag data = ExtractorItem.getData(stack);
        this.maxShield = data.getFloat("Shield") * cfg.shieldFraction;
        this.shield = this.maxShield;
        this.shieldRegenPerTick = data.getFloat("ShieldRegen") * cfg.shieldRegenFraction;
        this.healthRegenPerTick = data.getFloat("HealthRegen") * cfg.healthRegenFraction;
        this.armor = data.getFloat("Armor") * cfg.armorFraction;
        this.shieldToughness = data.getFloat("ShieldToughness") * cfg.shieldToughnessFraction;
        this.siteIndex = siteIndex;
        this.padIndex = padIndex;
        this.rigKey = rigKey;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0F, data.getFloat("MaxHealth") * cfg.healthFraction));
        this.setHealth(this.getMaxHealth());
        this.setYRot(0.0F);
        this.setYHeadRot(0.0F);
        this.yBodyRot = 0.0F;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "main", 4, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        int state = this.isWrecked() ? 2 : this.isActive() ? 1 : 0;
        if (state != this.chainState || this.chain == null) {
            this.chain = switch (state) {
                case 2 -> new AnimationBuilder().addAnimation("wreck", false);
                case 1 -> new AnimationBuilder().addAnimation("deploy", false).addAnimation("thump", true);
                default -> new AnimationBuilder().addAnimation("idle", true);
            };
            this.chainState = state;
        }
        event.getController().setAnimation(this.chain);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    /** Set by the objective every tick: true while the site's main rig drills. */
    public void setActive(boolean active) {
        if (active != this.isActive()) {
            this.entityData.set(DATA_ACTIVE, active);
        }
    }

    public boolean isWrecked() {
        return this.entityData.get(DATA_WRECKED);
    }

    public UUID getRigKey() {
        return this.rigKey;
    }

    @Override
    public int getSiteIndex() {
        return this.siteIndex;
    }

    @Override
    public int getPadIndex() {
        return this.padIndex;
    }

    @Override
    public boolean isRigTargetable() {
        return this.isActive() && !this.isWrecked();
    }

    public float getShield() {
        return this.shield;
    }

    public float getMaxShield() {
        return this.maxShield;
    }

    @Override
    public float ramHitDamage(float killSeconds) {
        return this.shieldRegenPerTick * 20.0F + (this.maxShield + this.getMaxHealth()) / Math.max(1.0F, killSeconds);
    }

    @Override
    public void applyRamHit(LivingEntity ram, float damage) {
        float taken = Math.min(this.shield, damage);
        this.shield -= taken;
        if (taken > 0.0F && this.shield <= 0.0F) {
            this.shield = 0.0F;
            this.shieldDownTicks = SHIELD_REACTIVATION_TICKS;
            this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
        }
        float overflow = damage - taken;
        if (overflow > 0.0F) {
            this.damageHull(DamageSource.mobAttack(ram), overflow);
        }
        this.level.playSound(null, this.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (this.level.isClientSide() || this.isRemoved()) {
            return false;
        }
        if (source == DamageSource.OUT_OF_WORLD) {
            if (this.isRigTargetable() && amount >= this.getHealth()) {
                this.wreck();
                return true;
            }
            return super.hurt(source, amount);
        }
        if (!this.isRigTargetable() || !(source.getEntity() instanceof LivingEntity attacker) || attacker instanceof Player) {
            return false;
        }
        if (attacker.getTags().contains(ExtractionSpawnManager.TAG_RAM)) {
            this.applyRamHit(attacker, this.ramHitDamage(ModConfigs.EXTRACTION_OBJECTIVE.WAVES.ram.killSeconds));
            return true;
        }
        if (this.shield > 0.0F) {
            float absorbed = CombatRules.getDamageAfterAbsorb(amount, this.shieldToughness, 0.0F);
            float taken = Math.min(this.shield, absorbed);
            this.shield -= taken;
            this.invulnerableTime = 0;
            if (this.shield <= 0.0F) {
                this.shield = 0.0F;
                this.shieldDownTicks = SHIELD_REACTIVATION_TICKS;
                this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
            } else {
                this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.5F, 1.6F);
            }
            float overflow = absorbed <= 0.0F ? 0.0F : amount * (1.0F - taken / absorbed);
            if (overflow > 0.0F) {
                this.damageHull(source, CombatRules.getDamageAfterAbsorb(overflow, this.armor, 0.0F));
            }
            return true;
        }
        return this.damageHull(source, CombatRules.getDamageAfterAbsorb(amount, this.armor, 0.0F));
    }

    private boolean damageHull(DamageSource source, float amount) {
        if (amount <= 0.0F || this.isWrecked()) {
            return false;
        }
        if (amount >= this.getHealth()) {
            this.wreck();
            return true;
        }
        boolean result = super.hurt(source, amount);
        this.invulnerableTime = 0;
        return result;
    }

    private void wreck() {
        this.entityData.set(DATA_WRECKED, true);
        this.entityData.set(DATA_ACTIVE, false);
        this.setHealth(1.0F);
        this.shield = 0.0F;
        this.level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 0.8F);
        if (this.level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.2D, this.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.02D);
        }
        VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get)
                .ifPresentOrElse(objective -> objective.onThumperWrecked(this),
                        () -> WoldsVaults.LOGGER.error("Thumper {} was wrecked outside an extraction objective; no penalty was charged.", this.getUUID()));
    }

    /** Shield and health regeneration; the objective calls it only while the main rig drills with energy. */
    public void tickRegen() {
        if (this.isWrecked()) {
            return;
        }
        if (this.shieldDownTicks > 0) {
            this.shieldDownTicks--;
        } else if (this.shield < this.maxShield) {
            this.shield = Math.min(this.maxShield, this.shield + this.shieldRegenPerTick);
        }
        if (this.healthRegenPerTick > 0.0F && this.getHealth() < this.getMaxHealth()) {
            this.heal(this.healthRegenPerTick);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 localHit, InteractionHand hand) {
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get)
                .ifPresentOrElse(objective -> objective.interactThumper(this, player, player.getItemInHand(hand)),
                        () -> WoldsVaults.LOGGER.error("Thumper {} was used outside an extraction objective.", this.getUUID()));
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level.isClientSide()) {
            return;
        }
        if (this.isActive() && this.tickCount % THUMP_PERIOD_TICKS == 0 && this.level instanceof ServerLevel server) {
            BlockState below = this.level.getBlockState(this.blockPosition().below());
            if (!below.isAir()) {
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below), this.getX(), this.getY() + 0.1D, this.getZ(), 12, 0.6D, 0.05D, 0.6D, 0.1D);
            }
            this.level.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.25F, 0.5F);
        }
        if (this.tickCount % 100 == 0 && VaultUtils.getVault(this.level).isEmpty()) {
            WoldsVaults.LOGGER.info("Thumper entity {} found itself outside a vault and self-destructed.", this.getUUID());
            this.discard();
        }
    }

    @Override
    protected void tickDeath() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public void travel(@Nonnull Vec3 input) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean canBeAffected(@Nonnull MobEffectInstance effect) {
        return false;
    }

    @Override
    protected int getExperienceReward(@Nonnull Player player) {
        return 0;
    }

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(@Nonnull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("WvShield", this.shield);
        tag.putFloat("WvMaxShield", this.maxShield);
        tag.putFloat("WvShieldRegen", this.shieldRegenPerTick);
        tag.putFloat("WvHealthRegen", this.healthRegenPerTick);
        tag.putFloat("WvArmor", this.armor);
        tag.putFloat("WvShieldToughness", this.shieldToughness);
        tag.putInt("WvShieldDown", this.shieldDownTicks);
        tag.putInt("WvSiteIndex", this.siteIndex);
        tag.putInt("WvPadIndex", this.padIndex);
        tag.putBoolean("WvWrecked", this.isWrecked());
        if (this.rigKey != null) {
            tag.putUUID("WvRigKey", this.rigKey);
        }
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.shield = tag.getFloat("WvShield");
        this.maxShield = tag.getFloat("WvMaxShield");
        this.shieldRegenPerTick = tag.getFloat("WvShieldRegen");
        this.healthRegenPerTick = tag.getFloat("WvHealthRegen");
        this.armor = tag.getFloat("WvArmor");
        this.shieldToughness = tag.getFloat("WvShieldToughness");
        this.shieldDownTicks = tag.getInt("WvShieldDown");
        this.siteIndex = tag.getInt("WvSiteIndex");
        this.padIndex = tag.getInt("WvPadIndex");
        this.entityData.set(DATA_WRECKED, tag.getBoolean("WvWrecked"));
        this.rigKey = tag.hasUUID("WvRigKey") ? tag.getUUID("WvRigKey") : null;
    }
}
