package xyz.iwolfking.woldsvaults.client.invhud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.core.data.adapter.IJsonAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LightmanWalletHudOptions {
    private HudPosition hudPosition;
    private float size = 0.5F;
    private boolean showWalletIcon;
    private boolean enabled;
    private int itemGap;
    private DisplayMode displayMode;
    public static final LightmanWalletHudOptions.Adapter ADAPTER = new LightmanWalletHudOptions.Adapter();

    private LightmanWalletHudOptions(HudPosition position) {
        this.showWalletIcon = true;
        this.enabled = true;
        this.hudPosition = position;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getItemGap() {
        return this.itemGap;
    }


    public static LightmanWalletHudOptions create(HudPosition hudPosition, float size, boolean showWalletIcon, int itemGap, DisplayMode displayMode, boolean enabled) {
        LightmanWalletHudOptions options = new LightmanWalletHudOptions(hudPosition);
        options.showWalletIcon = showWalletIcon;
        options.size = size;
        options.itemGap = itemGap;
        options.displayMode = displayMode;
        options.enabled = enabled;
        return options;
    }

    public static LightmanWalletHudOptions createDefault(HudPosition hudPosition) {
        return create(hudPosition, 0.5f, true, 17, DisplayMode.ITEMS, true);
    }

    public HudPosition getHudPosition() {
        return this.hudPosition;
    }

    public boolean shouldShowWalletIcon() {
        return this.showWalletIcon;
    }

    public float getSize() {
        return this.size;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }


    public LightmanWalletHudOptions setHudPosition(HudPosition hudPosition) {
        this.hudPosition = hudPosition;
        return this;
    }

    public LightmanWalletHudOptions setSize(float size) {
        this.size = size;
        return this;
    }


    public LightmanWalletHudOptions setShowWalletIcon(boolean showWalletIcon) {
        this.showWalletIcon = showWalletIcon;
        return this;
    }


    public LightmanWalletHudOptions setItemGap(int gap) {
        this.itemGap = gap;
        return this;
    }

    public LightmanWalletHudOptions setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        return this;
    }


    public static class Adapter implements IJsonAdapter<LightmanWalletHudOptions, JsonElement, Object> {
        public Optional<JsonElement> writeJson(@Nullable LightmanWalletHudOptions value, Object context) {
            JsonObject json = new JsonObject();
            if (value == null) {
                return Optional.empty();
            } else {
                HudPosition.ADAPTER.writeJson(value.hudPosition, context).ifPresent((pos) -> json.add("hudPosition", pos));
                json.addProperty("size", value.size);
                json.addProperty("showWalletIcon", value.showWalletIcon);
                json.addProperty("itemGap", value.itemGap);
                json.addProperty("displayMode", value.displayMode.serializedName());
                json.addProperty("enabled", value.enabled);
                return Optional.of(json);
            }
        }

        public Optional<LightmanWalletHudOptions> readJson(@Nullable JsonElement json, Object context) {
            if (json != null && json.isJsonObject()) {
                JsonObject jsonObj = json.getAsJsonObject();
                HudPosition hudPosition = HudPosition.ADAPTER.readJson(jsonObj.get("hudPosition"), context).orElse(HudPosition.fromPixels((x, y) -> 0, (x, y) -> 0));
                float size = jsonObj.get("size").getAsFloat();
                boolean showWalletIcon = jsonObj.get("showWalletIcon").getAsBoolean();
                int itemGap = jsonObj.get("itemGap").getAsInt();
                DisplayMode displayMode = DisplayMode.fromString(jsonObj.get("displayMode").getAsString());
                boolean enabled = jsonObj.get("enabled").getAsBoolean();
                return Optional.of(LightmanWalletHudOptions.create(hudPosition, size, showWalletIcon, itemGap, displayMode, enabled));
            } else {
                return Optional.empty();
            }
        }
    }

    public enum DisplayMode {
        ITEMS,
        TEXT;

        public String serializedName() {
            return this.name().toLowerCase();
        }

        public static DisplayMode fromString(String name) {
            for(DisplayMode mode : values()) {
                if (mode.serializedName().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return ITEMS;
        }

        public DisplayMode next() {
            DisplayMode[] values = values();
            int index = (this.ordinal() + 1) % values.length;
            return values[index];
        }
    }

}
