package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.gear.trinket.TrinketEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.init.*;
import xyz.iwolfking.woldsvaults.lib.CustomScaleTypes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEvents {
    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        ModBlocks.registerBlocks(event);
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        ModBlocks.registerBlockItems(event);
    }

    @SubscribeEvent
    public static void onTileEntityRegister(RegistryEvent.Register<BlockEntityType<?>> event) {
        ModBlocks.registerTileEntities(event);
    }



    @SubscribeEvent
    public static void onContainerRegister(RegistryEvent.Register<MenuType<?>> event) {
        ModContainers.register(event);
    }

    @SubscribeEvent
    public static void onTrinketRegistry(RegistryEvent.Register<TrinketEffect<?>> event) {
        ModTrinkets.init(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModBlocks.registerTileEntityRenderers(event);
    }



    @SubscribeEvent
    public static void onEffectRegister(RegistryEvent.Register<MobEffect> event) {
        CustomScaleTypes.init();
        ModEffects.register(event);

    }

    @SubscribeEvent
    public static void onEntityRegister(RegistryEvent.Register<EntityType<?>> event) {
        ModEntities.register(event);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        xyz.iwolfking.woldsvaults.init.client.ModModels.setupRenderLayers();
    }

    @SubscribeEvent
    public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
        ModSounds.registerSounds(event);
    }

    @SubscribeEvent
    public static void onModificationRegistry(RegistryEvent.Register<GearModification> event) {
        ModGearModifications.init(event);
    }

}
