package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.SkillStyle;
import xyz.iwolfking.woldsvaults.config.PackAuthoredConfig;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where one god's nodes are drawn - {@code config/the_vault/gods/god_tree_<god>_gui_styles.json}:
 * position, frame type and icon per node id, as the base mod's {@link SkillStyle}.
 */
public class GodTreeGuiStylesConfig extends PackAuthoredConfig {
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

    /** Refuses a file with no node styles, falling back to the shipped layout. */
    @Override
    protected boolean isValid() {
        if (this.styles == null || this.styles.isEmpty()) {
            WoldsVaults.LOGGER.error("God tree style config {} defines no node styles. Falling back to the shipped "
                    + "layout.", this.getName());
            return false;
        }
        return true;
    }

    /** Restores the shipped layout from {@link GodTreeDefaults} rather than an empty one. */
    @Override
    protected void reset() {
        this.styles = new LinkedHashMap<>();
        GodTreeBuilder shipped = GodTreeDefaults.forGod(this.god);
        if (shipped == null) {
            WoldsVaults.LOGGER.error("No shipped god tree layout for '{}'; regenerating it empty.", this.god);
            return;
        }
        GodTreeGuiStylesConfig defaults = this.getGson().fromJson(shipped.buildStyles(), GodTreeGuiStylesConfig.class);
        this.styles = defaults.styles;
    }

    public Map<String, SkillStyle> getStyles() {
        return this.styles == null ? Collections.emptyMap() : this.styles;
    }
}
