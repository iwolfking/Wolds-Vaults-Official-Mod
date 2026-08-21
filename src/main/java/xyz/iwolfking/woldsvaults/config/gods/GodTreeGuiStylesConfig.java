package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.SkillStyle;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where one god's nodes are drawn - {@code config/the_vault/gods/god_tree_<god>_gui_styles.json}.
 * Reuses the base mod's {@link SkillStyle}, the same entry the talent and greed style configs
 * store, so free-form node positions, frame type and icon are expressed exactly as the pack's
 * other trees already express them.
 */
public class GodTreeGuiStylesConfig extends Config {
    private String god;

    @Expose private Map<String, SkillStyle> styles;

    public GodTreeGuiStylesConfig(String god) {
        this.god = god;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "god_tree_" + this.god + "_gui_styles";
    }

    @Override
    protected void onLoad(@Nullable Config oldConfigInstance) {
        if (oldConfigInstance instanceof GodTreeGuiStylesConfig previous) {
            this.god = previous.god;
        }
    }

    @Override
    protected void reset() {
        this.styles = new LinkedHashMap<>();
    }

    public Map<String, SkillStyle> getStyles() {
        return this.styles == null ? Collections.emptyMap() : this.styles;
    }
}
