package xyz.iwolfking.woldsvaults.spellcraft.bar;

import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.AbilityCastTracker;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona.ManaCostHelperAccessor;

import javax.annotation.Nullable;

/**
 * Mana for slot casts of augmented spells. The trigger step checks and pays the whole slot: its own cost (after VH's
 * mana attributes) plus every other ability in the spell, times the spell's mana factor. A sneaking Power Dump pays the
 * whole bar instead. Every other step of the spell (chained abilities, payloads, Echo, Shotgun copies) is free. The
 * Ethereal talents roll once per slot cast: when they fire, the whole slot is free. A trigger VH does not bill per
 * cast (a toggle switching on, a mana-free instant) pays the other abilities' share here, once per activation.
 */
public final class SlotBilling {
    private static boolean lastPaymentEthereal;

    private SlotBilling() {
    }

    /** The cost a slot trigger is checked and dumped against: the slot cost for the trigger step, {@code cost} otherwise. */
    public static float slotCost(ServerPlayer player, Skill skill, float cost) {
        CastScope.Billing billing = CastScope.billing(player);
        if (billing == null || billing.mode() != CastScope.Mode.TRIGGER || !isTrigger(billing, skill)) {
            return cost;
        }
        return (cost + billing.otherAbilitiesMana()) * billing.manaFactor();
    }

    public static float check(ServerPlayer player, Skill skill, float cost) {
        CastScope.Billing billing = CastScope.billing(player);
        if (billing == null) {
            return cost;
        }
        if (billing.mode() == CastScope.Mode.FREE) {
            return 0.0F;
        }
        return slotCost(player, skill, cost);
    }

    /** Records whether VH's Ethereal roll inside a payment fired. */
    public static void noteEtherealRoll(boolean free) {
        lastPaymentEthereal = free;
    }

    public static float pay(ServerPlayer player, Skill skill, float paid) {
        CastScope.Billing billing = CastScope.billing(player);
        if (billing == null) {
            return paid;
        }
        if (billing.mode() == CastScope.Mode.FREE) {
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
    public static boolean canPayUnbilledTrigger(ServerPlayer player, Ability trigger, CastScope.Billing billing) {
        if (trigger instanceof InstantManaAbility || player.isCreative()) {
            return true;
        }
        return Mana.get(player) >= billing.otherAbilitiesMana() * billing.manaFactor();
    }

    /**
     * Pays the slot share of a trigger VH does not bill, after it went through: the other abilities' mana times the
     * spell's mana factor, nothing when the Ethereal talents roll.
     */
    public static void payUnbilledTrigger(ServerPlayer player, @Nullable Ability trigger, CastScope.Billing billing) {
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
        CastScope.Billing billing = CastScope.billing(player);
        return billing != null && billing.suppressCastEvent();
    }

    public static void noteSuppressedCast(Entity entity, Ability ability) {
        AbilityCastTracker.note(ability);
    }

    private static boolean isTrigger(CastScope.Billing billing, Skill skill) {
        Skill parent = skill.getParent();
        return billing.triggerSpec() != null && parent != null && billing.triggerSpec().equals(parent.getId());
    }
}
