package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class LayoutModificationItem extends Item
        implements VaultLevelItem, DataTransferItem {

    public static final String TAG_LAYOUT = "layout";
    public static final String TAG_POOL = "pool";
    public static final String TAG_LEVEL = "level";
    public static final String LEGACY_TUNNEL = "tunnel";
    public static final String LEGACY_VALUE = "value";
    public static final String TAG_LAYOUT_DATA = "layout_data";

    public LayoutModificationItem(CreativeModeTab tab, ResourceLocation id) {
        super(new Item.Properties().tab(tab));
        setRegistryName(id);
    }


    @Override
    public void appendHoverText(ItemStack stack, Level level,
                                List<Component> tooltip, TooltipFlag flag) {

        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_LAYOUT)) return;

        LayoutDefinitionRegistry.get(root.getString(TAG_LAYOUT)).ifPresent(def -> {
            CompoundTag data = getOrUpgradeLayoutData(root, def);
            def.addTooltip(data, tooltip);
        });
    }

    public static Optional<CrystalLayout> getLayout(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_LAYOUT)) return Optional.empty();

        return LayoutDefinitionRegistry.get(root.getString(TAG_LAYOUT))
                .map(def -> def.create(getOrUpgradeLayoutData(root, def)));
    }

    private static CompoundTag getOrUpgradeLayoutData(
            CompoundTag root, LayoutDefinition def) {

        if (root.contains(TAG_LAYOUT_DATA)) {
            return root.getCompound(TAG_LAYOUT_DATA);
        }

        CompoundTag upgraded = def.upgradeLegacy(root);
        root.put(TAG_LAYOUT_DATA, upgraded);
        return upgraded;
    }

    public static ItemStack create(CrystalLayout layout) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        CompoundTag tag = stack.getOrCreateTag();
        LayoutDefinition definition = LayoutDefinitionRegistry.getForLayout(layout).orElse(null);

        if(definition == null) {
            return ItemStack.EMPTY;
        }

        definition.writeFromLayout(layout, tag);

        return stack;
    }

    public static ItemStack create(String layoutId, CompoundTag layoutData) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(TAG_LAYOUT, layoutId);
        tag.put(TAG_LAYOUT_DATA, layoutData);

        return stack;
    }

    public static ItemStack createLegacy(String layoutId, int value) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(TAG_LAYOUT, layoutId);
        tag.putInt(LEGACY_TUNNEL, 1);
        tag.putInt(LEGACY_VALUE, value);

        return stack;
    }


    @Override
    public void initializeVaultLoot(
            int level, ItemStack stack,
            @Nullable BlockPos pos, @Nullable Vault vault) {
        if(stack.hasTag() && stack.getTag().contains("pool")) {
            if(stack.getTag().get("pool") instanceof IntTag) {
                rollLayoutFromLevel(level, stack);
            }
            else {
                rollLayoutFromLevel(stack.getTag().getString("pool"), level, stack);
            }
        }
        else {
            rollLayoutFromLevel(level, stack);
        }
    }

    @Override
    public ItemStack convertStack(ItemStack stack, RandomSource random) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_POOL)) {
            if(!tag.contains(TAG_LEVEL)) {
                return stack;
            }

            rollLayoutFromLevel("default", tag.getInt(TAG_LEVEL), stack);
            return stack;
        }

        if(tag.contains(TAG_LEVEL)) {
            rollLayoutFromLevel(tag.getString(TAG_POOL), 0, stack);
        }
        else {
            rollLayoutFromLevel(tag.getString(TAG_POOL), tag.getInt(TAG_LEVEL), stack);
        }

        return stack;
    }

    //Handles legacy items
    private static void rollLayoutFromLevel(int level, ItemStack stack) {
        rollLayoutFromLevel("default", level, stack);
    }

    private static void rollLayoutFromLevel(String pool, int level, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_LAYOUT)) return;

        if(!ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.containsKey(pool)) {
            WoldsVaults.LOGGER.warn("Attempted to roll an Etched Vault Layout but pool {} was invalid, please report this to the developer!", pool);
            WoldsVaults.LOGGER.warn(tag.toString());
            rollLayoutFromLevel("default", level, stack);
            return;
        }

        Optional<VaultCrystalConfig.LayoutEntry> rolled =
                ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.get(pool).getForLevel(level);
        rolled.flatMap(layoutEntry -> layoutEntry.pool.getRandom()).ifPresent(crystalLayout -> {
            LayoutDefinitionRegistry.getForLayout(crystalLayout).ifPresent(def -> {
                def.writeFromLayout(crystalLayout, tag);

                if(tag.contains(TAG_POOL)) {
                    tag.remove(TAG_POOL);
                }
                if(tag.contains(TAG_LEVEL)) {
                    tag.remove(TAG_LEVEL);
                }
            });
        });

    }

    @Override
    public @Nonnull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {

        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_LAYOUT)) {return Optional.empty();}

        return LayoutDefinitionRegistry
            .get(root.getString(TAG_LAYOUT))
            .flatMap(def -> def.getTooltipImage(getOrUpgradeLayoutData(root, def)));
    }
}
