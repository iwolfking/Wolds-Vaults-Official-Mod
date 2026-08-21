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
    public static final String BAD_EFFECTS_KEY = "the_vault.gear_attribute.effect_avoidance.avoidance.bad_effects";
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
        addBanded(values, player, VelaraNode.TOUGH, VelaraNode.TOUGH_II, ModGearAttributes.HEALTH_PERCENTILE);
        addBanded(values, player, VelaraNode.ARMORED, VelaraNode.ARMORED_II, ModGearAttributes.ARMOR_PERCENTILE);
        addBanded(values, player, VelaraNode.HEALTHY, VelaraNode.HEALTHY_II, ModGearAttributes.HEALING_EFFECTIVENESS);
        addBanded(values, player, VelaraNode.FAST_REFLEXES, VelaraNode.FAST_REFLEXES_II, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT);
        addBanded(values, player, VelaraNode.GUARDED, VelaraNode.GUARDED_II, ModGearAttributes.BLOCK);
        addBanded(values, player, VelaraNode.THORNY, VelaraNode.THORNY_II, ModGearAttributes.THORNS_DAMAGE_FLAT);
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

    private static void addBanded(List<VaultGearAttributeInstance<?>> values, ServerPlayer player, VelaraNode lesser,
                                  VelaraNode greater, VaultGearAttribute<Float> attribute) {
        float value = VelaraValues.banded(VelaraValues.bands(lesser, greater),
                VelaraNodeState.investedPoints(player, lesser),
                VelaraNodeState.investedPoints(player, greater));
        if (value > 0.0F) {
            values.add(new VaultGearAttributeInstance<>(attribute, value));
        }
    }

    private static void addUtilized(List<VaultGearAttributeInstance<?>> values, int points) {
        if (points <= 0) {
            return;
        }
        values.add(new VaultGearAttributeInstance<>(ModGearAttributes.ABILITY_LEVEL,
                new AbilityLevelAttribute("UTILITY", VelaraValues.utilizedAbilityLevels())));
    }

    private static void addEffectAvoidance(List<VaultGearAttributeInstance<?>> values, ServerPlayer player) {
        float chance = VelaraValues.banded(VelaraValues.bands(VelaraNode.IMMUNE, VelaraNode.IMMUNE_II),
                VelaraNodeState.investedPoints(player, VelaraNode.IMMUNE),
                VelaraNodeState.investedPoints(player, VelaraNode.IMMUNE_II));
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

    public static synchronized List<MobEffect> resolveBadEffects() {
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
