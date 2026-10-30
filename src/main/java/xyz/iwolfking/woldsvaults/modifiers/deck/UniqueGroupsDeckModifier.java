
package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.world.roll.FloatRoll;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

public class UniqueGroupsDeckModifier extends DeckModifier<UniqueGroupsDeckModifier.Config> {
    public UniqueGroupsDeckModifier(Config config) {
        super(config);
    }

    public UniqueGroupsDeckModifier() {
        this(new Config());
    }

    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (card != null && deck != null && card.getGroups().contains("Stat")) {
            int uniqueGroupCount = this.getUniqueGroupCount(deck);
            return uniqueGroupCount > 0 && !(this.getModifierValue() <= 0.0F) ? 1.0F + this.getModifierValue() * (float)uniqueGroupCount : 1.0F;
        } else {
            return 1.0F;
        }
    }

    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        MutableComponent comp = (new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(String.format(Locale.ROOT, "%.1f%%", this.getModifierValue() * 100.0F))).withStyle(ChatFormatting.WHITE)).append((new TextComponent(" Stat card efficiency per unique card group")).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String var10001 = df.format(((Config)this.getConfig()).modifierRoll.getMin() * 100.0F);
            comp.append(" (" + var10001 + "%-" + df.format(((Config)this.getConfig()).modifierRoll.getMax() * 100.0F) + "%)");
        }

        tooltip.add(comp);
    }

    public boolean voidConfigIfPopulated() {
        return false;
    }

    private int getUniqueGroupCount(CardDeck deck) {
        Set<String> uniqueGroups = new HashSet<>();
        Stream<List<String>> groups = deck.getCards().values().stream().filter(Objects::nonNull).map(Card::getGroups);
        groups.forEach(uniqueGroups::addAll);
        return uniqueGroups.size();
    }

    public static class Config extends DeckModifier.Config {
        public Config(FloatRoll roll) {
            this.modifierRoll = roll;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.05F, 0.2F));
        }
    }
}
