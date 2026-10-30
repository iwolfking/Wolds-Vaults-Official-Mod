package xyz.iwolfking.woldsvaults.modifiers.deck;


import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.world.roll.FloatRoll;
import iskallia.vault.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import java.util.*;

public class CardNeighborTypeDeckModifier extends DeckModifier<CardNeighborTypeDeckModifier.Config> {


    public CardNeighborTypeDeckModifier(Config config) {
        super(config);
    }

    public CardNeighborTypeDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);

        String neighborTypesString = this.config.getValidNeighorTypes().stream()
                .map(Enum::name)
                .map(StringUtils::convertToTitleCase)
                .collect(java.util.stream.Collectors.joining(", "));

        long additionalTimes = Math.round(this.getModifierValue() - 1.0F);

        MutableComponent comp = new TextComponent("Cards that scale with ")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent("[" + neighborTypesString + "]").withStyle(ChatFormatting.GOLD))
                .append(new TextComponent(" count cards an additional ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(String.valueOf(additionalTimes)).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" time(s).").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
            String min = df.format(this.config.modifierRoll.getMin());
            String max = df.format(this.config.modifierRoll.getMax());
            comp.append(" (" + min + "-" + max + ")");
        }

        tooltip.add(comp);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static class Config extends DeckModifier.Config {
        private final Set<CardNeighborType> validNeighorTypes = new LinkedHashSet<>();

        public Config(FloatRoll roll, Set<CardNeighborType> validNeighorTypes) {
            this.modifierRoll = roll;
            this.validNeighorTypes.addAll(validNeighorTypes);
        }

        public Config() {
            this(FloatRoll.ofUniform(0.05F, 0.1F), Set.of());
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.validNeighorTypes.clear();
            String[] names = Arrays.stream(GROUPS.readNbt(nbt.get("validNeighborTypes")).orElse(new String[0])).toArray(String[]::new);
            for (String name : names) {
                try {
                    this.validNeighorTypes.add(CardNeighborType.valueOf(name));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                String[] names = this.validNeighorTypes.stream().map(Enum::name).toArray(String[]::new);
                nbt.put("validNeighborTypes", GROUPS.writeNbt(names).orElseThrow());
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.validNeighorTypes.clear();
            String[] names = Arrays.stream(GROUPS.readJson(json.get("validNeighborTypes")).orElse(new String[0])).toArray(String[]::new);
            for (String name : names) {
                try {
                    this.validNeighorTypes.add(CardNeighborType.valueOf(name));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                String[] names = this.validNeighorTypes.stream().map(Enum::name).toArray(String[]::new);
                json.add("validNeighborTypes", GROUPS.writeJson(names).orElseThrow());
                return json;
            });
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            String[] names = this.validNeighorTypes.stream().map(Enum::name).toArray(String[]::new);
            GROUPS.writeBits(names, buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.validNeighorTypes.clear();
            String[] names = Arrays.stream(GROUPS.readBits(buffer).orElse(new String[0])).toArray(String[]::new);
            for (String name : names) {
                try {
                    this.validNeighorTypes.add(CardNeighborType.valueOf(name));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        public Set<CardNeighborType> getValidNeighorTypes() {
            return this.validNeighorTypes;
        }
    }
}
