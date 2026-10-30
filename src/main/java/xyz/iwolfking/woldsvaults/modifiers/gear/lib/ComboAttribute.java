package xyz.iwolfking.woldsvaults.modifiers.gear.lib;

import com.google.gson.annotations.Expose;
import io.netty.buffer.ByteBuf;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.modifiers.gear.DamageBurstComboAttribute;

public abstract class ComboAttribute {
    private final int requiredHits;
    private final ResourceLocation associatedEffect;
    private final int effectDuration;

    protected ComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration) {
        this.requiredHits = requiredHits;
        this.associatedEffect = associatedEffect;
        this.effectDuration = effectDuration;
    }

    public abstract void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute);

    public int getRequiredHits() {
        return this.requiredHits;
    }

    public ResourceLocation getAssociatedEffectId() {
        return this.associatedEffect;
    }

    public MobEffect getAssociatedEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(this.associatedEffect);
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public void writeToBuf(BitBuffer buf) {
        buf.writeInt(this.requiredHits);
        buf.writeIdentifier(this.associatedEffect == null ? ResourceLocation.parse("") : this.associatedEffect);
        buf.writeInt(this.effectDuration);
    }

    public void writeNetToBuf(ByteBuf buf) {
        buf.writeInt(this.requiredHits);
        NetcodeUtils.writeIdentifier(buf, this.associatedEffect == null ? ResourceLocation.parse("") : this.associatedEffect);
        buf.writeInt(this.effectDuration);
    }

    protected static BaseData readBaseNetFromBuf(ByteBuf buf) {
        int hits = buf.readInt();
        ResourceLocation effectRes = NetcodeUtils.readIdentifier(buf);
        int duration = buf.readInt();
        return new BaseData(hits, effectRes.toString().isEmpty() ? null : effectRes, duration);
    }

    protected static BaseData readBaseFromBuf(BitBuffer buf) {
        int hits = buf.readInt();
        ResourceLocation effectRes = buf.readIdentifier();
        int duration = buf.readInt();
        return new BaseData(hits, effectRes.toString().isEmpty() ? null : effectRes, duration);
    }

    public CompoundTag writeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("requiredHits", this.requiredHits);
        tag.putString("associatedEffect", this.associatedEffect == null ? "" : this.associatedEffect.toString());
        tag.putInt("effectDuration", this.effectDuration);
        return tag;
    }

    protected static BaseData readBaseFromNbt(CompoundTag tag) {
        int hits = tag.getInt("requiredHits");
        String effectLoc = tag.getString("associatedEffect");
        ResourceLocation effectRes = effectLoc.isEmpty() ? null : ResourceLocation.parse(effectLoc);
        int duration = tag.getInt("effectDuration");
        return new BaseData(hits, effectRes, duration);
    }

    protected static class BaseData {
        public final int requiredHits;
        public final ResourceLocation associatedEffect;
        public final int effectDuration;

        public BaseData(int requiredHits, ResourceLocation associatedEffect, int effectDuration) {
            this.requiredHits = requiredHits;
            this.associatedEffect = associatedEffect;
            this.effectDuration = effectDuration;
        }
    }

    public abstract static class Config {
        @Expose
        private final IntRollRangeEntry requiredHits;
        @Expose
        private final ResourceLocation associatedEffect;
        @Expose
        private final int effectDuration;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration) {
            this.requiredHits = requiredHits;
            this.associatedEffect = associatedEffect;
            this.effectDuration = effectDuration;
        }

        public IntRollRangeEntry getRequiredHits() {
            return this.requiredHits;
        }

        public ResourceLocation getAssociatedEffectId() {
            return this.associatedEffect;
        }

        public int getEffectDuration() {
            return this.effectDuration;
        }
    }
}