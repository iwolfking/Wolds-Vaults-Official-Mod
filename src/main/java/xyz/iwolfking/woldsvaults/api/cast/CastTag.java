package xyz.iwolfking.woldsvaults.api.cast;

import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredGlyphs;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * One ability cast: who cast it, which skill, and the per-cast state its hits share. Entities spawned and tasks
 * scheduled during the cast carry the same instance, so a value filled in after the cast starts (the mana it paid) is
 * visible to every later hit.
 */
public final class CastTag {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final long castId;
    private final UUID owner;
    private final Skill skill;
    private float damageMultiplier = 1.0F;
    private float manaConsumed;
    private boolean powerDumpResolved;
    private boolean dumpsBar;
    private boolean leechUsed;
    private boolean luckyResetUsed;
    private boolean arcaneStrikeUsed;
    private Object manaStealEvent;
    private Object lifeStealEvent;
    @Nullable
    private DeferredGlyphs glyphs;

    public CastTag(ServerPlayer owner, Skill skill) {
        this.castId = NEXT_ID.incrementAndGet();
        this.owner = owner.getUUID();
        this.skill = skill;
    }

    public long castId() {
        return this.castId;
    }

    public UUID owner() {
        return this.owner;
    }

    public Skill skill() {
        return this.skill;
    }

    public boolean isOwnedBy(ServerPlayer player) {
        return player != null && this.owner.equals(player.getUUID());
    }

    public boolean isCastOf(ServerPlayer player, Skill skill) {
        return this.isOwnedBy(player) && this.skill == skill;
    }

    /** The spell step's frozen glyphs when this cast ran as a spell step; null for any other cast. */
    @Nullable
    public DeferredGlyphs glyphs() {
        return this.glyphs;
    }

    public void setGlyphs(@Nullable DeferredGlyphs glyphs) {
        this.glyphs = glyphs;
    }

    public float damageMultiplier() {
        return this.damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public float manaConsumed() {
        return this.manaConsumed;
    }

    public void setManaConsumed(float manaConsumed) {
        this.manaConsumed = manaConsumed;
    }

    public boolean powerDumpResolved() {
        return this.powerDumpResolved;
    }

    public void resolvePowerDump(boolean dumpsBar, float damageMultiplier) {
        this.powerDumpResolved = true;
        this.dumpsBar = dumpsBar;
        this.damageMultiplier = damageMultiplier;
    }

    public boolean dumpsBar() {
        return this.dumpsBar;
    }

    /** Claims the cast's one leech; false when an earlier hit of this cast already leeched. */
    public boolean claimLeech() {
        if (this.leechUsed) {
            return false;
        }
        this.leechUsed = true;
        return true;
    }

    /** Claims the cast's one Lucky Reset; false when an earlier hit of this cast already reset. */
    public boolean claimLuckyReset() {
        if (this.luckyResetUsed) {
            return false;
        }
        this.luckyResetUsed = true;
        return true;
    }

    /** Claims the cast's one Arcane Strike; false when an earlier lucky hit of this cast already cut cooldowns. */
    public boolean claimArcaneStrike() {
        if (this.arcaneStrikeUsed) {
            return false;
        }
        this.arcaneStrikeUsed = true;
        return true;
    }

    /**
     * Claims the cast's one Mana Steal proc for {@code event}. The same hurt event may claim again, so a proc that runs
     * twice on one hit (Luckiest Hit) still doubles.
     */
    public boolean claimManaSteal(Object event) {
        if (this.manaStealEvent == null) {
            this.manaStealEvent = event;
            return true;
        }
        return this.manaStealEvent == event;
    }

    /** Claims the cast's one Life Steal proc for {@code event}, with the same rule as Mana Steal. */
    public boolean claimLifeSteal(Object event) {
        if (this.lifeStealEvent == null) {
            this.lifeStealEvent = event;
            return true;
        }
        return this.lifeStealEvent == event;
    }
}
