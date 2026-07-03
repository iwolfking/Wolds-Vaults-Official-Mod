package xyz.iwolfking.woldsvaults.modifiers.deck;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.IPopulateOnApplyModifier;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.random.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class ImplicitDeckModifier extends DeckModifier<DeckModifier.Config> implements IPopulateOnApplyModifier {
    
    private DeckModifier<?> wrappedModifier;

    public ImplicitDeckModifier(DeckModifier<?> wrappedModifier) {
        super(wrappedModifier.getConfig());
        this.wrappedModifier = wrappedModifier;
        this.syncProperties();
    }

    public ImplicitDeckModifier() {
        super(new DeckModifier.Config());
    }

    private void syncProperties() {
        if (this.wrappedModifier != null) {
            this.setId(this.wrappedModifier.getId());
            this.setName(this.wrappedModifier.getName());
            this.setColour(this.wrappedModifier.getColour());
            this.setModelId(this.wrappedModifier.getModelId());
        }
    }

    public boolean isImplicit() {
        return true;
    }

    @Override
    public boolean onPopulate(iskallia.vault.core.random.RandomSource random) {
        if (this.wrappedModifier != null) {
            boolean populated = this.wrappedModifier.onPopulate(random);
            this.setPopulated(this.wrappedModifier.isPopulated());
            return populated;
        }
        return super.onPopulate(random);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        DeckModifier.ADAPTER.writeBits(this.wrappedModifier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.wrappedModifier = DeckModifier.ADAPTER.readBits(buffer).orElseThrow();
        this.syncProperties();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            DeckModifier.ADAPTER.writeNbt(this.wrappedModifier)
                .ifPresent(wrappedNbt -> nbt.put("wrappedModifier", wrappedNbt));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        if (nbt.contains("wrappedModifier")) {
            this.wrappedModifier = DeckModifier.ADAPTER.readNbt(nbt.get("wrappedModifier")).orElse(null);
            this.syncProperties();
        }
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            DeckModifier.ADAPTER.writeJson(this.wrappedModifier)
                .ifPresent(wrappedJson -> json.add("wrappedModifier", wrappedJson));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        if (json.has("wrappedModifier")) {
            this.wrappedModifier = DeckModifier.ADAPTER.readJson(json.get("wrappedModifier")).orElse(null);
            this.syncProperties();
        }
    }

    @Override
    public float getModifierValue(Card card, CardPos pos, CardDeck deck) {
        return this.wrappedModifier != null ? this.wrappedModifier.getModifierValue(card, pos, deck) : 1.0F;
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        wrappedModifier.addText(tooltip, minIndex, flag, time);
    }

    @Override
    public float getModifierValue() {
        return this.wrappedModifier != null ? this.wrappedModifier.getModifierValue() : 1.0F;
    }

    @Override
    public boolean voidConfigIfPopulated() {
        return this.wrappedModifier != null && this.wrappedModifier.voidConfigIfPopulated();
    }

    @Override
    public boolean isBetter(DeckModifier<?> other) {
        return false;
    }

    @Override
    public void populateOnApply(CardDeck cardDeck, RandomSource randomSource) {
        if(this.wrappedModifier != null) {
            if(wrappedModifier instanceof IPopulateOnApplyModifier populateOnApplyModifier) {
                populateOnApplyModifier.populateOnApply(cardDeck, randomSource);
            }
        }
    }
}