package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.entry.SkillStyle;

public class GodNodeStyle extends SkillStyle {
    @Expose public int size;

    public GodNodeStyle() {
        super(0, 0, null);
    }
}
