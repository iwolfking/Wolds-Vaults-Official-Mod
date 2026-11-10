package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.cast.DeferredGlyphs;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The deferred scope: a spell step's glyphs acting on what its ability does after the cast returned. The glyphs ride on
 * the cast's tag ({@link CastContext}) onto scheduled work, entities and block entities; a damage-over-time entry keeps
 * the tag of the cast that applied it; Necromancy's minions read the player's last Necromancy cast. A deferred scope
 * opens no spell step, so it never bills mana, starts or blocks cooldowns, fires cast events or resolves Power Dump.
 * Hits count only when {@link SpellHits} lists them for the carrier that delivers them. Server thread only.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class DeferredReach {
    private static final Map<DamageSource, CastTag> DOT_SOURCES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<UUID, DeferredGlyphs> MINIONS = new ConcurrentHashMap<>();
    private static int suppressed;
    private static int echoing;

    private DeferredReach() {
    }

    /** A step's frozen glyphs and the carriers whose hit sources they reach here. */
    public record Reach(DeferredGlyphs glyphs, Set<SpellHits.Carrier> carriers) {
    }

    /**
     * The deferred glyphs acting for {@code owner} right now: those of the current cast tag when a deferred carrier
     * (not the cast itself) delivers it and the tag belongs to one of {@code owner}'s spell steps.
     */
    @Nullable
    public static Reach current(Entity owner) {
        if (suppressed > 0 || !(owner instanceof ServerPlayer)) {
            return null;
        }
        CastTag tag = CastContext.current();
        SpellHits.Carrier carrier = CastContext.currentCarrier();
        if (tag == null || carrier == null || tag.glyphs() == null || !tag.glyphs().isOwnedBy((ServerPlayer) owner)) {
            return null;
        }
        Set<SpellHits.Carrier> reaches = carrier.reaches();
        return reaches == null ? null : new Reach(tag.glyphs(), reaches);
    }

    /**
     * The deferred glyphs a hurt belongs to: a damage-over-time tick of a spell step's entry, a Necromancy minion hit of
     * the owner's last Necromancy cast, the current deferred carrier, or a tagged entity hitting outside its own tick.
     */
    @Nullable
    public static Reach forHit(DamageSource source) {
        if (suppressed > 0) {
            return null;
        }
        Entity attacker = source.getEntity();
        ServerPlayer owner = ownerOf(attacker);
        if (owner == null) {
            return null;
        }
        CastTag dot = DOT_SOURCES.get(source);
        if (dot != null && dot.glyphs() != null && dot.glyphs().isOwnedBy(owner) && ActiveFlags.IS_DOT_ATTACKING.isSet()) {
            return new Reach(dot.glyphs(), EnumSet.of(SpellHits.Carrier.DOT));
        }
        if (attacker == owner && ActiveFlags.IS_NECROMANCY_MINION_ATTACKING.isSet()) {
            DeferredGlyphs minion = MINIONS.get(owner.getUUID());
            return minion == null ? null : new Reach(minion, EnumSet.of(SpellHits.Carrier.MINION));
        }
        Reach frame = current(owner);
        if (frame != null) {
            return frame;
        }
        CastTag direct = CastContext.tagOf(source.getDirectEntity());
        if (direct != null && direct.glyphs() != null && direct.glyphs().isOwnedBy(owner)) {
            return new Reach(direct.glyphs(), SpellHits.Carrier.ENTITY.reaches());
        }
        return null;
    }

    @Nullable
    private static ServerPlayer ownerOf(@Nullable Entity attacker) {
        if (attacker instanceof ServerPlayer player) {
            return player;
        }
        CastTag tag = CastContext.tagOf(attacker);
        if (tag == null || attacker.getServer() == null) {
            return null;
        }
        return attacker.getServer().getPlayerList().getPlayer(tag.owner());
    }

    /** Runs {@code action} with every deferred scope closed: etching recasts that share an own hit's carrier and signature. */
    public static <T> T suppress(Supplier<T> action) {
        suppressed++;
        try {
            return action.get();
        } finally {
            suppressed--;
        }
    }

    /** A damage-over-time entry was created: its ticks belong to the spell step whose tag is current. */
    public static void noteDamageOverTime(DamageSource source) {
        CastTag tag = CastContext.current();
        if (tag != null && tag.glyphs() != null && source != null) {
            DOT_SOURCES.put(source, tag);
        }
    }

    /** A Necromancy cast went through: its minions follow this cast's glyphs (none for a plain cast) until the next one. */
    public static void noteMinionCast(ServerPlayer player, @Nullable DeferredGlyphs glyphs) {
        if (glyphs == null) {
            MINIONS.remove(player.getUUID());
        } else {
            MINIONS.put(player.getUUID(), glyphs);
        }
    }

    /** The glyphs {@code player}'s Necromancy minions follow, or null. */
    @Nullable
    public static DeferredGlyphs minionGlyphs(ServerPlayer player) {
        return MINIONS.get(player.getUUID());
    }

    public static boolean echoingMinionHit() {
        return echoing > 0;
    }

    /** Runs a minion hit's Echo follow-up: its hits on the owner's targets ignore hurt immunity. */
    public static void echoMinionHit(Runnable hit) {
        echoing++;
        try {
            hit.run();
        } finally {
            echoing--;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (echoing > 0 && event.getSource().getEntity() instanceof ServerPlayer && ActiveFlags.IS_NECROMANCY_MINION_ATTACKING.isSet()) {
            event.getEntityLiving().invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MINIONS.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        MINIONS.clear();
        DOT_SOURCES.clear();
        suppressed = 0;
        echoing = 0;
    }
}
