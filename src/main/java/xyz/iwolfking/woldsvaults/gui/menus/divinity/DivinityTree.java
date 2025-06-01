package xyz.iwolfking.woldsvaults.gui.menus.divinity;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.SkillTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import xyz.iwolfking.woldsvaults.network.message.KnownDivinityTreeMessage;


public class DivinityTree extends SkillTree {
    public DivinityTree() {
    }

    public void sync(SkillContext context) {
        this.syncTree(context);
    }

    public void syncTree(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent((player) -> {
            xyz.iwolfking.woldsvaults.init.ModNetwork.CHANNEL.sendTo(new KnownDivinityTreeMessage(this), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        });
    }
}
