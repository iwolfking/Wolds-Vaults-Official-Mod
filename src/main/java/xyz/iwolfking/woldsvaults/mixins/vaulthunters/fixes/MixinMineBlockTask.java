package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.core.data.adapter.ISimpleAdapter;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.item.tool.ToolItem;
import iskallia.vault.task.MineBlockTask;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.counter.TaskCounter;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = MineBlockTask.class, remap = false)
public abstract class MixinMineBlockTask extends ProgressConfiguredTask<Integer, MineBlockTask.Config>  {


    public MixinMineBlockTask(MineBlockTask.Config config, ISimpleAdapter<? extends TaskCounter<Integer, ?>, ? super Tag, ? super JsonElement> adapter) {
        super(config, adapter);
    }

    @Redirect(method = "lambda$onAttach$0", at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/data/tile/TilePredicate;test(Liskallia/vault/core/world/data/tile/PartialTile;)Z"))
    private boolean handleBreachingCoinPiles(TilePredicate instance, PartialTile tile, @Local(argsOnly = true) BlockEvent.BreakEvent data) {
        if(data.getPlayer() != null) {
            BlockState state = data.getState();
            if(state.getBlock() instanceof CoinPileBlock) {
                Block block = tile.getState().getBlock().asWhole().orElse(Blocks.AIR);
                if(block instanceof CoinPileBlock) {
                    Player player = data.getPlayer();
                    ItemStack mainHandStack = player.getMainHandItem();
                    if(mainHandStack.getItem() instanceof ToolItem) {
                        if(GearDataCache.of(mainHandStack).hasAttribute(ModGearAttributes.BREACHING)) {
                            return true;
                        }
                    }
                }

            }
        }

        return this.getConfig().filter.test(tile);
    }
}
