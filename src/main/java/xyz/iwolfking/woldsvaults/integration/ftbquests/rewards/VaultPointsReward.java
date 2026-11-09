package xyz.iwolfking.woldsvaults.integration.ftbquests.rewards;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class VaultPointsReward extends Reward {
    public static RewardType TYPE;

    public int skillPoints;
    public int knowledgePoints;
    public int expertisePoints;
    public int prestigePoints;
    public int regretPoints;

    public VaultPointsReward(Quest quest) {
        super(quest);
        this.skillPoints = 0;
        this.knowledgePoints = 0;
        this.expertisePoints = 0;
        this.prestigePoints = 0;
        this.regretPoints = 0;
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag tag) {
        super.writeData(tag);
        tag.putInt("skillPoints", this.skillPoints);
        tag.putInt("knowledgePoints", this.knowledgePoints);
        tag.putInt("expertisePoints", this.expertisePoints);
        tag.putInt("prestigePoints", this.prestigePoints);
        tag.putInt("regretPoints", this.regretPoints);
    }

    @Override
    public void readData(CompoundTag tag) {
        super.readData(tag);
        this.skillPoints = tag.getInt("skillPoints");
        this.knowledgePoints = tag.getInt("knowledgePoints");
        this.expertisePoints = tag.getInt("expertisePoints");
        this.prestigePoints = tag.getInt("prestigePoints");
        this.regretPoints = tag.getInt("regretPoints");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buf) {
        super.writeNetData(buf);
        buf.writeInt(this.skillPoints);
        buf.writeInt(this.knowledgePoints);
        buf.writeInt(this.expertisePoints);
        buf.writeInt(this.prestigePoints);
        buf.writeInt(this.regretPoints);
    }

    @Override
    public void readNetData(FriendlyByteBuf buf) {
        super.readNetData(buf);
        this.skillPoints = buf.readInt();
        this.knowledgePoints = buf.readInt();
        this.expertisePoints = buf.readInt();
        this.prestigePoints = buf.readInt();
        this.regretPoints = buf.readInt();
    }

    @Override
    public void getConfig(ConfigGroup group) {
        super.getConfig(group);
        group.addInt("skillPoints", this.skillPoints, v -> this.skillPoints = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.woldsvaults.vault_points.skill");
        group.addInt("knowledgePoints", this.knowledgePoints, v -> this.knowledgePoints = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.woldsvaults.vault_points.knowledge");
        group.addInt("expertisePoints", this.expertisePoints, v -> this.expertisePoints = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.woldsvaults.vault_points.expertise");
        group.addInt("prestigePoints", this.prestigePoints, v -> this.prestigePoints = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.woldsvaults.vault_points.prestige");
        group.addInt("regretPoints", this.regretPoints, v -> this.regretPoints = v, 0, 0, Integer.MAX_VALUE).setNameKey("ftbquests.reward.woldsvaults.vault_points.regret");
    }

    @Override
    public void claim(ServerPlayer player, boolean auto) {
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.server);
        if (this.skillPoints > 0) data.addSkillPoints(player, this.skillPoints);
        if (this.knowledgePoints > 0) data.addKnowledgePoints(player, this.knowledgePoints);
        if (this.expertisePoints > 0) data.addExpertisePoints(player, this.expertisePoints);
        if (this.prestigePoints > 0) data.addPrestigePoints(player, this.prestigePoints);
    }

    @Override
    public Component getAltTitle() {
        return new TranslatableComponent("ftbquests.reward.woldsvaults.vault_points");
    }

    @Override
    public String getButtonText() {
        return "Claim";
    }

    @Override
    public Icon getAltIcon() {
        return Icons.STAR;
    }

    public static void register() {
        TYPE = RewardTypes.register(WoldsVaults.id("vault_points"), VaultPointsReward::new, () -> Icons.STAR);
    }
}
