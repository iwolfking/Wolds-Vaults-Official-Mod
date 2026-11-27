package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.util.List;
import java.util.Locale;

/** Multiplies the cards in its rolled slots by {@code 1 + value}, on top of the additive core sum. */
public class MultiplicativeSlotDeckModifier extends SlotDeckModifier implements IMultiplicativeDeckModifier {

    public MultiplicativeSlotDeckModifier(Config config) {
        super(config);
    }

    public MultiplicativeSlotDeckModifier() {
        super();
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public float getMultiplierValue(Card card, CardPos pos, CardDeck deck) {
        return super.getModifierValue(card, pos, deck);
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        tooltip.add(new TextComponent(String.format(Locale.ROOT, "Multiplies: x%.2f", 1.0F + this.getModifierValue())).withStyle(ChatFormatting.GRAY));
    }
}
