package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/** Multiplies every card by {@code 1 + value x empty slots}. Void and Shadow share one factor. */
public class MultiplicativeEmptySlotDeckModifier extends EmptySlotDeckModifier implements IMultiplicativeDeckModifier {
    public static final String GROUP = "void";

    public MultiplicativeEmptySlotDeckModifier(Config config) {
        super(config);
    }

    public MultiplicativeEmptySlotDeckModifier() {
        super();
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public float getMultiplierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F + this.getModifierValue() * ((ICardDeckCache) deck).wv$getEmptySlotCount();
    }

    @Override
    public String wv$multGroup() {
        return GROUP;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        MutableComponent comp = new TextComponent("x(1 + ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%%", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" per empty slot) card effectiveness").withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            comp.append(" (" + df.format(this.getConfig().modifierRoll.getMin() * 100.0F) + "%-" + df.format(this.getConfig().modifierRoll.getMax() * 100.0F) + "%)");
        }
        tooltip.add(comp);
    }
}
