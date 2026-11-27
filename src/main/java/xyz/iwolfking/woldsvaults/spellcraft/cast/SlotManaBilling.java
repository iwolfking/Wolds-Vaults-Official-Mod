package xyz.iwolfking.woldsvaults.spellcraft.cast;

import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona.ManaCostHelperAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityCastTracker;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastBilling;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;

import javax.annotation.Nullable;

/**
 * Mana for slot casts of spells. The trigger step checks and pays the whole slot: its own cost (after VH's
 * mana attributes) plus every other ability in the spell, times the spell's mana factor, or the whole mana bar for a
 * Power Dump. Every other step is free, the Ethereal talents roll once per slot cast and free the whole slot when they
 * fire, and a trigger VH does not bill per cast (a toggle switching on) pays the other abilities' share here once per
 * activation.
 */
public final class SlotManaBilling {
    private static boolean lastPaymentEthereal;

    private SlotManaBilling() {
    }

    /**
     * The cost a slot trigger is checked and dumped against: the slot cost for the trigger step, {@code cost}
     * otherwise.
     */
    public static float slotCost(ServerPlayer player, Skill skill, float cost) {
        CastBilling billing = CastScope.billing(player);
        if (billing == null || !billing.isTrigger() || !isTrigger(billing, skill)) {
            return cost;
        }
        return (cost + billing.otherAbilitiesMana()) * billing.manaFactor();
    }

    public static float check(ServerPlayer player, Skill skill, float cost) {
        CastBilling billing = CastScope.billing(player);
        if (billing == null) {
            return cost;
        }
        if (!billing.isTrigger()) {
            return 0.0F;
        }
        return slotCost(player, skill, cost);
    }

    public static void noteEtherealRoll(boolean free) {
        lastPaymentEthereal = free;
    }

    public static float pay(ServerPlayer player, Skill skill, float paid) {
        CastBilling billing = CastScope.billing(player);
        if (billing == null) {
            return paid;
        }
        if (!billing.isTrigger()) {
            return 0.0F;
        }
        if (!isTrigger(billing, skill)) {
            return paid;
        }
        CastTag tag = CastContext.current();
        if (tag != null && tag.isCastOf(player, skill) && tag.dumpsBar()) {
            return Math.max(paid, Mana.get(player));
        }
        if (lastPaymentEthereal) {
            return 0.0F;
        }
        return (Math.max(0.0F, paid) + billing.otherAbilitiesMana()) * billing.manaFactor();
    }

    /** Whether the player can pay the slot share of a trigger VH does not bill; always true for triggers VH bills. */
    static boolean canPayUnbilledTrigger(ServerPlayer player, Ability trigger, CastBilling billing) {
        if (trigger instanceof InstantManaAbility || player.isCreative()) {
            return true;
        }
        return Mana.get(player) >= billing.otherAbilitiesMana() * billing.manaFactor();
    }

    /**
     * Pays the slot share of a trigger VH does not bill, after it went through: the other abilities' mana times the
     * spell's mana factor, nothing when the Ethereal talents roll.
     */
    static void payUnbilledTrigger(ServerPlayer player, @Nullable Ability trigger, CastBilling billing) {
        if (trigger == null || trigger instanceof InstantManaAbility || player.isCreative()) {
            return;
        }
        float due = billing.otherAbilitiesMana() * billing.manaFactor();
        if (due <= 0.0F || ManaCostHelperAccessor.woldsvaults$rollsEthereal(player)) {
            return;
        }
        Mana.decrease(player, ManaAction.PLAYER_ACTION, due);
    }

    public static boolean suppressesCastEvent(Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        CastBilling billing = CastScope.billing(player);
        return billing != null && billing.suppressCastEvent();
    }

    public static void noteSuppressedCast(Ability ability) {
        AbilityCastTracker.note(ability);
    }

    private static boolean isTrigger(CastBilling billing, Skill skill) {
        Skill parent = skill.getParent();
        return billing.triggerSpec() != null && parent != null && billing.triggerSpec().equals(parent.getId());
    }
}
