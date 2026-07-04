package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
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
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SymmetricBalanceDeckModifier extends DeckModifier<SymmetricBalanceDeckModifier.Config> implements IMultiplicativeDeckModifier {

    public SymmetricBalanceDeckModifier(Config config) {
        super(config);
    }

    public SymmetricBalanceDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);

        MutableComponent comp = new TextComponent("x")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("card effectiveness if color-mirrored ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent("(" + this.config.getAxis().name() + ")").withStyle(ChatFormatting.GREEN));

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

    @Override
    public float getMultiplierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty() || card == null || card.getColor() == null) {
            return 1.0F;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (CardPos slot : deck.getSlots()) {
            if (slot.x < minX) minX = slot.x;
            if (slot.x > maxX) maxX = slot.x;
            if (slot.y < minY) minY = slot.y;
            if (slot.y > maxY) maxY = slot.y;
        }

        if (minX > maxX || minY > maxY) return 1.0F;

        Axis axis = this.config.getAxis();
        boolean horizontalPass = false;
        boolean verticalPass = false;

        if (axis == Axis.HORIZONTAL || axis == Axis.BOTH) {
            int mirroredX = maxX - (pos.x - minX);
            CardPos mirrorPos = new CardPos(mirroredX, pos.y);

            if (mirrorPos.equals(pos)) {
                horizontalPass = true;
            } else {
                Card mirrorCard = deck.getCard(mirrorPos).orElse(null);
                if (mirrorCard != null && mirrorCard.getColor() == card.getColor()) {
                    horizontalPass = true;
                }
            }
        }

        if (axis == Axis.VERTICAL || axis == Axis.BOTH) {
            int mirroredY = maxY - (pos.y - minY);
            CardPos mirrorPos = new CardPos(pos.x, mirroredY);

            if (mirrorPos.equals(pos)) {
                verticalPass = true;
            } else {
                Card mirrorCard = deck.getCard(mirrorPos).orElse(null);
                if (mirrorCard != null && mirrorCard.getColor() == card.getColor()) {
                    verticalPass = true;
                }
            }
        }

        boolean isBalanced = false;
        if (axis == Axis.HORIZONTAL) isBalanced = horizontalPass;
        else if (axis == Axis.VERTICAL) isBalanced = verticalPass;
        else if (axis == Axis.BOTH) isBalanced = horizontalPass && verticalPass;

        return isBalanced ? this.getModifierValue() : 1.0F;
    }

    public enum Axis {
        HORIZONTAL,
        VERTICAL,
        BOTH
    }

    public static class Config extends DeckModifier.Config {
        private Axis axis;

        public Config(FloatRoll roll, Axis axis) {
            this.modifierRoll = roll;
            this.axis = axis;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.1F, 0.2F), Axis.HORIZONTAL);
        }

        public Axis getAxis() {
            return this.axis;
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.axis = Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).readNbt(nbt.get("symmetryAxis")).orElse(Axis.HORIZONTAL);
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).writeNbt(this.axis).ifPresent((tag) -> nbt.put("symmetryAxis", tag));
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.axis = Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).readJson(json.get("symmetryAxis")).orElse(Axis.HORIZONTAL);
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).writeJson(this.axis).ifPresent((tag) -> json.add("symmetryAxis", tag));
                return json;
            });
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).writeBits(this.axis, buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.axis = Adapters.ofEnum(Axis.class, EnumAdapter.Mode.NAME).readBits(buffer).orElse(Axis.HORIZONTAL);
        }
    }
}