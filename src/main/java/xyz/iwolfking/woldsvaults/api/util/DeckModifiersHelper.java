package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardDeckItem;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.mixin.accessors.DeckModifiersConfigAccessor;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.DeckModifierValueAccessor;
import xyz.iwolfking.woldsvaults.modifiers.deck.CardNeighborTypeDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

public class DeckModifiersHelper {
    private static final String MYSTERY_IMPLICIT_POOL = "card_deck_implicits";
    private static final int MYSTERY_IMPLICIT_COUNT = 2;
    private static final float MYSTERY_IMPLICIT_STRENGTH = 0.75F;

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
     * Scales a rolled mystery implicit to {@code MYSTERY_IMPLICIT_STRENGTH} of its strength: frequency
     * scalers keep their base count ({@code 1 + s x (v - 1)}), every other modifier scales its value.
     * Integer readers (Arcane levels) round the result; Extended's time factor has no value to scale.
     */
    private static void weakenMysteryImplicit(DeckModifier<?> modifier) {
        float value = modifier.getModifierValue();
        if (value < 0.0F) {
            WoldLogger.error("Mystery implicit {} was not populated before scaling; it keeps full strength.", modifier.getId());
            return;
        }
        float scaled = modifier instanceof CardNeighborTypeDeckModifier
                ? 1.0F + MYSTERY_IMPLICIT_STRENGTH * (value - 1.0F)
                : MYSTERY_IMPLICIT_STRENGTH * value;
        ((DeckModifierValueAccessor) modifier).woldsvaults$setModifierValue(scaled);
    }

    /**
     * Rolls the pair of implicits that define the mystery deck, drawn without replacement from the
     * {@code card_deck_implicits} pool, and adds them to the deck.
     */
    public static boolean applyMysteryImplicits(CardDeck deck) {
        WeightedList<String> modifierPool = ((DeckModifiersConfigAccessor) ModConfigs.DECK_MODIFIERS).getPools().get(MYSTERY_IMPLICIT_POOL);

        if(modifierPool == null) {
            WoldLogger.error("Deck modifier pool {} is missing; the mystery deck rolls with no implicits.", MYSTERY_IMPLICIT_POOL);
            return false;
        }

        List<Map.Entry<String, Double>> poolList = new ArrayList<>(modifierPool.entrySet());

        if(poolList.size() < MYSTERY_IMPLICIT_COUNT) {
            WoldLogger.error("Deck modifier pool {} holds {} entries, fewer than the {} the mystery deck needs; it rolls with no implicits.", MYSTERY_IMPLICIT_POOL, poolList.size(), MYSTERY_IMPLICIT_COUNT);
            return false;
        }

        Collections.shuffle(poolList);
        boolean applied = false;

        for(int i = 0; i < MYSTERY_IMPLICIT_COUNT; i++) {
            String modifierId = poolList.get(i).getKey();
            DeckModifier<?> modifier = createModifierWithId(modifierId);

            if(modifier == null) {
                WoldLogger.error("Deck modifier {} from pool {} could not be created; the mystery deck loses one implicit.", modifierId, MYSTERY_IMPLICIT_POOL);
                continue;
            }

            modifier.onPopulate(ChunkRandom.ofNanoTime());
            weakenMysteryImplicit(modifier);
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
            WoldLogger.error("A loot table rolled a card deck with the unknown id {}; it is left for the item tick to fill in.", deckId);
            return false;
        }

        CardDeck deck = ModConfigs.CARD_DECK.generate(deckId, ChunkRandom.ofNanoTime()).orElse(null);

        if(deck == null) {
            WoldLogger.error("The card deck config could not generate the {} deck; the rolled stack is left for the item tick to fill in.", deckId);
            return false;
        }

        if(WoldConstants.MYSTERY_DECK_ID.equals(deckId)) {
            applyMysteryImplicits(deck);
        }
        else {
            DeckModifier<?> implicit = ImplicitDeckModifiersConfig.getImplicitDeckModifier(deckId).orElse(null);

            if(implicit != null) {
                DeckModifier<?> modifier = cloneModifier(implicit);

                if(modifier == null) {
                    WoldLogger.error("The implicit modifier of the {} deck could not be cloned; the rolled deck loses it.", deckId);
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
