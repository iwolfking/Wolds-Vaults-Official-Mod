package xyz.iwolfking.woldsvaults.spellcraft.ability;

import iskallia.vault.core.vault.challenge.curse.ChallengeCurseHelper;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * One VH ability specialization as a spell step: its traits from the spellcraft table, and the bridge that casts it. A
 * native cast is one real key press on the player's own ability (mana, cooldown, curses, downed, cast-failure chance).
 * An extra cast runs only the instant's action, starts no cooldown, and is refused under the same curse and downed
 * gates a key press checks.
 */
public final class SpellAbility {
    private final String id;
    private final Set<AbilityTrait> traits;

    public SpellAbility(String id, Set<AbilityTrait> traits) {
        this.id = id;
        this.traits = traits.isEmpty() ? EnumSet.noneOf(AbilityTrait.class) : EnumSet.copyOf(traits);
    }

    public String id() {
        return this.id;
    }

    public boolean has(AbilityTrait trait) {
        return this.traits.contains(trait);
    }

    private Optional<TieredSkill> findSpecialization(AbilityTree tree) {
        if (tree == null) {
            return Optional.empty();
        }
        return tree.getForId(this.id).filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast);
    }

    public Optional<Ability> findAbility(ServerPlayer player) {
        return findSpecialization(AbilityLookup.tree(player))
                .filter(TieredSkill::isUnlocked)
                .map(TieredSkill::getChild)
                .filter(Ability.class::isInstance)
                .map(Ability.class::cast);
    }

    /** One native key press on the player's own ability. With an origin, VH's skill source carries that position. */
    public CastResult cast(ServerPlayer player, @Nullable CastOrigin origin) {
        AbilityTree tree = AbilityLookup.tree(player);
        Class<? extends Ability> kind = has(AbilityTrait.TOGGLE) ? ToggleAbility.class : InstantAbility.class;
        Lookup<? extends Ability> lookup = lookup(tree, kind);
        if (lookup.failure() != null) {
            return lookup.failure();
        }
        Ability ability = lookup.ability();
        if (ability.isTreeOnCooldown()) {
            return CastResult.ON_COOLDOWN;
        }
        boolean wasActive = ability.isActive();
        SkillContext context = contextFor(player, origin);
        ability.onKeyDown(context);
        AbilityCastTracker.Outcome outcome = AbilityCastTracker.watch(ability, () -> ability.onKeyUp(context));
        if (!outcome.accepted()) {
            return CastResult.BLOCKED;
        }
        tree.sync(context);
        if (ability instanceof ToggleAbility) {
            if (ability.isActive() != wasActive) {
                return ability.isActive() ? CastResult.STARTED : CastResult.STOPPED;
            }
            return CastResult.FAILED;
        }
        return outcome.cast() ? CastResult.SUCCESS : CastResult.FAILED;
    }

    /**
     * One extra cast, used for every step after the trigger: runs the instant's action without the key-press cooldown.
     * Refused while the player is downed or a challenge curse disables the ability, as a key press would be.
     */
    public CastResult castExtra(ServerPlayer player, @Nullable CastOrigin origin) {
        AbilityTree tree = AbilityLookup.tree(player);
        Lookup<InstantAbility> lookup = lookup(tree, InstantAbility.class);
        if (lookup.failure() != null) {
            return lookup.failure();
        }
        InstantAbility ability = lookup.ability();
        if (DownedPlayerManager.isPlayerDowned(player)
                || ChallengeCurseHelper.isAbilityDisabled(player.getUUID(), ability.getClass().getSimpleName())) {
            return CastResult.BLOCKED;
        }
        SkillContext context = contextFor(player, origin);
        AbilityCastTracker.Outcome outcome = AbilityCastTracker.watch(ability, () -> {
            ability.onAction(context);
            return true;
        });
        tree.sync(context);
        return outcome.cast() ? CastResult.SUCCESS : CastResult.FAILED;
    }

    private static SkillContext contextFor(ServerPlayer player, @Nullable CastOrigin origin) {
        return origin == null
                ? SkillContext.of(player)
                : SkillContext.of(player, SkillSource.of(player).setPos(origin.position()));
    }

    private <T extends Ability> Lookup<T> lookup(AbilityTree tree, Class<T> type) {
        Optional<TieredSkill> specialization = findSpecialization(tree);
        if (specialization.isEmpty()) {
            return Lookup.failed(CastResult.UNSUPPORTED);
        }
        if (!specialization.get().isUnlocked()) {
            return Lookup.failed(CastResult.NOT_LEARNED);
        }
        if (!type.isInstance(specialization.get().getChild())) {
            return Lookup.failed(CastResult.UNSUPPORTED);
        }
        return new Lookup<>(type.cast(specialization.get().getChild()), null);
    }

    private record Lookup<T>(T ability, CastResult failure) {
        static <T> Lookup<T> failed(CastResult failure) {
            return new Lookup<>(null, failure);
        }
    }
}
