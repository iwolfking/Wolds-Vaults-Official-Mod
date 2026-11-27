package xyz.iwolfking.woldsvaults.entities.extraction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.extraction.ExtractionSpawnManager;

/**
 * A rig's shield and armor and its regeneration, shared by the main rig and its thumpers. Mob hits strike the shield
 * first (mitigated by shield toughness) and overflow to the hull; ram hits are true damage, shield then hull. A shield
 * knocked to zero stays offline for the table's reactivation time before it recharges.
 */
public final class RigPool {
    private float shield;
    private float maxShield;
    private float shieldRegenPerTick;
    private float healthRegenPerTick;
    private float armor;
    private float shieldToughness;
    private int shieldDownTicks;
    private long lastHitTime = Long.MIN_VALUE;

    public void init(float maxShield, float shieldRegenPerTick, float healthRegenPerTick, float armor, float shieldToughness) {
        this.maxShield = maxShield;
        this.shield = maxShield;
        this.shieldRegenPerTick = shieldRegenPerTick;
        this.healthRegenPerTick = healthRegenPerTick;
        this.armor = armor;
        this.shieldToughness = shieldToughness;
        this.shieldDownTicks = 0;
    }

    public float getShield() {
        return this.shield;
    }

    public float getMaxShield() {
        return this.maxShield;
    }

    public boolean isShieldUp() {
        return this.shield > 0.0F;
    }

    public void dropShield() {
        this.shield = 0.0F;
    }

    /**
     * A mob hit after the table's rig resistance for the medallion factor the attacker was scaled to; applied before
     * the shield, armor and durability see the hit. Ram hits never pass through here.
     */
    public static float resistMobHit(LivingEntity attacker, float amount) {
        return (float) (amount * ModConfigs.EXTRACTION_OBJECTIVE.WAVES.rigResistance.damageTaken(ExtractionSpawnManager.mobFactor(attacker)));
    }

    /** Takes a mob hit on the shield; returns the share of {@code amount} that overflows to the hull, before armor. */
    public float shieldHit(LivingEntity rig, float amount) {
        float absorbed = CombatRules.getDamageAfterAbsorb(amount, this.shieldToughness, 0.0F);
        float taken = Math.min(this.shield, absorbed);
        this.shield -= taken;
        if (this.shield <= 0.0F) {
            this.knockDown(rig);
        } else {
            rig.level.playSound(null, rig.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.6F, 1.4F);
        }
        return absorbed <= 0.0F ? 0.0F : amount * (1.0F - taken / absorbed);
    }

    /** Hull damage after armor. */
    public float armorMitigate(float amount) {
        return CombatRules.getDamageAfterAbsorb(amount, this.armor, 0.0F);
    }

    /** Damage of one ram hit: shield regeneration per second plus (max shield + max health) / killSeconds. */
    public float ramHitDamage(float maxHealth, float killSeconds) {
        return this.shieldRegenPerTick * 20.0F + (this.maxShield + maxHealth) / Math.max(1.0F, killSeconds);
    }

    /** Takes a ram hit as true damage on the shield; returns what overflows to the hull, ignoring armor. */
    public float ramHit(LivingEntity rig, float damage) {
        float taken = Math.min(this.shield, damage);
        this.shield -= taken;
        if (taken > 0.0F && this.shield <= 0.0F) {
            this.knockDown(rig);
        }
        rig.level.playSound(null, rig.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 1.0F, 0.5F);
        return damage - taken;
    }

    /** Records that a mob or ram hit reached the rig, for the hurt outline. */
    public void noteHit(LivingEntity rig) {
        this.lastHitTime = rig.level.getGameTime();
    }

    /**
     * Keeps the rig's glow outline: none when {@code shown} is false, else the table's outline colour, switching with
     * the hurt colour for a while after a hit.
     */
    public void tickOutline(LivingEntity rig, boolean shown) {
        rig.setGlowingTag(shown);
        if (!shown) {
            return;
        }
        ExtractionObjectiveConfig.DeploymentEntry cfg = ModConfigs.EXTRACTION_OBJECTIVE.DEPLOYMENT;
        long now = rig.level.getGameTime();
        boolean hurt = now - this.lastHitTime < cfg.outlineHurtTicks && (now / Math.max(1, cfg.outlineBlinkTicks)) % 2L == 0L;
        GlowTeams.setColour(rig, hurt ? cfg.hurtOutlineColour : cfg.outlineColour);
    }

    /** One tick of shield recharge, or of the reactivation countdown while the shield is down. */
    public void tickShield(LivingEntity rig) {
        if (this.shieldDownTicks > 0) {
            this.shieldDownTicks--;
            if (this.shieldDownTicks == 0 && this.maxShield > 0.0F) {
                rig.level.playSound(null, rig.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.5F, 1.6F);
            }
            return;
        }
        if (this.shield < this.maxShield) {
            this.shield = Math.min(this.maxShield, this.shield + this.shieldRegenPerTick);
        }
    }

    /** One tick of hull regeneration. */
    public void tickHealth(LivingEntity rig) {
        if (this.healthRegenPerTick > 0.0F && rig.getHealth() < rig.getMaxHealth()) {
            rig.heal(this.healthRegenPerTick);
        }
    }

    private void knockDown(LivingEntity rig) {
        this.shield = 0.0F;
        this.shieldDownTicks = ModConfigs.EXTRACTION_OBJECTIVE.DEPLOYMENT.shieldReactivationTicks;
        rig.level.playSound(null, rig.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
    }

    public void save(CompoundTag tag) {
        tag.putFloat("WvShield", this.shield);
        tag.putFloat("WvMaxShield", this.maxShield);
        tag.putFloat("WvShieldRegen", this.shieldRegenPerTick);
        tag.putFloat("WvHealthRegen", this.healthRegenPerTick);
        tag.putFloat("WvArmor", this.armor);
        tag.putFloat("WvShieldToughness", this.shieldToughness);
        tag.putInt("WvShieldDown", this.shieldDownTicks);
    }

    public void load(CompoundTag tag) {
        this.shield = tag.getFloat("WvShield");
        this.maxShield = tag.getFloat("WvMaxShield");
        this.shieldRegenPerTick = tag.getFloat("WvShieldRegen");
        this.healthRegenPerTick = tag.getFloat("WvHealthRegen");
        this.armor = tag.getFloat("WvArmor");
        this.shieldToughness = tag.getFloat("WvShieldToughness");
        this.shieldDownTicks = tag.getInt("WvShieldDown");
    }
}
