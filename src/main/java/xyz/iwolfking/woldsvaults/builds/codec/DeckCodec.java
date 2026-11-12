package xyz.iwolfking.woldsvaults.builds.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.Json;
import xyz.iwolfking.woldsvaults.api.util.DeckModifiersHelper;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;
import xyz.iwolfking.vhapi.mixin.accessors.DeckModifiersConfigAccessor;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;

/**
 * Builds a card deck from a named spec: a deck template, the card in each slot and the cores. Cards are made the way
 * booster packs make them (card config, colour, scaler) and then populated, so their values are the game's; a
 * requested value that differs is reported. Core values are set to the requested roll when the game's roll range
 * allows it.
 */
public final class DeckCodec {
    private static final Set<String> DECK_KEYS = Set.of("deck", "sockets", "cards", "cores", "implicits", "layout");
    private static final Set<String> CARD_KEYS = Set.of("row", "col", "card", "tier", "color", "scaler", "groups", "value");
    private static final Set<String> CORE_KEYS = Set.of("id", "value", "roll");
    private static final int LAYOUT_TRIES = 500;
    private static final String MYSTERY_POOL = "card_deck_implicits";
    private static final float VALUE_TOLERANCE = 1.0E-4F;

    private DeckCodec() {
    }

    /** The deck item a spec describes. */
    public static ItemStack read(JsonObject json, String path, BuildReport report) {
        Json.onlyKeys(json, path, DECK_KEYS);
        String id = Json.string(json, "deck", path);
        if (!ModConfigs.CARD_DECK.has(id)) {
            throw new BuildException(path + ".deck: no card deck '" + id + "' exists in this game");
        }
        JsonArray cards = Json.optArray(json, "cards", path);
        Map<String, JsonObject> wanted = new HashMap<>();
        Map<String, String> wantedPaths = new HashMap<>();
        if (cards != null) {
            for (int i = 0; i < cards.size(); i++) {
                String cardPath = path + ".cards[" + i + "]";
                JsonObject card = Json.asObject(cards.get(i), cardPath);
                Json.onlyKeys(card, cardPath, CARD_KEYS);
                String key = Json.intValue(card, "col", cardPath) + "," + Json.intValue(card, "row", cardPath);
                if (wanted.put(key, card) != null) {
                    throw new BuildException(cardPath + ": two cards in the slot at column,row " + key);
                }
                wantedPaths.put(key, cardPath);
            }
        }
        RandomSource random = JavaRandom.ofNanoTime();
        JsonArray forced = Json.optArray(json, "layout", path);
        CardDeck deck = forced == null ? layout(id, wanted.keySet(), path, random)
                : forcedLayout(id, forced, wanted.keySet(), path, random, report);
        Map<String, CardPos> slots = new HashMap<>();
        deck.getSlots().forEach(pos -> slots.put(pos.x + "," + pos.y, pos));
        for (Map.Entry<String, JsonObject> entry : wanted.entrySet()) {
            CardPos pos = slots.get(entry.getKey());
            deck.setCard(pos, card(entry.getValue(), pos, wantedPaths.get(entry.getKey()), report));
        }
        Integer sockets = Json.optInt(json, "sockets", path);
        JsonArray cores = Json.optArray(json, "cores", path);
        int coreCount = cores == null ? 0 : cores.size();
        if (sockets != null) {
            if (sockets < coreCount) {
                throw new BuildException(path + ".sockets: " + sockets + " sockets cannot hold " + coreCount + " cores");
            }
            deck.setSocketCount(sockets);
        } else if (deck.getSocketCount() < coreCount) {
            report.bypassed(path + ": deck " + id + " has " + deck.getSocketCount() + " core sockets but the build lists "
                    + coreCount + " cores; sockets raised to " + coreCount);
            deck.setSocketCount(coreCount);
        }
        addModifiers(deck, cores, path + ".cores", random, report);
        addImplicits(deck, id, Json.optArray(json, "implicits", path), path + ".implicits", random, report);
        long socketed = deck.getModifiers().stream().filter(modifier -> !(modifier instanceof ImplicitDeckModifier)).count();
        if (socketed > deck.getSocketCount()) {
            report.bypassed(path + ": " + socketed + " cores need more than the deck's " + deck.getSocketCount()
                    + " sockets; sockets raised to " + socketed);
            deck.setSocketCount((int) socketed);
        }
        ItemStack stack = new ItemStack(ModItems.CARD_DECK);
        CardDeckItem.setId(stack, id);
        CardDeckItem.setVersion(stack, 1);
        CardDeckItem.setCardDeck(stack, deck);
        return stack;
    }

    /**
     * Adds a deck's implicits the way the deck forge does: a Mystery deck takes the listed ids from the Mystery
     * implicit pool, each wrapped as an implicit so it holds no socket; any other deck takes its own configured
     * implicit, whatever the build lists.
     */
    private static void addImplicits(CardDeck deck, String deckId, JsonArray listed, String path, RandomSource random,
                                     BuildReport report) {
        if (WoldConstants.MYSTERY_DECK_ID.equals(deckId)) {
            if (listed == null) {
                report.bypassed(path + ": a Mystery deck always has two implicits; the build lists none");
                return;
            }
            WeightedList<String> pool = ((DeckModifiersConfigAccessor) ModConfigs.DECK_MODIFIERS).getPools().get(MYSTERY_POOL);
            for (int i = 0; i < listed.size(); i++) {
                String entryPath = path + "[" + i + "]";
                String implicitId = Json.asString(listed.get(i), entryPath);
                if (pool == null || !pool.containsKey(implicitId)) {
                    report.bypassed(entryPath + ": " + implicitId + " is not in the Mystery implicit pool");
                }
                DeckModifier<?> modifier = DeckModifiersHelper.createModifierWithId(implicitId);
                if (modifier == null) {
                    throw new BuildException(entryPath + ": no deck modifier '" + implicitId + "' exists in this game");
                }
                deck.addModifier(new ImplicitDeckModifier(modifier), random);
            }
            if (listed.size() != 2) {
                report.bypassed(path + ": a Mystery deck rolls two implicits; the build lists " + listed.size());
            }
            return;
        }
        DeckModifier<?> own = ImplicitDeckModifiersConfig.getImplicitDeckModifier(deckId).orElse(null);
        if (own != null) {
            DeckModifier<?> clone = DeckModifiersHelper.cloneModifier(own);
            if (clone == null) {
                throw new BuildException(path + ": the implicit of deck " + deckId + " could not be copied");
            }
            deck.addModifier(clone, random);
            report.note(path + ": deck " + deckId + " carries its own implicit, as forged decks do");
        }
        if (listed != null && listed.size() > 0) {
            report.note(path + ": deck " + deckId + " is not a Mystery deck, so the listed implicits " + listed
                    + " are replaced by its own");
        }
    }

    /** A deck of template {@code id} whose layout holds every requested slot; templates may roll their layout. */
    private static CardDeck layout(String id, Set<String> wanted, String path, RandomSource random) {
        for (int attempt = 0; attempt < LAYOUT_TRIES; attempt++) {
            CardDeck deck = ModConfigs.CARD_DECK.generate(id, random).orElseThrow(
                    () -> new BuildException(path + ".deck: the game could not generate deck '" + id + "'"));
            Set<String> have = new HashSet<>();
            deck.getSlots().forEach(pos -> have.add(pos.x + "," + pos.y));
            if (have.containsAll(wanted)) {
                return deck;
            }
        }
        throw new BuildException(path + ": no layout of deck '" + id + "' has a slot at every listed column,row");
    }

    /**
     * A deck of template {@code id} with exactly the listed slots. Used when the build needs a shape the template
     * cannot roll (Construction cores add slots at random); reported as a bypassed rule when no template layout fits.
     */
    private static CardDeck forcedLayout(String id, JsonArray slots, Set<String> wanted, String path, RandomSource random,
                                         BuildReport report) {
        CardDeck template = ModConfigs.CARD_DECK.generate(id, random).orElseThrow(
                () -> new BuildException(path + ".deck: the game could not generate deck '" + id + "'"));
        CardDeck deck = new CardDeck();
        deck.setSocketCount(template.getSocketCount());
        Set<String> have = new HashSet<>();
        for (int i = 0; i < slots.size(); i++) {
            String slotPath = path + ".layout[" + i + "]";
            JsonObject slot = Json.asObject(slots.get(i), slotPath);
            Json.onlyKeys(slot, slotPath, Set.of("row", "col", "arcane"));
            int row = Json.intValue(slot, "row", slotPath);
            int col = Json.intValue(slot, "col", slotPath);
            if (row < 0 || col < 0) {
                throw new BuildException(slotPath + ": rows and columns start at 0");
            }
            if (!have.add(col + "," + row)) {
                throw new BuildException(slotPath + ": the slot at column,row " + col + "," + row + " is listed twice");
            }
            boolean arcane = Boolean.TRUE.equals(Json.optBoolean(slot, "arcane", slotPath));
            deck.setCard(arcane ? new CardPos(col, row, "Arcane") : new CardPos(col, row), null);
        }
        if (!have.containsAll(wanted)) {
            throw new BuildException(path + ".layout: a card sits in a slot the layout does not list");
        }
        Set<String> templateSlots = new HashSet<>();
        template.getSlots().forEach(pos -> templateSlots.add(pos.x + "," + pos.y));
        if (!templateSlots.equals(have)) {
            report.bypassed(path + ": deck " + id + " uses the build's own " + have.size() + "-slot layout; the game's "
                    + id + " template rolls " + templateSlots.size() + " slots (Construction cores add slots at random)");
        }
        return deck;
    }

    private static Card card(JsonObject json, CardPos pos, String path, BuildReport report) {
        String cardId = Json.string(json, "card", path);
        if (cardId.startsWith("@")) {
            throw new BuildException(path + ".card: give a card id, not a pool");
        }
        CardEntry.Config config = ModConfigs.CARD_MODIFIERS.getRandom(cardId, JavaRandom.ofNanoTime()).orElseThrow(
                () -> new BuildException(path + ".card: no card '" + cardId + "' exists in this game"));
        int tier = Json.intValue(json, "tier", path);
        if (tier < 1) {
            throw new BuildException(path + ".tier: card tiers start at 1");
        }
        String colorName = Json.optString(json, "color", path);
        CardEntry.Color color = null;
        if (colorName != null) {
            color = GearCodec.enumByName(CardEntry.Color.values(), colorName, path + ".color");
            config.colors = new HashSet<>(config.colors == null ? Set.of() : config.colors);
            config.colors.add(color);
        }
        JsonArray groups = Json.optArray(json, "groups", path);
        if (groups != null) {
            config.groups = new HashSet<>(config.groups == null ? Set.of() : config.groups);
            for (int i = 0; i < groups.size(); i++) {
                config.groups.add(Json.asString(groups.get(i), path + ".groups[" + i + "]"));
            }
        }
        CardEntry entry = config.toEntry();
        String scalerName = Json.optString(json, "scaler", path);
        if (scalerName != null) {
            CardNeighborType neighbor = GearCodec.enumByName(CardNeighborType.values(), scalerName, path + ".scaler");
            entry.setScaler(scaler(neighbor, color, path));
        }
        if (entry.getModifier() != null) {
            entry.getModifier().onPopulate();
        }
        if (entry.getScaler() != null) {
            entry.getScaler().onPopulate();
        }
        if (entry.getCondition() != null) {
            entry.getCondition().onPopulate();
        }
        Card card = new Card(tier, List.of(entry));
        Float wantedValue = json.has("value") ? (float) Json.asDouble(json.get("value"), path + ".value") : null;
        if (wantedValue != null) {
            Float gameValue = cardValue(card, tier);
            if (gameValue == null) {
                report.note(path + ": card " + cardId + " has no single value to compare with the requested " + wantedValue);
            } else if (Math.abs(gameValue - wantedValue) > VALUE_TOLERANCE * Math.max(1.0F, Math.abs(wantedValue))) {
                report.bypassed(path + ": card " + cardId + " tier " + tier + " is worth " + gameValue + " in this game, not the "
                        + wantedValue + " the build assumed; the game value is kept");
            }
        }
        return card;
    }

    private static CardScaler scaler(CardNeighborType neighbor, CardEntry.Color color, String path) {
        JsonObject filter = new JsonObject();
        filter.add("neighborFilter", weighted(neighbor.name()));
        if (color != null) {
            filter.add("colorFilter", weighted(color.name()));
        }
        JsonArray filters = new JsonArray();
        filters.add(filter);
        JsonObject option = new JsonObject();
        option.add("filters", filters);
        option.addProperty("weight", 1.0);
        JsonArray pool = new JsonArray();
        pool.add(option);
        JsonObject tierEntry = new JsonObject();
        tierEntry.addProperty("tier", 1);
        tierEntry.add("pool", pool);
        JsonArray tiers = new JsonArray();
        tiers.add(tierEntry);
        JsonObject json = new JsonObject();
        json.add("tiers", tiers);
        try {
            return CardScaler.ADAPTER.readJson(json).orElseThrow();
        } catch (RuntimeException e) {
            throw new BuildException(path + ".scaler: the game would not build a " + neighbor + " scaler ("
                    + e.getClass().getSimpleName() + ")", e);
        }
    }

    private static JsonArray weighted(String value) {
        JsonArray values = new JsonArray();
        values.add(value);
        JsonObject option = new JsonObject();
        option.add("value", values);
        option.addProperty("weight", 1.0);
        JsonArray list = new JsonArray();
        list.add(option);
        return list;
    }

    /** The card's value at {@code tier} from its populated NBT, or null when it has none or several. */
    private static Float cardValue(Card card, int tier) {
        CompoundTag tag = Card.ADAPTER.writeNbt(card).orElse(null);
        if (tag == null) {
            return null;
        }
        Float found = null;
        for (Tag entryTag : tag.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag modifier = ((CompoundTag) entryTag).getCompound("modifier");
            ListTag values = modifier.getList("values", Tag.TAG_COMPOUND);
            Float best = null;
            int bestTier = Integer.MIN_VALUE;
            for (Tag valueTag : values) {
                CompoundTag value = (CompoundTag) valueTag;
                int valueTier = value.getInt("tier");
                if (valueTier <= tier && valueTier > bestTier && value.get("value") instanceof NumericTag number) {
                    best = number.getAsFloat();
                    bestTier = valueTier;
                }
            }
            if (best == null || found != null) {
                return null;
            }
            found = best;
        }
        return found;
    }

    private static void addModifiers(CardDeck deck, JsonArray list, String path, RandomSource random, BuildReport report) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            String corePath = path + "[" + i + "]";
            JsonObject json = list.get(i).isJsonPrimitive() ? null : Json.asObject(list.get(i), corePath);
            String id = json == null ? Json.asString(list.get(i), corePath) : Json.string(json, "id", corePath);
            if (json != null) {
                Json.onlyKeys(json, corePath, CORE_KEYS);
            }
            DeckModifier<?> modifier = ModConfigs.DECK_MODIFIERS.getById(id).orElseThrow(
                    () -> new BuildException(corePath + ": no deck core or modifier '" + id + "' exists in this game"));
            CompoundTag template = nbt(modifier, corePath);
            String roll = json == null ? null : Json.optString(json, "roll", corePath);
            if (roll != null) {
                CompoundTag config = template.getCompound("config");
                if (!config.getCompound("modifierValues").contains(roll)) {
                    throw new BuildException(corePath + ".roll: core " + id + " has no '" + roll + "' roll (it has "
                            + config.getCompound("modifierValues").getAllKeys() + ")");
                }
                config.putString("selectedRoll", roll);
                template.put("config", config);
                modifier = DeckModifier.ADAPTER.readNbt(template).orElseThrow();
            }
            int before = deck.getModifiers().size();
            deck.addModifier(modifier, random);
            if (deck.getModifiers().size() == before) {
                report.bypassed(corePath + ": the deck already holds a better " + id + "; the duplicate was not added");
                continue;
            }
            if (json != null && json.has("value")) {
                setValue(deck, deck.getModifiers().size() - 1, (float) Json.asDouble(json.get("value"), corePath + ".value"),
                        corePath, id, report);
            }
        }
    }

    private static void setValue(CardDeck deck, int index, float wanted, String path, String id, BuildReport report) {
        DeckModifier<?> added = deck.getModifiers().get(index);
        CompoundTag tag = nbt(added, path);
        if (!tag.contains("modifier_value", Tag.TAG_ANY_NUMERIC)) {
            report.note(path + ": core " + id + " has no single value; it keeps the game's roll");
            return;
        }
        float rolled = tag.getFloat("modifier_value");
        float[] range = rollRange(tag.getCompound("config"));
        float value = wanted;
        if (range != null && (wanted < range[0] - VALUE_TOLERANCE || wanted > range[1] + VALUE_TOLERANCE)) {
            value = Math.max(range[0], Math.min(range[1], wanted));
            report.bypassed(path + ": core " + id + " cannot roll " + wanted + " in this game (range " + range[0] + " to "
                    + range[1] + "); set to " + value);
        } else if (range == null) {
            report.note(path + ": core " + id + " roll range unknown; value set to " + wanted + " (game rolled " + rolled + ")");
        }
        tag.putFloat("modifier_value", value);
        DeckModifier<?> replaced = DeckModifier.ADAPTER.readNbt(tag).orElseThrow(
                () -> new BuildException(path + ": the game could not read back core " + id));
        deck.removeModifiersMatching(modifier -> modifier == added);
        deck.addModifier(replaced, JavaRandom.ofNanoTime());
        DeckModifier<?> last = deck.getModifiers().get(deck.getModifiers().size() - 1);
        float now = nbt(last, path).getFloat("modifier_value");
        if (Math.abs(now - value) > VALUE_TOLERANCE * Math.max(1.0F, Math.abs(value))) {
            report.bypassed(path + ": core " + id + " kept the game's roll " + now + "; the requested " + value
                    + " did not stick");
        }
    }

    private static CompoundTag nbt(DeckModifier<?> modifier, String path) {
        Tag tag = DeckModifier.ADAPTER.writeNbt(modifier).orElse(null);
        if (!(tag instanceof CompoundTag compound)) {
            throw new BuildException(path + ": the game could not write this deck modifier");
        }
        return compound.copy();
    }

    private static float[] rollRange(CompoundTag config) {
        String selected = config.getString("selectedRoll");
        Tag roll = selected.isEmpty() ? config.get("modifierValue")
                : config.getCompound("modifierValues").getCompound(selected).get("value");
        if (!(roll instanceof CompoundTag compound)) {
            return null;
        }
        if (compound.contains("min", Tag.TAG_ANY_NUMERIC) && compound.contains("max", Tag.TAG_ANY_NUMERIC)) {
            float a = compound.getFloat("min");
            float b = compound.getFloat("max");
            return new float[]{Math.min(a, b), Math.max(a, b)};
        }
        if (compound.contains("value", Tag.TAG_ANY_NUMERIC)) {
            float v = compound.getFloat("value");
            return new float[]{v, v};
        }
        return null;
    }
}
