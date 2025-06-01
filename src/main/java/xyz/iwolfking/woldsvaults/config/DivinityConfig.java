package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.skill.tree.AbilityTree;
import xyz.iwolfking.woldsvaults.divinities.ExpertisePointIncreaseDivinity;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;

public class DivinityConfig extends Config {
    @Expose
    public DivinityTree tree;

    public DivinityConfig() {
    }

    public String getName() {
        return "divinity";
    }

    public DivinityTree getAll() {
        return this.tree == null ? new DivinityTree() : this.tree;
    }

    protected boolean isValid() {
        return this.tree != null;
    }

    protected void reset() {
        this.tree = new DivinityTree();
        this.tree.skills.add(new ExpertisePointIncreaseDivinity());
    }

}
