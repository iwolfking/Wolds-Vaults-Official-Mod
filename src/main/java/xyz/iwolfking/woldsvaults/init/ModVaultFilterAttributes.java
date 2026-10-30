package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.integration.vaultfilters.*;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.etched_layout.*;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlRewardsAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlSizeAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlWavesAttribute;

public class ModVaultFilterAttributes {
    public static void initAttributes() {
        new HasUnusualAffixAttribute(true).register(HasUnusualAffixAttribute::new);
        new UnusualPrefixAttribute("Attack Damage").register(UnusualPrefixAttribute::new);
        new UnusualSuffixAttribute("Attack Damage").register(UnusualSuffixAttribute::new);
        new EtchedLayoutTypeAttribute("Infinite").register(EtchedLayoutTypeAttribute::new);
        new EtchedLayoutTunnelAttribute(1).register(EtchedLayoutTunnelAttribute::new);
        new EtchedLayoutValueAttribute(1).register(EtchedLayoutValueAttribute::new);
        new EtchedLayoutCircleAttribute(1).register(EtchedLayoutCircleAttribute::new);
        new EtchedLayoutPolygonAttribute("").register(EtchedLayoutPolygonAttribute::new);
        new EtchedLayoutRingsAttribute(1).register(EtchedLayoutRingsAttribute::new);
        new EtchedLayoutRingsIntervalAttribute(1).register(EtchedLayoutRingsIntervalAttribute::new);
        new EtchedLayoutSpiralAttribute(1).register(EtchedLayoutSpiralAttribute::new);
        new EtchedLayoutTunnelHeightAttribute(1).register(EtchedLayoutTunnelHeightAttribute::new);
        new EtchedLayoutTunnelIntervalAttribute(1).register(EtchedLayoutTunnelIntervalAttribute::new);
        new EtchedLayoutTunnelWidthAttribute(1).register(EtchedLayoutTunnelWidthAttribute::new);
        new EtchedLayoutWaveAmplitudeAttribute(1).register(EtchedLayoutWaveAmplitudeAttribute::new);
        new EtchedLayoutWaveLengthAttribute(1).register(EtchedLayoutWaveLengthAttribute::new);
        new EtchedLayoutWaveFrequencyAttribute(1.0).register(EtchedLayoutWaveFrequencyAttribute::new);
        new AntiqueAttribute("Acquired taste").register(AntiqueAttribute::new);
        new GatePearlSizeAttribute("").register(GatePearlSizeAttribute::new);
        new GatePearlRewardsAttribute("").register(GatePearlRewardsAttribute::new);
        new GatePearlWavesAttribute(1).register(GatePearlWavesAttribute::new);
        new TargetedModBoxResearchAttribute("Create").register(TargetedModBoxResearchAttribute::new);
        new HasDivineAttribute(true).register(HasDivineAttribute::new);
        new MapTierAttribute(0).register(MapTierAttribute::new);
        new AlchemyItemAttribute(true).register(AlchemyItemAttribute::new);
        new CatalystItemAttribute(true).register(CatalystItemAttribute::new);
        new AlchemyIngredientTypeAttribute("Negative").register(AlchemyIngredientTypeAttribute::new);
        new VaultDollCompletedAttribute(true).register(VaultDollCompletedAttribute::new);
        //new CardDeckModifierAttribute("").register(CardDeckModifierAttribute::new);
    }
}
