package xyz.iwolfking.woldsvaults.builds.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectAvoidanceListGearAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.VaultUsesHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.Json;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.models.MythicCharms;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Locale;

/**
 * Builds the mythic god charm a build file's {@code mythic_charm} spec describes. The charm is made the way the god
 * debug command makes one (identified, item level 0, the god, loot, legendary when a draw is), its draws are stored
 * as given and materialized at the build's piety units, and it is pinned so the wearer's piety never rescales it.
 */
public final class MythicCharmCodec {
    private static final Set<String> KEYS = Set.of("god", "units", "uses", "rolls");
    private static final Set<String> ROLL_KEYS = Set.of("slot", "key", "q", "legendary", "value", "attribute", "per_unit");
    private static final String ROLL_TYPE = "Mythic";
    private static final int DEFAULT_USES = 30;
    private static final double RELATIVE_TOLERANCE = 1.0E-4;
    private static final double ABSOLUTE_TOLERANCE = 1.0E-6;

    private MythicCharmCodec() {
    }

    /**
     * Builds the pinned mythic charm of {@code json} ({god, units, uses, rolls: [{slot, key, q, legendary, value}]}).
     * Unknown roll keys are errors; draws an identification could not roll are listed as bypassed gates; every
     * materialized value that differs from the file's {@code value} is a fallback.
     */
    public static ItemStack read(JsonObject json, String path, BuildReport report) {
        Json.onlyKeys(json, path, KEYS);
        VaultGod god = GearCodec.enumByName(VaultGod.values(), Json.string(json, "god", path), path + ".god");
        Integer units = Json.optInt(json, "units", path);
        if (units == null) {
            throw new BuildException(path + ".units: missing (the piety units the build's values were materialized at)");
        }
        if (units < 0) {
            throw new BuildException(path + ".units: must be 0 or more, got " + units);
        }
        Integer uses = Json.optInt(json, "uses", path);
        if (uses == null) {
            uses = DEFAULT_USES;
            report.fallback(path + ": no vault uses given; using " + DEFAULT_USES + " like a fresh identification");
        }
        JsonArray rolls = Json.optArray(json, "rolls", path);
        if (rolls == null || rolls.size() == 0) {
            throw new BuildException(path + ".rolls: list the charm's draws");
        }
        List<MythicCharmRolls.StoredRoll> draws = new ArrayList<>();
        List<JsonObject> entries = new ArrayList<>();
        for (int i = 0; i < rolls.size(); i++) {
            String entryPath = path + ".rolls[" + i + "]";
            JsonObject entry = Json.asObject(rolls.get(i), entryPath);
            Json.onlyKeys(entry, entryPath, ROLL_KEYS);
            VaultGearModifier.AffixType slot = GearCodec.enumByName(VaultGearModifier.AffixType.values(),
                    Json.string(entry, "slot", entryPath), entryPath + ".slot");
            String key = Json.string(entry, "key", entryPath);
            if (!MythicCharmRolls.knowsKey(god, slot, key)) {
                throw new BuildException(entryPath + ".key: a " + god.getName() + " mythic charm has no "
                        + slot.name().toLowerCase(Locale.ROOT) + " roll '" + key + "'");
            }
            Integer quality = Json.optInt(entry, "q", entryPath);
            if (quality == null) {
                if (!MythicCharmRolls.isTemporal(key)) {
                    throw new BuildException(entryPath + ".q: missing (the draw's quality step)");
                }
                quality = 0;
            }
            boolean legendary = Boolean.TRUE.equals(Json.optBoolean(entry, "legendary", entryPath));
            draws.add(new MythicCharmRolls.StoredRoll(slot, key, quality, legendary));
            entries.add(entry);
        }
        for (String issue : MythicCharmRolls.legalityIssues(god, draws)) {
            report.bypassed(path + ": " + issue);
        }
        ItemStack stack = build(god, units, uses, draws);
        compare(stack, god, units, draws, entries, path, report);
        report.note(path + ": mythic " + god.getName() + " charm built from " + draws.size() + " draws at " + units
                + " piety units");
        return stack;
    }

    private static ItemStack build(VaultGod god, int units, int uses, List<MythicCharmRolls.StoredRoll> draws) {
        ItemStack stack = new ItemStack(ModItems.MYTHIC_VAULT_CHARM);
        VaultGearData data = VaultGearData.read(stack);
        data.setState(VaultGearState.IDENTIFIED);
        data.setItemLevel(0);
        data.createOrReplaceAttributeValue(ModGearAttributes.CHARM_VAULT_GOD, god);
        data.createOrReplaceAttributeValue(ModGearAttributes.IS_LOOT, Boolean.TRUE);
        if (draws.stream().anyMatch(MythicCharmRolls.StoredRoll::legendary)) {
            data.createOrReplaceAttributeValue(ModGearAttributes.IS_LEGENDARY, Boolean.TRUE);
        }
        data.setRarity(VaultGearRarity.valueOf("MYTHIC"));
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, MythicCharms.of(god).getId());
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, ROLL_TYPE);
        data.write(stack);
        MythicCharmRolls.writeRolls(stack, draws);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(MythicCharmRolls.PINNED_TAG, true);
        tag.putInt(MythicCharmRolls.TEMPORAL_TOTAL_TAG, 0);
        tag.putInt(MythicCharmRolls.TEMPORAL_REMAINING_TAG, 0);
        MythicCharmRolls.materialize(stack, units);
        VaultUsesHelper.setUses(stack, uses);
        return stack;
    }

    /** Walks the materialized modifiers slot by slot alongside the draws and reports each value the game changed. */
    private static void compare(ItemStack stack, VaultGod god, int units, List<MythicCharmRolls.StoredRoll> draws,
                                List<JsonObject> entries, String path, BuildReport report) {
        VaultGearData data = VaultGearData.read(stack);
        for (VaultGearModifier.AffixType type : new VaultGearModifier.AffixType[]{VaultGearModifier.AffixType.IMPLICIT,
                VaultGearModifier.AffixType.PREFIX, VaultGearModifier.AffixType.SUFFIX}) {
            Iterator<VaultGearModifier<?>> made = data.getModifiers(type).iterator();
            for (int i = 0; i < draws.size(); i++) {
                MythicCharmRolls.StoredRoll draw = draws.get(i);
                if (draw.slot() != type) {
                    continue;
                }
                String entryPath = path + ".rolls[" + i + "] " + draw.key();
                JsonObject entry = entries.get(i);
                boolean temporal = MythicCharmRolls.isTemporal(draw.key());
                Optional<VaultGearModifier<?>> expected = MythicCharmRolls.materializeDraw(god, draw, units);
                if (!temporal && expected.isEmpty()) {
                    double given = entry.has("value") ? Json.asDouble(entry.get("value"), entryPath + ".value") : 0.0;
                    if (given != 0.0) {
                        report.fallback(entryPath + ": dormant at " + units + " piety units (the game drops it), but the "
                                + "build file has value " + given);
                    }
                    continue;
                }
                if (!made.hasNext()) {
                    report.fallback(entryPath + ": the charm materialized fewer " + type.getPlural() + " than its draws; "
                            + "this draw has no modifier");
                    continue;
                }
                VaultGearModifier<?> modifier = made.next();
                if (temporal) {
                    report.note(entryPath + ": temporal blessing; its time is not compared with the build file");
                    continue;
                }
                compareOne(modifier, entry, entryPath, report);
            }
            if (made.hasNext()) {
                report.fallback(path + ": the charm materialized more " + type.getPlural() + " than its draws");
            }
        }
    }

    private static void compareOne(VaultGearModifier<?> modifier, JsonObject entry, String path, BuildReport report) {
        String attribute = String.valueOf(modifier.getAttribute().getRegistryName());
        String wanted = Json.optString(entry, "attribute", path);
        if (wanted != null && !wanted.equals(attribute)) {
            report.fallback(path + ": the value lands on " + attribute + ", the build file names " + wanted);
        }
        Double game = numeric(modifier.getValue());
        if (!entry.has("value")) {
            report.fallback(path + ": no value given; the game materialized " + (game == null ? modifier.getValue() : game));
            return;
        }
        double given = Json.asDouble(entry.get("value"), path + ".value");
        if (game == null) {
            report.fallback(path + ": the game value " + modifier.getValue() + " has no number to compare with " + given);
            return;
        }
        if (Math.abs(game - given) > Math.max(ABSOLUTE_TOLERANCE, RELATIVE_TOLERANCE * Math.abs(given))) {
            report.fallback(path + ": the game materializes " + game + ", the build file has " + given);
        }
    }

    @Nullable
    private static Double numeric(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof EffectGearAttribute effect) {
            return (double) effect.getAmplifier();
        }
        if (value instanceof AbilityLevelAttribute ability) {
            return (double) ability.getLevelChange();
        }
        if (value instanceof TalentLevelAttribute talent) {
            return (double) talent.getLevelChange();
        }
        if (value instanceof EffectAvoidanceListGearAttribute avoidance) {
            return (double) avoidance.getChance();
        }
        return null;
    }
}
