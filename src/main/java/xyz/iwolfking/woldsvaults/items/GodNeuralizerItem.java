package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.BasicItem;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A god-attuned neuralizer: consumed on use to reset that god's constellation tree, refunding
 * every spent god point and unlearning all purchased nodes. God experience, levels, sacrifices,
 * bonus points and minor-transfer bindings are untouched. Unusable inside a vault - the tree
 * snapshot mid-run would desync from the carryover fold.
 */
public class GodNeuralizerItem extends BasicItem {
    private final VaultGod god;

    public GodNeuralizerItem(ResourceLocation id, VaultGod god) {
        super(id, new net.minecraft.world.item.Item.Properties()
                .tab(xyz.iwolfking.woldsvaults.init.ModCreativeTabs.WOLDS_VAULTS).stacksTo(16));
        this.god = god;
    }

    public VaultGod getGod() {
        return this.god;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        if (ServerVaults.get(level).isPresent()) {
            serverPlayer.displayClientMessage(new TextComponent("The gods will not be renounced inside a vault.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        GodAlignmentData data = GodAlignmentData.get(serverPlayer.getServer());
        int spent = data.getSpentPoints(serverPlayer.getUUID(), this.god);
        if (spent <= 0 && data.getPurchasedTreeNodes(serverPlayer.getUUID(), this.god).isEmpty()) {
            serverPlayer.displayClientMessage(new TextComponent("No " + this.god.getName()
                    + " constellation to forget.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        data.refundAll(serverPlayer, this.god);
        AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 0.6F);
        serverPlayer.displayClientMessage(new TextComponent("The " + this.god.getName()
                + " constellation fades from memory - ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(spent + " god points refunded.")
                        .withStyle(Style.EMPTY.withColor(this.god.getChatColor()))), false);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponent("Resets the ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(this.god.getName()).withStyle(Style.EMPTY.withColor(this.god.getChatColor())))
                .append(new TextComponent(" god tree, refunding every spent god point.")
                        .withStyle(ChatFormatting.GRAY)));
        tooltip.add(new TextComponent("God experience, levels and sacrifices are kept.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
