package xyz.iwolfking.woldsvaults.integration.jade.components;

import iskallia.vault.block.RunePillarBlock;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.item.tool.ToolItem;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.BrewingAltar;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.integration.jade.WoldsJadePlugin;

public class VaultObjectiveBlocksComponent implements IComponentProvider {
    public static final VaultObjectiveBlocksComponent INSTANCE = new VaultObjectiveBlocksComponent();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!iPluginConfig.get(WoldsJadePlugin.INSTANCE)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Block block = blockAccessor.getBlock();

        if(blockAccessor.getBlockEntity() instanceof BrewingAltarTileEntity brewingAltar) {
            int remainingUses = blockAccessor.getBlockState().getValue(BrewingAltar.USES);
            iTooltip.add(new TranslatableComponent("vaultobjectiveblocks.jade.tooltip.brewing_altar", remainingUses));
        }

    }



}