package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectAvoidanceListGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodNodeAttributeSource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Turns invested Velara points into vault gear attributes for the snapshot fold.
 *
 * <p>Only the nodes whose whole effect is a stat live here; every node with behaviour is wired to
 * its own bus. Points are read straight from the ledger rather than through
 * {@link xyz.iwolfking.woldsvaults.gods.GodNodeGate}, because the caller
 * ({@link xyz.iwolfking.woldsvaults.gods.GodCarryover}) already decides whether this tree is the
 * active one and scales foreign trees to a quarter itself.
 */
public final class VelaraAttributeProvider implements GodTreeAttributeProviders.Provider {
    private static final String BAD_EFFECTS_KEY = "the_vault.gear_attribute.effect_avoidance.avoidance.bad_effects";
    private static final String[] BAD_EFFECT_IDS = {
            "poison",
            "wither",
            "levitation",
            "slowness",
            "blindness",
            "hunger",
            "the_vault:bleed",
            "the_vault:chilled",
            "the_vault:corruption"
    };

    private static List<MobEffect> badEffects;

    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, GodNodeAttributeSource.Scope scope) {
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        addRanked(values, player, VelaraNode.TOUGH, ModGearAttributes.HEALTH_PERCENTILE, VelaraValues.TOUGH_HEALTH_PERCENT);
        addRanked(values, player, VelaraNode.ARMORED, ModGearAttributes.ARMOR_PERCENTILE, VelaraValues.ARMORED_ARMOR_PERCENT);
        addRanked(values, player, VelaraNode.HEALTHY, ModGearAttributes.HEALING_EFFECTIVENESS, VelaraValues.HEALTHY_HEALING);
        addRanked(values, player, VelaraNode.FAST_REFLEXES, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, VelaraValues.FAST_REFLEXES_DODGE);
        addRanked(values, player, VelaraNode.GUARDED, ModGearAttributes.BLOCK, VelaraValues.GUARDED_BLOCK);
        addRanked(values, player, VelaraNode.THORNY, ModGearAttributes.THORNS_DAMAGE_FLAT, VelaraValues.THORNY_FLAT);
        addEffectAvoidance(values, player);
        if (scope == GodNodeAttributeSource.Scope.ALL) {
            addUtilized(values, VelaraNodeState.investedPoints(player, VelaraNode.UTILIZED));
        }
        return values;
    }

    /**
     * Minor-transfer resolution. Returns nothing while Velara is the active tree: the {@code ALL}
     * scope above already contributed every Velara minor, and any node id is accepted into a
     * minor-transfer slot, so a player who parks a Velara minor in Velara's own slots would
     * otherwise receive it twice.
     */
    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        if (ActiveGodResolver.isActive(player, VaultGod.VELARA) || !nodeIds.contains(VelaraNode.UTILIZED.getId())) {
            return List.of();
        }
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        addUtilized(values, VelaraNodeState.investedPoints(player, VelaraNode.UTILIZED));
        return values;
    }

    private static void addRanked(List<VaultGearAttributeInstance<?>> values, ServerPlayer player, VelaraNode node,
                                  VaultGearAttribute<Float> attribute, float[] table) {
        float value = VelaraValues.atRank(table, VelaraNodeState.investedPoints(player, node));
        if (value > 0.0F) {
            values.add(new VaultGearAttributeInstance<>(attribute, value));
        }
    }

    private static void addUtilized(List<VaultGearAttributeInstance<?>> values, int points) {
        if (points <= 0) {
            return;
        }
        values.add(new VaultGearAttributeInstance<>(ModGearAttributes.ABILITY_LEVEL,
                new AbilityLevelAttribute("UTILITY", VelaraValues.UTILIZED_ABILITY_LEVELS)));
    }

    private static void addEffectAvoidance(List<VaultGearAttributeInstance<?>> values, ServerPlayer player) {
        float chance = VelaraValues.atRank(VelaraValues.IMMUNE_AVOIDANCE, VelaraNodeState.investedPoints(player, VelaraNode.IMMUNE));
        if (chance <= 0.0F) {
            return;
        }
        List<MobEffect> effects = resolveBadEffects();
        if (effects.isEmpty()) {
            return;
        }
        values.add(new VaultGearAttributeInstance<>(ModGearAttributes.EFFECT_LIST_AVOIDANCE,
                new EffectAvoidanceListGearAttribute(effects, BAD_EFFECTS_KEY, chance)));
    }

    private static synchronized List<MobEffect> resolveBadEffects() {
        if (badEffects != null) {
            return badEffects;
        }
        List<MobEffect> resolved = new ArrayList<>();
        for (String id : BAD_EFFECT_IDS) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(id));
            if (effect == null) {
                WoldsVaults.LOGGER.error("Velara Immune could not resolve bad effect {}; it is dropped from the avoidance list.", id);
                continue;
            }
            resolved.add(effect);
        }
        badEffects = List.copyOf(resolved);
        return badEffects;
    }
}
