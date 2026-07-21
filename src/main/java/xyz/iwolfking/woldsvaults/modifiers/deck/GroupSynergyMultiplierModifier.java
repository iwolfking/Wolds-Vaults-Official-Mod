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
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class GroupSynergyMultiplierModifier extends DeckModifier<GroupSynergyMultiplierModifier.Config> {

    public GroupSynergyMultiplierModifier(Config config) {
        super(config);
    }

    public GroupSynergyMultiplierModifier() {
        super(new GroupSynergyMultiplierModifier.Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (this.config == null) {
            return 1.0F;
        }

        long synergyCardCount = deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getGroups().contains(this.config.getGroupName()))
                .count();

        if (synergyCardCount == 0) {
            return 1.0F;
        }

        return (float) Math.pow(1.0F + this.getModifierValue(), synergyCardCount);
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        float rollValue = this.getModifierValue() * 100.0F;
        String groupName = this.config.getGroupName();

        MutableComponent mainComponent = new TextComponent("+")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(java.util.Locale.ROOT, "%.1f%% ", rollValue)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("Card Effectiveness per ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(groupName).withStyle(ChatFormatting.GOLD))
                .append(new TextComponent(" Card ").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
            String min = df.format(this.config.modifierRoll.getMin() * 100.0F);
            String max = df.format(this.config.modifierRoll.getMax() * 100.0F);
            mainComponent.append(new TextComponent(" (" + min + "% - " + max + "%)").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(mainComponent);

        if (Screen.hasShiftDown()) {
            MutableComponent formulaComponent = new TextComponent("  Formula: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(new TextComponent("(1.0 + ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(String.format(java.util.Locale.ROOT, "%.3f", this.getModifierValue())).withStyle(ChatFormatting.WHITE))
                    .append(new TextComponent(")^N").withStyle(ChatFormatting.GRAY));
            tooltip.add(formulaComponent);
        }
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static class Config extends DeckModifier.Config {
        private String groupName;

        public Config(FloatRoll roll, String groupName) {
            this.modifierRoll = roll;
            this.groupName = groupName;
        }

        public Config() {
            this.modifierRoll = FloatRoll.ofConstant(0.05F);
            this.groupName = "Arcane";
        }

        public String getGroupName() {
            return this.groupName;
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.groupName = Adapters.UTF_8.readNbt(nbt.get("groupName")).orElse("");
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                Adapters.UTF_8.writeNbt(this.groupName).ifPresent((tag) -> nbt.put("groupName", tag));
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.groupName = Adapters.UTF_8.readJson(json.get("groupName")).orElse("");
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                Adapters.UTF_8.writeJson(this.groupName).ifPresent((tag) -> json.add("groupName", tag));
                return json;
            });
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.groupName = Adapters.UTF_8.readBits(buffer).orElse("");
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.UTF_8.writeBits(this.groupName, buffer);
        }
    }
}