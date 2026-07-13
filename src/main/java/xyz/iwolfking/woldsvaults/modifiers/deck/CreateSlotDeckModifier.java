package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CardDeckAccessor;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IRemovableSlotModifier;
import java.util.*;

public class CreateSlotDeckModifier extends DeckModifier<CreateSlotDeckModifier.Config> implements IPopulateOnApplyModifier, ISlotModifier, IRemovableSlotModifier {
    private List<CardPos> affectedSlots = new ArrayList<>();
    private int slotRoll;

    private static final int MAX_WIDTH = 8;
    private static final int MAX_HEIGHT = 5;

    public CreateSlotDeckModifier(Config config) {
        super(config);
        this.affectedSlots = new ArrayList<>();
    }

    public CreateSlotDeckModifier() {
        super(new Config(""));
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
        Map<CardPos, Card> internalCardsMap = ((CardDeckAccessor) deck).getCardsMap();
        if (internalCardsMap == null || internalCardsMap.isEmpty()) return;

        int baseMinX = Integer.MAX_VALUE;
        int baseMaxX = Integer.MIN_VALUE;
        int baseMinY = Integer.MAX_VALUE;
        int baseMaxY = Integer.MIN_VALUE;

        for (CardPos pos : internalCardsMap.keySet()) {
            baseMinX = Math.min(baseMinX, pos.x);
            baseMaxX = Math.max(baseMaxX, pos.x);
            baseMinY = Math.min(baseMinY, pos.y);
            baseMaxY = Math.max(baseMaxY, pos.y);
        }

        for (int i = 0; i < slotRoll; ++i) {
            List<CardPos> validCandidates = findValidAdjacentPositions(internalCardsMap.keySet(), baseMinX, baseMaxX, baseMinY, baseMaxY);
            if (validCandidates.isEmpty()) {
                break;
            }

            CardPos templatePos = validCandidates.get(rand.nextInt(validCandidates.size()));

            CardPos chosenPos = new CardPos(templatePos.x, templatePos.y);
            chosenPos.allowedGroups.add(config.groupName);

            internalCardsMap.put(chosenPos, null);
            this.getAffectedSlots().add(chosenPos);
        }
    }

    private List<CardPos> findValidAdjacentPositions(Set<CardPos> existingSlots, int baseMinX, int baseMaxX, int baseMinY, int baseMaxY) {
        Set<CardPos> candidates = new HashSet<>();
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (CardPos pos : existingSlots) {
            for (int i = 0; i < 4; i++) {
                CardPos neighbor = new CardPos(pos.x + dx[i], pos.y + dy[i]);
                if (!existingSlots.contains(neighbor)) {
                    candidates.add(neighbor);
                }
            }
        }

        List<CardPos> validCandidates = new ArrayList<>();
        for (CardPos candidate : candidates) {
            if (isValidSlot(candidate)) {
                validCandidates.add(candidate);
            }
        }

        return validCandidates;
    }

    private boolean isValidSlot(CardPos candidate) {
        return candidate.x >= 0 && candidate.x <= MAX_WIDTH && candidate.y >= 0 && candidate.y <= MAX_HEIGHT;
    }

    @Override
    public void onRemove(CardDeck deck) {
        Map<CardPos, Card> internalCardsMap = ((CardDeckAccessor)deck).getCardsMap();;
        if (internalCardsMap != null) {
            for (CardPos affected : this.affectedSlots) {
                internalCardsMap.remove(affected);
            }
        }
        this.affectedSlots.clear();
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        MutableComponent comp = (new TextComponent("+")).append((new TextComponent(slotRoll + " additional ").append(new TextComponent(config.groupName.isEmpty() ? "" : config.groupName).withStyle(ChatFormatting.GOLD))));

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

    //Prevent upgrading this core directly
    @Override
    public boolean isBetter(DeckModifier<?> other) {
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
                        if(var10000.isEmpty()) {
                            continue;
                        }
                        jsonElement.add(var10000.get());
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
            Adapters.UTF_8.writeBits(groupName, buffer);
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