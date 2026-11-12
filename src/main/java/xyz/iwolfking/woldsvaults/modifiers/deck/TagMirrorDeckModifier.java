package xyz.iwolfking.woldsvaults.modifiers.deck;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Runic: a card whose mirrored slot holds a non-greed card of the same colour gets
 * {@code x(1 + value x (shared groups + 1))}, the colour counting as one shared tag. A card on the
 * mirror axis matches itself: all its groups plus its colour.
 */
public class TagMirrorDeckModifier extends SymmetricBalanceDeckModifier {
    private static final String GREED_GROUP = "Greed";

    public TagMirrorDeckModifier(Config config) {
        super(config);
    }

    public TagMirrorDeckModifier() {
        super();
    }

    @Override
    public float getMultiplierValue(Card card, CardPos pos, CardDeck deck) {
        if (card == null || card.getColor() == null || deck.getSlots().isEmpty()) {
            return 1.0F;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CardPos slot : deck.getSlots()) {
            minX = Math.min(minX, slot.x);
            maxX = Math.max(maxX, slot.x);
            minY = Math.min(minY, slot.y);
            maxY = Math.max(maxY, slot.y);
        }
        Axis axis = this.getConfig().getAxis();
        int tags = Integer.MAX_VALUE;
        if (axis == Axis.HORIZONTAL || axis == Axis.BOTH) {
            tags = Math.min(tags, this.sharedTags(card, pos, new CardPos(maxX - (pos.x - minX), pos.y), deck));
        }
        if (axis == Axis.VERTICAL || axis == Axis.BOTH) {
            tags = Math.min(tags, this.sharedTags(card, pos, new CardPos(pos.x, maxY - (pos.y - minY)), deck));
        }
        if (tags <= 0 || tags == Integer.MAX_VALUE) {
            return 1.0F;
        }
        return 1.0F + this.getModifierValue() * tags;
    }

    private int sharedTags(Card card, CardPos pos, CardPos mirrorPos, CardDeck deck) {
        if (mirrorPos.equals(pos)) {
            return card.getGroups().size() + 1;
        }
        Card mirror = deck.getCard(mirrorPos).orElse(null);
        if (mirror == null || mirror.getColor() != card.getColor() || mirror.getGroups().contains(GREED_GROUP)) {
            return 0;
        }
        Set<String> shared = new HashSet<>(card.getGroups());
        shared.retainAll(mirror.getGroups());
        return shared.size() + 1;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        MutableComponent comp = new TextComponent("x(1 + ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.0f%%", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" per shared tag) if color-mirrored ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent("(" + this.getConfig().getAxis().name() + ")").withStyle(ChatFormatting.GREEN));
        tooltip.add(comp);
    }
}
