package xyz.iwolfking.woldsvaults.spellcraft.buff;

import iskallia.vault.block.entity.TotemTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.scope.GlyphGrants;
import xyz.iwolfking.woldsvaults.spellcraft.scope.PlacedBlockGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notes on how lingering buffs were cast, so an etching glyph a buff reads after its cast comes from the cast that made
 * the buff. Every ability cast leaves a note per player and ability group: the spell step's grants, or plain for a base
 * bar cast, which reads the player's gear auto line. Damage totems note each player they cover on VH's totem tick with
 * the glyphs of the spell that placed them, or plain for a totem placed from the base bar, which reads its owner's gear
 * auto line.
 */
public final class LingeringBuffs {
    private static final Object LISTENER_OWNER = new Object();
    private static final Map<UUID, Map<String, Source>> NOTES = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<BlockPos, Cover>> COVERS = new ConcurrentHashMap<>();
    private static final long COVER_TICKS = 5L;

    private LingeringBuffs() {
    }

    private record Source(@Nullable GlyphGrants grants) {
    }

    private record Cover(Source source, @Nullable UUID owner, long expires) {
    }

    /** The outcome of a lingering read: whether a note decided it, and the grant it gives (null for none). */
    public record Resolution(boolean known, @Nullable GlyphGrants.Grant grant) {
        static final Resolution UNKNOWN = new Resolution(false, null);
    }

    public static void init() {
        CommonEvents.ABILITY_CAST.register(LISTENER_OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                note(player, data.getAbility());
            }
        });
    }

    private static void note(ServerPlayer player, Ability ability) {
        String group = groupOf(ability.getParent());
        if (group == null) {
            WoldLogger.errorOnce("LingeringBuffs:group:" + ability.getClass().getSimpleName(), "Spellcraft: an ability cast (" + ability.getClass().getSimpleName()
                    + ") has no ability group; its lingering buffs keep no cast note and read the gear auto line");
            return;
        }
        CastScope.Active scope = CastScope.forPlayer(player);
        Source source = new Source(scope == null ? null : scope.modifiers().grants());
        NOTES.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(group, source);
    }

    /** Notes that the damage totem {@code tile} covers {@code player} this tick; called from VH's totem tick. */
    public static void noteCover(ServerPlayer player, TotemTileEntity tile) {
        CastTag tag = PlacedBlockGlyphs.tagOrRestore(tile);
        DeferredGlyphs glyphs = tag == null ? null : tag.glyphs();
        Source source = new Source(glyphs == null ? null : glyphs.modifiers().grants());
        long expires = player.getLevel().getGameTime() + COVER_TICKS;
        COVERS.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(tile.getBlockPos().immutable(), new Cover(source, tile.getPlayerUUID(), expires));
    }

    /** The glyph a lingering etching read gets for {@code player}, from the note its source kind names. */
    public static Resolution resolve(ServerPlayer player, GlyphType type, String lingering, String spec, SpellcraftConfig config) {
        if (SpellcraftConfig.EtchingGlyph.LINGERING_TOTEM.equals(lingering)) {
            return fromCovers(player, type, config);
        }
        return fromNote(player, type, spec, config);
    }

    private static Resolution fromNote(ServerPlayer player, GlyphType type, String spec, SpellcraftConfig config) {
        Map<String, Source> notes = NOTES.get(player.getUUID());
        String group = AbilityLookup.tree(player).getForId(spec).map(LingeringBuffs::groupOf).orElse(spec);
        Source source = notes == null ? null : notes.get(group);
        if (source == null) {
            return Resolution.UNKNOWN;
        }
        return new Resolution(true, source.grants() == null ? autoLine(player, type, config) : source.grants().get(type));
    }

    private static Resolution fromCovers(ServerPlayer player, GlyphType type, SpellcraftConfig config) {
        Map<BlockPos, Cover> covers = COVERS.get(player.getUUID());
        if (covers == null || covers.isEmpty()) {
            return Resolution.UNKNOWN;
        }
        long now = player.getLevel().getGameTime();
        boolean live = false;
        GlyphGrants.Grant best = null;
        for (Iterator<Cover> it = covers.values().iterator(); it.hasNext(); ) {
            Cover cover = it.next();
            if (cover.expires() < now) {
                it.remove();
                continue;
            }
            live = true;
            GlyphGrants.Grant grant = cover.source().grants() != null ? cover.source().grants().get(type) : ownerAutoLine(player.getServer(), cover.owner(), type, config);
            if (grant != null && (best == null || grant.potency() > best.potency())) {
                best = grant;
            }
        }
        return live ? new Resolution(true, best) : Resolution.UNKNOWN;
    }

    @Nullable
    private static GlyphGrants.Grant ownerAutoLine(@Nullable MinecraftServer server, @Nullable UUID owner, GlyphType type, SpellcraftConfig config) {
        ServerPlayer player = server == null || owner == null ? null : server.getPlayerList().getPlayer(owner);
        return player == null ? null : autoLine(player, type, config);
    }

    /** The player's best gear auto line of the type, as a grant, or null. */
    @Nullable
    public static GlyphGrants.Grant autoLine(ServerPlayer player, GlyphType type, SpellcraftConfig config) {
        GlyphViewLine line = EquippedSources.get(player).autoLines(config).get(type);
        return line == null ? null : new GlyphGrants.Grant(line.potency(), line.quality());
    }

    @Nullable
    private static String groupOf(@Nullable Skill spec) {
        if (spec == null) {
            return null;
        }
        Skill group = spec.getParent();
        return group instanceof SpecializedSkill && group.getId() != null ? group.getId() : spec.getId();
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        NOTES.remove(event.getPlayer().getUUID());
        COVERS.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NOTES.clear();
        COVERS.clear();
    }
}
