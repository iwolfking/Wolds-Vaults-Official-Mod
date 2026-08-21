package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.init.ModAttributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.RangedAttributeAccessor;

/**
 * Widens base attribute registration bounds that the rework's stacking sources can realistically
 * hit. The base mana-max attribute registers with a hard ceiling of 4096; every read is clamped
 * through it, so god-tree mana scaling and mythic charm mana rolls would silently stop there. A
 * million is far past anything reachable while staying well inside exact float integer range.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModAttributeBounds {
    private static final double MANA_MAX_CEILING = 1_000_000.0D;

    private ModAttributeBounds() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModAttributes.MANA_MAX instanceof RangedAttribute ranged) {
                ((RangedAttributeAccessor) ranged).woldsVaults$setMaxValue(MANA_MAX_CEILING);
            } else {
                WoldsVaults.LOGGER.error("MANA_MAX is not a RangedAttribute; the mana ceiling stays at the base bound.");
            }
        });
    }
}
