package xyz.iwolfking.woldsvaults.modifiers.gear;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@EventBusSubscriber
public class ParticleTrailAttribute {

    private final ResourceLocation particleId;
    private final int count;
    private final float speed;

    public ParticleTrailAttribute(ResourceLocation particleId, int count, float speed) {
        this.particleId = particleId;
        this.count = count;
        this.speed = speed;
    }

    public ResourceLocation getParticleId() { return particleId; }
    public int getCount() { return count; }
    public float getSpeed() { return speed; }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if (player.level.isClientSide || !(player instanceof ServerPlayer sp))
            return;

        if (!sp.isOnGround() || sp.isFallFlying()) return;

        NonNullList<ItemStack> armor = sp.getInventory().armor;

        ParticleTrailAttribute strongest = null;

        // Find highest intensity particle attribute
        for (ItemStack stack : armor) {
            if (stack.getItem() instanceof VaultGearItem) {
                VaultGearData data = VaultGearData.read(stack);

                if (data.hasAttribute(ModGearAttributes.PARTICLE_TRAIL)) {
                    Optional<ParticleTrailAttribute> inst = data.getFirstValue(ModGearAttributes.PARTICLE_TRAIL);
                    if (inst.isPresent()) {
                        ParticleTrailAttribute attr = inst.get();
                        if (strongest == null || attr.count > strongest.count)
                            strongest = attr;
                    }
                }
            }
        }

        if (strongest == null) return;

        ParticleOptions particle = (ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(strongest.getParticleId());
        if (particle == null) return;

        ServerLevel level = sp.getLevel();
        double x = player.getX();
        double y = player.getY() + 0.1;
        double z = player.getZ();

        level.sendParticles(
                particle,
                x, y, z,
                strongest.getCount(),
                0.15, 0.01, 0.15,
                strongest.getSpeed()
        );
    }


    public static VaultGearAttributeType<ParticleTrailAttribute> type() {
        return VaultGearAttributeType.of((buf, attribute) -> {
            buf.writeIdentifier(attribute.getParticleId());
            buf.writeInt(attribute.getCount());
            buf.writeFloat(attribute.getSpeed());
        }, buf -> new ParticleTrailAttribute(buf.readIdentifier(), buf.readInt(), buf.readFloat()), (buf, attribute) -> {
            NetcodeUtils.writeIdentifier(buf, attribute.getParticleId());
            buf.writeInt(attribute.getCount());
            buf.writeFloat(attribute.getSpeed());
        }, buf -> new ParticleTrailAttribute(NetcodeUtils.readIdentifier(buf), buf.readInt(), buf.readFloat()), VaultGearAttributeType.GSON::toJsonTree, tag -> {
            CompoundTag ctag = new CompoundTag();
            ResourceLocation effectId = ResourceLocation.parse(ctag.getString("particleId"));
            int count = ctag.getInt("count");
            float speed = ctag.getFloat("speed");
            return new ParticleTrailAttribute(effectId, count, speed);
        }, attribute -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("particleId", attribute.getParticleId().toString());
            tag.putInt("count", attribute.getCount());
            tag.putFloat("speed", attribute.getSpeed());
            return tag;
        });
    }


    public static VaultGearAttributeComparator<ParticleTrailAttribute> comparator() {
        return new ComparatorImpl();
    }

    private static class ComparatorImpl extends VaultGearAttributeComparator<ParticleTrailAttribute> {
        public Optional<ParticleTrailAttribute> merge(ParticleTrailAttribute a, ParticleTrailAttribute b) {
            return Optional.of(new ParticleTrailAttribute(b.particleId, Math.max(a.count, b.count), Math.max(a.speed, b.speed)));
        }
        public Optional<ParticleTrailAttribute> difference(ParticleTrailAttribute a, ParticleTrailAttribute b) {
            return Optional.empty();
        }
        public Comparator<ParticleTrailAttribute> getComparator() {
            return Comparator.comparingInt(ParticleTrailAttribute::getCount);
        }
    }


    public static VaultGearModifierReader<ParticleTrailAttribute> reader() {
        return new Reader();
    }

    private static class Reader extends VaultGearModifierReader<ParticleTrailAttribute> {
        protected Reader() { super("", 0xEEDD55); }

        @Nullable
        public MutableComponent getDisplay(VaultGearAttributeInstance<ParticleTrailAttribute> instance, VaultGearModifier.AffixType type) {
            ParticleTrailAttribute value = instance.getValue();
            return type.getAffixPrefixComponent(true)
                    .append(new TextComponent(ResourceLocUtils.formatReadableName(value.getParticleId()))
                    .append(new TextComponent(" Trail "))
                    .append(new TextComponent(Integer.toString(value.getCount())))).withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        @Nullable
        public MutableComponent getValueDisplay(ParticleTrailAttribute value) {
            return new TextComponent(ResourceLocUtils.formatReadableName(value.getParticleId()) + " Trail " + value.getCount());
        }

        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<ParticleTrailAttribute> inst, VaultGearModifier.AffixType type) {
            ParticleTrailAttribute v = inst.getValue();
            out.add(ResourceLocUtils.formatReadableName(v.getParticleId()));
            out.add(" Trail " + v.getCount());
        }
    }

    public static ConfigurableAttributeGenerator<ParticleTrailAttribute, Config> generator() {
        return new Generator();
    }

    public static class Generator extends ConfigurableAttributeGenerator<ParticleTrailAttribute, Config> {

        @Override
        public Class<Config> getConfigurationObjectClass() { return Config.class; }

        public ParticleTrailAttribute generateRandomValue(Config cfg, Random randIn) {
            JavaRandom rand = JavaRandom.ofScrambled(randIn.nextLong());
            int count = cfg.count.getRandom(rand);
            float speed = cfg.speed;
            return new ParticleTrailAttribute(cfg.particleId, count, speed);
        }

        public Optional<ParticleTrailAttribute> getMinimumValue(List<Config> list) {
            return list.stream()
                    .min(Comparator.comparingInt(c -> c.count.getMin()))
                    .map(c -> new ParticleTrailAttribute(c.particleId, c.count.getMin(), c.speed));
        }

        public Optional<ParticleTrailAttribute> getMaximumValue(List<Config> list) {
            return list.stream()
                    .max(Comparator.comparingInt(c -> c.count.getMax()))
                    .map(c -> new ParticleTrailAttribute(c.particleId, c.count.getMax(), c.speed));
        }
    }

    public static class Config {
        @Expose public final ResourceLocation particleId;
        @Expose public final iskallia.vault.config.entry.IntRollRangeEntry count;
        @Expose public final float speed;

        public Config(ResourceLocation particleId, iskallia.vault.config.entry.IntRollRangeEntry count, float speed) {
            this.particleId = particleId;
            this.count = count;
            this.speed = speed;
        }
    }
}

