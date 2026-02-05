package xyz.iwolfking.woldsvaults.client.invhud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.core.data.adapter.IJsonAdapter;
import iskallia.vault.util.Alignment.Horizontal;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LightmanWalletHudOptions {
    private HudPosition hudPosition;
    private float size;
    private boolean showWalletIcon;
    private boolean enabled;
    private Alignment alignment;
    private DisplayMode displayMode;
    private int itemGap;
    public static final LightmanWalletHudOptions.Adapter ADAPTER = new LightmanWalletHudOptions.Adapter();

    private LightmanWalletHudOptions(
        HudPosition hudPosition, float size, boolean enabled, boolean showWalletIcon, Alignment alignment, DisplayMode displayMode, int itemGap
    ) {
        this.hudPosition = hudPosition;
        this.size = size;
        this.enabled = enabled;
        this.showWalletIcon = showWalletIcon;
        this.alignment = alignment;
        this.displayMode = displayMode;
        this.itemGap = itemGap;
    }

    public static LightmanWalletHudOptions createDefault(HudPosition hudPosition) {
        return new LightmanWalletHudOptions(hudPosition, 1f, true, true, Alignment.RIGHT, DisplayMode.ITEMS, 17);
    }

    //<editor-fold desc="Getters/Setters" >
    public HudPosition getHudPosition() {
        return this.hudPosition;
    }

    public LightmanWalletHudOptions setHudPosition(HudPosition hudPosition) {
        this.hudPosition = hudPosition;
        return this;
    }

    public float getSize() {
        return this.size;
    }

    public LightmanWalletHudOptions setSize(float size) {
        this.size = size;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getShowWalletIcon() {
        return this.showWalletIcon;
    }

    public LightmanWalletHudOptions setShowWalletIcon(boolean showWalletIcon) {
        this.showWalletIcon = showWalletIcon;
        return this;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public LightmanWalletHudOptions setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public LightmanWalletHudOptions setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        return this;
    }

    public int getItemGap() {
        return this.itemGap;
    }

    public LightmanWalletHudOptions setItemGap(int gap) {
        this.itemGap = gap;
        return this;
    }

    //</editor-fold>

    public static class Adapter implements IJsonAdapter<LightmanWalletHudOptions, JsonElement, Object> {
        public Optional<JsonElement> writeJson(@Nullable LightmanWalletHudOptions value, Object context) {
            JsonObject json = new JsonObject();
            if (value == null) {
                return Optional.empty();
            } else {
                HudPosition.ADAPTER.writeJson(value.hudPosition, context).ifPresent((pos) -> json.add("hudPosition", pos));
                json.addProperty("size", value.size);
                json.addProperty("enabled", value.enabled);
                json.addProperty("showWalletIcon", value.showWalletIcon);
                json.addProperty("alignment", value.alignment.serializedName());
                json.addProperty("displayMode", value.displayMode.serializedName());
                json.addProperty("itemGap", value.itemGap);
                return Optional.of(json);
            }
        }

        public Optional<LightmanWalletHudOptions> readJson(@Nullable JsonElement json, Object context) {
            if (json != null && json.isJsonObject()) {
                JsonObject jsonObj = json.getAsJsonObject();
                HudPosition hudPosition = HudPosition.ADAPTER.readJson(jsonObj.get("hudPosition"), context).orElse(HudPosition.fromPixels((x, y) -> 0, (x, y) -> 0));
                float size = jsonObj.get("size").getAsFloat();
                boolean enabled = jsonObj.get("enabled").getAsBoolean();
                boolean showWalletIcon = jsonObj.get("showWalletIcon").getAsBoolean();
                Alignment alignment = Alignment.fromString(jsonObj.get("alignment").getAsString());
                DisplayMode displayMode = DisplayMode.fromString(jsonObj.get("displayMode").getAsString());
                int itemGap = jsonObj.get("itemGap").getAsInt();
                return Optional.of(new LightmanWalletHudOptions(hudPosition, size, enabled, showWalletIcon, alignment, displayMode, itemGap));
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

    public enum Alignment {
        LEFT,
        RIGHT;

        public String serializedName() {
            return this.name().toLowerCase();
        }

        public static Alignment fromString(String name) {
            for(Alignment mode : values()) {
                if (mode.serializedName().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return RIGHT;
        }

        public Alignment next() {
            Alignment[] values = values();
            int index = (this.ordinal() + 1) % values.length;
            return values[index];
        }

        public iskallia.vault.util.Alignment toVanilla() {
            return new iskallia.vault.util.Alignment(
                this == LEFT ? Horizontal.LEFT : Horizontal.RIGHT,
                iskallia.vault.util.Alignment.Vertical.MIDDLE
            );
        }
    }
}
