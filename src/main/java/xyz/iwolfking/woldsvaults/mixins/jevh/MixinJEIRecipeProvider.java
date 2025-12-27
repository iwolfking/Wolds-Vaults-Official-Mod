package xyz.iwolfking.woldsvaults.mixins.jevh;

import dev.attackeight.just_enough_vh.jei.JEIRecipeProvider;
import dev.attackeight.just_enough_vh.jei.LabeledLootInfo;
import iskallia.vault.config.entry.IntRangeEntry;
import iskallia.vault.config.entry.ItemStackEntry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = JEIRecipeProvider.class, remap = false)
public abstract class MixinJEIRecipeProvider {

    /**
     * @author iwolfking
     * @reason Fix bounty rewards
     */
    @Overwrite
    public static List<LabeledLootInfo> getBountyRewards() {
        List<LabeledLootInfo> toReturn = new ArrayList();
        ModConfigs.REWARD_CONFIG.POOLS.forEach((id, entry) -> {
            TreeMap<Integer, LabeledLootInfo> lootInfo = new TreeMap();
            if (!id.equals("submission")) {
                entry.forEach((minLevel, rewards) -> {
                    AtomicInteger totalWeight = new AtomicInteger();
                    List<ItemStack> results = new ArrayList();
                    IntRangeEntry vaultExp = rewards.vaultExp;
                    rewards.getItemPool().getPool().forEach((stack) -> totalWeight.addAndGet(stack.weight));
                    rewards.getItemPool().getPool().forEach((stack) -> {
                        if(stack.value == null) {
                            return;
                        }

                        results.add(formatItemStack(((ItemStackEntry) stack.value).getMatchingStack(), ((ItemStackEntry) stack.value).getMinCount(), ((ItemStackEntry) stack.value).getMaxCount(), (double) stack.weight, (double) totalWeight.get()));
                    });
                    TextComponent var10003 = new TextComponent("Reward Pool: " + id + " Level: " + minLevel + "+");
                    int var10006 = vaultExp.getMin();
                    lootInfo.put(minLevel, LabeledLootInfo.of(results, var10003, new TextComponent("Vault Exp Reward: " + var10006 + "-" + vaultExp.getMax())));
                });
            }

            lootInfo.forEach((n, i) -> toReturn.add(i));
        });
        return toReturn;
    }

    @Shadow
    protected static ItemStack formatItemStack(ItemStack item, int amountMin, int amountMax, double weight, double totalWeight, @Nullable Integer amount) {
        return null;
    }

    @Shadow
    private static ItemStack formatItemStack(ItemStack item, int amountMin, int amountMax, double weight, double totalWeight, @Nullable Integer amount, @Nullable String rollText) {
        return null;
    }

    @Shadow
    private static ItemStack formatItemStack(ItemStack item, int amountMin, int amountMax, double weight, double totalWeight) {
        return formatItemStack(item, amountMin, amountMax, weight, totalWeight, (Integer)null);
    }
}
