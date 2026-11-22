package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The item each greed milestone shows as its icon, from assets/woldsvaults/greed/milestone_icons.json (milestone id to
 * item id; keys starting with an underscore are notes). Read once, the first time an icon is asked for; a milestone with
 * no entry, or one naming an item that isn't there, shows paper.
 */
public final class GreedMilestoneIcons {
    private static final ResourceLocation FILE = WoldsVaults.id("greed/milestone_icons.json");
    private static Map<String, ItemStack> icons;

    private GreedMilestoneIcons() {
    }

    public static ItemStack icon(String milestoneId) {
        if (icons == null) {
            icons = load();
        }
        return icons.getOrDefault(milestoneId, new ItemStack(Items.PAPER));
    }

    private static Map<String, ItemStack> load() {
        Map<String, ItemStack> loaded = new HashMap<>();
        try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(FILE);
             InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getKey().startsWith("_") || !entry.getValue().isJsonPrimitive()) {
                    continue;
                }
                ResourceLocation id = ResourceLocation.tryParse(entry.getValue().getAsString());
                Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
                if (item != null && item != Items.AIR) {
                    loaded.put(entry.getKey(), new ItemStack(item));
                }
            }
        } catch (Exception e) {
            WoldLogger.error("Greed milestone icons could not be read from " + FILE, e);
        }
        return loaded;
    }
}
