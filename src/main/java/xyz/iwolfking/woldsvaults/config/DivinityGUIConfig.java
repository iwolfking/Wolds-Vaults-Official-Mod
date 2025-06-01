package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.SkillStyle;

import java.util.HashMap;

public class DivinityGUIConfig extends Config {
    @Expose
    private HashMap<String, SkillStyle> styles;

    public DivinityGUIConfig() {
    }

    public String getName() {
        return "divinity_gui_styles";
    }

    public HashMap<String, SkillStyle> getStyles() {
        return this.styles;
    }

    protected void reset() {
        this.styles = new HashMap<>();
    }

}
