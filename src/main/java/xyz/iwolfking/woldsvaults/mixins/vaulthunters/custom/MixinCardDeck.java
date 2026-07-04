package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.card.*;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.data.serializable.ISerializable;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.modifiers.deck.EmptySlotDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.ImplicitDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IMultiplicativeDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.lib.IRemovableSlotModifier;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(value = CardDeck.class, remap = false)
public abstract class MixinCardDeck implements ICardDeckCache, ISerializable<CompoundTag, JsonObject> {

    @Shadow
    @Final
    private List<DeckModifier<?>> modifiers;

    @Shadow
    public abstract List<VaultGearAttributeInstance<?>> getSnapshotAttributes();

    @Shadow
    @Final
    private Map<CardPos, Card> cards;
    @Shadow
    private int socketCount;
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
        if (wv$dominantGroup == null) {
            wv$recomputeGroupCache();
        }
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

    @Inject(method = "removeModifiersMatching", at = @At("HEAD"))
    private void callRemovalMethod(Predicate<DeckModifier<?>> predicate, CallbackInfo ci) {
        this.modifiers.forEach(deckModifier -> {
            if(predicate.test(deckModifier)) {
                if(deckModifier instanceof IRemovableSlotModifier removableSlotModifier) {
                    removableSlotModifier.onRemove((CardDeck)(Object)this);
                }
            }
        });
    }


    /**
     * @author iwolfking
     * @reason Make Deck Modifiers additive instead of multiplicative
     */
    @Overwrite
    public float getModifierValue(Card card, CardPos pos) {
        if (wv$emptySlots < 0) {
            wv$recomputeSlotCache();
        }
        if(wv$dominantGroup == null) {
            wv$recomputeGroupCache();
        }

        float value = 1.0F;
        float mult = 1.0F;

        for (DeckModifier<?> modifier : this.modifiers) {
            DeckModifier<?> modToProcess = modifier instanceof ImplicitDeckModifier implicitDeckModifier ? implicitDeckModifier.getModifier() : modifier;
            if(modToProcess instanceof EmptySlotDeckModifier emptySlotDeckModifier) {
                wv$recomputeSlotCache();
                value += (emptySlotDeckModifier.getModifierValue(card, pos, (CardDeck)(Object)this) - 1.0F);
            }
            else if(modToProcess instanceof IMultiplicativeDeckModifier multiplicativeDeckModifier) {
                mult *= multiplicativeDeckModifier.getMultiplierValue(card, pos, (CardDeck)(Object)this);
            }
            else {
                float modValue = modToProcess.getModifierValue(card, pos, (CardDeck)(Object)this);
                value += (modValue - 1.0F);
            }
        }

        return value * mult;
    }

    @WrapOperation(method = "addText", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int modifyImplicitModifierSocketCount(List<DeckModifier<?>> instance, Operation<Integer> original) {
        if(instance.stream().anyMatch(deckModifier -> deckModifier instanceof ImplicitDeckModifier)) {
            return original.call(instance) - 1;
        }

        return original.call(instance);
    }
}
