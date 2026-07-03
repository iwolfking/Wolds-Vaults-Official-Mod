package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardPos;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.card.modifier.card.CardModifier;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.talent.RandomVaultModifierAttribute;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.deck.ArcaneSlotDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.NitwitDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.TemporalTimeDeckModifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(value = CardEntry.class, remap = false)
public abstract class MixinCardEntry {
    @Shadow
    private Set<String> groups;
    @Shadow
    private CardModifier<?> modifier;

    @Inject(method = "getSnapshotAttributes", at = @At(value = "INVOKE", target = "Liskallia/vault/core/card/modifier/card/CardModifier;getSnapshotAttributes(I)Ljava/util/List;"), cancellable = true)
    private void disableArcaneInNitwitDecks(int tier, CardPos pos, CardDeck deck, CallbackInfoReturnable<List<VaultGearAttributeInstance<?>>> cir) {
        if(!deck.getModifiersOfType(NitwitDeckModifier.class).isEmpty()) {
            if(this.groups.contains("Arcane")) {
                cir.setReturnValue(List.of());
            }
        }
        if(!deck.getModifiersOfType(TemporalTimeDeckModifier.class).isEmpty()) {
            Optional<VaultGearAttributeInstance<?>> attributeInstance = this.modifier.getSnapshotAttributes(0).stream().filter(vaultGearAttributeInstance -> vaultGearAttributeInstance.getValue() instanceof RandomVaultModifierAttribute).findFirst();
            if(attributeInstance.isPresent()) {
                if(attributeInstance.get().getValue() instanceof RandomVaultModifierAttribute attribute) {
                    cir.setReturnValue(List.of(VaultGearAttributeInstance.cast(ModGearAttributes.RANDOM_VAULT_MODIFIER, (new RandomVaultModifierAttribute(attribute.getModifier(), attribute.getCount(), attribute.getTime() * 2)))));
                }
            }
        }
        if(!deck.getModifiersOfType(ArcaneSlotDeckModifier.class).isEmpty()) {
            if(this.groups.contains("Arcane")) {
               ArcaneSlotDeckModifier modifier = deck.getModifiersOfType(ArcaneSlotDeckModifier.class).get(0);
               if(modifier.getAffectedSlots().contains(pos)) {
                   List<VaultGearAttributeInstance<?>> base = this.modifier.getSnapshotAttributes(tier);
                   for(VaultGearAttributeInstance<?> attributeInstance : base) {
                       if(attributeInstance.getAttribute().equals(ModGearAttributes.ABILITY_LEVEL)) {
                            VaultGearAttributeInstance<AbilityLevelAttribute> attributeVaultGearAttributeInstance = (VaultGearAttributeInstance<AbilityLevelAttribute>) attributeInstance;
                            attributeVaultGearAttributeInstance.setValue(new AbilityLevelAttribute(attributeVaultGearAttributeInstance.getValue().getAbility(), attributeVaultGearAttributeInstance.getValue().getLevelChange() + Math.round(modifier.getModifierValue())));
                            cir.setReturnValue(List.of(attributeVaultGearAttributeInstance));
                       }
                   }
               }
            }
        }
    }

    @Inject(method = "applyModifier", at = @At("HEAD"), cancellable = true)
    private void handleRandomVaultModifierScaling(Object value, float modifier, CallbackInfoReturnable<Object> cir) {
        if (value instanceof RandomVaultModifierAttribute attr) {
            cir.setReturnValue(new RandomVaultModifierAttribute(
                    attr.getModifier(),
                    attr.getCount(),
                    (int)(attr.getTime() * modifier)
            ));
        }
    }
}
