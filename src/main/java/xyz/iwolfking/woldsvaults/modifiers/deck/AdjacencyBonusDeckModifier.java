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
import net.minecraft.network.chat.*;
import net.minecraft.world.item.TooltipFlag;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdjacencyBonusDeckModifier extends DeckModifier<AdjacencyBonusDeckModifier.Config> {

    public AdjacencyBonusDeckModifier(Config config) {
        super(config);
    }

    public AdjacencyBonusDeckModifier() {
        super(new Config());
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        if (deck.getCards().isEmpty()) {
            return 1.0F;
        }

        int matchingNeighbors = 0;

        for (int[] offset : this.config.type.getOffsets()) {
            CardPos neighborPos = pos.add(offset[0], offset[1]);
            Card neighbor = deck.getCard(neighborPos).orElse(null);

            if (neighbor == null) continue;
            for(String group : neighbor.getGroups()) {
                for(String requiredGroup : this.config.getGroups()) {
                    if(group.equals(requiredGroup)) {
                        matchingNeighbors++;
                    }
                }
            }
        }

        if (matchingNeighbors == 0) {
            return 1.0F;
        }

        float bonus = this.config.forEachAdjacent
                ? matchingNeighbors * this.getModifierValue()
                : this.getModifierValue();

        return 1.0F + bonus;
    }


    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        super.addText(tooltip, minIndex, flag, time);
        MutableComponent comp = (new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(String.format(Locale.ROOT, "%.1f%% ", this.getModifierValue() * 100.0F))).withStyle(ChatFormatting.WHITE)).append((new TranslatableComponent("deck.woldsvaults.adjacency_modifier", this.config.getDisplayGroups(), this.config.getAdjacencyDescriptionText())));
        if (Screen.hasShiftDown()) {
            DecimalFormat df = new DecimalFormat("#.##");
            String var10001 = df.format((double)(((AdjacencyBonusDeckModifier.Config)this.getConfig()).modifierRoll.getMin() * 100.0F));
            comp.append(" (" + var10001 + "%-" + df.format((double)(((AdjacencyBonusDeckModifier.Config)this.getConfig()).modifierRoll.getMax() * 100.0F)) + "%)");
        }

        tooltip.add(comp);
    }


    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    public static class Config extends DeckModifier.Config {

        private List<String> groups;

        private boolean forEachAdjacent;

        private Type type;

        public Config(FloatRoll roll, List<String> groups, boolean forEachAdjacent, Type type) {
            this.modifierRoll = roll;
            this.groups = groups;
            this.forEachAdjacent = forEachAdjacent;
            this.type = type;
        }

        public Config() {
            this(FloatRoll.ofUniform(0.05F, 0.1F), List.of("Arcane"), false, Type.ORTHOGONAL);
        }

        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.groups = Arrays.stream((String[])GROUPS.readNbt(nbt.get("requiredGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.forEachAdjacent = Adapters.BOOLEAN.readNbt(nbt.get("forEachAdjacent")).orElse(false);
            this.type = Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).readNbt(nbt.get("adjacencyType")).orElse(Type.ORTHOGONAL);
        }

        public void readJson(JsonObject json) {
            super.readJson(json);
            this.groups = Arrays.stream((String[])GROUPS.readJson(json.get("requiredGroups")).orElse(new String[0])).collect(Collectors.toList());
            this.forEachAdjacent = Adapters.BOOLEAN.readJson(json.get("forEachAdjacent")).orElse(false);
            this.type = Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).readJson(json.get("adjacencyType")).orElse(Type.ORTHOGONAL);
        }

        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                nbt.put("requiredGroups", (Tag)GROUPS.writeNbt((String[])this.groups.toArray(String[]::new)).orElseThrow());
                Adapters.BOOLEAN.writeNbt(shouldApplyForEachAdjacent()).ifPresent((tag) -> nbt.put("forEachAdjacent", tag));
                Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).writeNbt(type).ifPresent((tag) -> nbt.put("adjacencyType", tag));
                return nbt;
            });
        }

        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((nbt) -> {
                nbt.add("requiredGroups", (JsonElement)GROUPS.writeJson((String[])this.groups.toArray((x$0) -> new String[x$0])).orElseThrow());
                Adapters.BOOLEAN.writeJson(shouldApplyForEachAdjacent()).ifPresent((tag) -> nbt.add("forEachAdjacent", tag));
                Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).writeJson(type).ifPresent((tag) -> nbt.add("adjacencyType", tag));
                return nbt;
            });
        }

        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            GROUPS.writeBits((String[])this.groups.toArray((x$0) -> new String[x$0]), buffer);
            Adapters.BOOLEAN.writeBits(forEachAdjacent, buffer);
            Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).writeBits(type, buffer);
        }

        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.groups = Arrays.stream((String[])GROUPS.readBits(buffer).orElse(new String[0])).collect(Collectors.toList());
            this.forEachAdjacent = Adapters.BOOLEAN.readBits(buffer).orElse(false);
            this.type = Adapters.ofEnum(Type.class, EnumAdapter.Mode.NAME).readBits(buffer).orElse(Type.ORTHOGONAL);
        }

        public List<String> getGroups() {
            return groups;
        }

        public Component getDisplayGroups() {
            return ComponentUtils.formatList(groups);
        }

        public TranslatableComponent getAdjacencyDescriptionText() {
            return shouldApplyForEachAdjacent() ? new TranslatableComponent("deck.woldsvaults.adjacency_modifier_type_1", getAdjacencyTypeText()): new TranslatableComponent("deck.woldsvaults.adjacency_modifier_type_2", getAdjacencyTypeText());
        }

        public TranslatableComponent getAdjacencyTypeText() {
            switch (type) {
                case ORTHOGONAL -> {
                    return new TranslatableComponent("deck.woldsvaults.adjacency_type_orthogonal");
                }
                case DIAGONAL -> {
                    return new TranslatableComponent("deck.woldsvaults.adjacency_type_diagonal");
                }
                case SURROUNDING -> {
                    return new TranslatableComponent("deck.woldsvaults.adjacency_type_surrounding");
                }
                case WITHIN_TWO -> {
                    return new TranslatableComponent("deck.woldsvaults.adjacency_type_surrounding_2");
                }
                case STARCROSS -> {
                    return new TranslatableComponent("deck.woldsvaults.adjacency_type_starcross");
                }
            }

            return new TranslatableComponent("deck.woldsvaults.adjacency_type_failure");
        }

        public boolean shouldApplyForEachAdjacent() {
            return forEachAdjacent;
        }
    }

    public enum Type {
        ORTHOGONAL(new int[][]{
                { 1, 0 },
                { -1, 0 },
                { 0, 1 },
                { 0, -1 }
        }),
        DIAGONAL(new int[][]{
                { -1, -1 },
                {  1, -1 },
                { -1,  1 },
                {  1,  1 }
        }),
        SURROUNDING(new int[][]{
                {  0, -1 }, {  0,  1 }, { -1,  0 }, {  1,  0 },
                { -1, -1 }, {  1, -1 }, { -1,  1 }, {  1,  1 }
        }),
        WITHIN_TWO(new int[][]{
                {  0, -1 }, {  0,  1 }, { -1,  0 }, {  1,  0 },
                { -1, -1 }, {  1, -1 }, { -1,  1 }, {  1,  1 }
        }),
        STARCROSS(new int[][]{
                {  1,  0 },
                { -1,  0 },
                {  0,  1 },
                {  0, -1 },

                {  2,  0 },
                { -2,  0 },
                {  0,  2 },
                {  0, -2 },

                {  1,  1 },
                {  1, -1 },
                { -1,  1 },
                { -1, -1 }
        });


        private final int[][] offsets;

        Type(int[][] offsets) {
            this.offsets = offsets;
        }

        public int[][] getOffsets() {
            return offsets;
        }

        private static int[][] surroundingRadius(int r) {
            List<int[]> offsets = new ArrayList<>();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    if (x == 0 && y == 0) continue;
                    offsets.add(new int[]{x, y});
                }
            }
            return offsets.toArray(new int[0][]);
        }

    }
}
