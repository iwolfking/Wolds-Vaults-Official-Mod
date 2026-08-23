package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.mixin.accessors.DeckModifiersConfigAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeckModifiersHelper {
    public static final String MYSTERY_DECK_ID = "mystery";
    private static final String MYSTERY_IMPLICIT_POOL = "card_deck_implicits";
    private static final int MYSTERY_IMPLICIT_COUNT = 2;

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

    /**
     * Rolls the pair of implicits that define the mystery deck, drawn without replacement from the
     * {@code card_deck_implicits} pool, and adds them to the deck.
     */
    public static boolean applyMysteryImplicits(CardDeck deck) {
        WeightedList<String> modifierPool = ((DeckModifiersConfigAccessor) ModConfigs.DECK_MODIFIERS).getPools().get(MYSTERY_IMPLICIT_POOL);

        if(modifierPool == null) {
            WoldsVaults.LOGGER.error("Deck modifier pool {} is missing; the mystery deck rolls with no implicits.", MYSTERY_IMPLICIT_POOL);
            return false;
        }

        List<Map.Entry<String, Double>> poolList = new ArrayList<>(modifierPool.entrySet());

        if(poolList.size() < MYSTERY_IMPLICIT_COUNT) {
            WoldsVaults.LOGGER.error("Deck modifier pool {} holds {} entries, fewer than the {} the mystery deck needs; it rolls with no implicits.", MYSTERY_IMPLICIT_POOL, poolList.size(), MYSTERY_IMPLICIT_COUNT);
            return false;
        }

        Collections.shuffle(poolList);
        boolean applied = false;

        for(int i = 0; i < MYSTERY_IMPLICIT_COUNT; i++) {
            String modifierId = poolList.get(i).getKey();
            DeckModifier<?> modifier = createModifierWithId(modifierId);

            if(modifier == null) {
                WoldsVaults.LOGGER.error("Deck modifier {} from pool {} could not be created; the mystery deck loses one implicit.", modifierId, MYSTERY_IMPLICIT_POOL);
                continue;
            }

            deck.addModifier(new ImplicitDeckModifier(modifier), ChunkRandom.ofNanoTime());
            applied = true;
        }

        return applied;
    }

    /**
     * Fills in a card deck that a loot table rolled rather than the deck forge crafted, generating its layout and
     * adding the implicits the forge would otherwise have supplied.
     */
    public static boolean initializeLootDeck(ItemStack stack) {
        if(!(stack.getItem() instanceof CardDeckItem) || CardDeckItem.hasCardDeck(stack)) {
            return false;
        }

        String deckId = CardDeckItem.getId(stack);

        if(deckId == null || !ModConfigs.CARD_DECK.has(deckId)) {
            WoldsVaults.LOGGER.error("A loot table rolled a card deck with the unknown id {}; it is left for the item tick to fill in.", deckId);
            return false;
        }

        CardDeck deck = ModConfigs.CARD_DECK.generate(deckId, ChunkRandom.ofNanoTime()).orElse(null);

        if(deck == null) {
            WoldsVaults.LOGGER.error("The card deck config could not generate the {} deck; the rolled stack is left for the item tick to fill in.", deckId);
            return false;
        }

        if(MYSTERY_DECK_ID.equals(deckId)) {
            applyMysteryImplicits(deck);
        }
        else {
            DeckModifier<?> implicit = ImplicitDeckModifiersConfig.getImplicitDeckModifier(deckId).orElse(null);

            if(implicit != null) {
                DeckModifier<?> modifier = cloneModifier(implicit);

                if(modifier == null) {
                    WoldsVaults.LOGGER.error("The implicit modifier of the {} deck could not be cloned; the rolled deck loses it.", deckId);
                }
                else {
                    deck.addModifier(modifier, ChunkRandom.ofNanoTime());
                }
            }
        }

        CardDeckItem.setVersion(stack, CardDeckItem.latestVersion);
        CardDeckItem.setCardDeck(stack, deck);
        return true;
    }
}
