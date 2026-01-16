package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VaultNecklaceItem;
import iskallia.vault.item.gear.VoidStoneItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = ClassicListenersLogic.class, remap = false)
public class MixinClassicListenersLogic {
    @Redirect(method = "lambda$onJoin$11", at = @At(value = "INVOKE", target = "Liskallia/vault/item/gear/VaultUsesHelper;addUsedVault(Lnet/minecraft/world/item/ItemStack;Ljava/util/UUID;)V"))
    private void trinketerEffectsNecklaces(ItemStack stack, UUID vaultId, @Local(argsOnly = true) ServerPlayer player) {
        double damageAvoidanceChance = PlayerExpertisesData.get(player.getLevel())
                .getExpertises(player)
                .getAll(TrinketerExpertise.class, Skill::isUnlocked)
                .stream()
                .mapToDouble(TrinketerExpertise::getDamageAvoidanceChance)
                .sum();
        if (player.level.random.nextDouble() < damageAvoidanceChance) {
            if(stack.getItem() instanceof VaultNecklaceItem vaultNecklaceItem) {
                vaultNecklaceItem.addFreeUsedVault(stack, vaultId);
            }
        } else {
            if(stack.getItem() instanceof VaultNecklaceItem vaultNecklaceItem) {
                vaultNecklaceItem.addUsedVault(stack, vaultId);
            }
        }
    }

    @Redirect(method = "lambda$onJoin$12", at = @At(value = "INVOKE", target = "Liskallia/vault/item/gear/VaultUsesHelper;addUsedVault(Lnet/minecraft/world/item/ItemStack;Ljava/util/UUID;)V"))
    private void trinketerEffectsGodCharms(ItemStack stack, UUID vaultId, @Local(argsOnly = true) ServerPlayer player) {
        double damageAvoidanceChance = PlayerExpertisesData.get(player.getLevel())
                .getExpertises(player)
                .getAll(TrinketerExpertise.class, Skill::isUnlocked)
                .stream()
                .mapToDouble(TrinketerExpertise::getDamageAvoidanceChance)
                .sum();
        if (player.level.random.nextDouble() < damageAvoidanceChance) {
            if(stack.getItem() instanceof VaultCharmItem vaultCharmItem) {
                vaultCharmItem.addFreeUsedVault(stack, vaultId);
            }
        } else {
            if(stack.getItem() instanceof VaultCharmItem vaultCharmItem) {
                vaultCharmItem.addUsedVault(stack, vaultId);
            }
        }
    }

    @Inject(
            method = {"getVaultObjective"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void addCustomObjectiveNames(String key, CallbackInfoReturnable<String> cir) {
        if(key.equals("scaling_ballistic_bingo")) {
            cir.setReturnValue("Ballistic Bingo");
        }
    }
}
