package xyz.iwolfking.woldsvaults.mixins.hostilenetworks;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import shadows.hostilenetworks.data.CachedModel;
import shadows.hostilenetworks.data.DataModel;
import shadows.hostilenetworks.data.ModelTier;
import shadows.hostilenetworks.item.DataModelItem;
import shadows.hostilenetworks.util.Color;

import java.util.List;

import static shadows.hostilenetworks.item.DataModelItem.getData;
import static shadows.hostilenetworks.item.DataModelItem.getStoredModel;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "hostilenetworks")
        }
)
@Mixin(DataModelItem.class)
public class HostileDataModelMixin {
    /**
     * @author iwolfking
     * @reason Disable adding iterations to data model
     */
    @Overwrite(remap = false)
    public static void setIters(ItemStack stack, int data) {
    }

    /**
     * @author iwolfking
     * @reason Remove data per kill info
     */
    @Overwrite
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> list, TooltipFlag pFlag) {
        if (Screen.hasShiftDown()) {
            CachedModel cModel = new CachedModel(pStack, 0);
            if (cModel == null) {
                list.add(new TranslatableComponent("Error: %s", new TextComponent("Broke_AF")).withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.GRAY));
                return;
            }

            int data = getData(pStack);
            ModelTier tier = ModelTier.getByData(cModel.getModel(), data);
            list.add(new TranslatableComponent("hostilenetworks.info.tier", tier.getComponent()));
            int dProg = data - cModel.getTierData();
            int dMax = cModel.getNextTierData() - cModel.getTierData();
            if (tier != ModelTier.SELF_AWARE) {
                list.add(new TranslatableComponent("hostilenetworks.info.data", new TranslatableComponent("hostilenetworks.info.dprog", dProg, dMax).withStyle(ChatFormatting.GRAY)));
            }

            list.add(new TranslatableComponent("hostilenetworks.info.sim_cost", new Object[]{(new TranslatableComponent("hostilenetworks.info.rft", new Object[]{cModel.getModel().getSimCost()})).withStyle(ChatFormatting.GRAY)}));
        } else {
            list.add(new TranslatableComponent("hostilenetworks.info.hold_shift", Color.withColor("hostilenetworks.color_text.shift", ChatFormatting.WHITE.getColor())).withStyle(ChatFormatting.GRAY));
        }

    }
}
