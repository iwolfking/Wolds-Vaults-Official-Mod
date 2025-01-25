package xyz.iwolfking.woldsvaults.integration.vaultfilters.gte;

import iskallia.vault.util.StringUtils;
import net.joseph.vaultfilters.attributes.abstracts.StringListAttribute;

import net.minecraft.world.item.ItemStack;
import shadows.gateways.gate.Gateway;
import shadows.gateways.gate.Reward;
import shadows.gateways.item.GatePearlItem;

import java.util.ArrayList;
import java.util.List;

public class GatePearlRewardsAttribute extends StringListAttribute {
    public GatePearlRewardsAttribute(String value) {
        super(value);
    }

    public String getTranslationKey() {
        return "gte_gate_pearl_rewards";
    }

    public String getValue(ItemStack itemStack) {
        return "";
    }

    @Override
    public Object[] getTranslationParameters() {
        String modifiedItemName = (this.value).replace("[", "").replace("]", "").trim();
        return new Object[]{modifiedItemName};
    }

    public static List<String> getRewardItems(ItemStack stack) {
        List<String> itemNamesList = new ArrayList<>();
        if (stack.getItem() instanceof GatePearlItem) {
            Gateway gate = GatePearlItem.getGate(stack);

            if (gate == null) {
                return List.of();
            }

            List<Reward> gateRewards = gate.getRewards();


            for(Reward reward : gateRewards) {
                itemNamesList.addAll(getRewardNameByType(reward));
            }

        }

        return itemNamesList;
    }

    public List<String> getValues(ItemStack itemStack) {
        return getRewardItems(itemStack);
    }

    private static List<String> getRewardNameByType(Reward reward) {
        if(reward instanceof Reward.StackReward stackReward) {
            return List.of(stackReward.stack().getDisplayName().getString());
        }
        else if(reward instanceof Reward.ChancedReward chancedReward) {
            return getRewardNameByType(chancedReward.reward());
        }
        else if(reward instanceof Reward.LootTableReward lootTableReward) {
            return List.of(lootTableReward.table().getPath() + " loot table");
        }
        else if(reward instanceof Reward.EntityLootReward entityLootReward) {
            return List.of(StringUtils.convertToTitleCase(entityLootReward.type().getRegistryName().getPath()) + " loot");
        }
        else if(reward instanceof Reward.CommandReward commandReward) {
            return List.of(commandReward.command());
        }
        else if(reward instanceof Reward.StackListReward stackListReward) {
            List<String> itemList = new ArrayList<>();
            for(ItemStack stack : stackListReward.stacks()) {
                itemList.add(stack.getDisplayName().getString());
            }
            return itemList;
        }

        return List.of();
    }
}
