package xyz.iwolfking.woldsvaults.abilities.block;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class LifeBlockAbility extends AbstractBlockAbility {
    private float absorptionMin;
    private float absorptionMax;
    private float transfusionMin;
    private float transfusionMax;
    private int transfusionMaxStacks;
    private int transfusionStackTicks;
    private float lifeBondMin;
    private float lifeBondMax;

    public LifeBlockAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost,
                            float blockMultiplier, int minBuffTicks, int maxBuffTicks, int fastClearTicks,
                            int slowClearTicks, float absorptionMin, float absorptionMax, float transfusionMin,
                            float transfusionMax, int transfusionMaxStacks, int transfusionStackTicks,
                            float lifeBondMin, float lifeBondMax) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost, blockMultiplier, minBuffTicks,
                maxBuffTicks, fastClearTicks, slowClearTicks);
        this.absorptionMin = absorptionMin;
        this.absorptionMax = absorptionMax;
        this.transfusionMin = transfusionMin;
        this.transfusionMax = transfusionMax;
        this.transfusionMaxStacks = transfusionMaxStacks;
        this.transfusionStackTicks = transfusionStackTicks;
        this.lifeBondMin = lifeBondMin;
        this.lifeBondMax = lifeBondMax;
    }

    public LifeBlockAbility() {
    }

    @Override
    protected boolean isBlocking(ServerPlayer player) {
        return LifeBlockState.isBlocking(player);
    }

    @Override
    protected void raiseBlock(ServerPlayer player) {
        LifeBlockState.raise(player, this);
    }

    public float getAbsorption() {
        return this.scale(this.absorptionMin, this.absorptionMax);
    }

    public float getTransfusionPerStack() {
        return this.scale(this.transfusionMin, this.transfusionMax);
    }

    public int getTransfusionMaxStacks() {
        return this.transfusionMaxStacks;
    }

    public int getTransfusionStackTicks() {
        return this.transfusionStackTicks;
    }

    public float getLifeBond() {
        return this.scale(this.lifeBondMin, this.lifeBondMax);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.absorptionMin, buffer);
        Adapters.FLOAT.writeBits(this.absorptionMax, buffer);
        Adapters.FLOAT.writeBits(this.transfusionMin, buffer);
        Adapters.FLOAT.writeBits(this.transfusionMax, buffer);
        Adapters.INT.writeBits(this.transfusionMaxStacks, buffer);
        Adapters.INT.writeBits(this.transfusionStackTicks, buffer);
        Adapters.FLOAT.writeBits(this.lifeBondMin, buffer);
        Adapters.FLOAT.writeBits(this.lifeBondMax, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.absorptionMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.absorptionMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.transfusionMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.transfusionMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.transfusionMaxStacks = Adapters.INT.readBits(buffer).orElse(0);
        this.transfusionStackTicks = Adapters.INT.readBits(buffer).orElse(0);
        this.lifeBondMin = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.lifeBondMax = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.absorptionMin).ifPresent(tag -> nbt.put("absorptionMin", tag));
            Adapters.FLOAT.writeNbt(this.absorptionMax).ifPresent(tag -> nbt.put("absorptionMax", tag));
            Adapters.FLOAT.writeNbt(this.transfusionMin).ifPresent(tag -> nbt.put("transfusionMin", tag));
            Adapters.FLOAT.writeNbt(this.transfusionMax).ifPresent(tag -> nbt.put("transfusionMax", tag));
            Adapters.INT.writeNbt(this.transfusionMaxStacks).ifPresent(tag -> nbt.put("transfusionMaxStacks", tag));
            Adapters.INT.writeNbt(this.transfusionStackTicks).ifPresent(tag -> nbt.put("transfusionStackTicks", tag));
            Adapters.FLOAT.writeNbt(this.lifeBondMin).ifPresent(tag -> nbt.put("lifeBondMin", tag));
            Adapters.FLOAT.writeNbt(this.lifeBondMax).ifPresent(tag -> nbt.put("lifeBondMax", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.absorptionMin = Adapters.FLOAT.readNbt(nbt.get("absorptionMin")).orElse(0.0F);
        this.absorptionMax = Adapters.FLOAT.readNbt(nbt.get("absorptionMax")).orElse(0.0F);
        this.transfusionMin = Adapters.FLOAT.readNbt(nbt.get("transfusionMin")).orElse(0.0F);
        this.transfusionMax = Adapters.FLOAT.readNbt(nbt.get("transfusionMax")).orElse(0.0F);
        this.transfusionMaxStacks = Adapters.INT.readNbt(nbt.get("transfusionMaxStacks")).orElse(0);
        this.transfusionStackTicks = Adapters.INT.readNbt(nbt.get("transfusionStackTicks")).orElse(0);
        this.lifeBondMin = Adapters.FLOAT.readNbt(nbt.get("lifeBondMin")).orElse(0.0F);
        this.lifeBondMax = Adapters.FLOAT.readNbt(nbt.get("lifeBondMax")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.absorptionMin).ifPresent(element -> json.add("absorptionMin", element));
            Adapters.FLOAT.writeJson(this.absorptionMax).ifPresent(element -> json.add("absorptionMax", element));
            Adapters.FLOAT.writeJson(this.transfusionMin).ifPresent(element -> json.add("transfusionMin", element));
            Adapters.FLOAT.writeJson(this.transfusionMax).ifPresent(element -> json.add("transfusionMax", element));
            Adapters.INT.writeJson(this.transfusionMaxStacks).ifPresent(element -> json.add("transfusionMaxStacks", element));
            Adapters.INT.writeJson(this.transfusionStackTicks).ifPresent(element -> json.add("transfusionStackTicks", element));
            Adapters.FLOAT.writeJson(this.lifeBondMin).ifPresent(element -> json.add("lifeBondMin", element));
            Adapters.FLOAT.writeJson(this.lifeBondMax).ifPresent(element -> json.add("lifeBondMax", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.absorptionMin = this.jsonFloat(json, "absorptionMin", 0.0F);
        this.absorptionMax = this.jsonFloat(json, "absorptionMax", 0.0F);
        this.transfusionMin = this.jsonFloat(json, "transfusionMin", 0.0F);
        this.transfusionMax = this.jsonFloat(json, "transfusionMax", 0.0F);
        this.transfusionMaxStacks = this.jsonInt(json, "transfusionMaxStacks", 0);
        this.transfusionStackTicks = this.jsonInt(json, "transfusionStackTicks", 0);
        this.lifeBondMin = this.jsonFloat(json, "lifeBondMin", 0.0F);
        this.lifeBondMax = this.jsonFloat(json, "lifeBondMax", 0.0F);
    }
}
