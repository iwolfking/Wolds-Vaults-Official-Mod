package xyz.iwolfking.woldsvaults.spellcraft.scope;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The glyph state of one spell step, frozen when the step's cast opens: whose cast it is, the ability spec, the step's
 * modifiers (Echo power and Charge already folded into damage, unique glyph grants included) and its Shotgun volley
 * member. It rides on the cast's tag onto everything the cast leaves behind (scheduled work, entities, block entities,
 * minions), so what the ability does later still gets the step's glyphs.
 */
public record DeferredGlyphs(UUID owner, String spec, CastModifiers modifiers, @Nullable ShotgunShot shot) {
    private static final String OWNER = "owner";
    private static final String SPEC = "spec";
    private static final String SHOT_GROUP = "shot_group";
    private static final String SHOT_MEMBER = "shot_member";

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

    /** {@code tag} when it carries a spell step's glyphs, else null (no tag, or a plain cast's tag). */
    @Nullable
    public static CastTag spellTag(@Nullable CastTag tag) {
        return tag != null && tag.glyphs() != null ? tag : null;
    }

    public boolean isOwnedBy(@Nullable Player player) {
        return player != null && this.owner.equals(player.getUUID());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(OWNER, this.owner);
        tag.putString(SPEC, this.spec);
        this.modifiers.save(tag);
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
        CastModifiers modifiers = CastModifiers.load(tag);
        if (modifiers == null) {
            WoldLogger.error("Deferred glyphs: saved state for {} lacks {}; the glyphs are dropped.", tag.getString(SPEC), CastModifiers.missingKey(tag));
            return null;
        }
        ShotgunShot shot = tag.contains(SHOT_GROUP) ? new ShotgunShot(tag.getLong(SHOT_GROUP), tag.getInt(SHOT_MEMBER)) : null;
        return new DeferredGlyphs(tag.getUUID(OWNER), tag.getString(SPEC), modifiers, shot);
    }
}
