package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.world.roll.FloatRoll;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class RowPositionDeckModifier extends DeckModifier<RowPositionDeckModifier.Config> {
    public RowPositionDeckModifier(Config config) {
        super(config);
    }

    public RowPositionDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty() || card == null) return 1.0F;

        CardPos minSlot = deck.getMinSlot();
        CardPos maxSlot = deck.getMaxSlot();
        
        int totalRows = maxSlot.y - minSlot.y + 1;
        if (totalRows <= 0) return 1.0F;

        int rowDistance;
        if (config.isBottomToTop()) {
            rowDistance = maxSlot.y - pos.y + 1;
        } else {
            rowDistance = pos.y - minSlot.y + 1;
        }

        float bonus = this.getModifierValue() * rowDistance;
        return 1.0F + bonus;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);

        String referencePoint = config.isBottomToTop() ? "bottom" : "top";

        float displayPct = this.getModifierValue() * 100.0F;

        MutableComponent comp = new TextComponent("+")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", displayPct)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("card efficiency, increasing by ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", displayPct)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("per row further from the ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(referencePoint).withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent(" of the deck.").withStyle(ChatFormatting.GRAY));

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
        private boolean bottomToTop;

        public Config(FloatRoll roll, boolean bottomToTop) {
            this.modifierRoll = roll;
            this.bottomToTop = bottomToTop;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.02F, 0.05F), false);
        }

        public boolean isBottomToTop() {
            return this.bottomToTop;
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.bottomToTop = Adapters.BOOLEAN.readNbt(nbt.get("bottomToTop")).orElse(false);
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                Adapters.BOOLEAN.writeNbt(this.bottomToTop).ifPresent((tag) -> nbt.put("bottomToTop", tag));
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.bottomToTop = Adapters.BOOLEAN.readJson(json.get("bottomToTop")).orElse(false);
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                Adapters.BOOLEAN.writeJson(this.bottomToTop).ifPresent((element) -> json.add("bottomToTop", element));
                return json;
            });
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.BOOLEAN.writeBits(this.bottomToTop, buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.bottomToTop = Adapters.BOOLEAN.readBits(buffer).orElse(false);
        }
    }
}