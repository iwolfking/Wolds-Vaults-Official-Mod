package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.customisation.PatronAurasConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.looks.PlayerLooks;
import iskallia.vault.world.data.DiscoveredTrailsData;
import iskallia.vault.world.data.PatronAuraData;
import iskallia.vault.www.patreon.PatreonTier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = PlayerLooks.class, remap = false)
public abstract class MixinPlayerLooks {

    /**
     * @author iwolfking
     * @reason Equips Patron Auras based on DiscoveredTrailsData discovery instead of Patreon active tier.
     */
    @Overwrite
    private static void wearAura(ServerPlayer player, @Nullable String tierId) {
        if (tierId == null) {
            PatronAuraData.get(player.getServer()).pick(player, null);
            return;
        }

        PatreonTier tier = PatronAurasConfig.tierFromId(tierId);
        if (tier != null && ModConfigs.PATRON_AURAS.getAura(tier) != null) {
            DiscoveredTrailsData trails = DiscoveredTrailsData.get(player.getServer());
            
            if (trails.hasDiscoveredTrail(player.getUUID(), tierId)) {
                PatronAuraData.get(player.getServer()).pick(player, tier);
            }
        }
    }
}