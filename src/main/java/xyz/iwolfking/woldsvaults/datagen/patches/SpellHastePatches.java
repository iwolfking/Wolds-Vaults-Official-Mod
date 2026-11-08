package xyz.iwolfking.woldsvaults.datagen.patches;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.iwolfking.woldsvaults.config.patch.ConfigPatch;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.Node;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.Patch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.id;
import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.json;
import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.seeded;
import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.seededFiles;
import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.where;

/**
 * Spell haste replaces cooldown reduction: a cooldown lasts {@code base / (1 + haste)}. The attribute and the
 * {@code COOLDOWN_REDUCTION} player stat keep their ids and carry haste, so every upstream source is converted here
 * as a {@code scale} on upstream's own number, and upstream retunes keep flowing through the conversion. Each text
 * that quotes a converted value is formatted from the seeded value times the same factor.
 */
public final class SpellHastePatches {
    /** What one point of old cooldown reduction from an attribute source (gear, cards, charms, nodes, talents) is worth as haste. */
    public static final int ATTRIBUTE_FACTOR = 3;
    /** What one point of a vault modifier's cooldown reduction player stat addend is worth as haste. */
    public static final int VAULT_MODIFIER_FACTOR = 2;
    public static final String NAME = "Spell Haste";

    private static final String OLD_NAME = "Cooldown Reduction";
    private static final String ATTRIBUTE = "the_vault:cooldown_reduction";
    private static final String STAT = "cooldown_reduction";
    private static final String GEAR_MODIFIERS = "gear_modifiers/";
    private static final String PLAYER_STAT_MODIFIERS = "the_vault:modifier_type/player_stat";
    private static final String PLAYER_ATTRIBUTE_MODIFIERS = "the_vault:modifier_type/player_attribute";
    private static final String GROUPED_MODIFIERS = "the_vault:modifier_type/grouped";
    private static final Map<String, String> MODIFIER_NAMES = Map.of(
            "CDR", NAME,
            "Weakened CDR", "Weakened Haste",
            OLD_NAME, NAME);

    private SpellHastePatches() {
    }

    /** Registers every spell haste conversion; {@code patches} hands out the patch for a config path. */
    public static void register(Function<String, Patch> patches) {
        gearModifiers(patches);
        cards(patches.apply("card/modifiers.json"));
        greedNodes(patches.apply("greed/greed_nodes.json"), patches.apply("skill_descriptions.json"));
        mindMeld(patches.apply("talents.json"));
        vaultModifiers(patches.apply("vault_modifiers.json"));
        cufflinks(patches.apply("trinket.json"));
        statDescription(patches.apply("menu_player_stat_description.json"));
        quest(patches.apply("quest/quests.json"), "quest/quests.json");
        quest(patches.apply("quest/sky_quests.json"), "quest/sky_quests.json");
    }

    /** A value as a percent with up to two decimals, trailing zeros dropped: 0.015 reads {@code 1.5%}. */
    public static String percent(JsonElement value) {
        return value.getAsBigDecimal().movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }

    /** {@link #percent} with a sign: 0.8 reads {@code +80%}, -0.2 reads {@code -20%}. */
    public static String signedPercent(JsonElement value) {
        return (value.getAsBigDecimal().signum() < 0 ? "" : "+") + percent(value);
    }

    private static JsonPrimitive attributeScaled(JsonElement value) {
        return ConfigPatch.scaled(value, json(ATTRIBUTE_FACTOR));
    }

    private static JsonPrimitive modifierScaled(JsonElement value) {
        return ConfigPatch.scaled(value, json(VAULT_MODIFIER_FACTOR));
    }

    /** Every gear modifier paying the cooldown reduction attribute, in every upstream gear file, every tier. */
    private static void gearModifiers(Function<String, Patch> patches) {
        for (String path : seededFiles(GEAR_MODIFIERS)) {
            JsonElement root = seeded(path);
            List<List<Object>> found = new ArrayList<>();
            findGearModifiers(root, new ArrayList<>(), found);
            for (List<Object> modifier : found) {
                Patch patch = patches.apply(path);
                JsonArray tiers = locate(root, modifier).getAsJsonObject().getAsJsonArray("tiers");
                for (int tier = 0; tier < tiers.size(); tier++) {
                    JsonObject value = tiers.get(tier).getAsJsonObject().getAsJsonObject("value");
                    if (value == null || !isNumber(value.get("min")) || !isNumber(value.get("max"))) {
                        throw new IllegalStateException("Cooldown reduction modifier " + modifier + " in " + path + " tier " + tier
                                + " has no numeric min and max to convert to spell haste");
                    }
                    Node node = patch.at(modifier.toArray()).at("tiers", tier, "value");
                    node.scale("min", ATTRIBUTE_FACTOR).scale("max", ATTRIBUTE_FACTOR);
                }
            }
        }
    }

    private static void findGearModifiers(JsonElement node, List<Object> path, List<List<Object>> found) {
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            JsonElement attribute = object.get("attribute");
            if (attribute != null && attribute.isJsonPrimitive() && ATTRIBUTE.equals(attribute.getAsString()) && object.has("tiers")) {
                found.add(List.copyOf(path));
                return;
            }
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                path.add(entry.getKey());
                findGearModifiers(entry.getValue(), path, found);
                path.remove(path.size() - 1);
            }
        } else if (node.isJsonArray()) {
            JsonArray array = node.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                JsonElement identifier = element.isJsonObject() ? element.getAsJsonObject().get("identifier") : null;
                path.add(identifier != null && identifier.isJsonPrimitive() ? where("identifier", identifier.getAsString()) : i);
                findGearModifiers(element, path, found);
                path.remove(path.size() - 1);
            }
        }
    }

    private static JsonElement locate(JsonElement root, List<Object> path) {
        JsonElement node = root;
        for (Object step : path) {
            if (step instanceof String key) {
                node = node.getAsJsonObject().get(key);
            } else if (step instanceof Integer index) {
                node = node.getAsJsonArray().get(index);
            } else {
                String identifier = ((JsonObject) step).get("identifier").getAsString();
                JsonElement match = null;
                for (JsonElement element : node.getAsJsonArray()) {
                    if (element.isJsonObject() && identifier.equals(element.getAsJsonObject().get("identifier").getAsString())) {
                        match = element;
                    }
                }
                node = match;
            }
        }
        return node;
    }

    /** Every gear card paying cooldown reduction: each pool roll scaled, the card renamed. */
    private static void cards(Patch cards) {
        JsonObject values = seeded("card/modifiers.json").getAsJsonObject().getAsJsonObject("values");
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            JsonObject card = entry.getValue().getAsJsonObject();
            if (!card.has("attribute") || !ATTRIBUTE.equals(card.get("attribute").getAsString())) {
                continue;
            }
            JsonArray pool = card.getAsJsonArray("pool");
            for (int roll = 0; roll < pool.size(); roll++) {
                cards.at("values", entry.getKey(), "pool", roll).scale("min", ATTRIBUTE_FACTOR).scale("max", ATTRIBUTE_FACTOR);
            }
            String name = card.getAsJsonObject("name").get("text").getAsString();
            if (name.contains(OLD_NAME)) {
                cards.at("values", entry.getKey(), "name").set("text", name.replace(OLD_NAME, NAME));
            }
        }
    }

    /** The greed tree's cooldown reduction nodes, their node names and their skill descriptions. */
    private static void greedNodes(Patch nodes, Patch descriptions) {
        JsonArray skills = seeded("greed/greed_nodes.json").getAsJsonObject().getAsJsonObject("tree").getAsJsonArray("skills");
        for (JsonElement element : skills) {
            JsonObject skill = element.getAsJsonObject();
            JsonArray entries = skill.getAsJsonArray("entries");
            if (entries == null) {
                continue;
            }
            String nodeId = skill.get("id").getAsString();
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                if (!entry.has("attribute") || !ATTRIBUTE.equals(entry.get("attribute").getAsString())) {
                    continue;
                }
                JsonElement oldValue = entry.get("value");
                JsonPrimitive haste = attributeScaled(oldValue);
                Node node = nodes.at("tree", "skills", id(nodeId));
                node.at("entries", i).scale("value", ATTRIBUTE_FACTOR);
                node.set("name", signedPercent(haste) + " " + NAME);
                Node description = descriptions.at("descriptions", nodeId);
                description.at(where("text", OLD_NAME)).set("text", NAME);
                description.at(where("text", percent(oldValue))).set("text", percent(haste));
            }
        }
    }

    /** Mind Meld's cooldown reduction per mana step, every tier. */
    private static void mindMeld(Patch talents) {
        JsonArray skills = seeded("talents.json").getAsJsonObject().getAsJsonObject("tree").getAsJsonArray("skills");
        for (JsonElement element : skills) {
            JsonObject skill = element.getAsJsonObject();
            if (!"Mind_Meld".equals(skill.get("id").getAsString())) {
                continue;
            }
            for (JsonElement tier : skill.getAsJsonArray("tiers")) {
                talents.at("tree", "skills", id("Mind_Meld"), "tiers", id(tier.getAsJsonObject().get("id").getAsString()))
                        .scale("bonusPerStep", ATTRIBUTE_FACTOR);
            }
            return;
        }
        throw new IllegalStateException("The seeded talents.json has no Mind_Meld talent to convert to spell haste");
    }

    /** Every cooldown reduction player stat modifier, and the grouped modifiers whose text quotes one. */
    private static void vaultModifiers(Patch modifiers) {
        JsonObject groups = seeded("vault_modifiers.json").getAsJsonObject().getAsJsonObject("modifiers");
        JsonObject stats = groups.getAsJsonObject(PLAYER_STAT_MODIFIERS);
        for (Map.Entry<String, JsonElement> entry : stats.entrySet()) {
            JsonObject modifier = entry.getValue().getAsJsonObject();
            JsonObject properties = modifier.getAsJsonObject("properties");
            if (properties == null || !properties.has("stat") || !STAT.equals(properties.get("stat").getAsString())) {
                continue;
            }
            Node node = modifiers.at("modifiers", PLAYER_STAT_MODIFIERS, entry.getKey());
            node.at("properties").scale("addend", VAULT_MODIFIER_FACTOR);
            if (properties.has("reputation")) {
                node.at("properties", "reputation").scale("addendPerLevel", VAULT_MODIFIER_FACTOR);
            }
            JsonObject display = modifier.getAsJsonObject("display");
            Node shown = node.at("display");
            shown.set("description", signedPercent(modifierScaled(properties.get("addend"))) + " " + NAME);
            if (display.has("descriptionFormatted")) {
                shown.set("descriptionFormatted", display.get("descriptionFormatted").getAsString().replace(OLD_NAME, NAME));
            }
            String rename = MODIFIER_NAMES.get(display.get("name").getAsString());
            if (rename != null) {
                shown.set("name", rename);
            }
        }

        JsonObject attributes = groups.getAsJsonObject(PLAYER_ATTRIBUTE_MODIFIERS);
        Node grouped = modifiers.at("modifiers", GROUPED_MODIFIERS);
        String buffed = groups.getAsJsonObject(GROUPED_MODIFIERS).getAsJsonObject("the_vault:buffed")
                .getAsJsonObject("display").get("description").getAsString();
        grouped.at("the_vault:buffed", "display").set("description", buffed.replace(OLD_NAME, NAME));
        grouped.at("the_vault:weakened_powers", "display").set("description",
                "Lose " + percent(abs(modifierScaled(addend(stats, "the_vault:weakened_cdr")))) + " " + NAME + " and "
                        + percent(abs(amount(attributes, "the_vault:mana_regen_down"))) + " Mana Regeneration");
        grouped.at("the_vault:wendarr_favour", "display").set("description",
                signedPercent(amount(attributes, "the_vault:wendarr_regen")) + " Mana Regeneration & "
                        + signedPercent(modifierScaled(addend(stats, "the_vault:wendarr_cdr"))) + " " + NAME);
    }

    private static JsonElement addend(JsonObject stats, String id) {
        return stats.getAsJsonObject(id).getAsJsonObject("properties").get("addend");
    }

    private static JsonElement amount(JsonObject attributes, String id) {
        return attributes.getAsJsonObject(id).getAsJsonObject("properties").get("amount");
    }

    private static JsonPrimitive abs(JsonElement value) {
        return new JsonPrimitive(value.getAsBigDecimal().abs());
    }

    /** Cufflinks' effect text, quoting its own spell haste percentile. */
    private static void cufflinks(Patch trinkets) {
        JsonElement value = seeded("trinket.json").getAsJsonObject().getAsJsonObject("TRINKETS")
                .getAsJsonObject("the_vault:cufflinks").getAsJsonObject("config").get("value");
        trinkets.at("TRINKETS", "the_vault:cufflinks").set("effectText",
                "Increases your " + NAME + " by " + percent(value) + " of your total " + NAME);
    }

    /** The statistics tab's description of the attribute. */
    private static void statDescription(Patch descriptions) {
        descriptions.at("MOD_GEAR_ATTRIBUTE_DESCRIPTIONS").set(ATTRIBUTE,
                "Your \u00A7bspell haste\u00A77 for \u00A7aany Ability\u00A77: cooldowns are divided by \u00A7b1 + spell haste\u00A77,"
                        + " and negative spell haste multiplies them by \u00A7b1 + its size\u00A77");
    }

    /** The archetypes quest line naming cooldown reduction as a gear goal. */
    private static void quest(Patch quests, String path) {
        String lower = OLD_NAME.toLowerCase(Locale.ROOT);
        for (JsonElement element : seeded(path).getAsJsonObject().getAsJsonArray("quests")) {
            JsonObject quest = element.getAsJsonObject();
            JsonObject data = quest.getAsJsonObject("descriptionData");
            if (data == null || !data.has("description")) {
                continue;
            }
            for (JsonElement line : data.getAsJsonArray("description")) {
                if (!line.isJsonObject() || !line.getAsJsonObject().has("text")) {
                    continue;
                }
                String text = line.getAsJsonObject().get("text").getAsString();
                if (text.contains(lower)) {
                    quests.at("quests", id(quest.get("id").getAsString()), "descriptionData", "description", where("text", text))
                            .set("text", text.replace(lower, NAME.toLowerCase(Locale.ROOT)));
                }
            }
        }
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }
}
