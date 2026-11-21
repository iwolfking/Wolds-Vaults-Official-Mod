package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.AlchemyArchiveTileEntity;
import iskallia.vault.item.bottle.BottleItem;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.UUID;

//TODO: I don't think this is needed any more?
@Mixin(value = AlchemyArchiveTileEntity.class, remap = false)
public abstract class MixinAlchemyArchive {
//    @Shadow
//    public abstract boolean setUsedByPlayer(Player player);
//
//    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendMessage(Lnet/minecraft/network/chat/Component;Ljava/util/UUID;)V"), remap = true)
//    private void fillPlayersBrew(ServerPlayer instance, Component pComponent, UUID pSenderUUID) {
//        ItemStack bottle = InventoryUtil.getFirst(instance.getInventory().items, stack -> stack.getItem() instanceof BottleItem).orElse(null);
//        if(bottle != null) {
//            if(bottle.getItem() instanceof BottleItem) {
//                BottleItem.addCharges(bottle, 10);
//                instance.sendMessage((new TranslatableComponent("message.woldsvaults.filled_bottle_alchemy_archive", bottle.getDisplayName())).withStyle(ChatFormatting.GREEN), Util.NIL_UUID);
//                this.setUsedByPlayer(instance);
//            }
//        }
//    }
}
