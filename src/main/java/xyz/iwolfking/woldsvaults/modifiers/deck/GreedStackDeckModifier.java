package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.world.roll.FloatRoll;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Greed core: a greed card whose target has two or more greed cards on it adds this value to its
 * own bonus. Read by the deck's greed multiplier; it changes no card on its own.
 */
public class GreedStackDeckModifier extends DeckModifier<GreedStackDeckModifier.Config> {

    public GreedStackDeckModifier(Config config) {
        super(config);
    }

    public GreedStackDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        MutableComponent comp = new TextComponent("+").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.0f%%", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" to each Greed card on a card targeted by 2+ Greed cards").withStyle(ChatFormatting.GRAY));
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

    public static class Config extends DeckModifier.Config {
        public Config(FloatRoll roll) {
            this.modifierRoll = roll;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.1F, 0.15F));
        }
    }
}
