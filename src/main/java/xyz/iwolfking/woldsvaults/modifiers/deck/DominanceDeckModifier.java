package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonElement;
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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;
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
        String selectedGroup = this.config.mode == Mode.DOMINANT
                ? getMostCommonGroup(deck, config.excludedGroups)
                : getLeastCommonGroup(deck, config.excludedGroups);
        System.out.println(selectedGroup);
        if(card.getGroups().contains(selectedGroup)) {
            return 1.0F + this.getModifierValue();
        }

        return 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add(new TranslatableComponent("deck.woldsvaults.dominance_deck_modifier_" + config.mode.toString().toLowerCase(), String.format("%.1f%%", this.getModifierValue(), new TranslatableComponent("deck.woldsvaults.dominance_deck_modifier_exclusion", String.join(", ", this.config.excludedGroups)))));
        super.addText(tooltip, minIndex, flag, time);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static String getMostCommonGroup(CardDeck deck, Set<String> excluded) {
        return deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .flatMap(card -> card.getGroups().stream())
                .filter(group -> !excluded.contains(group))
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> {
                    int cmp = e2.getValue().compareTo(e1.getValue());
                    if (cmp != 0) return cmp;
                    return e1.getKey().compareTo(e2.getKey());
                })
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static String getLeastCommonGroup(CardDeck deck, Set<String> excluded) {
        return deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .flatMap(card -> card.getGroups().stream())
                .filter(group -> !excluded.contains(group))
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> {
                    int cmp = e2.getValue().compareTo(e1.getValue());
                    if (cmp != 0) return cmp;
                    return e1.getKey().compareTo(e2.getKey());
                })
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static class Config extends DeckModifier.Config {
        private Mode mode;
        private Set<String> excludedGroups;


        public Config(FloatRoll roll, Mode mode, Set<String> excludedGroups) {
            this.modifierRoll = roll;
            this.mode = mode;
            this.excludedGroups = excludedGroups;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.1F, 0.2F), Mode.DOMINANT, Set.of());
        }


        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.excludedGroups = Arrays.stream((String[])GROUPS.readNbt(nbt.get("excludedGroups")).orElse(new String[0])).collect(Collectors.toSet());
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readNbt(nbt.get("mode")).orElse(Mode.DOMINANT);
        }

        public void readJson(JsonObject json) {
            super.readJson(json);
            this.excludedGroups = Arrays.stream((String[])GROUPS.readJson(json.get("excludedGroups")).orElse(new String[0])).collect(Collectors.toSet());
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readJson(json.get("mode")).orElse(Mode.DOMINANT);
        }

        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                nbt.put("excludedGroups", (Tag)GROUPS.writeNbt((String[])this.excludedGroups.toArray(String[]::new)).orElseThrow());
                Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeNbt(mode).ifPresent((tag) -> nbt.put("mode", tag));
                return nbt;
            });
        }

        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((nbt) -> {
                nbt.add("excludedGroups", (JsonElement)GROUPS.writeJson((String[])this.excludedGroups.toArray(String[]::new)).orElseThrow());
                Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeJson(mode).ifPresent((tag) -> nbt.add("mode", tag));
                return nbt;
            });
        }

        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            GROUPS.writeBits((String[])this.excludedGroups.toArray(String[]::new), buffer);
            Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).writeBits(mode, buffer);
        }

        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.excludedGroups = Arrays.stream((String[])GROUPS.readBits(buffer).orElse(new String[0])).collect(Collectors.toSet());
            this.mode = Adapters.ofEnum(Mode.class, EnumAdapter.Mode.NAME).readBits(buffer).orElse(Mode.DOMINANT);
        }
    }


    public enum Mode {
        DOMINANT,
        MINORITY
    }
}
