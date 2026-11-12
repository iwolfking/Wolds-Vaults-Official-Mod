package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.event.GearAttributeEvents;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.chain.GlyphChains;
import xyz.iwolfking.woldsvaults.spellcraft.chain.SpellChainDamage;

@Mixin(value = GearAttributeEvents.class, remap = false)
public abstract class MixinGearAttributeEventsChainScope {
    /** A Chaining glyph link is pure damage, and a hit that starts a glyph chain does not also start VH's chain. */
    @Inject(method = "triggerChainAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeTriggerChainAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active() || GlyphChains.startsChain(event)) {
            ci.cancel();
        }
    }

    @Inject(method = "triggerAoEAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeTriggerAoEAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "triggerKnockbackAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeTriggerKnockbackAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "triggerStunAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeTriggerStunAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "triggerEffectCloudsActive", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeTriggerEffectCloudsActive(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "doFatalStrikeAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeDoFatalStrikeAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }
}
