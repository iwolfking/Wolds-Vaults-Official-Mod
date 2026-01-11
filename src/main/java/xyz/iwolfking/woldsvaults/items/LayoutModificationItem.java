package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutRegistry;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class LayoutModificationItem extends Item
        implements VaultLevelItem, DataTransferItem {

    public static final String TAG_LAYOUT = "layout";
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

        LayoutRegistry.get(root.getString(TAG_LAYOUT)).ifPresent(def -> {
            CompoundTag data = getOrUpgradeLayoutData(root, def);
            def.addTooltip(data, tooltip);
        });
    }

    public static Optional<CrystalLayout> getLayout(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_LAYOUT)) return Optional.empty();

        return LayoutRegistry.get(root.getString(TAG_LAYOUT))
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

    public static ItemStack create(String layoutId, CompoundTag layoutData) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(TAG_LAYOUT, layoutId);
        tag.put(TAG_LAYOUT_DATA, layoutData);

        return stack;
    }

    public static ItemStack create(String layoutId, int value) {
        ItemStack stack = new ItemStack(ModItems.LAYOUT_MANIPULATOR);
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(TAG_LAYOUT, layoutId);
        tag.putInt(LEGACY_TUNNEL, 1);
        tag.putInt(LEGACY_VALUE, 1);

        return stack;
    }


    @Override
    public void initializeVaultLoot(
            int level, ItemStack stack,
            @Nullable BlockPos pos, @Nullable Vault vault) {

        rollLayoutFromLevel(level, stack);
    }

    @Override
    public ItemStack convertStack(ItemStack stack, RandomSource random) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("pool")) return stack;

        rollLayoutFromLevel(tag.getInt("pool"), stack);
        return stack;
    }

    private static void rollLayoutFromLevel(int level, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_LAYOUT)) return;

        Optional<CrystalLayout> rolled =
                ModConfigs.VAULT_CRYSTAL.getRandomLayout(
                        level, JavaRandom.ofNanoTime());

        if (rolled.isEmpty()) return;

        CrystalLayout layout = rolled.get();

        LayoutRegistry.getForLayout(layout).ifPresent(def -> {
            CompoundTag data = new CompoundTag();
            def.writeFromLayout(layout, data);

            tag.putString(TAG_LAYOUT, def.id());
            tag.put(TAG_LAYOUT_DATA, data);
        });
    }
}
