package xyz.iwolfking.woldsvaults.builds.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.EtchingTierConfig;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.data.UsesGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.item.gear.VaultNecklaceItem;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.Json;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AttributeGearDataAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Converts the vault gear data of an item (gear, tools, jewels, charms, magnets, necklaces) to and from build JSON.
 * The JSON lists every field the data holds, in order, so a saved item rebuilds to identical data. Gear marked
 * {@code "authored": true} may leave fields out; each default it then takes is reported as a fallback.
 */
public final class GearCodec {
    private static final Set<String> GEAR_KEYS = Set.of("authored", "state", "rarity", "level", "repairs", "uses",
            "unique", "ancient", "model", "etching", "attributes", "implicits", "prefixes", "suffixes");
    private static final Set<String> ENTRY_KEYS = Set.of("attribute", "id", "group", "tier", "categories", "added",
            "modifier", "value", "snbt", "about");
    private static final Random RANDOM = new Random();

    private GearCodec() {
    }

    /** The JSON form of {@code data}, field for field. */
    public static JsonObject write(VaultGearData data) {
        JsonObject json = new JsonObject();
        json.addProperty("state", data.getState().name());
        json.addProperty("rarity", data.getRarity().name());
        json.addProperty("level", data.getItemLevel());
        JsonObject repairs = new JsonObject();
        repairs.addProperty("max", data.getRepairSlots());
        repairs.addProperty("used", data.getUsedRepairSlots());
        json.add("repairs", repairs);
        if (data instanceof UsesGearData uses) {
            JsonObject usesJson = new JsonObject();
            usesJson.addProperty("vault_uses", uses.getVaultUses());
            usesJson.add("used_vaults", uuids(uses.getUsedVaults()));
            usesJson.add("free_vault_uses", uuids(uses.getFreeVaultUses()));
            json.add("uses", usesJson);
        }
        json.add("attributes", entries(data.getAttributes(), true));
        json.add("implicits", entries(data.getModifiers(VaultGearModifier.AffixType.IMPLICIT), false));
        json.add("prefixes", entries(data.getModifiers(VaultGearModifier.AffixType.PREFIX), false));
        json.add("suffixes", entries(data.getModifiers(VaultGearModifier.AffixType.SUFFIX), false));
        return json;
    }

    private static JsonArray uuids(List<UUID> ids) {
        JsonArray array = new JsonArray();
        ids.forEach(id -> array.add(id.toString()));
        return array;
    }

    private static JsonArray entries(List<? extends VaultGearAttributeInstance<?>> instances, boolean attributeList) {
        JsonArray array = new JsonArray();
        for (VaultGearAttributeInstance<?> instance : instances) {
            array.add(entry(instance, attributeList));
        }
        return array;
    }

    private static JsonObject entry(VaultGearAttributeInstance<?> instance, boolean attributeList) {
        JsonObject json = new JsonObject();
        json.addProperty("attribute", String.valueOf(instance.getAttribute().getRegistryName()));
        if (instance instanceof VaultGearModifier<?> modifier) {
            ResourceLocation id = modifier.getModifierIdentifier();
            json.add("id", id == null ? JsonNull.INSTANCE : new JsonPrimitive(id.toString()));
            json.addProperty("group", modifier.getModifierGroup());
            json.addProperty("tier", modifier.getRolledTier());
            JsonArray categories = new JsonArray();
            modifier.getCategories().forEach(category -> categories.add(category.name()));
            json.add("categories", categories);
            if (modifier.hasGameTimeAdded()) {
                json.addProperty("added", modifier.getGameTimeAdded());
            }
        } else if (attributeList) {
            json.addProperty("modifier", false);
        }
        writeValue(instance, json);
        return json;
    }

    private static <T> void writeValue(VaultGearAttributeInstance<T> instance, JsonObject json) {
        ValueCodec.write(instance.getAttribute(), instance.getValue(), json);
    }

    /**
     * Fills the empty gear data of {@code stack} from {@code json} and writes it to the stack. {@code defaultLevel}
     * is the item level an authored item takes when it names none.
     */
    public static void read(ItemStack stack, JsonObject json, String path, int defaultLevel, BuildReport report) {
        Json.onlyKeys(json, path, GEAR_KEYS);
        boolean authored = Boolean.TRUE.equals(Json.optBoolean(json, "authored", path));
        VaultGearData data = VaultGearData.read(stack);
        if (!data.getAttributes().isEmpty() || !data.getModifiers(VaultGearModifier.AffixType.IMPLICIT).isEmpty()) {
            throw new BuildException(path + ": the item already carries gear data; remove 'vaultGearData' from its stack tag");
        }
        String uniqueText = Json.optString(json, "unique", path);
        ResourceLocation uniqueId = uniqueText == null ? null : Json.id(uniqueText, path + ".unique");
        if (!authored && (uniqueId != null || json.has("ancient") || json.has("model") || json.has("etching"))) {
            throw new BuildException(path + ": 'unique', 'ancient', 'model' and 'etching' are shorthand for authored gear; add \"authored\": true");
        }

        String state = Json.optString(json, "state", path);
        if (state == null) {
            requireAuthored(authored, path, "state");
            data.setState(VaultGearState.IDENTIFIED);
        } else {
            data.setState(enumByName(VaultGearState.values(), state, path + ".state"));
        }
        String rarity = Json.optString(json, "rarity", path);
        if (rarity == null) {
            requireAuthored(authored, path, "rarity");
            VaultGearRarity fallback = uniqueId != null ? VaultGearRarity.UNIQUE : VaultGearRarity.OMEGA;
            data.setRarity(fallback);
            report.fallback(path + ": no rarity given; using " + fallback.name());
        } else {
            data.setRarity(enumByName(VaultGearRarity.values(), rarity, path + ".rarity"));
        }
        Integer level = Json.optInt(json, "level", path);
        if (level == null) {
            requireAuthored(authored, path, "level");
            report.fallback(path + ": no item level given; using the player's level " + defaultLevel);
            level = defaultLevel;
        }
        if (level < 0 || level > 100) {
            throw new BuildException(path + ".level: item levels run 0 to 100, got " + level);
        }
        data.setItemLevel(level);
        JsonObject repairs = Json.optObject(json, "repairs", path);
        if (repairs == null) {
            requireAuthored(authored, path, "repairs");
            if (stack.getItem() instanceof VaultGearItem gearItem && gearItem.canStoreRepairSlots(stack)) {
                int slots = 1 + RANDOM.nextInt(8);
                data.setRepairSlots(slots);
                report.fallback(path + ": no repair slots given; rolled " + slots + " like a fresh identification");
            }
        } else {
            Json.onlyKeys(repairs, path + ".repairs", Set.of("max", "used"));
            data.setRepairSlots(Json.intValue(repairs, "max", path + ".repairs"));
            data.setUsedRepairSlots(Json.intValue(repairs, "used", path + ".repairs"));
        }
        readUses(data, json, path);
        if (authored && !json.has("uses") && data instanceof UsesGearData usesData) {
            if (stack.getItem() instanceof VaultNecklaceItem) {
                int uses = ModConfigs.VAULT_NECKLACE.getUses(JavaRandom.ofNanoTime());
                usesData.setVaultUses(uses);
                report.fallback(path + ": no vault uses given; rolled " + uses + " like a fresh necklace");
            } else {
                report.fallback(path + ": no vault uses given; the item has 0 uses (give \"uses\" to set them)");
            }
        }

        List<VaultGearAttributeInstance<?>> attributeList = ((AttributeGearDataAccessor) data).woldsvaults$getAttributeList();
        if (authored) {
            addAuthoredUniqueAttributes(stack, data, json, uniqueId, attributeList, path, report);
            data.write(stack);
            data = VaultGearData.read(stack);
            attributeList = ((AttributeGearDataAccessor) data).woldsvaults$getAttributeList();
        }
        Optional<VaultGearTierConfig> config = authored ? VaultGearTierConfig.getConfig(stack) : Optional.empty();

        readModifiers(data, VaultGearModifier.AffixType.IMPLICIT, json, "implicits", path, authored, config, level, report);
        readModifiers(data, VaultGearModifier.AffixType.PREFIX, json, "prefixes", path, authored, config, level, report);
        readModifiers(data, VaultGearModifier.AffixType.SUFFIX, json, "suffixes", path, authored, config, level, report);
        JsonArray attributes = Json.optArray(json, "attributes", path);
        if (attributes == null) {
            requireAuthored(authored, path, "attributes");
        } else {
            for (int i = 0; i < attributes.size(); i++) {
                String entryPath = path + ".attributes[" + i + "]";
                JsonObject entry = Json.asObject(attributes.get(i), entryPath);
                VaultGearAttributeInstance<?> instance = isModifierEntry(entry, entryPath)
                        ? readModifier(entry, entryPath, authored, config, level, report)
                        : readInstance(entry, entryPath);
                if (!instance.isValid()) {
                    throw new BuildException(entryPath + ": the game rejects this attribute value");
                }
                attributeList.add(instance);
            }
        }
        if (authored) {
            addAuthoredDefaults(stack, data, attributeList, config, level, path, report);
        }
        data.write(stack);
        JsonObject etching = authored ? Json.optObject(json, "etching", path) : null;
        if (etching != null) {
            applyEtching(stack, etching, path + ".etching", report);
        }
    }

    /**
     * Etches a built item the way the Etching Application Table does, which Wold's lets etch unique and sealed gear
     * (MixinEtchingApplicationTableTileEntity): the item becomes UNIQUE rarity, carries the etching id and the
     * etching's modifier as an implicit, and is renamed. The modifier is rolled from the etching config at
     * {@code greed_tier}; a given {@code value} replaces the rolled one. Every etching the table could not produce is
     * an error: an already etched item, a gear type outside the etching's groups, a greed tier below the etching's
     * minimum, an etching modifier already listed on the item, or a value outside what the etching rolls at that greed
     * tier.
     */
    private static void applyEtching(ItemStack stack, JsonObject json, String path, BuildReport report) {
        Json.onlyKeys(json, path, Set.of("id", "value", "snbt", "greed_tier"));
        ResourceLocation id = Json.id(Json.string(json, "id", path), path + ".id");
        VaultEtchingConfig.EtchingEntry entry = ModConfigs.ETCHINGS.getEtchingConfig(id);
        if (entry == null) {
            throw new BuildException(path + ".id: no etching '" + id + "' exists in this game");
        }
        if (!(stack.getItem() instanceof VaultGearItem gearItem)) {
            throw new BuildException(path + ": only vault gear can be etched");
        }
        VaultGearData data = VaultGearData.read(stack);
        if (hasAttribute(data, ModGearAttributes.ETCHING)) {
            throw new BuildException(path + ": the item already lists an etching attribute; an item holds one etching");
        }
        List<String> groups = entry.getTypeGroups();
        if (!groups.isEmpty() && groups.stream().noneMatch(group -> ModConfigs.ETCHINGS.getGroup(group).contains(gearItem.getGearType(stack)))) {
            throw new BuildException(path + ": etching " + id + " may not go on " + gearItem.getGearType(stack) + " (allowed groups " + groups + ")");
        }
        Integer greedTier = Json.optInt(json, "greed_tier", path);
        int tier = greedTier == null ? Math.max(entry.getMinGreedTier(), 0) : greedTier;
        if (tier < entry.getMinGreedTier()) {
            throw new BuildException(path + ".greed_tier: etching " + id + " first drops at greed tier " + entry.getMinGreedTier() + ", not " + tier);
        }
        ItemStack etchingStack = EtchingItem.create(id, entry, RANDOM, tier).orElseThrow(
                () -> new BuildException(path + ": etching " + id + " has no modifier at greed tier " + tier));
        List<VaultGearModifier<?>> rolled = VaultGearData.read(etchingStack).getModifiers(VaultGearModifier.AffixType.IMPLICIT);
        if (rolled.size() != 1) {
            throw new BuildException(path + ": etching " + id + " rolled " + rolled.size() + " modifiers; expected one");
        }
        VaultGearModifier<?> modifier = rolled.get(0);
        ResourceLocation etchingModifier = modifier.getModifierIdentifier();
        if (etchingModifier != null && data.getModifiers(etchingModifier).findAny().isPresent()) {
            throw new BuildException(path + ": the item already lists modifier " + etchingModifier + ", which etching " + id
                    + " adds; leave it out of the item and let the etching add it");
        }
        if (json.has("value") || json.has("snbt")) {
            modifier = withValue(modifier, json, path);
            checkEtchingValue(modifier, etchingTiers(entry, modifier, tier), id, tier, path, report);
        } else {
            report.fallback(path + ": no etching value given; rolled " + readable(modifier) + " at greed tier " + tier);
        }
        data.setRarity(VaultGearRarity.UNIQUE);
        data.createOrReplaceAttributeValue(ModGearAttributes.ETCHING, id, true);
        data.addModifier(VaultGearModifier.AffixType.IMPLICIT, modifier, true);
        data.write(stack);
        stack.setHoverName(new TextComponent(entry.getName() + " " + stack.getHoverName().getString()));
    }

    private static <T> String readable(VaultGearModifier<T> modifier) {
        try {
            return String.valueOf(modifier.getAttribute().getType().serialize(modifier.getValue()));
        } catch (RuntimeException e) {
            return String.valueOf(modifier.getValue());
        }
    }

    private static List<EtchingTierConfig.EtchingModifierTier<?>> etchingTiers(VaultEtchingConfig.EtchingEntry entry,
                                                                            VaultGearModifier<?> modifier, int greedTier) {
        List<EtchingTierConfig.EtchingModifierTier<?>> tiers = new ArrayList<>();
        for (EtchingTierConfig.EtchingModifierTierGroup group : entry.getAttributes()) {
            if (group.getAttribute().equals(modifier.getAttribute().getRegistryName())
                    && Objects.equals(group.getIdentifier(), modifier.getModifierIdentifier())) {
                tiers.addAll(group.getModifiersForGreedTier(greedTier));
            }
        }
        return tiers;
    }

    /**
     * Rejects an etching value outside the range the etching's tiers roll at {@code greedTier}. A value whose attribute
     * has no ordering (talent levels, ability values) must match the range when the range is a single value; a spread
     * range of such a value, or a generator that reports no range, cannot be checked and is reported as a fallback.
     */
    @SuppressWarnings("unchecked")
    private static <T, C> void checkEtchingValue(VaultGearModifier<T> modifier, List<EtchingTierConfig.EtchingModifierTier<?>> tiers,
                                                 ResourceLocation id, int greedTier, String path, BuildReport report) {
        VaultGearAttribute<T> attribute = modifier.getAttribute();
        if (tiers.isEmpty()) {
            throw new BuildException(path + ": etching " + id + " has no " + attribute.getRegistryName() + " tier at greed tier " + greedTier);
        }
        ConfigurableAttributeGenerator<T, C> generator = (ConfigurableAttributeGenerator<T, C>) attribute.getGenerator();
        List<C> configs = new ArrayList<>();
        tiers.forEach(tier -> configs.add((C) tier.getModifierConfiguration()));
        Optional<T> min = generator == null ? Optional.empty() : generator.getMinimumValue(configs);
        Optional<T> max = generator == null ? Optional.empty() : generator.getMaximumValue(configs);
        if (min.isEmpty() || max.isEmpty()) {
            report.fallback(path + ": etching " + id + " reports no value range for " + attribute.getRegistryName()
                    + "; the given value " + readable(modifier) + " was not range-checked");
            return;
        }
        T value = modifier.getValue();
        VaultGearAttributeComparator<T> comparator = attribute.getAttributeComparator();
        boolean inRange;
        if (comparator != null) {
            inRange = comparator.compare(value, min.get()) >= 0 && comparator.compare(value, max.get()) <= 0;
        } else if (serialized(attribute, min.get()).equals(serialized(attribute, max.get()))) {
            inRange = serialized(attribute, value).equals(serialized(attribute, min.get()));
        } else {
            report.fallback(path + ": " + attribute.getRegistryName() + " values have no ordering; the given value "
                    + readable(modifier) + " was not range-checked against etching " + id);
            return;
        }
        if (!inRange) {
            throw new BuildException(path + ".value: etching " + id + " rolls " + serialized(attribute, min.get()) + " to "
                    + serialized(attribute, max.get()) + " at greed tier " + greedTier + ", not " + readable(modifier));
        }
    }

    private static <T> String serialized(VaultGearAttribute<T> attribute, T value) {
        try {
            return String.valueOf(attribute.getType().serialize(value));
        } catch (RuntimeException e) {
            return String.valueOf(value);
        }
    }

    private static <T> VaultGearModifier<T> withValue(VaultGearModifier<T> template, JsonObject json, String path) {
        VaultGearModifier<T> copy = new VaultGearModifier<>(template.getAttribute(), ValueCodec.read(template.getAttribute(), json, path));
        copy.setModifierIdentifier(template.getModifierIdentifier());
        copy.setModifierGroup(template.getModifierGroup());
        copy.setRolledTier(template.getRolledTier());
        template.getCategories().forEach(copy::addCategory);
        return copy;
    }

    private static void requireAuthored(boolean authored, String path, String key) {
        if (!authored) {
            throw new BuildException(path + "." + key + ": missing (only \"authored\": true gear may leave it out)");
        }
    }

    private static void readUses(VaultGearData data, JsonObject json, String path) {
        JsonObject uses = Json.optObject(json, "uses", path);
        if (uses == null) {
            return;
        }
        if (!(data instanceof UsesGearData usesData)) {
            throw new BuildException(path + ".uses: this item does not track vault uses in its gear data");
        }
        Json.onlyKeys(uses, path + ".uses", Set.of("vault_uses", "used_vaults", "free_vault_uses"));
        usesData.setVaultUses(Json.intValue(uses, "vault_uses", path + ".uses"));
        usesData.setUsedVaults(readUuids(uses, "used_vaults", path + ".uses"));
        usesData.setFreeVaultUses(readUuids(uses, "free_vault_uses", path + ".uses"));
    }

    private static List<UUID> readUuids(JsonObject parent, String key, String path) {
        List<UUID> ids = new LinkedList<>();
        JsonArray array = Json.optArray(parent, key, path);
        if (array == null) {
            return ids;
        }
        for (int i = 0; i < array.size(); i++) {
            String text = Json.asString(array.get(i), path + "." + key + "[" + i + "]");
            try {
                ids.add(UUID.fromString(text));
            } catch (IllegalArgumentException e) {
                throw new BuildException(path + "." + key + "[" + i + "]: '" + text + "' is not a UUID");
            }
        }
        return ids;
    }

    private static void readModifiers(VaultGearData data, VaultGearModifier.AffixType type, JsonObject json, String key,
                                      String path, boolean authored, Optional<VaultGearTierConfig> config, int level,
                                      BuildReport report) {
        JsonArray array = Json.optArray(json, key, path);
        if (array == null) {
            requireAuthored(authored, path, key);
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            String entryPath = path + "." + key + "[" + i + "]";
            VaultGearModifier<?> modifier = readModifier(Json.asObject(array.get(i), entryPath), entryPath, authored,
                    config, level, report);
            if (!data.addModifier(type, modifier, true)) {
                throw new BuildException(entryPath + ": the game rejects this modifier value");
            }
        }
    }

    private static boolean isModifierEntry(JsonObject entry, String path) {
        Boolean flag = Json.optBoolean(entry, "modifier", path);
        if (flag != null) {
            return flag;
        }
        return entry.has("id") || entry.has("group") || entry.has("tier") || entry.has("categories");
    }

    private static VaultGearAttributeInstance<?> readInstance(JsonObject entry, String path) {
        Json.onlyKeys(entry, path, ENTRY_KEYS);
        VaultGearAttribute<?> attribute = attribute(Json.string(entry, "attribute", path), path + ".attribute");
        return instance(attribute, entry, path);
    }

    private static <T> VaultGearAttributeInstance<T> instance(VaultGearAttribute<T> attribute, JsonObject entry, String path) {
        return new VaultGearAttributeInstance<>(attribute, ValueCodec.read(attribute, entry, path));
    }

    private static VaultGearModifier<?> readModifier(JsonObject entry, String path, boolean authored,
                                                     Optional<VaultGearTierConfig> config, int level, BuildReport report) {
        Json.onlyKeys(entry, path, ENTRY_KEYS);
        ResourceLocation identifier = null;
        if (entry.has("id") && !entry.get("id").isJsonNull()) {
            identifier = Json.id(Json.string(entry, "id", path), path + ".id");
        }
        VaultGearTierConfig.ModifierTierGroup tierGroup = null;
        if (authored && identifier != null) {
            ResourceLocation lookup = identifier;
            tierGroup = config.map(c -> c.getTierGroup(lookup)).orElse(null);
        }
        String attributeText = Json.optString(entry, "attribute", path);
        VaultGearAttribute<?> attribute;
        if (attributeText != null) {
            attribute = attribute(attributeText, path + ".attribute");
            if (tierGroup != null && !tierGroup.getAttribute().equals(attribute.getRegistryName())) {
                throw new BuildException(path + ": modifier " + identifier + " is a " + tierGroup.getAttribute()
                        + " modifier in this item's config, not " + attributeText);
            }
        } else {
            if (!authored) {
                throw new BuildException(path + ".attribute: missing");
            }
            if (identifier == null) {
                throw new BuildException(path + ": an entry needs an 'attribute' or an 'id'");
            }
            if (tierGroup == null) {
                throw new BuildException(path + ": modifier " + identifier + " is not in this item's modifier config, so its "
                        + "attribute is unknown; give 'attribute'");
            }
            attribute = attribute(tierGroup.getAttribute().toString(), path + ".id");
        }
        VaultGearModifier<?> modifier;
        if (authored && !entry.has("value") && !entry.has("snbt")) {
            if (tierGroup == null) {
                throw new BuildException(path + ": no value given and " + identifier + " is not in this item's modifier config to "
                        + "generate one from");
            }
            Integer givenTier = Json.optInt(entry, "tier", path);
            VaultGearTierConfig.ModifierTier<?> source = givenTier != null ? tierGroup.getModifierForTier(givenTier)
                    : tierGroup.getHighestForLevel(level);
            if (source == null) {
                throw new BuildException(path + ": " + identifier + " has no tier to generate a value from at level " + level);
            }
            modifier = source.makeModifier(tierGroup, RANDOM);
            report.fallback(path + ": no value given for " + identifier + "; generated " + readable(modifier)
                    + " from the game's tier T" + (source.getModifierTier() + 1) + " config");
        } else {
            modifier = modifier(attribute, entry, path);
        }
        modifier.setModifierIdentifier(identifier);
        String group = Json.optString(entry, "group", path);
        if (group == null && tierGroup != null) {
            group = tierGroup.getModifierGroup();
        }
        modifier.setModifierGroup(group == null ? "" : group);
        Integer tier = Json.optInt(entry, "tier", path);
        if (tier == null && tierGroup != null) {
            VaultGearTierConfig.ModifierTier<?> highest = tierGroup.getHighestForLevel(level);
            if (highest != null) {
                tier = highest.getModifierTier();
                report.fallback(path + ": no tier given for " + identifier + "; using its highest tier at level " + level
                        + " (T" + (tier + 1) + "), which only changes tooltips and crafting ranges");
            }
        }
        modifier.setRolledTier(tier == null ? -1 : tier);
        JsonArray categories = Json.optArray(entry, "categories", path);
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                String name = Json.asString(categories.get(i), path + ".categories[" + i + "]");
                modifier.addCategory(enumByName(VaultGearModifier.AffixCategory.values(), name, path + ".categories[" + i + "]"));
            }
        }
        Long added = Json.optLong(entry, "added", path);
        if (added != null) {
            modifier.setGameTimeAdded(added);
        }
        return modifier;
    }

    private static <T> VaultGearModifier<T> modifier(VaultGearAttribute<T> attribute, JsonObject entry, String path) {
        return new VaultGearModifier<>(attribute, ValueCodec.read(attribute, entry, path));
    }

    private static VaultGearAttribute<?> attribute(String text, String path) {
        ResourceLocation id = Json.id(text, path);
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(id);
        if (attribute == null) {
            throw new BuildException(path + ": no gear attribute '" + id + "' exists in this game");
        }
        return attribute;
    }

    private static void addAuthoredUniqueAttributes(ItemStack stack, VaultGearData data, JsonObject json,
                                                    @Nullable ResourceLocation uniqueId,
                                                    List<VaultGearAttributeInstance<?>> attributeList, String path,
                                                    BuildReport report) {
        String modelText = Json.optString(json, "model", path);
        boolean ancient = Boolean.TRUE.equals(Json.optBoolean(json, "ancient", path));
        if (ancient && uniqueId == null) {
            throw new BuildException(path + ".ancient: only a unique can be ancient");
        }
        if (uniqueId != null) {
            UniqueGearConfig.Entry entry = ModConfigs.UNIQUE_GEAR.getEntry(uniqueId).orElseThrow(
                    () -> new BuildException(path + ".unique: no unique '" + uniqueId + "' exists in this game"));
            if (entry.getItem() != null && entry.getItem() != stack.getItem()) {
                throw new BuildException(path + ".unique: " + uniqueId + " is a " + entry.getItem().getRegistryName()
                        + ", not a " + stack.getItem().getRegistryName());
            }
            attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueId));
            String name = entry.getName();
            if (ancient) {
                Optional<String> ancientName = AncientUniqueHelper.getAncientName(uniqueId);
                if (ancientName.isPresent()) {
                    name = ancientName.get();
                } else {
                    report.fallback(path + ": ancient " + uniqueId + " has no ancient name; it keeps the unique name " + name);
                }
            }
            if (name != null) {
                attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.GEAR_NAME, name));
            }
            if (modelText == null && entry.getModels() != null) {
                Optional<ResourceLocation> model = entry.getModels().getRandom();
                if (model.isPresent()) {
                    attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.GEAR_MODEL, model.get()));
                    report.fallback(path + ": no model given for unique " + uniqueId + "; picked " + model.get());
                }
            }
        }
        if (modelText != null) {
            attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.GEAR_MODEL, Json.id(modelText, path + ".model")));
        }
        if (ancient) {
            attributeList.add(new VaultGearAttributeInstance<>(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true));
        }
    }

    private static void addAuthoredDefaults(ItemStack stack, VaultGearData data,
                                            List<VaultGearAttributeInstance<?>> attributeList,
                                            Optional<VaultGearTierConfig> config, int level, String path,
                                            BuildReport report) {
        if (!hasAttribute(data, ModGearAttributes.GEAR_ROLL_TYPE)) {
            String rollType = ModConfigs.VAULT_GEAR_TYPE_CONFIG.getDefaultRoll().getName();
            attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.GEAR_ROLL_TYPE, rollType));
            report.fallback(path + ": no gear roll type given; using the default '" + rollType + "'");
        }
        if (!hasAttribute(data, ModGearAttributes.PREFIXES)) {
            int count = data.getModifiers(VaultGearModifier.AffixType.PREFIX).size();
            attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.PREFIXES, count));
            report.fallback(path + ": no prefix slot count given; using the " + count + " listed");
        }
        if (!hasAttribute(data, ModGearAttributes.SUFFIXES)) {
            int count = data.getModifiers(VaultGearModifier.AffixType.SUFFIX).size();
            attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.SUFFIXES, count));
            report.fallback(path + ": no suffix slot count given; using the " + count + " listed");
        }
        if (!hasAttribute(data, ModGearAttributes.GEAR_MODEL) && data.getRarity() != VaultGearRarity.UNIQUE
                && stack.getItem() instanceof VaultGearItem gearItem && !(stack.getItem() instanceof ToolItem)) {
            ResourceLocation model = gearItem.getRandomModel(stack, RANDOM, null, null);
            if (model != null) {
                attributeList.add(new VaultGearAttributeInstance<>(ModGearAttributes.GEAR_MODEL, model));
                report.fallback(path + ": no model given; picked " + model);
            }
        }
        if (!hasAttribute(data, ModGearAttributes.DURABILITY) && !(stack.getItem() instanceof ToolItem)) {
            Optional<Tuple<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.ModifierTierGroup>> durability =
                    config.flatMap(c -> c.getAnyGroupsFulfilling(group ->
                            ModGearAttributes.DURABILITY.getRegistryName().equals(group.getAttribute())).stream().findFirst());
            if (durability.isPresent()) {
                VaultGearTierConfig.ModifierTierGroup group = durability.get().getB();
                VaultGearTierConfig.ModifierTier<?> tier = group.getHighestForLevel(level);
                if (tier != null) {
                    VaultGearModifier<?> rolled = tier.makeModifier(group, RANDOM);
                    attributeList.add(rolled);
                    report.fallback(path + ": no durability given; rolled " + rolled.getValue() + " from the item's top "
                            + "durability tier at level " + level);
                }
            } else {
                report.fallback(path + ": no durability given and this item's config has none; the item has no durability");
            }
        }
    }

    private static boolean hasAttribute(VaultGearData data, VaultGearAttribute<?> attribute) {
        return data.getAllAttributes().anyMatch(instance -> instance.getAttribute() == attribute)
                || data.getAttributes().stream().anyMatch(instance -> instance.getAttribute() == attribute);
    }

    /** Looks up an enum constant by name among {@code values}, so constants added by mixins are found too. */
    public static <E extends Enum<E>> E enumByName(E[] values, String name, String path) {
        List<String> names = new ArrayList<>();
        for (E value : values) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
            names.add(value.name());
        }
        throw new BuildException(path + ": '" + name + "' is not one of " + String.join(", ", names));
    }
}
