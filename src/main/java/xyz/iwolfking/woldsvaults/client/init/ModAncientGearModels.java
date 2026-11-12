package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The ember-toned unidentified sprites shown for gear that dropped already flagged ancient. One special model per
 * gear item that has an ancient unique, keyed by the item's registry path; a gear type without one keeps the normal
 * unidentified sprite and logs once.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModAncientGearModels {
    private static final List<String> GEAR_TYPES = List.of("sword", "axe", "helmet", "chestplate", "leggings", "boots", "shield", "focus", "wand", "magnet", "jewel", "battlestaff", "rang", "trident", "plushie");
    private static final Set<ResourceLocation> LOGGED_MISSING = new HashSet<>();

    private ModAncientGearModels() {
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (String type : GEAR_TYPES) {
            ForgeModelBakery.addSpecialModel(modelId(type));
        }
    }

    /** The ancient unidentified model for the item, or null (logged once per item) if no sprite exists for it. */
    @Nullable
    public static BakedModel get(Item item) {
        ResourceLocation id = item.getRegistryName();
        if (id == null || !GEAR_TYPES.contains(id.getPath())) {
            if (id != null && LOGGED_MISSING.add(id)) {
                WoldsVaults.LOGGER.error("No unidentified-ancient sprite exists for gear item {}; it keeps the normal unidentified sprite.", id);
            }
            return null;
        }
        return Minecraft.getInstance().getModelManager().getModel(modelId(id.getPath()));
    }

    private static ResourceLocation modelId(String type) {
        return WoldsVaults.id("item/unidentified_ancient/" + type);
    }
}
