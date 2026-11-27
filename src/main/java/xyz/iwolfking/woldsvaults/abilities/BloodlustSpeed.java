package xyz.iwolfking.woldsvaults.abilities;

import iskallia.vault.skill.ability.effect.RampageBloodlustAbility;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Bloodlust's movement speed per stack. VH's speed effect hard-codes 1% per stack and never reads the ability's
 * {@code speedPerStack}; the effect asks here instead: the player's learned Bloodlust tier value times the Intensify
 * factor of the toggle's activation.
 */
public final class BloodlustSpeed {
    private static final Set<UUID> MISSING_WARNED = ConcurrentHashMap.newKeySet();

    private BloodlustSpeed() {
    }

    /**
     * Speed per stack for {@code player}'s Bloodlust, or {@code fallback} (the effect's own value) with an error logged
     * once per player when the player has no learned Bloodlust tier.
     */
    public static double speedPerStack(ServerPlayer player, double fallback) {
        RampageBloodlustAbility ability = learned(player);
        if (ability == null) {
            if (MISSING_WARNED.add(player.getUUID())) {
                WoldLogger.error("Bloodlust speed: {} has the speed effect but no learned Bloodlust tier; using the effect's own {} per stack.",
                        player.getGameProfile().getName(), fallback);
            }
            return fallback;
        }
        return ability.getSpeedPerStack() * BuffGlyphs.intensity(ability);
    }

    private static RampageBloodlustAbility learned(ServerPlayer player) {
        AbilityTree tree = PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);
        RampageBloodlustAbility[] found = new RampageBloodlustAbility[1];
        tree.iterate(TieredSkill.class, skill -> {
            if (found[0] == null && skill.isUnlocked() && skill.getChild() instanceof RampageBloodlustAbility bloodlust) {
                found[0] = bloodlust;
            }
        });
        return found[0];
    }
}
