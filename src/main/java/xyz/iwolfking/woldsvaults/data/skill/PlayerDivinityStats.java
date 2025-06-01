package xyz.iwolfking.woldsvaults.data.skill;

import iskallia.vault.util.NetcodeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.DivinityPointMessage;

import java.util.UUID;

public class PlayerDivinityStats implements INBTSerializable<CompoundTag> {
    private final UUID uuid;
    private int unspentDivinityPoints;
    private int totalSpentDivinityPoints;

    public PlayerDivinityStats(UUID uuid) {
        this.uuid = uuid;
    }


    public int getUnspentDivinityPoints() {
        return this.unspentDivinityPoints;
    }

    public int getTotalSpentDivinityPoints() {
        return this.totalSpentDivinityPoints;
    }

    public void spendDivinityPoints(MinecraftServer server, int amount) {
        this.unspentDivinityPoints -= amount;
        this.totalSpentDivinityPoints += amount;
        this.sync(server);
    }

    public PlayerDivinityStats reset(MinecraftServer server) {
        this.unspentDivinityPoints = 0;
        this.sync(server);
        return this;
    }

    public PlayerDivinityStats resetDivinity(MinecraftServer server) {
        this.unspentDivinityPoints = 0;
        this.totalSpentDivinityPoints = 0;
        this.sync(server);
        return this;
    }

    public void setTotalSpentDivinityPoints(int totalSpentExpertisePoints) {
        this.totalSpentDivinityPoints = totalSpentExpertisePoints;
    }

    public PlayerDivinityStats addDivinityPoints(int amount) {
        this.unspentDivinityPoints += amount;
        return this;
    }

    public PlayerDivinityStats setDivinityPoints(int amount) {
        this.unspentDivinityPoints = amount;
        this.sync(ServerLifecycleHooks.getCurrentServer());
        return this;
    }

    public PlayerDivinityStats resetAndReturnDivinityPoints() {
        this.unspentDivinityPoints = this.unspentDivinityPoints + this.totalSpentDivinityPoints;
        this.totalSpentDivinityPoints = 0;
        return this;
    }

    public PlayerDivinityStats refundDivinityPoints(int amount) {
        this.unspentDivinityPoints += amount;
        this.totalSpentDivinityPoints -= amount;
        return this;
    }

    public void sync(MinecraftServer server) {
        NetcodeUtils.runIfPresent(server, this.uuid, (player) -> {
            ModNetwork.CHANNEL.sendTo(new DivinityPointMessage(this.unspentDivinityPoints), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        });
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("unspentDivinityPoints", this.unspentDivinityPoints);
        nbt.putInt("totalSpentDivinityPoints", this.totalSpentDivinityPoints);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.unspentDivinityPoints = nbt.getInt("unspentDivinityPoints");
        this.totalSpentDivinityPoints = nbt.getInt("totalSpentDivinityPoints");
    }
}

