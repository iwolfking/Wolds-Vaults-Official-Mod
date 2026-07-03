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
        DECKS_TO_MODIFIERS_MAP.put("merchant", "bazaar");
    }

    public static Optional<DeckModifier<?>> getImplicitDeckModifier(String deckId) {
        if(xyz.iwolfking.woldsvaults.init.ModConfigs.IMPLICIT_DECK_MODIFIERS.DECKS_TO_MODIFIERS_MAP.containsKey(deckId)) {
            Optional<DeckModifier<?>> deckModifier = ModConfigs.DECK_MODIFIERS.getById(xyz.iwolfking.woldsvaults.init.ModConfigs.IMPLICIT_DECK_MODIFIERS.DECKS_TO_MODIFIERS_MAP.get(deckId));
            return deckModifier.map(ImplicitDeckModifier::new);
        }

        return Optional.empty();
    }
}
