package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeckModifiersHelper {
    public static <T extends DeckModifier<?>> boolean hasImplicitDeckModifierOfType(CardDeck deck, Class<T> instance) {
        List<ImplicitDeckModifier> implicitDeckModifiers = deck.getModifiersOfType(ImplicitDeckModifier.class);
        return implicitDeckModifiers.stream().anyMatch(implicitDeckModifier -> instance.isInstance(implicitDeckModifier.getModifier()));
    }

    public static <T extends DeckModifier<?>> List<T> getDeckModifiersOfType(CardDeck deck, Class<T> instance) {
        List<T> deckMods = new ArrayList<>();
        List<ImplicitDeckModifier> implicitDeckModifiers = deck.getModifiersOfType(ImplicitDeckModifier.class);
        deckMods.addAll(implicitDeckModifiers.stream().map(ImplicitDeckModifier::getModifier).filter(instance::isInstance).map(instance::cast).toList());
        deckMods.addAll(deck.getModifiersOfType(instance));
        return deckMods;
    }

    public static DeckModifier<?> cloneModifier(DeckModifier<?> modifier) {
        DeckModifier<?> outputModifier = DeckModifier.ADAPTER.writeJson(modifier).flatMap(DeckModifier.ADAPTER::readJson).orElse(null);

        if(outputModifier == null) {
            return null;
        }

        outputModifier.onPopulate(ChunkRandom.ofNanoTime());

        return outputModifier;
    }

    public static DeckModifier<?> createModifierWithId(String modfierId) {
        Optional<DeckModifier<?>> modifier = ModConfigs.DECK_MODIFIERS.getById(modfierId);
        return modifier.map(DeckModifiersHelper::cloneModifier).orElse(null);

    }
}
