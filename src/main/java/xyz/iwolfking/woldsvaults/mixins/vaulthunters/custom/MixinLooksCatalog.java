package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.data.ClientDiscoveredTrailsData;
import iskallia.vault.client.data.ClientSeenData;
import iskallia.vault.client.gui.screen.player.looks.LooksCatalog;
import iskallia.vault.config.customisation.CustomisationDiscovery;
import iskallia.vault.config.customisation.PatronAurasConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.looks.LooksCategory;
import iskallia.vault.looks.LooksKeys;
import iskallia.vault.www.patreon.PatreonTier;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LooksCatalog.class, remap = false)
public abstract class MixinLooksCatalog {

    @Shadow
    @Final
    private static List<PatreonTier> AURA_TIERS;

    @Shadow
    public static String auraName(PatreonTier tier) {
        throw new AssertionError();
    }

    /**
     * @author YourName
     * @reason Allow Patron Auras to use CustomisationDiscovery instead of requiring Patreon
     */
    @Overwrite
    private static List<LooksCatalog.Entry> auras() {
        List<LooksCatalog.Entry> entries = new ArrayList<>();

        for (PatreonTier tier : AURA_TIERS) {
            if (ModConfigs.PATRON_AURAS.getAura(tier) != null) {
                String id = PatronAurasConfig.tierId(tier);
                String key = LooksKeys.aura(id);

                CustomisationDiscovery discovery = resolveAuraDiscovery(id);

                boolean unlocked = isAuraUnlocked(id);

                entries.add(
                    new LooksCatalog.Entry(
                        LooksCategory.AURA,
                        id,
                        key,
                        auraName(tier),
                        tier.getColor().getValue(),
                        unlocked,
                        discovery,
                        tier.getDisplayPrefix(),
                        null,
                        null
                    )
                );
            }
        }

        return entries;
    }

    @Inject(method = "anyNew", at = @At("RETURN"), cancellable = true)
    private static void woldsVaults$checkNewAuras(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }

        for (PatreonTier tier : AURA_TIERS) {
            if (ModConfigs.PATRON_AURAS.getAura(tier) != null) {
                String id = PatronAurasConfig.tierId(tier);
                String key = LooksKeys.aura(id);
                if (isAuraUnlocked(id) && ClientSeenData.isNew(key)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private static CustomisationDiscovery resolveAuraDiscovery(String id) {
        return ModConfigs.CUSTOMISATION_DISCOVERY.getTrailDiscovery(id);
    }

    private static boolean isAuraUnlocked(String id) {
        return ClientDiscoveredTrailsData.hasDiscoveredTrail(id);
    }
}