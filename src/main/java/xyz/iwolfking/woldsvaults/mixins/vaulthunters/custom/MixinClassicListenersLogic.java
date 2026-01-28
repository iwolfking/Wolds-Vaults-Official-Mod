package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VaultNecklaceItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @WrapOperation(method = {"printJoinMessage", "lambda$initServer$1"/*leave*/}, at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/player/ClassicListenersLogic;getVaultObjective(Ljava/lang/String;)Ljava/lang/String;"))
    private String improveRaidChatMessage(ClassicListenersLogic instance, String key, Operation<String> original, @Local(argsOnly = true) Vault vault){
        if (!"raid".equals(key)) {
            return original.call(instance, key);
        }
        Set<ResourceLocation> infRaidThemes =
            Optional.ofNullable(ModConfigs.VAULT_CRYSTAL.THEMES.get(VaultMod.id("infinite_raid")))
                .orElse(new LevelEntryList<>())
                .stream()
                .map(themeEntry -> themeEntry.pool.keySet())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Set<ResourceLocation> infBrutalRaidThemes =
            Optional.ofNullable(ModConfigs.VAULT_CRYSTAL.THEMES.get(VaultMod.id("infinite_raid_hard")))
            .orElse(new LevelEntryList<>())
            .stream()
            .map(themeEntry -> themeEntry.pool.keySet())
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        ResourceLocation themeReg = vault.get(Vault.WORLD).get(WorldManager.THEME);
        if (themeReg != null) {
            if (infRaidThemes.contains(themeReg)) {
                return "∞ Raid";
            }
            if (infBrutalRaidThemes.contains(themeReg)) {
                return "∞ Brutal Raid";
            }
        }

        return original.call(instance, key);
    }
}
