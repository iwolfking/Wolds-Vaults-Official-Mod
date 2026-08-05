package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ImplicitDeckModifiersConfig extends Config {
    @Expose
    public Map<String, String> DECKS_TO_MODIFIERS_MAP = new LinkedHashMap<>();

    @Override
    public String getName() {
        return "card%simplicit_deck_modifiers".formatted(File.separator);
    }

    @Override
    protected void reset() {
        DECKS_TO_MODIFIERS_MAP.put("merchant", "merchant_deck");
        DECKS_TO_MODIFIERS_MAP.put("extended", "extended_deck");
        DECKS_TO_MODIFIERS_MAP.put("treasure", "treasure_deck");
        DECKS_TO_MODIFIERS_MAP.put("arcane", "arcane_deck");
        DECKS_TO_MODIFIERS_MAP.put("idona", "idona_deck");
        DECKS_TO_MODIFIERS_MAP.put("velara", "velara_deck");
        DECKS_TO_MODIFIERS_MAP.put("tenos", "tenos_deck");
        DECKS_TO_MODIFIERS_MAP.put("wendarr", "wendarr_deck");
        DECKS_TO_MODIFIERS_MAP.put("cactus", "cactus_deck");
        DECKS_TO_MODIFIERS_MAP.put("champion", "champion_deck");
        DECKS_TO_MODIFIERS_MAP.put("wall", "rook_deck");
        DECKS_TO_MODIFIERS_MAP.put("cake", "cake_deck");
        DECKS_TO_MODIFIERS_MAP.put("puzzle", "puzzle_deck");
        DECKS_TO_MODIFIERS_MAP.put("mutant", "mutant_deck");
        DECKS_TO_MODIFIERS_MAP.put("pillager", "pillager_deck");
        DECKS_TO_MODIFIERS_MAP.put("belt", "belt_deck");
        DECKS_TO_MODIFIERS_MAP.put("villager", "villager_deck");
        DECKS_TO_MODIFIERS_MAP.put("runic", "runic_deck");
        DECKS_TO_MODIFIERS_MAP.put("relic", "relic_deck");
        DECKS_TO_MODIFIERS_MAP.put("fairy", "fairy_deck");
        DECKS_TO_MODIFIERS_MAP.put("snake", "snake_deck");
        DECKS_TO_MODIFIERS_MAP.put("gilded", "gilded_deck");
        DECKS_TO_MODIFIERS_MAP.put("living", "living_deck");
        DECKS_TO_MODIFIERS_MAP.put("ornate", "ornate_deck");
        DECKS_TO_MODIFIERS_MAP.put("anvil", "anvil_deck");
        DECKS_TO_MODIFIERS_MAP.put("bishop", "bishop_deck");
        DECKS_TO_MODIFIERS_MAP.put("shadow", "shadow_deck");
        DECKS_TO_MODIFIERS_MAP.put("skull", "skull_deck");
    }

    public static Optional<DeckModifier<?>> getImplicitDeckModifier(String deckId) {
        if(xyz.iwolfking.woldsvaults.init.ModConfigs.IMPLICIT_DECK_MODIFIERS.DECKS_TO_MODIFIERS_MAP.containsKey(deckId)) {
            Optional<DeckModifier<?>> deckModifier = ModConfigs.DECK_MODIFIERS.getById(xyz.iwolfking.woldsvaults.init.ModConfigs.IMPLICIT_DECK_MODIFIERS.DECKS_TO_MODIFIERS_MAP.get(deckId));
            return deckModifier.map(ImplicitDeckModifier::new);
        }

        return Optional.empty();
    }
}
