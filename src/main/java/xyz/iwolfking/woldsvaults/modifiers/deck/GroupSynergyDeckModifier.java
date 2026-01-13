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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GroupSynergyDeckModifier extends DeckModifier<GroupSynergyDeckModifier.Config> {

    public GroupSynergyDeckModifier(Config config) {
        super(config);
    }

    public GroupSynergyDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty()) return 1.0F;

        int countX = 0;

        for (Card c : deck.getCards().values()) {
            if(c == null || c.getGroups() == null || c.getGroups().isEmpty()) {
                continue;
            }

            for (String g : c.getGroups()) {
                if (config.synergyGroups.contains(g)) countX++;
            }
        }

        boolean applies = config.boostGroups.isEmpty() || card.getGroups().stream().anyMatch(config.boostGroups::contains);
        if (!applies || countX == 0) return 1.0F;

        float bonus = config.forEveryCardInGroup ? countX * this.getModifierValue() : this.getModifierValue();
        return 1.0F + bonus;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);

        MutableComponent comp = new TextComponent("+")
                .withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F)).withStyle(ChatFormatting.WHITE))
                .append(new TranslatableComponent("deck.woldsvaults.group_synergy_modifier",
                        new TextComponent(String.join(", ", config.synergyGroups)).withStyle(ChatFormatting.GOLD),
                        new TextComponent(config.boostGroups.isEmpty() ? "all" : String.join(", ", config.boostGroups)).withStyle(ChatFormatting.GREEN),
                        config.forEveryCardInGroup ? "for each " : "if you have a "
                ));

        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String min = df.format(config.modifierRoll.getMin() * 100.0F);
            String max = df.format(config.modifierRoll.getMax() * 100.0F);
            comp.append(" (" + min + "%-" + max + "%)");
        }

        tooltip.add(comp);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    /* ------------------------------
       CONFIG CLASS
       ------------------------------ */
    public static class Config extends DeckModifier.Config {

        private List<String> synergyGroups;
        private List<String> boostGroups;
        private boolean forEveryCardInGroup;

        public Config(FloatRoll roll, List<String> groupX, List<String> groupN, boolean forEachX) {
            this.synergyGroups = groupX;
            this.boostGroups = groupN;
            this.forEveryCardInGroup = forEachX;
            this.modifierRoll = roll;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.05F, 0.1F), List.of("Arcane"), List.of("Utility"), true);
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.synergyGroups = Arrays.stream((String[])GROUPS.readNbt(nbt.get("synergyGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.boostGroups = Arrays.stream((String[])GROUPS.readNbt(nbt.get("boostGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.forEveryCardInGroup = Adapters.BOOLEAN.readNbt(nbt.get("forEveryCardInGroup")).orElse(true);
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                nbt.put("synergyGroups", (Tag)GROUPS.writeNbt((String[])this.synergyGroups.toArray(String[]::new)).orElseThrow());
                nbt.put("boostGroups", (Tag)GROUPS.writeNbt((String[])this.boostGroups.toArray(String[]::new)).orElseThrow());
                Adapters.BOOLEAN.writeNbt(forEveryCardInGroup).ifPresent((tag) -> nbt.put("forEveryCardInGroup", tag));
                return nbt;
            });
        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.synergyGroups = Arrays.stream((String[])GROUPS.readJson(json.get("synergyGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.boostGroups = Arrays.stream((String[])GROUPS.readJson(json.get("boostGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.forEveryCardInGroup = Adapters.BOOLEAN.readJson(json.get("forEveryCardInGroup")).orElse(true);
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((nbt) -> {
                nbt.add("synergyGroups", (JsonElement)GROUPS.writeJson((String[])this.synergyGroups.toArray((x$0) -> new String[x$0])).orElseThrow());
                nbt.add("boostGroups", (JsonElement)GROUPS.writeJson((String[])this.boostGroups.toArray((x$0) -> new String[x$0])).orElseThrow());
                Adapters.BOOLEAN.writeJson(forEveryCardInGroup).ifPresent((tag) -> nbt.add("forEveryCardInGroup", tag));
                return nbt;
            });
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            GROUPS.writeBits((String[])this.synergyGroups.toArray((x$0) -> new String[x$0]), buffer);
            GROUPS.writeBits((String[])this.boostGroups.toArray((x$0) -> new String[x$0]), buffer);
            Adapters.BOOLEAN.writeBits(forEveryCardInGroup, buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.synergyGroups = Arrays.stream((String[])GROUPS.readBits(buffer).orElse(new String[0])).collect(Collectors.toList());
            this.boostGroups = Arrays.stream((String[])GROUPS.readBits(buffer).orElse(new String[0])).collect(Collectors.toList());
            this.forEveryCardInGroup = Adapters.BOOLEAN.readBits(buffer).orElse(true);
        }

        public List<String> getSynergyGroups() {
            return synergyGroups;
        }

        public List<String> getBoostGroups() {
            return boostGroups;
        }

        public boolean isForEveryCardInGroup() {
            return forEveryCardInGroup;
        }
    }
}
