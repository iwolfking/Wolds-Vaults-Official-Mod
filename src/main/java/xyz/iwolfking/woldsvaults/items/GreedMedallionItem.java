package xyz.iwolfking.woldsvaults.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionDisplay;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

import java.util.List;

/**
 * A greed medallion: applied to a vault crystal to trade difficulty for greed coins and loot. One per
 * crystal, locked once applied, and gated on the player's greed rank.
 */
public class GreedMedallionItem extends Item {
    private final GreedMedallionTier tier;

    public GreedMedallionItem(GreedMedallionTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        this.setRegistryName(WoldsVaults.id("greed_medallion_" + tier.getPathName()));
    }

    public GreedMedallionTier getTier() {
        return this.tier;
    }

    /** The medallion tier an arbitrary stack carries, or null when the stack is not a medallion. */
    @Nullable
    public static GreedMedallionTier getTier(ItemStack stack) {
        return stack.getItem() instanceof GreedMedallionItem medallion ? medallion.getTier() : null;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return new TranslatableComponent(this.getDescriptionId(stack))
                .withStyle(GreedMedallionDisplay.getBandColor(this.tier));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        addSummary(this.tier, tooltip);
    }

    /** The medallion's effect table, one line per non-zero column. The tier name is not printed. */
    public static void addSummary(GreedMedallionTier tier, List<Component> tooltip) {
        addLine(tooltip, "Base Greed Coins: " + tier.getBaseGreedCoins(), ChatFormatting.GOLD);
        addLine(tooltip, "+" + tier.getMobHealthDamageBonus() + "% Mob Health/Damage", ChatFormatting.RED);
        if (tier.getCrateLootBonus() != 0) {
            addLine(tooltip, "+" + tier.getCrateLootBonus() + "% Crate Loot", ChatFormatting.YELLOW);
        }
        if (tier.getGreedCrateLootTier() != 0) {
            addLine(tooltip, "Greed Crate Loot Tier " + tier.getGreedCrateLootTier(), ChatFormatting.YELLOW);
        }
        if (tier.getChestRollsBonus() != 0) {
            addLine(tooltip, "+" + tier.getChestRollsBonus() + "% Chest Rolls", ChatFormatting.YELLOW);
        }
        if (tier.getTrapDisarmPenalty() != 0) {
            addLine(tooltip, "-" + tier.getTrapDisarmPenalty() + "% Trap Disarm", ChatFormatting.RED);
        }
        if (tier.getMobSpawnBonus() != 0) {
            addLine(tooltip, "+" + tier.getMobSpawnBonus() + "% Mobs", ChatFormatting.RED);
        }
        if (tier.getObjectiveDifficultyBonus() != 0) {
            addLine(tooltip, "+" + tier.getObjectiveDifficultyBonus() + "% Objective Difficulty", ChatFormatting.RED);
        }
        tooltip.add(new TextComponent(GreedMedallionDisplay.getParadigmLine(tier)).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(new TextComponent("Requires greed rank " + GreedMedallionDisplay.getDisplayName(tier) + " to apply")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void addLine(List<Component> tooltip, String text, ChatFormatting color) {
        tooltip.add(new TextComponent(text).withStyle(color));
    }
}
