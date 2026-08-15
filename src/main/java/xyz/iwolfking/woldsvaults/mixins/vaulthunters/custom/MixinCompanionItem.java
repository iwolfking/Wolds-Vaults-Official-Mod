package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CompanionItem;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.Milestones;
import xyz.iwolfking.woldsvaults.modifiers.vault.PlayerNoTemporalShardModifier;

import java.util.UUID;

@Mixin(value = CompanionItem.class, remap = false)
public class MixinCompanionItem {
    @Inject(method = "activateTemporalModifier", at = @At("HEAD"), cancellable = true)
    private static void preventActivation(ServerPlayer player, ItemStack stack, Vault vault, CallbackInfo ci) {
        if(vault.get(Vault.MODIFIERS).getModifiers().stream().anyMatch(vaultModifier -> vaultModifier instanceof PlayerNoTemporalShardModifier)) {
            player.displayClientMessage(new TextComponent("The Companion does not respond."), true);
            ci.cancel();
        }
    }

    /**
     * Feeds the "Pal Trainer" milestone. Injected at the end of the experience path rather than at
     * the one-shot ancient relic slot unlock so that companions that were already at max level
     * before the milestone shipped still register on their next experience grant; the milestone
     * stores companion UUIDs, so the repeat calls that this costs are free.
     */
    @Inject(method = "addCompanionXP", at = @At("TAIL"))
    private static void countMaxLevelCompanion(ItemStack stack, int xpToAdd, CallbackInfo ci) {
        if (CompanionItem.getCompanionLevel(stack) < ModConfigs.COMPANIONS.getMaxLevel()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        UUID owner = CompanionItem.getOwner(stack);
        if (server == null || owner == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player == null) {
            return;
        }
        Milestones.onCompanionReachedMaxLevel(player, CompanionItem.getCompanionUUID(stack));
    }
}
