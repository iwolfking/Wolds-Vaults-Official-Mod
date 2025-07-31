package xyz.iwolfking.woldsvaults.mixins.ftbquests;

import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.helper.ItemStackHooks;

@Mixin(value = ItemReward.class, remap = false)
public class MixinItemReward {
    @Shadow public int count;

    @Redirect(method = "claim", at = @At(value = "INVOKE", target = "Ldev/architectury/hooks/item/ItemStackHooks;giveItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V"))
    private void modifyVaultGearLevelReward(ServerPlayer player, ItemStack stack) {
        if(stack.getItem() instanceof VaultGearItem) {
            VaultGearData data = VaultGearData.read(stack);
            PlayerVaultStats playerVaultStats = PlayerVaultStatsData.get(player.getLevel()).getVaultStats(player);
            data.setItemLevel(playerVaultStats.getVaultLevel());
            data.write(stack);
        }

        ItemStackHooks.giveItem(player, ItemStackHooks.copyWithCount(stack, count));

    }
}
