package xyz.iwolfking.woldsvaults.client.init;

import iskallia.vault.client.util.color.ColorUtil;
import iskallia.vault.config.gear.VaultGearTypeConfig;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.init.ModConfigs;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.WorldHelper;
import xyz.iwolfking.woldsvaults.blocks.models.MonolithControllerModel;
import xyz.iwolfking.woldsvaults.entities.astral.client.model.LoginarModel;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.Optional;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModModels {
    public static void setupRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SURVIVAL_MOB_BARRIER, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ISKALLIAN_LEAVES_BLOCK, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GATEWAY_CHANNELING_BLOCK, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BREWING_ALTAR, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DOLL_DISMANTLING_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRATE_CRACKER_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DECO_MONOLITH_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRAVEYARD_LOOT_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TIME_TRIAL_TROPHY_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRINKET_FUSION_BLOCK, RenderType.translucent());
    }


    @SubscribeEvent
    public static void registerBlockColorHandles(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, blockDisplayReader, pos, tintIndex) -> {
            if(tintIndex < 0 || tintIndex > 1 || pos == null) {
                return -1;
            }
            return WorldHelper.getBlockEntity(blockDisplayReader, pos, BackpackBlockEntity.class).map(te -> tintIndex == 0 ? te.getBackpackWrapper().getMainColor() : te.getBackpackWrapper().getAccentColor()).orElse(getDefaultColor(tintIndex));
        }, ModBlocks.XL_BACKPACK);
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        event.getItemColors().register((backpack, layer) -> {
            if(layer > 1 || !(backpack.getItem() instanceof BackpackItem)) {
                return -1;
            }
            return backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).map(backpackWrapper -> {
                if(layer == 0) {
                    return backpackWrapper.getMainColor();
                }
                else if(layer == 1) {
                    return backpackWrapper.getAccentColor();
                }
                return -1;
            }).orElse(BackpackWrapper.DEFAULT_CLOTH_COLOR);
        }, ModItems.XL_BACKPACK);


        event.getItemColors().register((stack, layer) -> {
            if (layer == 1) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("PotionColor", Tag.TAG_INT)) {
                    return tag.getInt("PotionColor");
                }
            }
            return -1;
        }, ModItems.DECO_POTION);

        event.getItemColors().register((stack, layer) -> {
            if (layer > 0) {
                GearDataCache cache = GearDataCache.of(stack);
                if (cache.getState() == VaultGearState.UNIDENTIFIED) {
                    VaultGearTypeConfig.RollType rollType = Optional.ofNullable(cache.getGearRollType())
                            .flatMap(ModConfigs.VAULT_GEAR_TYPE_CONFIG::getRollPool).orElse(null);
                    if (rollType != null) {
                        return ColorUtil.blendColors(rollType.getColor(), -1, 0.8F);
                    }
                }
                return -1;
            }
            return VaultGearHelper.getGearColor(stack);
        }, ModItems.MYTHIC_VAULT_CHARM);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MonolithControllerModel.MODEL_LOCATION, MonolithControllerModel::createBodyLayer);
        event.registerLayerDefinition(LoginarModel.LAYER_LOCATION, LoginarModel::createBodyLayer);
    }

    public static int getDefaultColor(int tintIndex) {
        return tintIndex == 0 ? BackpackWrapper.DEFAULT_CLOTH_COLOR : BackpackWrapper.DEFAULT_BORDER_COLOR;
    }
}
