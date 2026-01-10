package xyz.iwolfking.woldsvaults.integration.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.impl.config.ConfigEntry;
import mcp.mobius.waila.impl.config.PluginConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.jade.components.SpeedometerComponent;
import xyz.iwolfking.woldsvaults.integration.jade.components.VaultObjectiveBlocksComponent;

@WailaPlugin
public class WoldsJadePlugin implements IWailaPlugin {
    public static final ResourceLocation INSTANCE = ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "jade_plugin");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        PluginConfig.INSTANCE.addConfig(new ConfigEntry(INSTANCE, true, false));
        registration.registerComponentProvider(SpeedometerComponent.INSTANCE, TooltipPosition.TAIL, Block.class);
        registration.registerComponentProvider(VaultObjectiveBlocksComponent.INSTANCE, TooltipPosition.TAIL, Block.class);
    }
}
