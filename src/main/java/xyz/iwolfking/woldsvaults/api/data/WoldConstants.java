package xyz.iwolfking.woldsvaults.api.data;

import com.google.common.base.Suppliers;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.world.item.Item;
import xyz.iwolfking.woldsvaults.api.util.ItemHelper;
import java.util.List;
import java.util.function.Supplier;

public class WoldConstants {
    public static int MAX_MAP_TIER = 5;
    public static final Supplier<List<Item>> ALL_VAULT_GEAR_ITEMS = Suppliers.memoize(() -> ItemHelper.getAllRegisteredItems(stack -> stack instanceof VaultGearItem));
}
