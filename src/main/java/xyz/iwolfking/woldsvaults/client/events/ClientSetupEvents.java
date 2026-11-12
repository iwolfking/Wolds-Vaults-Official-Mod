package xyz.iwolfking.woldsvaults.client.events;

import xyz.iwolfking.woldsvaults.client.spellcraft.GlyphAffixLine;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipClientComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;
import xyz.iwolfking.woldsvaults.client.init.ModEntityRenderers;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;
import xyz.iwolfking.woldsvaults.client.init.ModScreens;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftTooltipRow;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModVaultarHudScreenRegistry;
import xyz.iwolfking.woldsvaults.items.alchemy.DecoPotionItem;
import xyz.iwolfking.woldsvaults.client.init.ExtractorIcons;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.items.MobileFluidTankItem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientSetupEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void setupClient(FMLClientSetupEvent event) {
        ModScreens.register();
        ModEntityRenderers.register(event);
        ModKeybinds.registerKeyBinds();
        ModVaultarHudScreenRegistry.init();
        registerItemProperties(event);
        registerClientTooltips();
    }


    @OnlyIn(Dist.CLIENT)
    private static void registerItemProperties(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.EXTRACTOR, WoldsVaults.id("deployed"),
                    (pStack, pLevel, pEntity, pSeed) -> ExtractorItem.isDeployed(pStack) ? 1.0F : 0.0F);
            ExtractorIcons.register();
            ItemProperties.register(ModBlocks.MOBILE_FLUID_TANK.asItem(), WoldsVaults.id("fill"),
                    (pStack, pLevel, pEntity, pSeed) -> MobileFluidTankItem.fillLevel(
                            MobileFluidTankItem.getFluid(pStack).getAmount()));
            ItemProperties.register(ModItems.DECO_POTION, WoldsVaults.id("potion_variant"),
                    (pStack, pLevel, pEntity, pSeed) ->  {
                        if (!(pStack.getItem() instanceof DecoPotionItem item)) return 0.0f;
                        return switch (item.getType()) {
                            case ONE_INGREDIENT -> 0.1F;
                            case TWO_INGREDIENT -> 0.2F;
                            case THREE_INGREDIENT -> 0.3F;
                            case BREWING -> 0.4F;
                            case UNKNOWN -> 0.0F;
                        };
                    }
            );
        });
    }

    private static void registerClientTooltips() {
        MinecraftForgeClient.registerTooltipComponentFactory(LayoutTooltipComponent.class, LayoutTooltipClientComponent::new);
        MinecraftForgeClient.registerTooltipComponentFactory(SpellcraftTooltipRow.class, SpellcraftTooltipRow.Client::new);
        MinecraftForgeClient.registerTooltipComponentFactory(GlyphAffixLine.class, GlyphAffixLine.Client::new);
    }
}