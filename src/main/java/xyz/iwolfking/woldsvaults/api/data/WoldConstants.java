package xyz.iwolfking.woldsvaults.api.data;

import com.google.common.base.Suppliers;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.item.gear.IVaultUsesItem;
import iskallia.vault.item.gear.IdolItem;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.util.MiscUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ItemHelper;
import xyz.iwolfking.woldsvaults.api.util.VaultGearAttributeHelper;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.gear.VaultDaggerSubItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WoldConstants {
    public static int MAX_MAP_TIER = 5;
    public static final Supplier<List<Item>> ALL_VAULT_GEAR_ITEMS = Suppliers.memoize(() -> ItemHelper.getAllRegisteredItems(stack -> stack instanceof VaultGearItem && !(stack instanceof ToolItem || stack instanceof IVaultUsesItem || stack instanceof VaultDaggerSubItem || stack instanceof EtchingItem || stack instanceof IdolItem || stack instanceof JewelItem || stack instanceof VaultMapItem)));
    public static final List<Item> ALL_WOLD_MAINHANDS = List.of(ModItems.BATTLESTAFF, ModItems.SCEPTER, ModItems.DAGGER_MAIN, ModItems.RANG, ModItems.TRIDENT, ModItems.VAULT_ROD);
    public static final List<Item> ALL_WOLD_OFFHANDS = List.of(ModItems.PLUSHIE, ModItems.LOOT_SACK);
    public static final List<Item> ALL_WOLD_GEAR = Stream.concat(ALL_WOLD_MAINHANDS.stream(), ALL_WOLD_OFFHANDS.stream()).toList();
    public static final Supplier<List<VaultGearAttribute<?>>> ALL_COMBO_ATTRIBUTES = Suppliers.memoize(() -> VaultGearAttributeHelper.getAllRegisteredAttributes(
            attr -> true,
            ComboAttribute.Config.class
    ));
    public static final UUID SECOND_CHANCE_HEALTH_REDUCTION_UUID = UUID.fromString("377c9fce-a8bd-4a3a-8123-180b5d633a23");

    //Caps
    public static final float DODGE_PERCENT_CAP = 0.95F;

    //Constant Strings
    public static final String TRASH_SLOT_RESEARCH = "Trash Slot";
    public static final String DELOOT_RESEARCH = "Delooting Pro";
}
