package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityData;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityStatData;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityStats;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;
import xyz.iwolfking.woldsvaults.util.DivinityUtils;

import java.util.function.Supplier;

public class DivinityLevelMessage {
    private final String divinityName;
    private final boolean isUpgrade;

    public DivinityLevelMessage(String powerName, boolean isUpgrade) {
        this.divinityName = powerName;
        this.isUpgrade = isUpgrade;
    }

    public static void encode(DivinityLevelMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.divinityName);
        buffer.writeBoolean(message.isUpgrade);
    }

    public static DivinityLevelMessage decode(FriendlyByteBuf buffer) {
        return new DivinityLevelMessage(buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(DivinityLevelMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                if (message.isUpgrade) {
                    upgradeDivinity(message, sender);
                }

            }
        });
        context.setPacketHandled(true);
    }

    private static void upgradeDivinity(DivinityLevelMessage message, ServerPlayer player) {
        ServerLevel level = player.getLevel();
        PlayerDivinityStatData statsData = PlayerDivinityStatData.get(level);
        PlayerDivinityData expertisesData = PlayerDivinityData.get(level);
        DivinityTree tree = expertisesData.getDivinityTree(player);
        if (!ModConfigs.SKILL_GATES.getGates().isLocked(message.divinityName, tree)) {
            tree.getForId(message.divinityName).ifPresent((skill) -> {
                SkillContext context = DivinityUtils.ofDivinity(player);
                if (skill instanceof LearnableSkill learnable) {
                    if (learnable.canLearn(context)) {
                        learnable.learn(context);
                        PlayerDivinityStats stats = statsData.getVaultStats(player);
                        int learnPoints = stats.getUnspentDivinityPoints() - context.getLearnPoints();
                        stats.spendDivinityPoints(player.getServer(), learnPoints);
                        tree.sync(context);
                    }
                }

            });
        }
    }
}
