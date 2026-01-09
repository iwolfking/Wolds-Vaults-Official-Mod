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

public class NitwitDeckModifier extends DeckModifier<NitwitDeckModifier.Config> {

    public NitwitDeckModifier(Config config) {
        super(config);
    }

    public NitwitDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if(!pos.allowedGroups.contains("Arcane")) {
            return 1.0F;
        }

        return 1.0F + this.getModifierValue();
    }



    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        MutableComponent comp = (new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F))).withStyle(ChatFormatting.WHITE)).append((new TranslatableComponent("deck.woldsvaults.nitwit_modifier")).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String var10001 = df.format((double)(((NitwitDeckModifier.Config)this.getConfig()).modifierRoll.getMin() * 100.0F));
            comp.append(" (" + var10001 + "%-" + df.format((double)(((NitwitDeckModifier.Config)this.getConfig()).modifierRoll.getMax() * 100.0F)) + "%)");
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
            this(FloatRoll.ofUniform(0.25F, 0.5F));
        }
    }
}
