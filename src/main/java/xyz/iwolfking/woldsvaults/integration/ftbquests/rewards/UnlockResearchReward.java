package xyz.iwolfking.woldsvaults.integration.ftbquests.rewards;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.PlayerResearchesData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class UnlockResearchReward extends Reward {
    public static RewardType TYPE;

    public String researchId;

    public UnlockResearchReward(Quest quest) {
        super(quest);
        this.researchId = "";
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag tag) {
        super.writeData(tag);
        tag.putString("researchId", this.researchId);
    }

    @Override
    public void readData(CompoundTag tag) {
        super.readData(tag);
        this.researchId = tag.getString("researchId");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buf) {
        super.writeNetData(buf);
        buf.writeUtf(this.researchId);
    }

    @Override
    public void readNetData(FriendlyByteBuf buf) {
        super.readNetData(buf);
        this.researchId = buf.readUtf();
    }

    @Override
    public void getConfig(ConfigGroup group) {
        super.getConfig(group);
        group.addString("researchId", this.researchId, v -> this.researchId = v, "").setNameKey("ftbquests.reward.woldsvaults.unlock_research.id");
    }

    @Override
    public void claim(ServerPlayer player, boolean auto) {
        PlayerResearchesData data = PlayerResearchesData.get(player.getLevel());
        if(!data.getResearches(player).isResearched(researchId)) {
            data.research(player, ModConfigs.RESEARCHES.getByName(researchId));
        }
    }

    @Override
    public Component getAltTitle() {
        return new TranslatableComponent("ftbquests.reward.woldsvaults.unlock_research");
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
        TYPE = RewardTypes.register(WoldsVaults.id("unlock_research"), UnlockResearchReward::new, () -> Icons.STAR);
    }
}
