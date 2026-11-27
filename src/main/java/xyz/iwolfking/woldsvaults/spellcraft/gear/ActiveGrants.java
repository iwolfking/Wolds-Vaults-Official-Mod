package xyz.iwolfking.woldsvaults.spellcraft.gear;

import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;
import xyz.iwolfking.woldsvaults.spellcraft.scope.GlyphGrants;

import javax.annotation.Nullable;

public final class ActiveGrants {
    private ActiveGrants() {
    }

    /**
     * The grants of the open cast scope's step, else of the deferred scope of one of the player's spell steps; null
     * outside both, so a step that grants nothing still decides the read.
     */
    @Nullable
    static GlyphGrants ofCast(ServerPlayer player) {
        return of(player, CastScope.forPlayer(player));
    }

    /**
     * Like {@link #ofCast}, for a read made by {@code reader}: the open cast scope decides only when the reader is the
     * step's own ability, and is passed over for any other skill's read and for a read that names no skill.
     */
    @Nullable
    static GlyphGrants ofRead(ServerPlayer player, @Nullable Skill reader) {
        return of(player, CastScope.forRead(player, reader));
    }

    @Nullable
    private static GlyphGrants of(ServerPlayer player, @Nullable CastScope.Active scope) {
        if (scope != null) {
            return scope.modifiers().grants();
        }
        DeferredReach.Reach reach = DeferredReach.current(player);
        if (reach != null) {
            return reach.glyphs().modifiers().grants();
        }
        return null;
    }
}
