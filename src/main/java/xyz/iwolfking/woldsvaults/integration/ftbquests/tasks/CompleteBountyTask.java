package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.BooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import iskallia.vault.VaultMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.init.ModFTBQuestsTaskTypes;

public class CompleteBountyTask extends BooleanTask {
    private ResourceLocation taskType;

    public CompleteBountyTask(Quest q) {
        super(q);
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer serverPlayer) {
        return false;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("value", String.valueOf(taskType));
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        taskType = ResourceLocation.tryParse(nbt.getString("value"));
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeResourceLocation(taskType);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        taskType = buffer.readResourceLocation();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addString("value", String.valueOf(taskType), v -> taskType = ResourceLocation.tryParse(v), "the_vault:any");
    }


    @Override
    public TaskType getType() {
        return ModFTBQuestsTaskTypes.COMPLETE_BOUNTY;
    }

    public ResourceLocation getTaskType() {
        return taskType;
    }

}
