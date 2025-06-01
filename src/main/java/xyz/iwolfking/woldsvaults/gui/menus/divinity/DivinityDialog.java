package xyz.iwolfking.woldsvaults.gui.menus.divinity;

import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.SkillDialog;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.source.SkillSource;
import net.minecraft.client.Minecraft;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.DivinityLevelMessage;
import xyz.iwolfking.woldsvaults.util.DivinityUtils;

import java.util.HashMap;

public class DivinityDialog extends SkillDialog<DivinityTree, DivinityElementContainerScreen> {

    public DivinityDialog(DivinityTree powerTree, DivinityElementContainerScreen powerElementContainerScreen) {
        super(powerTree, powerElementContainerScreen);
    }

    protected int getUnspentSkillPoints() {
        return DivinityUtils.unspentDivinityPoints;
    }

    protected void updateRegretButton() {
        this.regretButton = null;
    }

    protected HashMap<String, SkillStyle> getStyles() {
        return ModConfigs.DIVINITY_GUI_CONFIG.getStyles();
    }

    protected SkillContext getSkillContext() {
        return new SkillContext(VaultBarOverlay.vaultLevel, DivinityUtils.unspentDivinityPoints, 0, SkillSource.of(Minecraft.getInstance().player));
    }

    protected void sendUpgradeMessage() {
        ModNetwork.CHANNEL.sendToServer(new DivinityLevelMessage(this.skillGroup.getId(), true));
    }

}
