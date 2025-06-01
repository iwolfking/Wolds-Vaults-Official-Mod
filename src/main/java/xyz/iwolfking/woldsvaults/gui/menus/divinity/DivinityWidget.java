package xyz.iwolfking.woldsvaults.gui.menus.divinity;

import iskallia.vault.client.gui.screen.player.legacy.widget.SkillWidget;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.network.chat.TextComponent;

public class DivinityWidget extends SkillWidget<DivinityTree> {

    public DivinityWidget(TieredSkill skill, DivinityTree divinityTree, SkillStyle style) {
        super(divinityTree, new TextComponent("woldsvaults.widgets.divinity"), skill, style);
    }

}
