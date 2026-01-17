package xyz.iwolfking.woldsvaults.datagen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.data.CopyBackpackDataFunction;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModBlockLootTables implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {

    Map<ResourceLocation, LootTable.Builder> map = new HashMap<>();
    private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(Blocks.DRAGON_EGG, Blocks.BEACON, Blocks.CONDUIT, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX).map(ItemLike::asItem).collect(ImmutableSet.toImmutableSet());
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));

    protected void addTables() {
        dropSelf(ModBlocks.CARBON_BLOCK);
        dropSelf(ModBlocks.PRISMATIC_FIBER_BLOCK);
        dropSelf(ModBlocks.CHROMATIC_GOLD_BLOCK);
        dropSelf(ModBlocks.SILVER_SCRAP_BLOCK);
        dropSelf(ModBlocks.VAULT_ESSENCE_BLOCK);
        dropSelf(ModBlocks.VAULT_INGOT_BLOCK);
        dropSelf(ModBlocks.OMEGA_POG_BLOCK);
        dropSelf(ModBlocks.POG_BLOCK);
        dropSelf(ModBlocks.ECHO_POG_BLOCK);
        dropSelf(ModBlocks.VAULT_PLATING_BLOCK);
        dropSelf(ModBlocks.INFUSED_DRIFTWOOD_PLANKS);
        dropSelf(ModBlocks.DECO_SCAVENGER_ALTAR_BLOCK);
        dropSelf(ModBlocks.DECO_OBELISK_BLOCK);
        dropSelf(ModBlocks.DECO_LODESTONE_BLOCK);
        dropSelf(ModBlocks.DECO_MONOLITH_BLOCK);
        dropSelf(ModBlocks.SURVIVAL_MOB_BARRIER);
        dropSelf(ModBlocks.VAULT_INFUSER_BLOCK);
        dropSelf(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK);
        dropSelf(ModBlocks.DOLL_DISMANTLING_BLOCK);
        dropSelf(ModBlocks.VAULT_SALVAGER_BLOCK);
        dropSelf(ModBlocks.CONFIGURABLE_FLOATING_TEXT_BLOCK);
        dropSelf(ModBlocks.CRATE_CRACKER_BLOCK);
        dropSelf(ModBlocks.TIME_TRIAL_TROPHY_BLOCK);
        dropOther(ModBlocks.NULLITE_ORE, ModItems.NULLITE_FRAGMENT);
        add(ModBlocks.GRAVEYARD_LOOT_BLOCK, noDrop());
        add(ModBlocks.GATEWAY_CHANNELING_BLOCK, noDrop());
        add(ModBlocks.FRACTURED_OBELISK, noDrop());
        add(ModBlocks.MONOLITH_CONTROLLER, noDrop());
        add(ModBlocks.DUNGEON_PEDESTAL_BLOCK, noDrop());
        add(ModBlocks.AUGMENT_CRAFTING_TABLE, noDrop());
        add(ModBlocks.MOD_BOX_WORKSTATION, noDrop());
        ModBlocks.CUSTOM_VAULT_CRATES.forEach((s, crateBlock) -> {
            add(crateBlock, noDrop());
        });
        add(ModBlocks.BREWING_ALTAR, noDrop());
        add(ModBlocks.SURVIVAL_OBJECTIVE_BLOCK, noDrop());
        add(ModBlocks.WEAVING_STATION, noDrop());
        add(ModBlocks.ETCHING_PEDESTAL, noDrop());
        add(ModBlocks.GOD_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.RARE_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.OMEGA_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.SPOOKY_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.CARD_VENDOR_PEDESTAL, noDrop());
        add(ModBlocks.XL_BACKPACK, LootTable.lootTable().withPool(LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(ModItems.XL_BACKPACK))).apply(new CopyBackpackDataFunction.Builder()));
        dropWhenSilkTouch(ModBlocks.ISKALLIAN_LEAVES_BLOCK);
        dropWhenSilkTouch(ModBlocks.HELLISH_SAND_BLOCK);
        dropWhenSilkTouch(ModBlocks.PUZZLE_CUBE_BLOCK);
        ModBlocks.COLORED_UNOBTANIUMS.forEach((dyeColor, block) -> {
            dropSelf(block);
        });
        dropSelf(ModBlocks.RAINBOW_UNOBTANIUM);

    }

    protected LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    protected void add(Block pBlock, Function<Block, LootTable.Builder> pFactory) {
        this.add(pBlock, pFactory.apply(pBlock));
    }

    protected void add(Block pBlock, LootTable.Builder pLootTableBuilder) {
        this.map.put(pBlock.getLootTable(), pLootTableBuilder);
    }

    public void dropSelf(Block pBlock) {
        this.dropOther(pBlock, pBlock);
    }

    public void dropOther(Block pBlock, ItemLike pDrop) {
        this.add(pBlock, createSingleItemTable(pDrop));
    }

    protected static LootTable.Builder createSilkTouchDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pAlternativeEntryBuilder) {
        return createSelfDropDispatchTable(pBlock, HAS_SILK_TOUCH, pAlternativeEntryBuilder);
    }

    protected static LootTable.Builder createSelfDropDispatchTable(Block pBlock, LootItemCondition.Builder pConditionBuilder, LootPoolEntryContainer.Builder<?> pAlternativeEntryBuilder) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pBlock).when(pConditionBuilder).otherwise(pAlternativeEntryBuilder)));
    }

    protected static LootTable.Builder createSilkTouchOnlyTable(ItemLike pItem) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(pItem)));
    }

    public void otherWhenSilkTouch(Block pBlock, Block pSilkTouchDrop) {
        this.add(pBlock, createSilkTouchOnlyTable(pSilkTouchDrop));
    }


    public void dropWhenSilkTouch(Block pBlock) {
        this.otherWhenSilkTouch(pBlock, pBlock);
    }

    protected static LootTable.Builder createSingleItemTable(ItemLike p_124127_) {
        return LootTable.lootTable().withPool(applyExplosionCondition(p_124127_, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(p_124127_))));
    }

    protected static <T> T applyExplosionDecay(ItemLike pItem, FunctionUserBuilder<T> pFunction) {
        return (T)(!EXPLOSION_RESISTANT.contains(pItem.asItem()) ? pFunction.apply(ApplyExplosionDecay.explosionDecay()) : pFunction.unwrap());
    }

    protected static <T> T applyExplosionCondition(ItemLike pItem, ConditionUserBuilder<T> pCondition) {
        return (T)(!EXPLOSION_RESISTANT.contains(pItem.asItem()) ? pCondition.when(ExplosionCondition.survivesExplosion()) : pCondition.unwrap());
    }


    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> p_124179_) {
        this.addTables();
        Set<ResourceLocation> set = Sets.newHashSet();

        for (Block block : ForgeRegistries.BLOCKS.getEntries().stream().map(c -> c.getValue()).filter(block -> block.getRegistryName().getNamespace().equals(WoldsVaults.MOD_ID)).toList()) {
            ResourceLocation resourcelocation = block.getLootTable();
            if (resourcelocation != BuiltInLootTables.EMPTY && set.add(resourcelocation)) {
                LootTable.Builder loottable$builder = map.remove(block.getLootTable());
                if (loottable$builder == null) {
                    throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourcelocation, ForgeRegistries.BLOCKS.getKey(block)));
                }

                p_124179_.accept(resourcelocation, loottable$builder);
            }
        }

        if (!this.map.isEmpty()) {
            //throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
        }
    }
}
