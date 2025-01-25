package xyz.iwolfking.woldsvaults.integration.jewelsorting;

import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;

public class SortableVaultItems {

    /**
     * Add VaultGearItems to the Jewel Sorting mod's gear set.
     * Only for items that require no special handling.
     * It sorts by name, rarity, state, level, transmog and doesn't consider gear attributes.
     */

    @SuppressWarnings("unchecked")
    public static void addGear(VaultGearItem... items) {
        try { // avoid comptime dependency - bonne has disabled 3rd party sharing
            Class<?> sortingHelper = Class.forName("lv.id.bonne.vaulthunters.jewelsorting.utils.SortingHelper");
            VarHandle gearSetHandle = MethodHandles.publicLookup().findStaticVarHandle(sortingHelper, "VAULT_GEAR_SET", Set.class);
            Set<ResourceLocation> gearSet = (Set<ResourceLocation>) gearSetHandle.get();
            if (gearSet == null) {
                return;
            }
            for (VaultGearItem item : items) {
                gearSet.add(item.getItem().getRegistryName());
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
            // jewel sorting not present or the class was changed - gear won't be sorted
        }
    }
}
