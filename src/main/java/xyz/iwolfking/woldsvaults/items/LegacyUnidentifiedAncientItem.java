package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import iskallia.vault.util.EntityHelper;
import iskallia.vault.util.LootInitialization;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;

import java.util.List;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The retired stackable "unidentified ancient unique". Unidentified ancients are now real unidentified gear
 * ({@link AncientUniqueHelper#createUnidentifiedAncient}); any leftover stack of this item converts itself into
 * that many rolled pieces the first time it ticks in a player's inventory, then this item can be dropped.
 */
public class LegacyUnidentifiedAncientItem extends BasicItem {
    private static final Random RANDOM = new Random();

    public LegacyUnidentifiedAncientItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || stack.isEmpty()) {
            return;
        }
        int vaultLevel = PlayerVaultStatsData.get(player.getLevel()).getVaultStats(player).getVaultLevel();
        int count = stack.getCount();
        ItemStack first = ItemStack.EMPTY;
        int produced = 0;
        for (int i = 0; i < count; i++) {
            ItemStack ancient = AncientUniqueHelper.rollUnidentifiedAncient(RANDOM);
            if (ancient.isEmpty()) {
                break;
            }
            ancient = LootInitialization.initializeVaultLoot(ancient, vaultLevel);
            produced++;
            if (first.isEmpty()) {
                first = ancient;
            } else {
                EntityHelper.giveItem(player, ancient);
            }
        }
        if (produced == 0) {
            WoldLogger.error("A legacy unidentified ancient unique could not convert because no ancient could be rolled; leaving the stack as is.");
            return;
        }
        WoldLogger.info("Converted {} legacy unidentified ancient unique(s) held by {} into unidentified ancient gear.", produced, player.getGameProfile().getName());
        if (produced >= count && itemSlot >= 0 && itemSlot < player.getInventory().getContainerSize() && player.getInventory().getItem(itemSlot) == stack) {
            player.getInventory().setItem(itemSlot, first);
            return;
        }
        stack.shrink(produced);
        EntityHelper.giveItem(player, first);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(new TextComponent("Retired item: converts into unidentified ancient gear when held.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
