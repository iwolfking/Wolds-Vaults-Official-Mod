package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.modifiers.deck.EmptySlotDeckModifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mixin(value = CardDeck.class, remap = false)
public abstract class MixinCardDeck implements ICardDeckCache {

    @Unique private int wv$emptySlots = -1;
    @Unique private int wv$filledSlots = -1;
    @Unique private String wv$dominantGroup = null;
    @Unique private String wv$minorityGroup = null;

    @Unique
    private void wv$invalidateCaches() {
        wv$invalidateSlotCache();
        wv$invalidateGroupCache();
    }

    @Unique
    private void wv$invalidateSlotCache() {
        wv$emptySlots = -1;
        wv$filledSlots = -1;
    }

    @Unique
    private void wv$invalidateGroupCache() {
        wv$dominantGroup = null;
        wv$minorityGroup = null;
    }


    @Override
    public int wv$getEmptySlotCount() {
        if (wv$emptySlots < 0) {
            wv$recomputeSlotCache();
        }
        return wv$emptySlots;
    }

    @Override
    public int wv$getFilledSlotCount() {
        if (wv$filledSlots < 0) {
            wv$recomputeSlotCache();
        }
        return wv$filledSlots;
    }

    @Inject(method = "setCard", at = @At("TAIL"))
    private void wv$onSetCard(CardPos pos, Card card, CallbackInfo ci) {
        wv$invalidateCaches();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void wv$onReadNbt(CompoundTag nbt, CallbackInfo ci) {
        wv$invalidateCaches();
    }

    @Inject(method = "addText", at = @At("HEAD"))
    private void wv$onAddText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, CallbackInfo ci) {
        if (wv$emptySlots < 0) {
            wv$recomputeSlotCache();
        }
        if(wv$dominantGroup == null) {
            wv$recomputeGroupCache();
        }
    }

    @Inject(method = "getModifierValue", at = @At("HEAD"))
    private void wv$onGetModifierValue(Card card, CardPos pos, CallbackInfoReturnable<Float> cir) {
        if (wv$emptySlots < 0) {
            wv$recomputeSlotCache();
        }
        if(wv$dominantGroup == null) {
            wv$recomputeGroupCache();
        }
    }

    @Redirect(method = "getModifierValue", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/modifier/deck/DeckModifier;getModifierValue(Liskallia/vault/core/card/Card;Liskallia/vault/core/card/CardPos;Liskallia/vault/core/card/CardDeck;)F"))
    private float alwaysRecomputeEmptySlots(DeckModifier<?> instance, Card card, CardPos cardPos, CardDeck cardDeck) {
        if(instance instanceof EmptySlotDeckModifier emptySlotDeckModifier) {
           wv$recomputeSlotCache();
           return emptySlotDeckModifier.getModifierValue(card, cardPos, cardDeck);
        }

        return instance.getModifierValue(card, cardPos, cardDeck);
    }

    @Unique
    private void wv$recomputeCaches() {
        wv$recomputeSlotCache();
        wv$recomputeGroupCache();
    }

    @Unique
    private void wv$recomputeSlotCache() {
        CardDeck deck = (CardDeck) (Object) this;

        int total = deck.getCards().size();
        int filled = (int) deck.getCards().values()
                .stream()
                .filter(Objects::nonNull)
                .count();

        wv$filledSlots = filled;
        wv$emptySlots = total - filled;
    }

    @Unique
    private void wv$recomputeGroupCache() {
        CardDeck deck = (CardDeck) (Object) this;

        Map<String, Long> counts = deck.getCards().values().stream()
                .filter(Objects::nonNull)
                .flatMap(card -> card.getGroups().stream())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        if (counts.isEmpty()) {
            wv$dominantGroup = null;
            wv$minorityGroup = null;
        } else {
            wv$dominantGroup = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            wv$minorityGroup = counts.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }


}
