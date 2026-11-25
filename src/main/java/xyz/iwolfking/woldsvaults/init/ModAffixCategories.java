package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearModifier;
import xyz.iwolfking.vhapi.api.extensions.gear.AffixCategoryFactory;
import xyz.iwolfking.woldsvaults.api.gear.actions.CustomModifierCategoryTooltip;

public class ModAffixCategories {
    public static VaultGearModifier.AffixCategory UNUSUAL;
    public static VaultGearModifier.AffixCategory ETCHING;

    public static void init() {
        UNUSUAL = AffixCategoryFactory.INSTANCE.create(
                "UNUSUAL",
                "Unusual", CustomModifierCategoryTooltip::modifyUnusualTooltip,
                VaultMod.id("textures/item/gear/unusual_overlay.png")
        );

        ETCHING = AffixCategoryFactory.INSTANCE.create(
                "ETCHING",
                "Etching",
                CustomModifierCategoryTooltip::modifyUnusualTooltip,
                VaultMod.id("textures/item/gear/etching_overlay.png")
        );
    }
}
