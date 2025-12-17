package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.gui.screen.UniqueCodexScreen;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = UniqueCodexScreen.class, remap = false)
public class MixinUniqueCodexScreen extends AbstractElementScreen {

    public MixinUniqueCodexScreen(Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementScreen> tooltipRendererFactory) {
        super(title, elementRenderer, tooltipRendererFactory);
    }
//
//    /**
//     * @author iwolfking
//     * @reason yeah yeah whatever I do what I want
//     */
//    @Overwrite
//    private ItemStack getItemStackForPage(ResourceLocation uniqueId) {
//        Optional<UniqueGearConfig.Entry> location = ModConfigs.UNIQUE_GEAR.getEntry(uniqueId);
//        new ItemStack(ModItems.SWORD);
//        if (location.isPresent()) {
//            if (((UniqueGearConfig.Entry) location.get()).getModels() == null) {
//                ItemStack var5 = new ItemStack(ModItems.JEWEL);
//                VaultGearData data = VaultGearData.read(var5);
//                data.setRarity(VaultGearRarity.UNIQUE);
//                data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, ((UniqueGearConfig.Entry) location.get()).getId().toString());
//                data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueId);
//                data.setItemLevel(VaultBarOverlay.vaultLevel);
//                data.write(var5);
//                GearRollHelper.initializeGear(var5, this.getMinecraft().player);
//                return var5;
//            } else {
//                ItemStack stack;
//                if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("sword")) {
//                    stack = new ItemStack(ModItems.SWORD);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("shield")) {
//                    stack = new ItemStack(ModItems.SHIELD);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("wand")) {
//                    stack = new ItemStack(ModItems.WAND);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("focus")) {
//                    stack = new ItemStack(ModItems.FOCUS);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("helmet")) {
//                    stack = new ItemStack(ModItems.HELMET);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("chestplate")) {
//                    stack = new ItemStack(ModItems.CHESTPLATE);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("leggings")) {
//                    stack = new ItemStack(ModItems.LEGGINGS);
//                } else if (((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("boots")) {
//                    stack = new ItemStack(ModItems.BOOTS);
//                }
//                else if(location.get().getModel().toString().contains("battlestaff")) {
//                    stack = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF);
//                }
//                else if(location.get().getModel().toString().contains("magnet")) {
//                    stack = new ItemStack(ModItems.MAGNET);
//                }
//                else if(location.get().getModel().toString().contains("trident")) {
//                    stack = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT);
//                }
//                else if(location.get().getModel().toString().contains("rang")) {
//                    stack = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.RANG);
//                }
//                else if(location.get().getModel().toString().contains("plushie")) {
//                    stack = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE);
//                }
//                else if(location.get().getModel().toString().contains("loot_sack")) {
//                    stack = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.LOOT_SACK);
//                }
//                else if(location.get().getModel().toString().contains("jewel")) {
//                    stack = new ItemStack(ModItems.JEWEL);
//                }
//                else {
//                    if (!((UniqueGearConfig.Entry) location.get()).getModel().toString().contains("axe")) {
//                        return new ItemStack(ModItems.ERROR_ITEM);
//                    }
//
//                    stack = new ItemStack(ModItems.AXE);
//                }
//
//                VaultGearData data = VaultGearData.read(stack);
//                data.setRarity(VaultGearRarity.UNIQUE);
//                data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, ((UniqueGearConfig.Entry) location.get()).getId().toString());
//                data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueId);
//                data.setItemLevel(VaultBarOverlay.vaultLevel);
//                data.write(stack);
//                GearRollHelper.initializeGear(stack, this.getMinecraft().player);
//                return stack;
//            }
//        } else {
//            return new ItemStack(ModItems.ERROR_ITEM);
//        }
//    }
//
//    private ItemStack getItemStackForPage(ResourceLocation uniqueId) {
//        Optional<UniqueGearConfig.Entry> location = ModConfigs.UNIQUE_GEAR.getEntry(uniqueId);
//        ItemStack stack = new ItemStack(ModItems.SWORD);
//        if (location.isPresent()) {
//            if (((UniqueGearConfig.Entry)location.get()).getModels() == null) {
//                stack = new ItemStack(ModItems.JEWEL);
//                VaultGearData data = VaultGearData.read(stack);
//                data.setRarity(VaultGearRarity.UNIQUE);
//                data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, ((UniqueGearConfig.Entry)location.get()).getId().toString());
//                data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueId);
//                data.setItemLevel(VaultBarOverlay.vaultLevel);
//                data.write(stack);
//                GearRollHelper.initializeGear(stack, this.getMinecraft().player);
//                return stack;
//            } else {
//                ResourceLocation model = (ResourceLocation)((UniqueGearConfig.Entry)location.get()).getModels().getRandom().orElse((Object)null);
//                if (model == null) {
//                    return new ItemStack(ModItems.ERROR_ITEM);
//                } else {
//                    Optional<Pair<? extends DynamicModel<?>, Item>> item = ModDynamicModels.REGISTRIES.getModelAndAssociatedItem(model);
//                    if (item.isPresent()) {
//                        stack = new ItemStack((ItemLike)((Pair)item.get()).getSecond());
//                    }
//
//                    VaultGearData data = VaultGearData.read(stack);
//                    data.setRarity(VaultGearRarity.UNIQUE);
//                    data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, ((UniqueGearConfig.Entry)location.get()).getId().toString());
//                    data.createOrReplaceAttributeValue(ModGearAttributes.UNIQUE_ITEM_KEY, uniqueId);
//                    data.setItemLevel(VaultBarOverlay.vaultLevel);
//                    data.write(stack);
//                    GearRollHelper.initializeGear(stack, this.getMinecraft().player);
//                    return stack;
//                }
//            }
//        } else {
//            return new ItemStack(ModItems.ERROR_ITEM);
//        }
//    }
}
