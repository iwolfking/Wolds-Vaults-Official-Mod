package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VoidStoneItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = ClassicListenersLogic.class, remap = false)
public class MixinClassicListenersLogic {
    @Redirect(method = "lambda$onJoin$11", at = @At(value = "INVOKE", target = "Liskallia/vault/item/gear/VoidStoneItem;addUsedVault(Lnet/minecraft/world/item/ItemStack;Ljava/util/UUID;)V"))
    private void trinketerEffectsVoidStones(ItemStack stack, UUID vaultId, @Local(argsOnly = true) ServerPlayer player) {
        double damageAvoidanceChance = PlayerExpertisesData.get(player.getLevel())
                .getExpertises(player)
                .getAll(TrinketerExpertise.class, Skill::isUnlocked)
                .stream()
                .mapToDouble(TrinketerExpertise::getDamageAvoidanceChance)
                .sum();
        if (player.level.random.nextDouble() < damageAvoidanceChance) {
            VoidStoneItem.addFreeUsedVault(stack, vaultId);
        } else {
            VoidStoneItem.addUsedVault(stack, vaultId);
        }
    }

    @Redirect(method = "lambda$onJoin$12", at = @At(value = "INVOKE", target = "Liskallia/vault/item/gear/VaultCharmItem;addUsedVault(Lnet/minecraft/world/item/ItemStack;Ljava/util/UUID;)V"))
    private void trinketerEffectsGodCharms(ItemStack stack, UUID vaultId, @Local(argsOnly = true) ServerPlayer player) {
        double damageAvoidanceChance = PlayerExpertisesData.get(player.getLevel())
                .getExpertises(player)
                .getAll(TrinketerExpertise.class, Skill::isUnlocked)
                .stream()
                .mapToDouble(TrinketerExpertise::getDamageAvoidanceChance)
                .sum();
        if (player.level.random.nextDouble() < damageAvoidanceChance) {
            VaultCharmItem.addFreeUsedVault(stack, vaultId);
        } else {
            VaultCharmItem.addUsedVault(stack, vaultId);
        }
    }
}
