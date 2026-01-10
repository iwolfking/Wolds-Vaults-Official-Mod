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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DominanceDeckModifier extends DeckModifier<DominanceDeckModifier.Config> {

    public DominanceDeckModifier(Config config) {
        super(config);
    }

    public DominanceDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        String selectedGroup = this.config.mode == Mode.DOMINANT ? getMostCommonGroup(deck) : getLeastCommonGroup(deck);
        System.out.println(selectedGroup);
        if(card.getGroups().contains(selectedGroup)) {
            return 1.0F + this.getModifierValue();
        }

        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add(new TranslatableComponent("deck.woldsvaults.dominance_deck_modifier_" + config.mode.toString().toLowerCase(), this.getModifierValue()));
        super.addText(tooltip, minIndex, flag, time);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static String getMostCommonGroup(CardDeck deck) {
        return deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .flatMap(card -> card.getGroups().stream())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static String getLeastCommonGroup(CardDeck deck) {
        return deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .flatMap(card -> card.getGroups().stream())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static class Config extends DeckModifier.Config {
        private Mode mode;

        public Config(FloatRoll roll, Mode mode) {
            this.modifierRoll = roll;
            this.mode = mode;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.1F, 0.2F), Mode.DOMINANT);
        }


        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readNbt(nbt.get("mode")).orElse(Mode.DOMINANT);
        }

        public void readJson(JsonObject json) {
            super.readJson(json);
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readJson(json.get("mode")).orElse(Mode.DOMINANT);
        }

        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeNbt(mode).ifPresent((tag) -> nbt.put("mode", tag));
                return nbt;
            });
        }

        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((nbt) -> {
                Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeJson(mode).ifPresent((tag) -> nbt.add("mode", tag));
                return nbt;
            });
        }

        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeBits(mode, buffer);
        }

        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readBits(buffer).orElse(Mode.DOMINANT);
        }
    }


    public enum Mode {
        DOMINANT,
        MINORITY
    }
}
