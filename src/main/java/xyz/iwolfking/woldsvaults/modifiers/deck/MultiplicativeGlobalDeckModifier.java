package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.GlobalDeckModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Multiplies matching cards by {@code 1 + value}, on top of the additive core sum. */
public class MultiplicativeGlobalDeckModifier extends GlobalDeckModifier implements IMultiplicativeDeckModifier {

    public MultiplicativeGlobalDeckModifier(Config config) {
        super(config);
    }

    public MultiplicativeGlobalDeckModifier() {
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
        List<String> affected = new ArrayList<>();
        this.getConfig().requiredColors.forEach(color -> affected.add(capitalize(color.name())));
        this.getConfig().requiredGroups.forEach(group -> affected.add(capitalize(group)));
        MutableComponent comp = new TextComponent(String.format(Locale.ROOT, "x%.2f", 1.0F + this.getModifierValue())).withStyle(ChatFormatting.WHITE)
                .append(new TextComponent(" efficiency to ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(affected.isEmpty() ? "all" : String.join(", ", affected)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" cards").withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            comp.append(" (x" + df.format(1.0F + this.getConfig().modifierRoll.getMin()) + "-x" + df.format(1.0F + this.getConfig().modifierRoll.getMax()) + ")");
        }
        tooltip.add(comp);
    }

    private static String capitalize(String value) {
        return value.isEmpty() ? value : value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }
}
