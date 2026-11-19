package xyz.iwolfking.woldsvaults.spellcraft.hit;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastModifiers;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class GlyphArea {
    private static final Object LISTENER_OWNER = new Object();
    @Nullable
    private static Skill reading;

    private GlyphArea() {
    }

    public static void register() {
        CommonEvents.PLAYER_STAT.register(LISTENER_OWNER, GlyphArea::onPlayerStat);
    }

    /**
     * Runs VH's area stat event for a read of {@code skill}'s area, so an open spell step's Area glyph applies only when
     * the skill read is the step's own ability. Server players only.
     */
    public static <T> T readFor(LivingEntity entity, @Nullable Skill skill, Supplier<T> read) {
        if (!(entity instanceof ServerPlayer)) {
            return read.get();
        }
        Skill outer = reading;
        reading = skill;
        try {
            return read.get();
        } finally {
            reading = outer;
        }
    }

    private static void onPlayerStat(PlayerStatEvent.Data data) {
        Entity entity = data.getEntity();
        if (entity == null) {
            return;
        }
        if (data.getStat() == PlayerStat.AREA_OF_EFFECT) {
            boolean inStep = CastScope.forPlayer(entity) != null;
            CastModifiers scope = scopeFor(entity);
            Projectile ticking = inStep ? null : HitAttribution.tickingProjectileOf(entity);
            float factor = inStep ? scope == null ? 1.0F : scope.area() : ticking != null ? ProjectileGlyphTags.area(ticking) : deferredArea(entity);
            if (factor != 1.0F) {
                data.setValue(data.getValue() * factor);
            }
        }
    }

    private static float deferredArea(Entity entity) {
        DeferredReach.Reach reach = DeferredReach.current(entity);
        if (reach == null || !OwnHits.hasSourceVia(reach.glyphs().spec(), reach.carriers())) {
            return 1.0F;
        }
        return reach.glyphs().modifiers().area();
    }

    @Nullable
    private static CastModifiers scopeFor(Entity entity) {
        CastScope.Active scope = CastScope.forRead(entity, reading);
        return scope == null ? null : scope.modifiers();
    }
}
