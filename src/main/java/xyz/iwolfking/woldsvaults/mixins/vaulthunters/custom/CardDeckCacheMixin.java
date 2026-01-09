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
import java.util.Objects;

@Mixin(value = CardDeck.class, remap = false)
public abstract class CardDeckCacheMixin implements ICardDeckCache {

    @Unique private int wv$emptySlots = -1;
    @Unique private int wv$filledSlots = -1;
    @Unique private int wv$totalSlots = -1;

    @Unique
    private void wv$invalidateSlotCache() {
        wv$emptySlots = -1;
        wv$filledSlots = -1;
        wv$totalSlots = -1;
    }

    @Override
    public int wv$getEmptySlotCount() {
        if (wv$emptySlots < 0) {
            wv$recomputeCache();
        }
        return wv$emptySlots;
    }

    @Override
    public int wv$getFilledSlotCount() {
        if (wv$filledSlots < 0) {
            wv$recomputeCache();
        }
        return wv$filledSlots;
    }

    @Inject(method = "setCard", at = @At("TAIL"))
    private void wv$onSetCard(CardPos pos, Card card, CallbackInfo ci) {
        wv$invalidateSlotCache();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void wv$onReadNbt(CompoundTag nbt, CallbackInfo ci) {
        wv$invalidateSlotCache();
    }

    @Inject(method = "addText", at = @At("HEAD"))
    private void wv$onAddText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, CallbackInfo ci) {
        if (wv$emptySlots < 0) {
            wv$recomputeCache();
        }
    }

    @Inject(method = "getModifierValue", at = @At("HEAD"))
    private void wv$onGetModifierValue(Card card, CardPos pos, CallbackInfoReturnable<Float> cir) {
        if (wv$emptySlots < 0) {
            wv$recomputeCache();
        }
    }

    @Redirect(method = "getModifierValue", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/modifier/deck/DeckModifier;getModifierValue(Liskallia/vault/core/card/Card;Liskallia/vault/core/card/CardPos;Liskallia/vault/core/card/CardDeck;)F"))
    private float alwaysRecomputeEmptySlots(DeckModifier<?> instance, Card card, CardPos cardPos, CardDeck cardDeck) {
        if(instance instanceof EmptySlotDeckModifier emptySlotDeckModifier) {
           wv$recomputeCache();
           return emptySlotDeckModifier.getModifierValue(card, cardPos, cardDeck);
        }

        return instance.getModifierValue(card, cardPos, cardDeck);
    }

    @Unique
    private void wv$recomputeCache() {
        CardDeck deck = (CardDeck) (Object) this;

        int total = deck.getCards().size();
        int filled = (int) deck.getCards().values()
                .stream()
                .filter(Objects::nonNull)
                .count();

        wv$totalSlots = total;
        wv$filledSlots = filled;
        wv$emptySlots = total - filled;
    }

}
