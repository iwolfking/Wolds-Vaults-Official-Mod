package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardEntry;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChainReactionDeckModifier extends DeckModifier<ChainReactionDeckModifier.Config> {

    public ChainReactionDeckModifier(Config config) {
        super(config);
    }

    public ChainReactionDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty() || card == null || card.getColor() == null) {
            return 1.0F;
        }

        Set<CardPos> visited = new HashSet<>();

        int chainLength = getChainLength(pos, card.getColor(), deck, visited);

        int actualConnections = Math.max(0, chainLength - 1);

        if (actualConnections <= 0) {
            return 1.0F;
        }

        float totalBonus = actualConnections * this.getModifierValue();
        return 1.0F + totalBonus;
    }

    private int getChainLength(CardPos currentPos, CardEntry.Color targetColor, CardDeck deck, Set<CardPos> visited) {
        if (visited.contains(currentPos)) {
            return 0;
        }

        visited.add(currentPos);

        Card card = deck.getCard(currentPos).orElse(null);
        if (card == null || card.getColor() != targetColor) {
            return 0;
        }

        int count = 1;

        int[][] directions = { {0, 1}, {0, -1}, {1, 0}, {-1, 0} };

        for (int[] dir : directions) {
            CardPos neighborPos = new CardPos(currentPos.x + dir[0], currentPos.y + dir[1]);
            if (deck.getSlots().contains(neighborPos)) {
                count += getChainLength(neighborPos, targetColor, deck, visited);
            }
        }

        return count;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);

        MutableComponent comp = new TextComponent("+")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("card effectiveness per matching connected color link").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String min = df.format(this.config.modifierRoll.getMin() * 100.0F);
            String max = df.format(this.config.modifierRoll.getMax() * 100.0F);
            comp.append(" (" + min + "%-" + max + "%)");
        }

        tooltip.add(comp);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static class Config extends DeckModifier.Config {
        public Config() {
            this.modifierRoll = FloatRoll.ofUniform(0.02F, 0.05F);
        }
    }
}