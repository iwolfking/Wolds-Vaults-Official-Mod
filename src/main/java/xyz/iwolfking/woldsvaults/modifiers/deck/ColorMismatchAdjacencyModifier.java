package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.basic.EnumAdapter;
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
import java.util.Set;

public class ColorMismatchAdjacencyModifier extends DeckModifier<ColorMismatchAdjacencyModifier.Config> {

    public ColorMismatchAdjacencyModifier(Config config) {
        super(config);
    }

    public ColorMismatchAdjacencyModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty() || card == null) {
            return 1.0F;
        }

        CardEntry.Color currentFuncColor = card.getColor();
        if (currentFuncColor == null) {
            return 1.0F;
        }

        int mismatchCount = 0;
        Set<CardPos> targetPositions = this.config.getType().getPositions(pos, deck);

        for (CardPos neighborPos : targetPositions) {
            if (neighborPos.equals(pos)) continue;

            Card neighbor = deck.getCard(neighborPos).orElse(null);
            if (neighbor == null) continue;

            CardEntry.Color neighborColor = neighbor.getColor();
            if (neighborColor != null && neighborColor != currentFuncColor) {
                mismatchCount++;
            }
        }

        if (mismatchCount == 0) {
            return 1.0F;
        }

        float bonus = this.config.shouldApplyForEachAdjacent()
                ? mismatchCount * this.getModifierValue()
                : this.getModifierValue();

        return 1.0F + bonus;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        
        String strategyText = this.config.shouldApplyForEachAdjacent() ? "per mismatching color adjacent card" : "if any adjacent color mismatches";
        
        MutableComponent comp = new TextComponent("+")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("card effectiveness ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(strategyText).withStyle(ChatFormatting.YELLOW));

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
        private boolean forEachAdjacent;
        private AdjacencyBonusDeckModifier.Type type;

        public Config(FloatRoll roll, boolean forEachAdjacent, AdjacencyBonusDeckModifier.Type type) {
            this.modifierRoll = roll;
            this.forEachAdjacent = forEachAdjacent;
            this.type = type;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.05F, 0.1F), false, AdjacencyBonusDeckModifier.Type.ORTHOGONAL);
        }

        public boolean shouldApplyForEachAdjacent() {
            return this.forEachAdjacent;
        }

        public AdjacencyBonusDeckModifier.Type getType() {
            return this.type;
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.forEachAdjacent = Adapters.BOOLEAN.readNbt(nbt.get("forEachAdjacent")).orElse(false);
            this.type = Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).readNbt(nbt.get("adjacencyType")).orElse(AdjacencyBonusDeckModifier.Type.ORTHOGONAL);
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                Adapters.BOOLEAN.writeNbt(this.forEachAdjacent).ifPresent((tag) -> nbt.put("forEachAdjacent", tag));
                Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).writeNbt(this.type).ifPresent((tag) -> nbt.put("adjacencyType", tag));
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.forEachAdjacent = Adapters.BOOLEAN.readJson(json.get("forEachAdjacent")).orElse(false);
            this.type = Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).readJson(json.get("adjacencyType")).orElse(AdjacencyBonusDeckModifier.Type.ORTHOGONAL);
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                Adapters.BOOLEAN.writeJson(this.forEachAdjacent).ifPresent((tag) -> json.add("forEachAdjacent", tag));
                Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).writeJson(this.type).ifPresent((tag) -> json.add("adjacencyType", tag));
                return json;
            });
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.BOOLEAN.writeBits(this.forEachAdjacent, buffer);
            Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).writeBits(this.type, buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.forEachAdjacent = Adapters.BOOLEAN.readBits(buffer).orElse(false);
            this.type = Adapters.ofEnum(AdjacencyBonusDeckModifier.Type.class, EnumAdapter.Mode.NAME).readBits(buffer).orElse(AdjacencyBonusDeckModifier.Type.ORTHOGONAL);
        }
    }
}