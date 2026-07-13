package xyz.iwolfking.woldsvaults.integration.arsnouveau;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeTypes;

public class ArsAPIRegistration {
    public static void register() {
        ArsNouveauAPI.getInstance().getEnchantingRecipeTypes().add(ArsRecipeTypes.VAULT_GEAR_APPARATUS_TYPE);
        ArsNouveauAPI.getInstance().getEnchantingRecipeTypes().add(ArsRecipeTypes.CATALYST_APPARATUS_TYPE);
    }
}
