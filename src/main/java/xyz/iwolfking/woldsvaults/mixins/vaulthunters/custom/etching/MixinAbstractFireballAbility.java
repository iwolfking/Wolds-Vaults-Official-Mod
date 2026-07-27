package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.BouncingFireballAbility;
import iskallia.vault.skill.ability.effect.FireballAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractFireballAbility;
import iskallia.vault.util.calc.CopiousHelper;
import iskallia.vault.util.calc.ItemQuantityHelper;
import iskallia.vault.util.calc.ItemRarityHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.WoldEtchingHelper;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;

@Mixin(value = AbstractFireballAbility.class, remap = false)
public abstract class MixinAbstractFireballAbility {

    @Inject(method = "getAbilityPower", at = @At("TAIL"), cancellable = true)
    private void greedballEtching(ServerPlayer player, CallbackInfoReturnable<Float> cir) {
        if(FireballAbility.class.isAssignableFrom(this.getClass()) && WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.FIREBALL_GREEDBALL)) {
            VaultGearAttributeInstance<Float> fireballGreedBall = EtchingHelper.getEtchings(player, ModEtchingGearAttributes.FIREBALL_GREEDBALL).stream().findFirst().orElse(null);
            float bonusDamage = 0.0F;
            if(fireballGreedBall != null) {
                bonusDamage += (ItemQuantityHelper.getItemQuantity(player) * 100) * fireballGreedBall.getValue();
                bonusDamage += (ItemRarityHelper.getItemRarity(player) * 100) * fireballGreedBall.getValue();
                bonusDamage += (CopiousHelper.getCopiousChance(player) * 100) * fireballGreedBall.getValue();
            }

            cir.setReturnValue((cir.getReturnValue()) + bonusDamage);
        }

        if(BouncingFireballAbility.class.isAssignableFrom(this.getClass()) && WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.FIREBALL_VOLLEY_MITOSIS)) {
            cir.setReturnValue(cir.getReturnValue() * 0.5F);
        }
    }
}
