package xyz.iwolfking.woldsvaults.integration.ftbquests.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.XPTask;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.init.ModFTBQuestsTaskTypes;
import xyz.iwolfking.woldsvaults.integration.ftbquests.tasks.lib.ISingleIntValueTask;

public class VaultLevelTask extends Task implements ISingleIntValueTask {
    public int value = 1;

    public VaultLevelTask(Quest quest) {
        super(quest);
    }


    @Override
    public TaskType getType() {
        return ModFTBQuestsTaskTypes.VAULT_LEVEL;
    }

    @Override
    public long getMaxProgress() {
        return value;
    }

    @Override
    public String formatMaxProgress() {
        return Long.toUnsignedString(value);
    }

    @Override
    public String formatProgress(TeamData teamData, long progress) {
        return Long.toUnsignedString(progress);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putInt("value", value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        value = nbt.getInt("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarInt(value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = buffer.readVarInt();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);
        config.addInt("value", value, v -> value = v, 1, 1, Integer.MAX_VALUE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MutableComponent getAltTitle() {
        return new TranslatableComponent("ftbquests.reward.ftbquests.xp_levels").append(": ").append(new TextComponent(formatMaxProgress()).withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean consumesResources() {
        return true;
    }

    @Override
    public void onButtonClicked(Button button, boolean canClick) {
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        int add = Math.toIntExact(Math.min(value - teamData.getProgress(this), Integer.MAX_VALUE));

        if (add <= 0) {
            return;
        }

        teamData.addProgress(this, add);
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }
}
