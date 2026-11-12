package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GlyphGrants;

import javax.annotation.Nullable;
import java.util.UUID;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The glyph state of one spell step, frozen when the step's cast opens: whose cast it is, the ability spec, the step's
 * modifiers (Echo power and Charge already folded into damage, unique glyph grants included) and its Shotgun volley
 * member. It rides on the cast's
 * tag onto everything the cast leaves behind (scheduled work, entities, block entities, minions), so what the ability
 * does later still gets the step's glyphs.
 */
public record DeferredGlyphs(UUID owner, String spec, CastModifiers modifiers, @Nullable ShotgunHits.Shot shot) {
    private static final String OWNER = "owner";
    private static final String SPEC = "spec";
    private static final String SHOT_GROUP = "shot_group";
    private static final String SHOT_MEMBER = "shot_member";
    private static final String GRANTS = "grants";
    private static final String[] FLOATS = {"damage", "area", "speed", "scale", "intensity", "duration", "drain", "heal_mana",
            "duration_upside", "homing_range", "homing_turn", "orbit", "gravity", "ethereal", "echo", "charge"};

    /**
     * The state for {@code player}'s cast of {@code spec} when a spell step for that spec and player is open, or null
     * outside a spell step (a plain key press, a proc that recasts the ability, another ability cast inside the step).
     */
    @Nullable
    public static DeferredGlyphs fromScope(ServerPlayer player, @Nullable String spec) {
        CastScope.Active scope = CastScope.current().orElse(null);
        if (scope == null || scope.player() != player || scope.spec() == null || !scope.spec().equals(spec)) {
            return null;
        }
        return new DeferredGlyphs(player.getUUID(), scope.spec(), scope.modifiers(), scope.shot());
    }

    public boolean isOwnedBy(@Nullable Player player) {
        return player != null && this.owner.equals(player.getUUID());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(OWNER, this.owner);
        tag.putString(SPEC, this.spec);
        CastModifiers m = this.modifiers;
        float[] values = {m.damage(), m.area(), m.speed(), m.scale(), m.intensity(), m.duration(), m.drain(), m.healMana(),
                m.durationUpside(), m.homingRange(), m.homingTurn(), m.orbitSeconds(), m.gravityReduction(), m.etherealSeconds(),
                m.echoPower(), m.chargeBonus()};
        for (int i = 0; i < FLOATS.length; i++) {
            tag.putFloat(FLOATS[i], values[i]);
        }
        tag.putInt("shotgun_copies", m.shotgunCopies());
        if (!m.grants().isEmpty()) {
            tag.put(GRANTS, m.grants().write());
        }
        if (this.shot != null) {
            tag.putLong(SHOT_GROUP, this.shot.group());
            tag.putInt(SHOT_MEMBER, this.shot.member());
        }
        return tag;
    }

    /** Reads a saved state; null with an error logged when the tag is incomplete. */
    @Nullable
    public static DeferredGlyphs load(CompoundTag tag) {
        if (!tag.hasUUID(OWNER) || !tag.contains(SPEC)) {
            WoldLogger.error("Deferred glyphs: saved state {} has no owner or spec; the glyphs are dropped.", tag);
            return null;
        }
        for (String key : FLOATS) {
            if (!tag.contains(key)) {
                WoldLogger.error("Deferred glyphs: saved state for {} lacks {}; the glyphs are dropped.", tag.getString(SPEC), key);
                return null;
            }
        }
        float[] v = new float[FLOATS.length];
        for (int i = 0; i < FLOATS.length; i++) {
            v[i] = tag.getFloat(FLOATS[i]);
        }
        CastModifiers modifiers = new CastModifiers(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12],
                v[13], tag.getInt("shotgun_copies"), v[14], v[15],
                tag.contains(GRANTS, Tag.TAG_COMPOUND) ? GlyphGrants.read(tag.getCompound(GRANTS)) : GlyphGrants.NONE);
        ShotgunHits.Shot shot = tag.contains(SHOT_GROUP) ? new ShotgunHits.Shot(tag.getLong(SHOT_GROUP), tag.getInt(SHOT_MEMBER)) : null;
        return new DeferredGlyphs(tag.getUUID(OWNER), tag.getString(SPEC), modifiers, shot);
    }
}
