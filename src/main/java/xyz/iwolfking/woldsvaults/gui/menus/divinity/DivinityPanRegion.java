package xyz.iwolfking.woldsvaults.gui.menus.divinity;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.SkillPanRegion;
import iskallia.vault.client.gui.screen.player.legacy.widget.TalentGroupWidget;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DivinityPanRegion extends SkillPanRegion<DivinityTree, DivinityElementContainerScreen, DivinityWidget> {

    public DivinityPanRegion(DivinityDialog powerDialog, DivinityElementContainerScreen parentScreen) {
        super(parentScreen, new TextComponent("Divinity Tab"), powerDialog);
    }

    protected void initSkillWidget(DivinityTree skillTree, String skillName, SkillStyle style, Map<String, DivinityWidget> widgets, List<TalentGroupWidget> groups) {
        widgets.put(skillName, new DivinityWidget((TieredSkill)skillTree.getForId(skillName).orElse(null), skillTree, style));
    }

    protected HashMap<String, SkillStyle> getStyles() {
        return ModConfigs.DIVINITY_GUI_CONFIG.getStyles();
    }


}
