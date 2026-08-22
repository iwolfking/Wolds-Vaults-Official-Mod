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
 * Where one god's nodes are drawn - {@code config/the_vault/gods/god_tree_<god>_gui_styles.json}.
 * Reuses the base mod's {@link SkillStyle}, the same entry the talent and greed style configs
 * store, so free-form node positions, frame type and icon are expressed exactly as the pack's
 * other trees already express them.
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

    /**
     * A style file with no entries draws every node of the tree stacked at the origin, which reads
     * as a rendering bug rather than a config mistake. Reporting it here and falling back to the
     * shipped layout keeps the tree legible while naming the file that needs fixing.
     */
    @Override
    protected boolean isValid() {
        if (this.styles == null || this.styles.isEmpty()) {
            WoldsVaults.LOGGER.error("God tree style config {} defines no node styles. Falling back to the shipped "
                    + "layout.", this.getName());
            return false;
        }
        return true;
    }

    /**
     * Restores the shipped layout rather than an empty one, for the same reason
     * {@link GodTreeConfig#reset()} does: an empty reset is written straight back over the file,
     * and a tree with no styles draws every node stacked at the origin.
     */
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
