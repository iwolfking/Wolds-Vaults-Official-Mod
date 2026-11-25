package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.config.gear.VaultGearTierConfig.ModifierAffixTagGroup;
import iskallia.vault.gear.attribute.VaultGearModifier.AffixType;
import xyz.iwolfking.vhapi.api.extensions.general.ModifierAffixTagGroupFactory;

public class ModModifierAffixTagGroups {
    public static final ModifierAffixTagGroup UNUSUAL_PREFIX =
            ModifierAffixTagGroupFactory.INSTANCE.createForAffixType("UNUSUAL_PREFIX", AffixType.PREFIX);

    public static final ModifierAffixTagGroup UNUSUAL_SUFFIX =
            ModifierAffixTagGroupFactory.INSTANCE.createForAffixType("UNUSUAL_SUFFIX", AffixType.SUFFIX);

    public static final ModifierAffixTagGroup TRAIT =
            ModifierAffixTagGroupFactory.INSTANCE.createForAffixType("TRAIT", AffixType.IMPLICIT);

    public static void init() {
    }
}