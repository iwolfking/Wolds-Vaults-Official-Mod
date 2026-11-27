package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.SkillStyle;
import xyz.iwolfking.woldsvaults.config.WoldsTable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Where one god's nodes are drawn, generated as {@code vault_configs/gods/styles/<god>.json}:
 * position, frame type and icon per node id, as the base mod's {@link SkillStyle}, plus an optional size.
 */
public class GodTreeGuiStylesConfig extends Config implements WoldsTable {
    @Expose private Map<String, GodNodeStyle> styles;

    @Override
    public String getName() {
        return "gods" + File.separator + "styles";
    }

    @Nullable
    @Override
    public String validate() {
        return this.styles == null || this.styles.isEmpty() ? "it defines no node styles" : null;
    }

    @Override
    protected void reset() {
    }

    public Map<String, GodNodeStyle> getStyles() {
        return this.styles == null ? Collections.emptyMap() : this.styles;
    }
}
