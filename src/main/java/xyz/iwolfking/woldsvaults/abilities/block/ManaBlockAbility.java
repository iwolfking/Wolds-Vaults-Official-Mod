package xyz.iwolfking.woldsvaults.abilities.block;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ManaBlockAbility extends AbstractBlockAbility {
    private float manaShieldMin;
    private float manaShieldMax;
    private float manaRegenMin;
    private float manaRegenMax;
    private float surgingAoeMin;
    private float surgingAoeMax;

    public ManaBlockAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost,
                            float blockMultiplier, int minBuffTicks, int maxBuffTicks, int fastClearTicks,
                            int slowClearTicks, float manaShieldMin, float manaShieldMax, float manaRegenMin,
                            float manaRegenMax, float surgingAoeMin, float surgingAoeMax) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost, blockMultiplier, minBuffTicks,
                maxBuffTicks, fastClearTicks, slowClearTicks);
        this.manaShieldMin = manaShieldMin;
        this.manaShieldMax = manaShieldMax;
        this.manaRegenMin = manaRegenMin;
        this.manaRegenMax = manaRegenMax;
        this.surgingAoeMin = surgingAoeMin;
        this.surgingAoeMax = surgingAoeMax;
    }

    public ManaBlockAbility() {
    }

    @Override
    protected boolean isBlocking(ServerPlayer player) {
        return ManaBlockState.isBlocking(player);
    }

    @Override
    protected void raiseBlock(ServerPlayer player) {
        ManaBlockState.raise(player, this);
    }

    public float getManaShield() {
        return this.scale(this.manaShieldMin, this.manaShieldMax);
    }

    public float getManaRegen() {
        return this.scale(this.manaRegenMin, this.manaRegenMax);
    }

    public float getSurgingAoe() {
        return this.scale(this.surgingAoeMin, this.surgingAoeMax);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.manaShieldMin, buffer);
        Adapters.FLOAT.writeBits(this.manaShieldMax, buffer);
        Adapters.FLOAT.writeBits(this.manaRegenMin, buffer);
        Adapters.FLOAT.writeBits(this.manaRegenMax, buffer);
        Adapters.FLOAT.writeBits(this.surgingAoeMin, buffer);
        Adapters.FLOAT.writeBits(this.surgingAoeMax, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.manaShieldMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.manaShieldMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.manaRegenMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.manaRegenMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.surgingAoeMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.surgingAoeMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.manaShieldMin).ifPresent(tag -> nbt.put("manaShieldMin", tag));
            Adapters.FLOAT.writeNbt(this.manaShieldMax).ifPresent(tag -> nbt.put("manaShieldMax", tag));
            Adapters.FLOAT.writeNbt(this.manaRegenMin).ifPresent(tag -> nbt.put("manaRegenMin", tag));
            Adapters.FLOAT.writeNbt(this.manaRegenMax).ifPresent(tag -> nbt.put("manaRegenMax", tag));
            Adapters.FLOAT.writeNbt(this.surgingAoeMin).ifPresent(tag -> nbt.put("surgingAoeMin", tag));
            Adapters.FLOAT.writeNbt(this.surgingAoeMax).ifPresent(tag -> nbt.put("surgingAoeMax", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.manaShieldMin = Adapters.FLOAT.readNbt(nbt.get("manaShieldMin")).orElse(0.0F);
        this.manaShieldMax = Adapters.FLOAT.readNbt(nbt.get("manaShieldMax")).orElse(0.0F);
        this.manaRegenMin = Adapters.FLOAT.readNbt(nbt.get("manaRegenMin")).orElse(0.0F);
        this.manaRegenMax = Adapters.FLOAT.readNbt(nbt.get("manaRegenMax")).orElse(0.0F);
        this.surgingAoeMin = Adapters.FLOAT.readNbt(nbt.get("surgingAoeMin")).orElse(0.0F);
        this.surgingAoeMax = Adapters.FLOAT.readNbt(nbt.get("surgingAoeMax")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.manaShieldMin).ifPresent(element -> json.add("manaShieldMin", element));
            Adapters.FLOAT.writeJson(this.manaShieldMax).ifPresent(element -> json.add("manaShieldMax", element));
            Adapters.FLOAT.writeJson(this.manaRegenMin).ifPresent(element -> json.add("manaRegenMin", element));
            Adapters.FLOAT.writeJson(this.manaRegenMax).ifPresent(element -> json.add("manaRegenMax", element));
            Adapters.FLOAT.writeJson(this.surgingAoeMin).ifPresent(element -> json.add("surgingAoeMin", element));
            Adapters.FLOAT.writeJson(this.surgingAoeMax).ifPresent(element -> json.add("surgingAoeMax", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.manaShieldMin = this.jsonFloat(json, "manaShieldMin", 0.0F);
        this.manaShieldMax = this.jsonFloat(json, "manaShieldMax", 0.0F);
        this.manaRegenMin = this.jsonFloat(json, "manaRegenMin", 0.0F);
        this.manaRegenMax = this.jsonFloat(json, "manaRegenMax", 0.0F);
        this.surgingAoeMin = this.jsonFloat(json, "surgingAoeMin", 0.0F);
        this.surgingAoeMax = this.jsonFloat(json, "surgingAoeMax", 0.0F);
    }
}
