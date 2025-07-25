package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.block.entity.ShopPedestalBlockTile;
import iskallia.vault.block.render.ShopPedestalBlockTileRenderer;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModEtchings;

@Mixin(value = ShopPedestalBlockTileRenderer.class, remap = false)
public abstract class MixinShopPedestalBlockTileRenderer implements BlockEntityRenderer<ShopPedestalBlockTile> {
}
