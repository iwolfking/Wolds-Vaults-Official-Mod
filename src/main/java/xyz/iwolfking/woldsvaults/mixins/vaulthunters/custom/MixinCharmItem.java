package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.charm.CharmEffect;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.reader.DecimalModifierReader;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.gear.CharmItem;
import iskallia.vault.item.gear.RecyclableItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

@Mixin(value = CharmItem.class, remap = false)
public abstract class MixinCharmItem extends BasicItem implements ICurioItem, DataTransferItem, IdentifiableItem, RecyclableItem {
    public MixinCharmItem(ResourceLocation id) {
        super(id);
    }

    @Shadow
    public static float getValue(ItemStack stack) {
        return 0;
    }


    /**
     * @author iwolfking
     * @reason Remove curio slot and uses text.
     */
    @Overwrite
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        AttributeGearData data = AttributeGearData.read(stack);
        data.getFirstValue(ModGearAttributes.CHARM_EFFECT).ifPresent((charmEffect) -> {
            DecimalModifierReader.Percentage<?> percentage = (DecimalModifierReader.Percentage) ((CharmEffect.Config) charmEffect.getCharmConfig().getConfig()).getAttribute().getReader();
            int var10003 = Math.round(getValue(stack) * 100.0F);
            tooltip.add((new TextComponent(var10003 + "% " + percentage.getModifierName())).setStyle(Style.EMPTY.withColor(percentage.getRgbColor())));
            tooltip.add((new TextComponent("Size: ")).append(new TextComponent(String.valueOf(10))));
            tooltip.add(TextComponent.EMPTY);
        });
    }


    /**
     * @author iwolfking
     * @reason Prevent equipping old charms.
     */
    @Overwrite
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return false;
    }
}
