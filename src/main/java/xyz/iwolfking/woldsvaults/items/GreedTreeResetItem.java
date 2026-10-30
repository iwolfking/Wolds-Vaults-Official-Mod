package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.UsableItem;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class GreedTreeResetItem extends UsableItem {
    public GreedTreeResetItem(ResourceLocation id) {
        super(id);
    }

    protected SoundEvent getSuccessSound() {
        return SoundEvents.BEACON_POWER_SELECT;
    }

    protected void doUse(ServerLevel level, ServerPlayer player, ItemStack heldStack) {
        PlayerGreedTreeData treeData = PlayerGreedTreeData.get(level);
        treeData.getGreedTree(player).resetAllNodes(player);
        treeData.setDirty(true);
        treeData.sync(player);
        level.sendParticles(ParticleTypes.FLASH, player.position().x() + (double)(Mth.cos((float)Math.toRadians((player.yHeadRot + 90.0F))) / 2.0F), player.position().y() + (double)player.getBbHeight() - (double)(player.getBbHeight() / 4.0F), player.position().z() + (double)(Mth.sin((float)Math.toRadians((player.yHeadRot + 90.0F))) / 2.0F), 1, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    protected TextColor getNameColor() {
        return TextColor.fromLegacyFormat(ChatFormatting.GOLD);
    }
}
