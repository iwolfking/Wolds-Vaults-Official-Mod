package xyz.iwolfking.woldsvaults.api.data;

import com.google.common.base.Suppliers;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ItemHelper;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class WoldConstants {
    public static int MAX_MAP_TIER = 5;
    public static final Supplier<List<Item>> ALL_VAULT_GEAR_ITEMS = Suppliers.memoize(() -> ItemHelper.getAllRegisteredItems(stack -> stack instanceof VaultGearItem));
    public static final UUID SECOND_CHANCE_HEALTH_REDUCTION_UUID = UUID.fromString("377c9fce-a8bd-4a3a-8123-180b5d633a23");
}
