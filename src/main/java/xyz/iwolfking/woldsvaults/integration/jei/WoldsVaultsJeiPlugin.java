package xyz.iwolfking.woldsvaults.integration.jei;

import dev.attackeight.just_enough_vh.jei.ForgeItem;
import dev.attackeight.just_enough_vh.jei.JEIRecipeProvider;
import dev.attackeight.just_enough_vh.jei.RecyclerRecipe;
import dev.attackeight.just_enough_vh.jei.TheVaultJEIPlugin;
import dev.attackeight.just_enough_vh.jei.category.ForgeItemRecipeCategory;
import iskallia.vault.VaultMod;
import iskallia.vault.config.ShopPedestalConfig;
import iskallia.vault.config.entry.recipe.ConfigForgeRecipe;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.modifier.card.CardModifier;
import iskallia.vault.core.card.modifier.card.TaskLootCardModifier;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.entry.LootEntry;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.util.StringUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.loading.LoadingModList;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.vhapi.integration.jevh.LabeledLootInfo;
import xyz.iwolfking.vhapi.integration.jevh.LabeledLootInfoRecipeCategory;
import xyz.iwolfking.vhapi.integration.the_vault.VaultSealHelper;
import xyz.iwolfking.vhapi.integration.wolds.WoldsSealHelper;
import xyz.iwolfking.vhapi.mixin.accessors.TemporalShardConfigAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;
import xyz.iwolfking.woldsvaults.config.lib.GenericShopPedestalConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.integration.jei.category.*;
import xyz.iwolfking.woldsvaults.integration.jei.category.lib.GenericLootableBoxCategory;
import xyz.iwolfking.woldsvaults.integration.jei.category.lib.ShopTierCategory;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.TaskLootCardModifierConfigAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

@JeiPlugin
@SuppressWarnings("unused")
public class WoldsVaultsJeiPlugin implements IModPlugin {

    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> CATALYST_BOX = RecipeType.create(WoldsVaults.MOD_ID, "catalyst_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> INSCRIPTION_BOX = RecipeType.create(WoldsVaults.MOD_ID, "inscription_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> AUGMENT_BOX = RecipeType.create(WoldsVaults.MOD_ID, "augment_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> SUPPLY_BOX = RecipeType.create(WoldsVaults.MOD_ID, "supply_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> GEM_BOX = RecipeType.create(WoldsVaults.MOD_ID, "gem_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> OMEGA_BOX = RecipeType.create(WoldsVaults.MOD_ID, "omega_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> ENIGMA_EGG = RecipeType.create(WoldsVaults.MOD_ID, "enigma_egg", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> VAULTAR_BOX = RecipeType.create(WoldsVaults.MOD_ID, "vaultar_box", GenericLootableBoxCategory.GenericLootableConfigPage.class);
    public static final RecipeType<GenericLootableBoxCategory.GenericLootableConfigPage> GATEWAY_PEARL = RecipeType.create(WoldsVaults.MOD_ID, "gateway_pearl", GenericLootableBoxCategory.GenericLootableConfigPage.class);

    public static final RecipeType<ShopPedestalConfig.ShopTier> ETCHING_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "etching_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> GOD_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "god_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> BLACKSMITH_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "blacksmith_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> RARE_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "rare_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> OMEGA_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "omega_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> SPOOKY_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "spooky_shop_pedestal", ShopPedestalConfig.ShopTier.class);
    public static final RecipeType<ShopPedestalConfig.ShopTier> CARD_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "card_shop_pedestal", ShopPedestalConfig.ShopTier.class);

    // requires vault jei mod
    public static final RecipeType<ForgeItem> MOD_BOX_WORKSTATION = RecipeType.create(WoldsVaults.MOD_ID, "mod_box_workstation", ForgeItem.class);
    public static final RecipeType<ForgeItem> AUGMENTS_ASSEMBLY = RecipeType.create(WoldsVaults.MOD_ID, "augment_assembly", ForgeItem.class);
    public static final RecipeType<ForgeItem> WEAVING = RecipeType.create(WoldsVaults.MOD_ID, "weaving", ForgeItem.class);
    public static final RecipeType<LabeledLootInfo> LAYOUTS = RecipeType.create(WoldsVaults.MOD_ID, "layouts", LabeledLootInfo.class);
    public static final RecipeType<LabeledLootInfo> ETCHED_LAYOUTS = RecipeType.create(WoldsVaults.MOD_ID, "etched_layouts", LabeledLootInfo.class);
    public static final RecipeType<LabeledLootInfo> RESOURCE_CARDS_LOOT = RecipeType.create(WoldsVaults.MOD_ID, "resource_cards_loot", LabeledLootInfo.class);

    public WoldsVaultsJeiPlugin() {}
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return WoldsVaults.id("wolds_jei_integration");
    }

    @Override @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.ENIGMA_EGG), ENIGMA_EGG);
        registration.addRecipeCatalyst(new ItemStack(ModItems.OMEGA_BOX), OMEGA_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GEM_BOX), GEM_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SUPPLY_BOX), SUPPLY_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.AUGMENT_BOX), AUGMENT_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.INSCRIPTION_BOX), INSCRIPTION_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CATALYST_BOX), CATALYST_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.VAULTAR_BOX), VAULTAR_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VAULT_INFUSER_BLOCK), InfuserCraftingCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK), InfuserCraftingCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.UNIDENTIFIED_GATEWAY_PEARL), GATEWAY_PEARL);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ETCHING_PEDESTAL), ETCHING_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOD_VENDOR_PEDESTAL), GOD_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL), BLACKSMITH_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RARE_VENDOR_PEDESTAL), RARE_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OMEGA_VENDOR_PEDESTAL), OMEGA_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPOOKY_VENDOR_PEDESTAL), SPOOKY_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CARD_VENDOR_PEDESTAL), CARD_SHOP_PEDESTAL);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VAULT_SALVAGER_BLOCK), TheVaultJEIPlugin.VAULT_RECYCLER);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MOD_BOX_WORKSTATION), MOD_BOX_WORKSTATION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AUGMENT_CRAFTING_TABLE), AUGMENTS_ASSEMBLY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WEAVING_STATION), WEAVING);

        registration.addRecipeCatalyst(new ItemStack(iskallia.vault.init.ModItems.VAULT_CRYSTAL), LAYOUTS);
        registration.addRecipeCatalyst(new ItemStack(ModItems.LAYOUT_MANIPULATOR), LAYOUTS);
        registration.addRecipeCatalyst(new ItemStack(ModItems.LAYOUT_MANIPULATOR), ETCHED_LAYOUTS);
        registration.addRecipeCatalyst(new ItemStack(iskallia.vault.init.ModItems.BOOSTER_PACK), RESOURCE_CARDS_LOOT);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.CATALYST_BOX, new TextComponent("Catalyst Box"), ModItems.CATALYST_BOX, CATALYST_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.VAULTAR_BOX, new TextComponent("Vaultar Box"), ModItems.VAULTAR_BOX, VAULTAR_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.INSCRIPTION_BOX, new TextComponent("Inscription Box"), ModItems.INSCRIPTION_BOX, INSCRIPTION_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.AUGMENT_BOX, new TextComponent("Augment Box"), ModItems.AUGMENT_BOX, AUGMENT_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.SUPPLY_BOX, new TextComponent("Supply Box"), ModItems.SUPPLY_BOX, SUPPLY_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.GEM_BOX, new TextComponent("Gem Box"), ModItems.GEM_BOX, GEM_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.OMEGA_BOX, new TextComponent("Omega Box"), ModItems.OMEGA_BOX, OMEGA_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.ENIGMA_EGG, new TextComponent("Enigma Egg"), ModItems.ENIGMA_EGG, ENIGMA_EGG));
        registration.addRecipeCategories(new InfuserCraftingCategory(guiHelper));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.GATEWAY_PEARL, new TextComponent("Gateway Pearl"), ModItems.UNIDENTIFIED_GATEWAY_PEARL, GATEWAY_PEARL));

        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Etching Shop Pedestal"), ModBlocks.ETCHING_PEDESTAL.asItem(), ETCHING_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("God Shop Pedestal"), ModBlocks.GOD_VENDOR_PEDESTAL.asItem(), GOD_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Blacksmith Shop Pedestal"), ModBlocks.BLACKSMITH_VENDOR_PEDESTAL.asItem(), BLACKSMITH_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Rare Shop Pedestal"), ModBlocks.RARE_VENDOR_PEDESTAL.asItem(), RARE_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Omega Shop Pedestal"), ModBlocks.OMEGA_VENDOR_PEDESTAL.asItem(), OMEGA_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Spooky Shop Pedestal"), ModBlocks.SPOOKY_VENDOR_PEDESTAL.asItem(), SPOOKY_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Card Shop Pedestal"), ModBlocks.CARD_VENDOR_PEDESTAL.asItem(), CARD_SHOP_PEDESTAL));

        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, MOD_BOX_WORKSTATION, new ItemStack(ModBlocks.MOD_BOX_WORKSTATION.asItem())));
        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, AUGMENTS_ASSEMBLY, new ItemStack(ModBlocks.AUGMENT_CRAFTING_TABLE.asItem())));
        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, WEAVING, new ItemStack(ModBlocks.WEAVING_STATION.asItem())));

        registration.addRecipeCategories(makeLabeledLootInfoCategory(guiHelper, LAYOUTS, ModItems.LAYOUT_MANIPULATOR, new TextComponent("Vault Layouts")));
        registration.addRecipeCategories(makeLabeledLootInfoCategory(guiHelper, ETCHED_LAYOUTS, ModItems.LAYOUT_MANIPULATOR, new TextComponent("Etched Vault Layout Pools")));
        registration.addRecipeCategories(makeLabeledLootInfoCategory(guiHelper, RESOURCE_CARDS_LOOT, iskallia.vault.init.ModItems.BOOSTER_PACK, new TextComponent("Resource Card Rewards")));
    }

    @Override @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;

        if (world != null) {
            var manager = world.getRecipeManager();
            registration.addRecipes(manager.byType(ModRecipeTypes.INFUSER).values(), InfuserCraftingCategory.UID);
        }


        registration.addRecipes(ENIGMA_EGG, makePages(ModConfigs.ENIGMA_EGG));
        registration.addRecipes(OMEGA_BOX, makePages(ModConfigs.OMEGA_BOX));
        registration.addRecipes(GEM_BOX, makePages(ModConfigs.GEM_BOX));
        registration.addRecipes(SUPPLY_BOX, makePages(ModConfigs.SUPPLY_BOX));
        registration.addRecipes(AUGMENT_BOX, makePages(ModConfigs.AUGMENT_BOX));
        registration.addRecipes(INSCRIPTION_BOX, makePages(ModConfigs.INSCRIPTION_BOX));
        registration.addRecipes(CATALYST_BOX, makePages(ModConfigs.CATALYST_BOX));
        registration.addRecipes(VAULTAR_BOX, makePages(ModConfigs.VAULTAR_BOX));
        registration.addRecipes(GATEWAY_PEARL, makePages(ModConfigs.GATEWAY_PEARL));

        registerShopPedestalRecipes(registration, ModConfigs.ETCHING_SHOP_PEDESTAL, ETCHING_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.GOD_SHOP_PEDESTAL, GOD_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.BLACKSMITH_SHOP_PEDESTAL, BLACKSMITH_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.RARE_SHOP_PEDESTAL, RARE_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.OMEGA_SHOP_PEDESTAL, OMEGA_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.SPOOKY_SHOP_PEDESTAL, SPOOKY_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.CARD_SHOP_PEDESTAL, CARD_SHOP_PEDESTAL);

        registration.addRecipes(MOD_BOX_WORKSTATION, getForgeRecipes(ModConfigs.MOD_BOX_RECIPES_CONFIG.getConfigRecipes()));
        registration.addRecipes(AUGMENTS_ASSEMBLY, getForgeRecipes(ModConfigs.AUGMENT_RECIPES.getConfigRecipes()));
        registration.addRecipes(WEAVING, getForgeRecipes(ModConfigs.WEAVING_RECIPES_CONFIG.getConfigRecipes()));
        registration.addRecipes(LAYOUTS, getLayoutsPerLevel());
        registration.addRecipes(ETCHED_LAYOUTS, getEtchedLayoutsPerLevel());
        registration.addRecipes(RESOURCE_CARDS_LOOT, getResourceCardLoot());
        addCustomRecyclerRecipes(registration);
    }


    private void registerShopPedestalRecipes(IRecipeRegistration registration, GenericShopPedestalConfig configInstance, RecipeType<ShopPedestalConfig.ShopTier> recipeType) {
        for (ShopPedestalConfig.ShopTier tier : configInstance.LEVELS) {
            registration.addRecipes(recipeType, List.of(tier));
        }
    }

    private static <R extends VaultForgeRecipe, T extends ConfigForgeRecipe<R>> List<ForgeItem> getForgeRecipes(List<T> configRecipes) {
        List<ForgeItem> recipes = new ArrayList<>();
        configRecipes.forEach(b -> recipes.add(new ForgeItem(b.makeRecipe().getInputs(), b.makeRecipe().getDisplayOutput(100))));
        return recipes;
    }

    private static void addCustomRecyclerRecipes(IRecipeRegistration registration) {
        List<RecyclerRecipe> recipes = new ArrayList<>();
        for (var rec : ModConfigs.CUSTOM_RECYCLER_CONFIG.getOutputs().entrySet()) {
            recipes.add(JEIRecipeProvider.getRecyclerRecipe(new ItemStack(Registry.ITEM.get(rec.getKey())), rec.getValue()));
        }
        registration.addRecipes(TheVaultJEIPlugin.VAULT_RECYCLER, recipes);
    }

    private List<GenericLootableBoxCategory.GenericLootableConfigPage> makePages(GenericLootableConfig cfg) {
        int max = 54;
        int size = cfg.POOL.size();
        int pageCount = (int)Math.ceil((double) size / max);

        List<GenericLootableBoxCategory.GenericLootableConfigPage> out = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            out.add(new GenericLootableBoxCategory.GenericLootableConfigPage(cfg, i));
        }
        return out;
    }


    public static List<LabeledLootInfo> getLayoutsPerLevel() {
        List<LabeledLootInfo> lootInfo = new ArrayList<>();
        iskallia.vault.init.ModConfigs.VAULT_CRYSTAL.LAYOUTS.forEach((layouts) -> {
            List<ItemStack> layoutStacks = new ArrayList<>();
            int totalWeight = (int) layouts.pool.getTotalWeight();
            int level = layouts.level;
            layouts.pool.forEach((layout, aDouble) -> {
                if(aDouble <= 0) {
                    return;
                }

                ItemStack layoutStack = LayoutModificationItem.create(layout);
                layoutStacks.add(formatItemStack(layoutStack, 1, 1, aDouble, totalWeight, 1));
            });
            lootInfo.add(LabeledLootInfo.of(layoutStacks, new TextComponent( "Layouts - Level " + level), null));
        });

        return lootInfo;
    }

    public static List<LabeledLootInfo> getEtchedLayoutsPerLevel() {
        List<LabeledLootInfo> lootInfo = new ArrayList<>();
        ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.forEach((pool, layouts) -> {
            layouts.forEach(layoutEntry -> {
                List<ItemStack> layoutStacks = new ArrayList<>();
                int totalWeight = (int) layoutEntry.pool.getTotalWeight();
                layoutEntry.pool.forEach((layout, aDouble) -> {
                    ItemStack etchedLayout = LayoutModificationItem.create(layout);
                    layoutStacks.add(formatItemStack(etchedLayout, 1, 1, aDouble, totalWeight, 1));
                });
                lootInfo.add(LabeledLootInfo.of(layoutStacks, new TextComponent(StringUtils.convertToTitleCase(pool) + " - Level " + layoutEntry.level), null));
            });
        });

        return lootInfo;
    }

    public static List<LabeledLootInfo> getResourceCardLoot() {
        List<LabeledLootInfo> lootInfo = new ArrayList<>();
        List<ItemStack> resourceCardStacks = new ArrayList<>();
        List<ItemStack> deluxeResourceCardStacks = new ArrayList<>();

        iskallia.vault.init.ModConfigs.CARD_MODIFIERS.getPools().get("resource").forEach(((s, aDouble) -> {
            CardEntry.Config cardConfig = iskallia.vault.init.ModConfigs.CARD_MODIFIERS.getValues().get(s);
            if(cardConfig.value instanceof TaskLootCardModifier taskLootCardModifier) {
                LootPool pool = ((TaskLootCardModifierConfigAccessor)taskLootCardModifier.getConfig()).getLoot();
                pool.getChildren().forEach((lootEntry, aDouble1) -> {
                    if(lootEntry instanceof LootEntry) {
                        resourceCardStacks.addAll(((LootEntry) lootEntry).getStack(ChunkRandom.any()));
                    }
                });
            }
        }));
        iskallia.vault.init.ModConfigs.CARD_MODIFIERS.getPools().get("deluxe_resource").forEach(((s, aDouble) -> {
            CardEntry.Config cardConfig = iskallia.vault.init.ModConfigs.CARD_MODIFIERS.getValues().get(s);
            if(cardConfig.value instanceof TaskLootCardModifier taskLootCardModifier) {
                LootPool pool = ((TaskLootCardModifierConfigAccessor)taskLootCardModifier.getConfig()).getLoot();
                pool.getChildren().forEach((lootEntry, aDouble1) -> {
                    if(lootEntry instanceof LootEntry) {
                        deluxeResourceCardStacks.addAll(((LootEntry) lootEntry).getStack(ChunkRandom.any()));
                    }
                });
            }
        }));

        lootInfo.add(LabeledLootInfo.of(resourceCardStacks, new TextComponent( "Resource Cards"), null));
        lootInfo.add(LabeledLootInfo.of(deluxeResourceCardStacks, new TextComponent( "Deluxe Resource Cards"), null));


        return lootInfo;
    }


    protected static ItemStack formatItemStack(ItemStack item, int amountMin, int amountMax, double weight, double totalWeight, @Nullable Integer amount) {
        return formatItemStack(item, amountMin, amountMax, weight, totalWeight, amount, (String)null);
    }

    private static ItemStack formatItemStack(ItemStack item, int amountMin, int amountMax, double weight, double totalWeight, @Nullable Integer amount, @Nullable String rollText) {
        ItemStack result = item.copy();
        if (item.isEmpty()) {
            result = new ItemStack(Items.BARRIER);
            result.setHoverName(new TextComponent("Nothing"));
        }
        else if(item.getItem() instanceof BlockItem blockItem && blockItem.getBlock().equals(Blocks.BARRIER)) {
            result.setHoverName(new TextComponent("Invalid Entry"));
        }

        result.setCount(amount == null ? amountMax : amount);
        double chance = weight / totalWeight * (double)100.0F;
        CompoundTag nbt = result.getOrCreateTagElement("display");
        ListTag list = nbt.getList("Lore", 8);
        MutableComponent chanceLabel = new TextComponent("Chance: ");
        chanceLabel.append(String.format("%.2f", chance));
        chanceLabel.append("%");
        list.add(StringTag.valueOf(Component.Serializer.toJson(chanceLabel.withStyle(ChatFormatting.YELLOW))));
        if (amountMin != amountMax) {
            MutableComponent countLabel = new TextComponent(amount == null ? "Count: " : "Cost: ");
            countLabel.append(amountMin + " - " + amountMax);
            list.add(StringTag.valueOf(Component.Serializer.toJson(countLabel)));
        }

        if (rollText != null) {
            MutableComponent rollLabel = new TextComponent(rollText);
            list.add(StringTag.valueOf(Component.Serializer.toJson(rollLabel.withStyle(ChatFormatting.DARK_AQUA))));
        }

        nbt.put("Lore", list);
        return result;
    }

    public static LabeledLootInfoRecipeCategory makeLabeledLootInfoCategory(IGuiHelper guiHelper, RecipeType<LabeledLootInfo> recipeType, ItemLike icon) {
        return new LabeledLootInfoRecipeCategory(guiHelper, recipeType, new ItemStack(icon), OUTPUT);
    }

    public static LabeledLootInfoRecipeCategory makeLabeledLootInfoCategory(IGuiHelper guiHelper, RecipeType<LabeledLootInfo> recipeType, ItemLike icon, Component title) {
        return new LabeledLootInfoRecipeCategory(guiHelper, recipeType, new ItemStack(icon), title, OUTPUT);
    }

    public static LabeledLootInfoRecipeCategory makeLabeledIngredientPoolCategory(IGuiHelper guiHelper, RecipeType<LabeledLootInfo> recipeType, ItemLike icon) {
        return new LabeledLootInfoRecipeCategory(guiHelper, recipeType, new ItemStack(icon), INPUT);
    }



}
