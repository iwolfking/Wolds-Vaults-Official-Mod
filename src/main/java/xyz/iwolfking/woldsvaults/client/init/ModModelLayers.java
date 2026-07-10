package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.models.treasure.LockModel;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModModelLayers {
    public static final ModelLayerLocation LOCK = new ModelLayerLocation(WoldsVaults.id("treasure_lock"), "main");

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(ModModelLayers.LOCK, LockModel::createBodyLayer);
    }
}
