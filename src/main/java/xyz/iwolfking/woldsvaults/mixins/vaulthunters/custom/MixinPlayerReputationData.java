package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.world.data.PlayerReputationData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.api.helper.VaultGodAffinityHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = PlayerReputationData.class, remap = false)
public class MixinPlayerReputationData {
    @Inject(method = "attemptFavour", at = @At("HEAD"), cancellable = true)
    private static void returnOldFavourHandling(Player player, VaultGod god, CallbackInfo ci) {
        if(GameruleHelper.isEnabled(ModGameRules.ENABLE_OLD_AFFINITY_HANDLING, player.getLevel())) {
            float chance = VaultGodAffinityHelper.getAffinityPercent(player, god);
            if (!(player.getRandom().nextFloat() + 0.5F <= chance)) {
                Component msg = new TextComponent("The god was not interested enough in you!").withStyle(god.getChatColor());
                player.displayClientMessage(msg, true);
                ci.cancel();
            }
        }
    }
}
