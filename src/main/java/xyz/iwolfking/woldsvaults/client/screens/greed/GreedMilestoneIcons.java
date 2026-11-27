package xyz.iwolfking.woldsvaults.client.screens.greed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.TagParser;
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
 * item id, or to an object with an {@code item} id and an {@code nbt} string for items whose model depends on their tag;
 * keys starting with an underscore are notes). Read once, the first time an icon is asked for; a milestone with no
 * entry, or one naming an item that isn't there, shows paper.
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
        ItemStack icon = icons.get(milestoneId);
        if (icon == null) {
            WoldLogger.errorOnce("greed_milestone_icon_" + milestoneId, "Greed milestone {} has no icon in {}; showing paper", milestoneId, FILE);
            return new ItemStack(Items.PAPER);
        }
        return icon;
    }

    private static Map<String, ItemStack> load() {
        Map<String, ItemStack> loaded = new HashMap<>();
        try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(FILE);
             InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getKey().startsWith("_")) {
                    continue;
                }
                JsonElement value = entry.getValue();
                String itemId;
                String nbt = null;
                if (value.isJsonPrimitive()) {
                    itemId = value.getAsString();
                } else if (value.isJsonObject() && value.getAsJsonObject().has("item")) {
                    itemId = value.getAsJsonObject().get("item").getAsString();
                    nbt = value.getAsJsonObject().has("nbt") ? value.getAsJsonObject().get("nbt").getAsString() : null;
                } else {
                    WoldLogger.error("Greed milestone icon for {} in {} is neither an item id nor an object with an item; skipped", entry.getKey(), FILE);
                    continue;
                }
                ResourceLocation id = ResourceLocation.tryParse(itemId);
                Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
                if (item == null || item == Items.AIR) {
                    WoldLogger.error("Greed milestone icon for {} names unknown item {} in {}", entry.getKey(), itemId, FILE);
                    continue;
                }
                ItemStack stack = new ItemStack(item);
                if (nbt != null) {
                    try {
                        stack.setTag(TagParser.parseTag(nbt));
                    } catch (CommandSyntaxException e) {
                        WoldLogger.error("Greed milestone icon for {} has unreadable nbt {} in {}; showing the item without it", entry.getKey(), nbt, FILE);
                    }
                }
                loaded.put(entry.getKey(), stack);
            }
        } catch (Exception e) {
            WoldLogger.error("Greed milestone icons could not be read from " + FILE, e);
        }
        return loaded;
    }
}
