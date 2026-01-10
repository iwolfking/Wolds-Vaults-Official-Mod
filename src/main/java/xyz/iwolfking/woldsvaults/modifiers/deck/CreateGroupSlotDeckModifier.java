package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.client.gui.screen.CardDeckScreen;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.IPopulateOnApplyModifier;
import iskallia.vault.core.card.modifier.deck.ISlotModifier;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CreateGroupSlotDeckModifier extends DeckModifier<CreateGroupSlotDeckModifier.Config> implements IPopulateOnApplyModifier, ISlotModifier {
    private List<CardPos> affectedSlots = new ArrayList<>();
    private int slotRoll;

    public CreateGroupSlotDeckModifier(Config config) {
        super(config);
        this.affectedSlots = new ArrayList<>();
    }

    public CreateGroupSlotDeckModifier() {
        super(new Config("Arcane"));
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return 1.0F;
    }

    public boolean onPopulate(RandomSource random) {
        if (!super.onPopulate(random)) {
            return false;
        } else {
            this.affectedSlots = new ArrayList<>();
            this.slotRoll = this.config.getSlotRoll(this.config.getSelectedRollId()).get(random);
            return true;
        }
    }

    @Override
    public void populateOnApply(CardDeck deck, RandomSource rand) {
        List<CardPos> availablePositions = new ArrayList<>(deck.getSlots());
        availablePositions.removeIf((pos) -> pos.allowedGroups.contains(config.groupName));

        for(int i = 0; i < slotRoll; ++i) {
            CardPos position = availablePositions.get(rand.nextInt(availablePositions.size()));
            if (position != null) {
                position.allowedGroups.add(config.groupName);
                this.getAffectedSlots().add(position);
                availablePositions.remove(position);
            }
        }
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        MutableComponent comp = (new TextComponent("+")).append((new TextComponent(slotRoll + " ").append(new TextComponent(config.groupName).withStyle(ChatFormatting.AQUA))));

        comp.append(StringUtils.pluralise(" slot", slotRoll));
        if (Screen.hasShiftDown()) {
            IntRoll slotRoll = this.getConfig().getSlotRoll(this.getConfig().getSelectedRollId());
            comp.append(" (Slots: " + slotRoll.getMin() + " - " + slotRoll.getMax() + ")");
        }

        tooltip.add(comp);
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return false;
    }

    @Override
    public List<CardPos> getAffectedSlots() {
        return this.affectedSlots;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            if (!this.isPopulated()) {
                return nbt;
            } else {
                ListTag listTag = new ListTag();

                for(CardPos cardPos : this.affectedSlots) {
                    CompoundTag compoundTag = new CompoundTag();
                    CardPos.ADAPTER.writeNbt(cardPos).ifPresent((tag) -> compoundTag.put("pos", tag));
                    listTag.add(compoundTag);
                }

                nbt.put("affectedSlots", listTag);
                Adapters.INT.writeNbt(this.slotRoll).ifPresent((tag) -> nbt.put("slotRoll", tag));
                return nbt;
            }
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.affectedSlots.clear();
        if (nbt.contains("affectedSlots")) {
            ListTag listTag = nbt.getList("affectedSlots", 10);

            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag entry = listTag.getCompound(i);
                Optional<CardPos> var10000 = CardPos.ADAPTER.readNbt(entry.get("pos"));
                List<CardPos> var10001 = this.affectedSlots;
                Objects.requireNonNull(var10001);
                var10000.ifPresent(var10001::add);
            }
        }

        this.slotRoll = Adapters.INT.readNbt(nbt.get("slotRoll")).orElse(((SlotDeckModifier.Config)this.getConfig()).slotRoll.get(JavaRandom.ofNanoTime()));
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            if (!this.isPopulated()) {
                return json;
            } else {
                if (this.affectedSlots != null && !this.affectedSlots.isEmpty()) {
                    JsonArray jsonElement = new JsonArray();

                    for(CardPos cardPos : this.affectedSlots) {
                        Optional<JsonElement> var10000 = CardPos.ADAPTER.writeJson(cardPos);
                        Objects.requireNonNull(jsonElement);
                        if(var10000.isEmpty()) {
                            continue;
                        }
                        jsonElement.add(var10000.get());
                        var10000.ifPresent(jsonElement::add);
                    }

                    json.add("affectedSlots", jsonElement);
                }

                Adapters.INT.writeJson(this.slotRoll).ifPresent((tag) -> json.add("slotRoll", tag));
                return json;
            }
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.affectedSlots.clear();
        if (json.has("affectedSlots")) {
            JsonArray jsonElement = json.getAsJsonArray("affectedSlots");

            for(int i = 0; i < jsonElement.size(); ++i) {
                Optional<CardPos> var10000 = CardPos.ADAPTER.readJson(jsonElement.get(i).getAsJsonObject());
                List<CardPos> var10001 = this.affectedSlots;
                Objects.requireNonNull(var10001);
                var10000.ifPresent(var10001::add);
            }
        }

        this.slotRoll = Adapters.INT.readJson(json.get("slotRoll")).orElse(((SlotDeckModifier.Config)this.getConfig()).slotRoll.get(JavaRandom.ofNanoTime()));
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.affectedSlots.size(), buffer);

        for(CardPos pos : this.affectedSlots) {
            CardPos.ADAPTER.writeBits(pos, buffer);
        }

        Adapters.INT_SEGMENTED_7.writeBits(this.slotRoll, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.affectedSlots.clear();
        int size = Adapters.INT_SEGMENTED_7.readBits(buffer).orElse(0);

        for(int i = 0; i < size; ++i) {
            CardPos.ADAPTER.readBits(buffer).ifPresent((pos) -> this.affectedSlots.add(pos));
        }

        this.slotRoll = Adapters.INT_SEGMENTED_7.readBits(buffer).orElse(((SlotDeckModifier.Config)this.getConfig()).slotRoll.get(JavaRandom.ofNanoTime()));
    }


    public static class Config extends SlotDeckModifier.Config {
        private String groupName;

        public Config(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void writeBits(BitBuffer buffer) {
            super.writeBits(buffer);
            Adapters.UTF_8.writeBits("groupName", buffer);
        }

        @Override
        public void readBits(BitBuffer buffer) {
            super.readBits(buffer);
            this.groupName = Adapters.UTF_8.readBits(buffer).orElse("");
        }

        @Override
        public void readNbt(CompoundTag nbt) {
            super.readNbt(nbt);
            this.groupName = Adapters.UTF_8.readNbt(nbt.get("groupName")).orElse("");

        }

        @Override
        public void readJson(JsonObject json) {
            super.readJson(json);
            this.groupName = Adapters.UTF_8.readJson(json.get("groupName")).orElse("");
        }

        @Override
        public Optional<JsonObject> writeJson() {
            return super.writeJson().map((json) -> {
                json.add("groupName", Adapters.UTF_8.writeJson(groupName).orElseThrow());
                return json;
            });
        }

        @Override
        public Optional<CompoundTag> writeNbt() {
            return super.writeNbt().map((nbt) -> {
                nbt.put("groupName", Adapters.UTF_8.writeNbt(groupName).orElseThrow());
                return nbt;
            });
        }
    }
}
