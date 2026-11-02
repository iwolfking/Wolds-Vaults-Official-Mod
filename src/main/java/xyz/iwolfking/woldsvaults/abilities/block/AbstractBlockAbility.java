package xyz.iwolfking.woldsvaults.abilities.block;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Optional;

public abstract class AbstractBlockAbility extends InstantManaAbility {
    public static final float MIN_BLOCK = 1.0F;
    public static final float MAX_BLOCK = 10.0F;

    private float blockMultiplier;
    private int minBuffTicks;
    private int maxBuffTicks;
    private int fastClearTicks;
    private int slowClearTicks;

    protected AbstractBlockAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                                   float manaCost, float blockMultiplier, int minBuffTicks, int maxBuffTicks,
                                   int fastClearTicks, int slowClearTicks) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.blockMultiplier = blockMultiplier;
        this.minBuffTicks = minBuffTicks;
        this.maxBuffTicks = maxBuffTicks;
        this.fastClearTicks = fastClearTicks;
        this.slowClearTicks = slowClearTicks;
    }

    protected AbstractBlockAbility() {
    }

    protected abstract boolean isBlocking(ServerPlayer player);

    protected abstract void raiseBlock(ServerPlayer player);

    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            if (this.isBlocking(player)) {
                player.displayClientMessage(new TranslatableComponent("message.woldsvaults.block_already_up")
                        .withStyle(ChatFormatting.GRAY), true);
                return ActionResult.fail();
            }
            this.raiseBlock(player);
            return ActionResult.successCooldownImmediate();
        }).orElse(ActionResult.fail());
    }

    public float getStrength() {
        return Mth.clamp((this.blockMultiplier - MIN_BLOCK) / (MAX_BLOCK - MIN_BLOCK), 0.0F, 1.0F);
    }

    protected float scale(float min, float max) {
        return Mth.lerp(this.getStrength(), min, max);
    }

    public int getBuffTicks(int clearTicks) {
        if (this.slowClearTicks <= this.fastClearTicks) {
            return clearTicks <= this.fastClearTicks ? this.maxBuffTicks : this.minBuffTicks;
        }
        float slowness = Mth.clamp((clearTicks - this.fastClearTicks) / (float) (this.slowClearTicks - this.fastClearTicks),
                0.0F, 1.0F);
        return Math.round(Mth.lerp(slowness, this.maxBuffTicks, this.minBuffTicks));
    }

    public float getBlockMultiplier() {
        return this.blockMultiplier;
    }

    public int getMinBuffTicks() {
        return this.minBuffTicks;
    }

    public int getMaxBuffTicks() {
        return this.maxBuffTicks;
    }

    public int getFastClearTicks() {
        return this.fastClearTicks;
    }

    public int getSlowClearTicks() {
        return this.slowClearTicks;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.blockMultiplier, buffer);
        Adapters.INT.writeBits(this.minBuffTicks, buffer);
        Adapters.INT.writeBits(this.maxBuffTicks, buffer);
        Adapters.INT.writeBits(this.fastClearTicks, buffer);
        Adapters.INT.writeBits(this.slowClearTicks, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.blockMultiplier = Adapters.FLOAT.readBits(buffer).orElse(MIN_BLOCK);
        this.minBuffTicks = Adapters.INT.readBits(buffer).orElse(0);
        this.maxBuffTicks = Adapters.INT.readBits(buffer).orElse(0);
        this.fastClearTicks = Adapters.INT.readBits(buffer).orElse(0);
        this.slowClearTicks = Adapters.INT.readBits(buffer).orElse(0);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.blockMultiplier).ifPresent(tag -> nbt.put("blockMultiplier", tag));
            Adapters.INT.writeNbt(this.minBuffTicks).ifPresent(tag -> nbt.put("minBuffTicks", tag));
            Adapters.INT.writeNbt(this.maxBuffTicks).ifPresent(tag -> nbt.put("maxBuffTicks", tag));
            Adapters.INT.writeNbt(this.fastClearTicks).ifPresent(tag -> nbt.put("fastClearTicks", tag));
            Adapters.INT.writeNbt(this.slowClearTicks).ifPresent(tag -> nbt.put("slowClearTicks", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.blockMultiplier = Adapters.FLOAT.readNbt(nbt.get("blockMultiplier")).orElse(MIN_BLOCK);
        this.minBuffTicks = Adapters.INT.readNbt(nbt.get("minBuffTicks")).orElse(0);
        this.maxBuffTicks = Adapters.INT.readNbt(nbt.get("maxBuffTicks")).orElse(0);
        this.fastClearTicks = Adapters.INT.readNbt(nbt.get("fastClearTicks")).orElse(0);
        this.slowClearTicks = Adapters.INT.readNbt(nbt.get("slowClearTicks")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.blockMultiplier).ifPresent(element -> json.add("blockMultiplier", element));
            Adapters.INT.writeJson(this.minBuffTicks).ifPresent(element -> json.add("minBuffTicks", element));
            Adapters.INT.writeJson(this.maxBuffTicks).ifPresent(element -> json.add("maxBuffTicks", element));
            Adapters.INT.writeJson(this.fastClearTicks).ifPresent(element -> json.add("fastClearTicks", element));
            Adapters.INT.writeJson(this.slowClearTicks).ifPresent(element -> json.add("slowClearTicks", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.blockMultiplier = this.jsonFloat(json, "blockMultiplier", MIN_BLOCK);
        this.minBuffTicks = this.jsonInt(json, "minBuffTicks", 0);
        this.maxBuffTicks = this.jsonInt(json, "maxBuffTicks", 0);
        this.fastClearTicks = this.jsonInt(json, "fastClearTicks", 0);
        this.slowClearTicks = this.jsonInt(json, "slowClearTicks", 0);
    }

    /** Reads a float key of this ability's config, logging when it is missing and the fallback is used. */
    protected float jsonFloat(JsonObject json, String key, float fallback) {
        Optional<Float> value = Adapters.FLOAT.readJson(json.get(key));
        if (value.isEmpty()) {
            WoldsVaults.LOGGER.warn("{} config has no {}; using {}.", this.getClass().getSimpleName(), key, fallback);
        }
        return value.orElse(fallback);
    }

    /** Reads an int key of this ability's config, logging when it is missing and the fallback is used. */
    protected int jsonInt(JsonObject json, String key, int fallback) {
        Optional<Integer> value = Adapters.INT.readJson(json.get(key));
        if (value.isEmpty()) {
            WoldsVaults.LOGGER.warn("{} config has no {}; using {}.", this.getClass().getSimpleName(), key, fallback);
        }
        return value.orElse(fallback);
    }
}
