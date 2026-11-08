package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SlotBilling;

@Mixin(value = InstantManaAbility.class, remap = false)
public abstract class MixinInstantManaAbilityBilling {

    /** The mana check of a slot cast: the trigger checks the whole slot's cost, other steps check nothing. */
    @WrapOperation(method = "lambda$canDoAction$0", at = @At(value = "INVOKE",
            target = "Liskallia/vault/util/calc/ManaCostHelper;adjustManaCost(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/base/Skill;F)F"))
    private float woldsvaults$checkSlotMana(ServerPlayer player, Skill skill, float cost, Operation<Float> original) {
        return SlotBilling.check(player, skill, original.call(player, skill, cost));
    }

    /** The mana payment of a slot cast: the trigger pays the whole slot's cost, other steps pay nothing. */
    @WrapOperation(method = "lambda$doActionPost$1", at = @At(value = "INVOKE",
            target = "Liskallia/vault/util/calc/ManaCostHelper;adjustManaCostForPayment(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/base/Skill;F)F"))
    private float woldsvaults$paySlotMana(ServerPlayer player, Skill skill, float cost, Operation<Float> original) {
        return SlotBilling.pay(player, skill, original.call(player, skill, cost));
    }
}
