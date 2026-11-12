package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.init.ModEffects;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.cast.DeferredGlyphs;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Intensify and Extend on buff abilities (redesign/spellcraft/BALANCE_PLAN_2026-11-10.md sec. 7). Each spec's own
 * getter or field asks here for its factor; the tier object is never changed. A value VH reads during the cast takes
 * the open spell step's factor ({@link #atCast}); a value VH re-reads while the buff runs takes the factor of the
 * activation that started it ({@link #live}), kept per player and spec until the buff or toggle ends, and replaced by
 * the next cast of that spec (with its own glyphs, or none). Which factors apply depends on the spec: a timed buff
 * takes intensity and duration, a toggle intensity and drain (its timed parts only the duration upside), a heal
 * intensity and heal mana.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class BuffGlyphs {
    private static final Set<String> HEALS = Set.of("Heal_Base", "Heal_Group");
    private static final Map<UUID, Map<String, Live>> BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<Skill, Live> BY_ABILITY = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<String, Supplier<MobEffect>> TIMED_EFFECTS = Map.of(
            "Colossus_Base", () -> xyz.iwolfking.woldsvaults.init.ModEffects.COLOSSUS,
            "Battle_Cry_Base", () -> ModEffects.BATTLE_CRY,
            "Battle_Cry_Spectral_Strike", () -> ModEffects.BATTLE_CRY_SPECTRAL_STRIKE,
            "Battle_Cry_Lucky_Strike", () -> ModEffects.BATTLE_CRY_LUCKY_STRIKE,
            "Stonefall_Base", () -> ModEffects.STONEFALL,
            "Stonefall_Snow", () -> ModEffects.STONEFALL_SHOCKWAVE,
            "Stonefall_Cold", () -> ModEffects.STONEFALL_COLD);
    private static Object checkedScope;
    private static Skill checkedAbility;
    private static boolean checkedBelongs;

    private BuffGlyphs() {
    }

    /** The factors one buff activation applies. Lower drain and heal mana are better. */
    public record Factors(float intensity, float duration, float durationUpside, float drain, float healMana) {
        public static final Factors NONE = new Factors(1.0F, 1.0F, 1.0F, 1.0F, 1.0F);

        public boolean isNone() {
            return this.equals(NONE);
        }
    }

    private record Live(UUID player, String spec, Skill ability, Factors factors) {
    }

    /** The factors a spell step's modifiers give {@code ability}, by what kind of buff it is. */
    public static Factors factorsOf(Skill ability, @Nullable CastModifiers modifiers) {
        if (modifiers == null) {
            return Factors.NONE;
        }
        if (ability instanceof ToggleAbility) {
            return new Factors(modifiers.intensity(), 1.0F, modifiers.durationUpside(), modifiers.drain(), 1.0F);
        }
        if (HEALS.contains(specOf(ability))) {
            return new Factors(modifiers.intensity(), 1.0F, 1.0F, 1.0F, modifiers.healMana());
        }
        return new Factors(modifiers.intensity(), modifiers.duration(), 1.0F, 1.0F, 1.0F);
    }

    /** Whether a step casting {@code spec} bills its heal mana factor (Intensify on a heal costs mana, not duration). */
    public static boolean billsHealMana(@Nullable String spec) {
        return HEALS.contains(spec);
    }

    /** The factors of the spell step open right now for {@code ability} (its owner's own instance), or none. */
    public static Factors atCast(Skill ability) {
        CastScope.Active scope = CastScope.current().orElse(null);
        if (scope == null || scope.spec() == null || !scope.spec().equals(specOf(ability))) {
            return Factors.NONE;
        }
        ServerPlayer player = scope.player();
        if (player.getServer() == null || !player.getServer().isSameThread() || !belongs(scope, player, ability)) {
            return Factors.NONE;
        }
        return factorsOf(ability, scope.modifiers());
    }

    /** The factors in effect for {@code ability} now: the open step's during its cast, else its running activation's. */
    public static Factors live(Skill ability) {
        Factors cast = atCast(ability);
        if (!cast.isNone()) {
            return cast;
        }
        Live live = BY_ABILITY.get(ability);
        return live == null ? Factors.NONE : live.factors();
    }

    public static float intensity(Skill ability) {
        return live(ability).intensity();
    }

    public static float drain(Skill ability) {
        return live(ability).drain();
    }

    public static float castIntensity(Skill ability) {
        return atCast(ability).intensity();
    }

    public static float castDuration(Skill ability) {
        return atCast(ability).duration();
    }

    /** An integer stat times a factor, rounded to nearest with a minimum of 1; zero and negative values stay. */
    public static int scale(int value, float factor) {
        if (factor == 1.0F || value <= 0) {
            return value;
        }
        return Math.max(1, Math.round(value * factor));
    }

    /** An instant cast of {@code ability} went through with {@code glyphs} (null: no spell step): starts its activation. */
    public static void onInstantCast(ServerPlayer player, Skill ability, @Nullable DeferredGlyphs glyphs) {
        String spec = specOf(ability);
        if (spec != null && TIMED_EFFECTS.containsKey(spec)) {
            start(player, spec, ability, factorsOf(ability, glyphs == null ? null : glyphs.modifiers()));
        }
    }

    /** A toggle switched: on, it starts an activation with the open step's factors (none outside a step); off, it ends. */
    public static void onToggle(ServerPlayer player, ToggleAbility ability) {
        String spec = specOf(ability);
        if (spec == null) {
            return;
        }
        if (!ability.isActive()) {
            end(player.getUUID(), spec);
            return;
        }
        DeferredGlyphs glyphs = DeferredGlyphs.fromScope(player, spec);
        start(player, spec, ability, factorsOf(ability, glyphs == null ? null : glyphs.modifiers()));
    }

    private static void start(ServerPlayer player, String spec, Skill ability, Factors factors) {
        end(player.getUUID(), spec);
        if (factors.isNone()) {
            return;
        }
        Live live = new Live(player.getUUID(), spec, ability, factors);
        BY_PLAYER.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(spec, live);
        BY_ABILITY.put(ability, live);
    }

    private static void end(UUID player, String spec) {
        Map<String, Live> lives = BY_PLAYER.get(player);
        Live live = lives == null ? null : lives.remove(spec);
        if (live != null) {
            BY_ABILITY.remove(live.ability());
        }
    }

    private static boolean alive(ServerPlayer player, Live live) {
        if (live.ability() instanceof ToggleAbility toggle) {
            return toggle.isActive();
        }
        Supplier<MobEffect> effect = TIMED_EFFECTS.get(live.spec());
        return effect != null && player.hasEffect(effect.get());
    }

    @Nullable
    private static String specOf(Skill ability) {
        Skill parent = ability.getParent();
        return parent == null ? null : parent.getId();
    }

    private static boolean belongs(CastScope.Active scope, ServerPlayer player, Skill ability) {
        if (checkedScope != scope || checkedAbility != ability) {
            checkedScope = scope;
            checkedAbility = ability;
            checkedBelongs = AugmentAbility.serverTree(player).getForId(scope.spec())
                    .filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast)
                    .map(TieredSkill::getChild).orElse(null) == ability;
        }
        return checkedBelongs;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Map<String, Live> lives = BY_PLAYER.get(player.getUUID());
        if (lives == null) {
            return;
        }
        lives.values().removeIf(live -> {
            if (alive(player, live)) {
                return false;
            }
            BY_ABILITY.remove(live.ability());
            return true;
        });
        if (lives.isEmpty()) {
            BY_PLAYER.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Map<String, Live> lives = BY_PLAYER.remove(event.getPlayer().getUUID());
        if (lives != null) {
            lives.values().forEach(live -> BY_ABILITY.remove(live.ability()));
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BY_PLAYER.clear();
        BY_ABILITY.clear();
        checkedScope = null;
        checkedAbility = null;
    }
}
