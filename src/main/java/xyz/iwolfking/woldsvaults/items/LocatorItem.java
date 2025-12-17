package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModParticles;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

import java.util.List;
import java.util.Random;

// Item that locates a certain block based on a y level filter
public class LocatorItem extends BasicItem {
    private final Block blockToLocate;
    private final int yLevelFilter;

    public LocatorItem(ResourceLocation id, Block blockToLocate, int yLevelFilter) {
        super(id, new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS));
        this.blockToLocate = blockToLocate;
        this.yLevelFilter = yLevelFilter;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(!pLevel.isClientSide()) {

            BlockPos foundPos = findClosestBlock(pLevel, pPlayer.blockPosition(), yLevelFilter);
            if (foundPos != null) {
                spawnGuidanceParticles((ServerLevel) pLevel, pPlayer.blockPosition(), foundPos);
            }
            pPlayer.getCooldowns().addCooldown(this, 200);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    private BlockPos findClosestBlock(Level level, BlockPos playerPos, int yLevel) {
        int searchRadius = 128;
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                BlockPos pos = new BlockPos(playerPos.getX() + dx, yLevel, playerPos.getZ() + dz);
                if (level.getBlockState(pos).getBlock() == this.blockToLocate) {
                    double dist = playerPos.distSqr(pos);
                    if (dist < closestDistance) {
                        closest = pos;
                        closestDistance = dist;
                    }
                }
            }
        }

        return closest;
    }

    private void spawnGuidanceParticles(ServerLevel level, BlockPos from, BlockPos to) {
        if (to == null) return;
        Vec3 dir = Vec3.atCenterOf(to).subtract(Vec3.atCenterOf(from)).normalize();
        Random random = level.getRandom();

        for (int i = 1; i <= 30; i++) {
            Vec3 pos = Vec3.atCenterOf(from).add(dir.scale(i * 3)) // distance between steps
                    .add(random.nextGaussian(), 0.3, random.nextGaussian());

            for (int j = 0; j < 3; j++) {
                pos.add(random.nextGaussian() * 0.1, 0.3 + random.nextDouble() * 0.2, random.nextGaussian() * 0.1);
                level.sendParticles(ModParticles.UBER_PYLON.get(), pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0);
            }

        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TextComponent("Locates Fractured Obelisks").withStyle(ChatFormatting.DARK_RED));



        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
