package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.BooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.init.ModFTBQuestsTaskTypes;

public class ResearchTask extends BooleanTask {
    public String research;

    public ResearchTask(Quest quest) {
        super(quest);
        this.research = "Automatic Genius";
    }

    public TaskType getType() {
        return ModFTBQuestsTaskTypes.RESEARCH;
    }

    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("research", research);
    }

    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        this.research = nbt.getString("research");
    }

    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(research);
    }

    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.research = buffer.readUtf();
    }

    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("research", research, (v) -> this.research = v, "Automatic Genius");
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return (new TextComponent("Unlock Research")).append(": ").append((new TextComponent(research)).withStyle(ChatFormatting.AQUA));
    }

    public int autoSubmitOnPlayerTick() {
        return 20;
    }

    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        return PlayerResearchesData.get(player.getLevel()).getResearches(player).isResearched(research);
    }
}
