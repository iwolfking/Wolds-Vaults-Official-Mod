package xyz.iwolfking.woldsvaults.integration.vhatcaniroll;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.Modifiers;
import com.radimous.vhatcaniroll.mixin.UniqueGearConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import com.radimous.vhatcaniroll.ui.GearModifierScreen;
import com.radimous.vhatcaniroll.ui.InnerGearScreen;
import com.radimous.vhatcaniroll.ui.ModifierListContainer;
import com.radimous.vhatcaniroll.ui.UniqueGearListContainer;
import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.EtchingConfig;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.etching.EtchingRegistry;
import iskallia.vault.etching.EtchingSet;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class EtchingListContainer extends VerticalScrollClipContainer<UniqueGearListContainer> implements InnerGearScreen {

    public EtchingListContainer(ISpatial spatial) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 9;
        EtchingConfig.EtchingMap etchingMap = ModConfigs.ETCHING.ETCHINGS;
        for(ResourceLocation etchingLoc : etchingMap.keySet()) {
            int iconHeight = labelY;
            labelY += 5;
            LabelElement<?> nameLabel = new LabelElement(Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(etchingMap.get(etchingLoc).getName()), LabelTextStyle.defaultStyle());
            this.addElement(nameLabel);
            labelY += 20;
            ItemStack displayStack = new ItemStack(ModItems.ETCHING);
            AttributeGearData data = AttributeGearData.read(displayStack);
            EtchingSet<?> set = EtchingRegistry.getEtchingSet(etchingLoc);
            if(set != null) {
                data.createOrReplaceAttributeValue(ModGearAttributes.ETCHING, set);
            }
            data.write(displayStack);
            this.addElement(new FakeItemSlotElement(Spatials.positionXY(labelX - 4, iconHeight).width(16).height(16), () -> displayStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY));
            int offset = 5;
            LabelElement<?> mcl = new LabelElement(Spatials.positionXY(labelX + offset, labelY).width(this.innerWidth() - labelX - offset), Spatials.width(this.innerWidth() - labelX * 2 - offset).height(9), new TextComponent(etchingMap.get(etchingLoc).getEffectText()), LabelTextStyle.wrap());
            this.addElement(mcl);
            labelY += Math.max(mcl.getTextStyle().calculateLines(new TextComponent(etchingMap.get(etchingLoc).getEffectText()), mcl.width()) * 10, 10);
            this.addElement(new NineSliceElement(Spatials.positionXY(0, labelY).width(this.innerWidth()).height(3), ScreenTextures.BUTTON_EMPTY));
            labelY += 10;
        }
    }

    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerGearScreen create(ISpatial iSpatial, int i, ModifierCategory modifierCategory, ItemStack itemStack) {
        return new ModifierListContainer(iSpatial, i, modifierCategory, itemStack);
    }

    public InnerGearScreen create(ISpatial spatial) {
        return new EtchingListContainer(spatial);
    }
}
