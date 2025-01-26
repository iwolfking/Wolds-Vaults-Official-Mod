package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.common.item.Items;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.integration.scannable.scanning.*;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "scannable")
        }
)
@Mixin(value = Items.class, remap = false)
public class MixinScannableItems {
    @Shadow @Final private static DeferredRegister<Item> ITEMS;
    private static final RegistryObject<Item> VAULT_ORES_MODULE = ITEMS.register("vault_ores_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultOresBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE = ITEMS.register("vault_chests_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_LIVING = ITEMS.register("vault_chests_module_living", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsLivingBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_ORNATE = ITEMS.register("vault_chests_module_ornate", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsOrnateBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_GILDED = ITEMS.register("vault_chests_module_gilded", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsGildedBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_WOODEN = ITEMS.register("vault_chests_module_wooden", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsWoodenBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_RAW = ITEMS.register("vault_chests_module_raw", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsRawBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CHESTS_MODULE_TREASURE = ITEMS.register("vault_chests_module_treasure", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultChestsTreasureBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_OBJECTIVES_MODULE = ITEMS.register("vault_objectives_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultObjectivesBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_DOORS_MODULE = ITEMS.register("vault_doors_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultDoorsBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_DUNGEON_DOORS_MODULE = ITEMS.register("vault_dungeon_doors_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultDungeonDoorsBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_TREASURE_DOORS_MODULE = ITEMS.register("vault_treasure_doors_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultTreasureDoorsBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_VENDOORS_MODULE = ITEMS.register("vault_vendoors_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultVendoorsBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_PYLON_MODULE = ITEMS.register("vault_pylon_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultPylonScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_CAKE_MODULE = ITEMS.register("vault_cake_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultCakeBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_COINS_MODULE = ITEMS.register("vault_coins_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultCoinStacksBlockScannerModule.INSTANCE));
    private static final RegistryObject<Item> VAULT_SPIRIT_MODULE = ITEMS.register("vault_spirit_module", () -> ScannerModuleItemAccessor.createScannerModuleItem(VaultSpiritEntityScannerModule.INSTANCE));
}
