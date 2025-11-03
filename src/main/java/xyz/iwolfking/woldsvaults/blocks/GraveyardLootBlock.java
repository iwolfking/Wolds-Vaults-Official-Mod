package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.LootableBlock;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.init.ModEntities;
import iskallia.vault.util.BlockHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.SpawnEntityVaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.tasks.SpawnMobTask;
import xyz.iwolfking.woldsvaults.blocks.tiles.GraveyardLootTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.HellishSandTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class GraveyardLootBlock extends LootableBlock {
    public GraveyardLootBlock() {
        super(Properties.copy(Blocks.DEEPSLATE));
    }

    VaultEvent TEST_EVENT = new VaultEvent("Test Event", new TextComponent("Boo! This is a test."), TextColor.fromLegacyFormat(ChatFormatting.GOLD), new TextComponent("Testing 123"), VaultEvent.EventDisplayType.ACTION_BAR, Set.of(EventTag.NEGATIVE), List.of(new SpawnMobTask(new WeightedList<EntityType<?>>().add(ModEntities.VAULT_SPIDER, 6.0).add(ModEntities.VAULT_SPIDER_BABY, 4.0).add(ModEntities.DUNGEON_SPIDER, 2.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(8, 10), WeightedList.empty(), ItemStack.EMPTY)));
    @Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockState state, BlockEntityType<A> type) {
        return !world.isClientSide() ? null : BlockHelper.getTicker(type, ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE, GraveyardLootTileEntity::tick);
    }

    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ModBlocks.GRAVEYARD_LOOT_BLOCK_BLOCK_ENTITY_TYPE.create(pos, state);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if(!level.isClientSide() && VaultUtils.getVault(level).isPresent()) {
            TEST_EVENT.triggerEvent(pos, (ServerPlayer) player, VaultUtils.getVault(level).get());
            //EnchantedEventsRegistry.BUNFUNGUS_EVENT.triggerEvent(pos, (ServerPlayer) player, VaultUtils.getVault(level).get());
        }
        //TODO: Spawn mob/Do event
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
