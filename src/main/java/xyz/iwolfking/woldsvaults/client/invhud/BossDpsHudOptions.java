package xyz.iwolfking.woldsvaults.client.invhud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.core.data.adapter.IJsonAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BossDpsHudOptions {
    private HudPosition hudPosition;
    private float size;
    private boolean enabled;
    private boolean detailed;
    private boolean showSources;
    public static final BossDpsHudOptions.Adapter ADAPTER = new BossDpsHudOptions.Adapter();

    private BossDpsHudOptions(HudPosition hudPosition, float size, boolean enabled, boolean detailed, boolean showSources) {
        this.hudPosition = hudPosition;
        this.size = size;
        this.enabled = enabled;
        this.detailed = detailed;
        this.showSources = showSources;
    }

    public static BossDpsHudOptions createDefault(HudPosition hudPosition) {
        return new BossDpsHudOptions(hudPosition, 1.0F, true, true, true);
    }

    public HudPosition getHudPosition() {
        return this.hudPosition;
    }

    public BossDpsHudOptions setHudPosition(HudPosition hudPosition) {
        this.hudPosition = hudPosition;
        return this;
    }

    public float getSize() {
        return this.size;
    }

    public BossDpsHudOptions setSize(float size) {
        this.size = size;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public BossDpsHudOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isDetailed() {
        return this.detailed;
    }

    public BossDpsHudOptions setDetailed(boolean detailed) {
        this.detailed = detailed;
        return this;
    }

    public boolean isShowSources() {
        return this.showSources;
    }

    public BossDpsHudOptions setShowSources(boolean showSources) {
        this.showSources = showSources;
        return this;
    }

    public static class Adapter implements IJsonAdapter<BossDpsHudOptions, JsonElement, Object> {
        public Optional<JsonElement> writeJson(@Nullable BossDpsHudOptions value, Object context) {
            if (value == null) {
                return Optional.empty();
            }
            JsonObject json = new JsonObject();
            HudPosition.ADAPTER.writeJson(value.hudPosition, context).ifPresent(pos -> json.add("hudPosition", pos));
            json.addProperty("size", value.size);
            json.addProperty("enabled", value.enabled);
            json.addProperty("detailed", value.detailed);
            json.addProperty("showSources", value.showSources);
            return Optional.of(json);
        }

        public Optional<BossDpsHudOptions> readJson(@Nullable JsonElement json, Object context) {
            if (json == null || !json.isJsonObject()) {
                return Optional.empty();
            }
            JsonObject obj = json.getAsJsonObject();
            HudPosition hudPosition = HudPosition.ADAPTER.readJson(obj.get("hudPosition"), context)
                    .orElse(HudPosition.fromPixels((x, y) -> 0, (x, y) -> 0));
            float size = obj.has("size") ? obj.get("size").getAsFloat() : 1.0F;
            boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();
            boolean detailed = !obj.has("detailed") || obj.get("detailed").getAsBoolean();
            boolean showSources = !obj.has("showSources") || obj.get("showSources").getAsBoolean();
            return Optional.of(new BossDpsHudOptions(hudPosition, size, enabled, detailed, showSources));
        }
    }
}
