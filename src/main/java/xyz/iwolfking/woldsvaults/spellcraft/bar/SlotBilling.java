package xyz.iwolfking.woldsvaults.spellcraft.bar;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.augments.AbilityCastTracker;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;

/**
 * Mana for slot casts of augmented spells. The trigger step checks and pays the whole slot: its own cost (after VH's
 * mana attributes and Power Dump) plus every other ability in the spell, times the spell's mana factor. Every other
 * step of the spell (chained abilities, payloads, Echo, Shotgun copies) is free. An Ethereal talent roll that makes the
 * trigger free makes the whole slot free.
 */
public final class SlotBilling {
    private SlotBilling() {
    }

    public static float check(ServerPlayer player, Skill skill, float cost) {
        CastScope.Billing billing = CastScope.billing(player);
        if (billing == null) {
            return cost;
        }
        if (billing.mode() == CastScope.Mode.FREE) {
            return 0.0F;
        }
        return isTrigger(billing, skill) ? (cost + billing.otherAbilitiesMana()) * billing.manaFactor() : cost;
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
        return paid <= 0.0F ? 0.0F : (paid + billing.otherAbilitiesMana()) * billing.manaFactor();
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
