package xyz.iwolfking.woldsvaults.integration.vaultfilters;

import iskallia.vault.gear.attribute.VaultGearModifier;
import net.joseph.vaultfilters.attributes.abstracts.AffixAttribute;
import xyz.iwolfking.woldsvaults.init.ModAffixCategories;

public class UnusualPrefixAttribute extends AffixAttribute {
    public UnusualPrefixAttribute(String value) {
        super(value);
    }

    public VaultGearModifier.AffixType getAffixType() {
        return VaultGearModifier.AffixType.PREFIX;
    }

    @Override
    public boolean shouldList(VaultGearModifier<?> modifier) {
        return modifier.hasCategory(ModAffixCategories.UNUSUAL);
    }

    @Override
    public boolean checkModifier(VaultGearModifier<?> modifier) {
        return modifier.hasCategory(ModAffixCategories.UNUSUAL) && this.value.equals(getName(modifier));
    }

    public String getTranslationKey() {
        return "unusual_prefix";
    }

    @Override
    public String getLegacyKey() {
        return "unusualPrefix";
    }
}
