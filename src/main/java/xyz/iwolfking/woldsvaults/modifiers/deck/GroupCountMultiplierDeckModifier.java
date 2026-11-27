package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** A card of the group gets {@code x(1 + value x other cards of the same group in the deck)}. */
public class GroupCountMultiplierDeckModifier extends DeckModifier<GroupSynergyMultiplierModifier.Config> implements IMultiplicativeDeckModifier {

    public GroupCountMultiplierDeckModifier(GroupSynergyMultiplierModifier.Config config) {
        super(config);
    }

    public GroupCountMultiplierDeckModifier() {
        super(new GroupSynergyMultiplierModifier.Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public float getMultiplierValue(Card card, CardPos pos, CardDeck deck) {
        String group = this.config.getGroupName();
        if (card == null || !card.getGroups().contains(group)) {
            return 1.0F;
        }
        long others = deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getGroups().contains(group))
                .count() - 1;
        return 1.0F + this.getModifierValue() * Math.max(0L, others);
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        String group = this.config.getGroupName();
        MutableComponent comp = new TextComponent(group).withStyle(ChatFormatting.GOLD)
                .append(new TextComponent(" cards: x(1 + ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%%", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" per other " + group + " card)").withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            comp.append(" (" + df.format(this.config.modifierRoll.getMin() * 100.0F) + "%-" + df.format(this.config.modifierRoll.getMax() * 100.0F) + "%)");
        }
        tooltip.add(comp);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }
}
