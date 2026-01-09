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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class EmptySlotDeckModifier extends DeckModifier<EmptySlotDeckModifier.Config> {

    private int cachedEmptySlots = -1;

    public EmptySlotDeckModifier(Config config) {
        super(config);
    }

    public EmptySlotDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        int emptySlotCount = ((ICardDeckCache) deck).wv$getEmptySlotCount();

        if(cachedEmptySlots == -1) {
            cachedEmptySlots = emptySlotCount;
        }

        float bonus;

        if(emptySlotCount != cachedEmptySlots && emptySlotCount == 0) {
            bonus = this.getModifierValue() * cachedEmptySlots;
        }
        else {
            bonus = this.getModifierValue() * emptySlotCount;
            cachedEmptySlots = emptySlotCount;
        }


        return 1.0F + bonus;
    }



    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        MutableComponent comp = (new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F))).withStyle(ChatFormatting.WHITE)).append((new TranslatableComponent("deck.woldsvaults.void_modifier")).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String var10001 = df.format((double)(((EmptySlotDeckModifier.Config)this.getConfig()).modifierRoll.getMin() * 100.0F));
            comp.append(" (" + var10001 + "%-" + df.format((double)(((EmptySlotDeckModifier.Config)this.getConfig()).modifierRoll.getMax() * 100.0F)) + "%)");
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
            this(FloatRoll.ofUniform(0.05F, 0.1F));
        }
    }
}
