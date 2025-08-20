package xyz.iwolfking.woldsvaults.init;

import xyz.iwolfking.woldsvaults.integration.vaultfilters.*;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.companion.*;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.companion.items.CompanionRelicAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.companion.items.CompanionTrailAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.deck.CardDeckHasModifierAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.deck.CardDeckIsEmptyAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.deck.CardDeckTypeAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlRewardsAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlSizeAttribute;
import xyz.iwolfking.woldsvaults.integration.vaultfilters.gte.GatePearlWavesAttribute;

public class ModVaultFilterAttributes {
    public static void initAttributes() {
        new HasUnusualAffixAttribute(true).register(HasUnusualAffixAttribute::new);
        new UnusualPrefixAttribute("Attack Damage").register(UnusualPrefixAttribute::new);
        new UnusualSuffixAttribute("Attack Damage").register(UnusualSuffixAttribute::new);
        new OfferingItemAttribute("").register(OfferingItemAttribute::new);
        new OfferingModifierAttribute("").register(OfferingModifierAttribute::new);
        new IsRottenOfferingAttribute(true).register(IsRottenOfferingAttribute::new);
        new EtchedLayoutTypeAttribute("Infinite").register(EtchedLayoutTypeAttribute::new);
        new EtchedLayoutTunnelAttribute(1).register(EtchedLayoutTunnelAttribute::new);
        new EtchedLayoutValueAttribute(1).register(EtchedLayoutValueAttribute::new);
        new AntiqueAttribute("Acquired taste").register(AntiqueAttribute::new);
        new GatePearlSizeAttribute("").register(GatePearlSizeAttribute::new);
        new GatePearlRewardsAttribute("").register(GatePearlRewardsAttribute::new);
        new GatePearlWavesAttribute(1).register(GatePearlWavesAttribute::new);
        new TargetedModBoxResearchAttribute("Create").register(TargetedModBoxResearchAttribute::new);
        new HasDivineAttribute(true).register(HasDivineAttribute::new);
        new MapTierAttribute(0).register(MapTierAttribute::new);
        new CompanionRelicAttribute("").register(CompanionRelicAttribute::new);
        new CompanionTrailAttribute("").register(CompanionTrailAttribute::new);
        new CompanionCooldownAttribute(true).register(CompanionCooldownAttribute::new);
        new CompanionCooldownAttribute(true).register(CompanionCooldownAttribute::new);
        new CompanionHeartAttribute(1).register(CompanionHeartAttribute::new);
        new CompanionLevelAttribute(1).register(CompanionLevelAttribute::new);
        new CompanionMaxHeartAttribute(1).register(CompanionMaxHeartAttribute::new);
        new CompanionSeriesAttribute("").register(CompanionSeriesAttribute::new);
        new CompanionSkinAttribute("").register(CompanionSkinAttribute::new);
        new CompanionTemporalAttribute("").register(CompanionTemporalAttribute::new);
        new CardDeckHasModifierAttribute(true).register(CardDeckHasModifierAttribute::new);
        //new CardDeckIsEmptyAttribute(true).register(CardDeckIsEmptyAttribute::new);
        new CardDeckTypeAttribute("").register(CardDeckTypeAttribute::new);
    }
}
