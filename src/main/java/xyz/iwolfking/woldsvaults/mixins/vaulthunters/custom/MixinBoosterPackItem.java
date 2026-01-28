package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.item.BoosterPackItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.IPlayerOutcomeHandler;

import java.util.List;

@Mixin(value = BoosterPackItem.class, remap = false)
public class MixinBoosterPackItem {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Liskallia/vault/config/card/BoosterPackConfig;getOutcomes(Ljava/lang/String;Liskallia/vault/core/random/RandomSource;)Ljava/util/List;", remap = false), remap = true)
    private List<Card> passPlayerToOutcomes(BoosterPackConfig instance, String id, RandomSource random, @Local(argsOnly = true) Player player) {
        return ((IPlayerOutcomeHandler)instance).woldsVaults$getOutcomes(player, id, random);
    }
}
