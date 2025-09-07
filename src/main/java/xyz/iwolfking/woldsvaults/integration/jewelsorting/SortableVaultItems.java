package xyz.iwolfking.woldsvaults.integration.jewelsorting;

import iskallia.vault.gear.item.VaultGearItem;
import lv.id.bonne.vaulthunters.jewelsorting.utils.SortingHelper;

public class SortableVaultItems {

    /**
     * Add VaultGearItems to the Jewel Sorting mod's gear set.
     * Only for items that require no special handling.
     * It sorts by name, rarity, state, level, transmog and doesn't consider gear attributes.
     */
    public static void addGear(VaultGearItem... items) {
        for (VaultGearItem item : items) {
            SortingHelper.VAULT_GEAR_SET.add(item.getItem().getRegistryName());
        }
    }
}
