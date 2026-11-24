package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.customisation.PatronAurasConfig;
import iskallia.vault.world.data.DiscoveredTrailsData;
import iskallia.vault.world.data.PatronAuraData;
import iskallia.vault.www.patreon.PatreonManager;
import iskallia.vault.www.patreon.PatreonTier;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PatronAuraData.class, remap = false)
public abstract class MixinPatronAuraData {

    @WrapOperation(
        method = "refresh",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/www/patreon/PatreonManager;hasActiveTier(Ljava/util/UUID;Liskallia/vault/www/patreon/PatreonTier;)Z"
        )
    )
    private boolean woldsVaults$allowDiscoveredAura(
        PatreonManager manager,
        UUID playerId,
        PatreonTier tier,
        Operation<Boolean> original,
        ServerPlayer player
    ) {
        if (original.call(manager, playerId, tier)) {
            return true;
        }
        if (player != null && player.getServer() != null && tier != null) {
            String tierId = PatronAurasConfig.tierId(tier);
            return DiscoveredTrailsData.get(player.getServer()).hasDiscoveredTrail(playerId, tierId);
        }
        return false;
    }
}