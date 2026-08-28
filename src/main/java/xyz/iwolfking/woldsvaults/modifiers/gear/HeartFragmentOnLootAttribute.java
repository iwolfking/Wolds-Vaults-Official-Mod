package xyz.iwolfking.woldsvaults.modifiers.gear;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.loot.LootTriggerAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModItems;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.NetcodeUtils;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class HeartFragmentOnLootAttribute extends LootTriggerAttribute {
   private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
   private final float heartGenerationChance;
   private final int heartsGenerated;

   public HeartFragmentOnLootAttribute(ResourceLocation tileGroupId, String displayName, float heartGenerationChance, int heartsGenerated) {
      super(tileGroupId, displayName);
      this.heartGenerationChance = heartGenerationChance;
      this.heartsGenerated = heartsGenerated;
   }

   public float getHeartGenerationChance() {
      return this.heartGenerationChance;
   }

   public int getHeartsGenerated() {
      return this.heartsGenerated;
   }

   @Override
   public void trigger(BlockEntity tile, RandomSource random, ServerPlayer player) {
      if (random.nextFloat() < this.getHeartGenerationChance()) {
         spawnHeartFragment(tile);
      }
   }

   private void spawnHeartFragment(BlockEntity target) {
      ItemStack heartFragment = new ItemStack(ModItems.HEART_FRAGMENT, this.getHeartsGenerated());

      FloatingItemEntity floatingItem = FloatingItemEntity.create(
              target.getLevel(),
              target.getBlockPos().above(),
              heartFragment
      ).setDroppingParticles(true);

      floatingItem.setColor(11540247, 7669511);
      if(target.getLevel() != null) target.getLevel().addFreshEntity(floatingItem);
   }

   @Override
   public String toString() {
      return "HeartFragmentOnLootAttribute{tileGroupId="
         + this.getTileGroupId()
         + ", displayName='"
         + this.getDisplayName()
         + "', heartGenerationChance="
         + this.heartGenerationChance
         + ", heartsGenerated="
         + this.heartsGenerated
         + "}";
   }

   public static VaultGearAttributeType<HeartFragmentOnLootAttribute> type() {
      return VaultGearAttributeType.of(
         (buf, attribute) -> {
            buf.writeIdentifier(attribute.getTileGroupId());
            buf.writeString(attribute.getDisplayName());
            buf.writeFloat(attribute.getHeartGenerationChance());
            buf.writeInt(attribute.getHeartsGenerated());
         },
         buf -> new HeartFragmentOnLootAttribute(buf.readIdentifier(), buf.readString(), buf.readFloat(), buf.readInt()),
         (buf, attribute) -> {
            NetcodeUtils.writeIdentifier(buf, attribute.getTileGroupId());
            NetcodeUtils.writeString(buf, attribute.getDisplayName());
            buf.writeFloat(attribute.getHeartGenerationChance());
            buf.writeInt(attribute.getHeartsGenerated());
         },
         buf -> new HeartFragmentOnLootAttribute(NetcodeUtils.readIdentifier(buf), NetcodeUtils.readString(buf), buf.readFloat(), buf.readInt()),
         VaultGearAttributeType.GSON::toJsonTree,
         tag -> {
            CompoundTag compoundTag = (CompoundTag)tag;
            ResourceLocation tileGroupId = ResourceLocation.parse(compoundTag.getString("tileGroupId"));
            String displayName = compoundTag.getString("displayName");
            float manaGenerationChance = compoundTag.getFloat("heartGenerationChance");
            int manaGenerated = compoundTag.getInt("heartsGenerated");
            return new HeartFragmentOnLootAttribute(tileGroupId, displayName, manaGenerationChance, manaGenerated);
         },
         attribute -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("tileGroupId", attribute.getTileGroupId().toString());
            compoundTag.putString("displayName", attribute.getDisplayName());
            compoundTag.putFloat("heartGenerationChance", attribute.getHeartGenerationChance());
            compoundTag.putInt("heartsGenerated", attribute.getHeartsGenerated());
            return compoundTag;
         }
      );
   }

   public static HeartFragmentOnLootAttribute.AttributeComparator comparator() {
      return new HeartFragmentOnLootAttribute.AttributeComparator();
   }

   public static HeartFragmentOnLootAttribute.Generator generator() {
      return new HeartFragmentOnLootAttribute.Generator();
   }

   public static HeartFragmentOnLootAttribute.Reader reader() {
      return new HeartFragmentOnLootAttribute.Reader();
   }

   private static class AttributeComparator extends VaultGearAttributeComparator<HeartFragmentOnLootAttribute> {
      public Optional<HeartFragmentOnLootAttribute> merge(HeartFragmentOnLootAttribute thisValue, HeartFragmentOnLootAttribute thatValue) {
         if (!Objects.equals(thisValue.getTileGroupId(), thatValue.getTileGroupId())) {
            return Optional.empty();
         } else {
            return thisValue.getHeartsGenerated() != thatValue.getHeartsGenerated()
               ? Optional.empty()
               : Optional.of(
                  new HeartFragmentOnLootAttribute(
                     thisValue.getTileGroupId(),
                     thisValue.getDisplayName(),
                     thisValue.getHeartGenerationChance() + thatValue.getHeartGenerationChance(),
                     thisValue.getHeartsGenerated()
                  )
               );
         }
      }

      public Optional<HeartFragmentOnLootAttribute> difference(HeartFragmentOnLootAttribute thisValue, HeartFragmentOnLootAttribute thatValue) {
         return Optional.empty();
      }

      @Nonnull
      @Override
      public Comparator<HeartFragmentOnLootAttribute> getComparator() {
         return Comparator.comparing(HeartFragmentOnLootAttribute::getHeartsGenerated).thenComparing(HeartFragmentOnLootAttribute::getHeartGenerationChance);
      }
   }

   public static class Config extends LootTriggerAttribute.Config {
      @Expose
      private final FloatRollRangeEntry heartGenerationChance;
      @Expose
      private final IntRollRangeEntry heartsGenerated;

      public Config(ResourceLocation tileEntityGroupId, String displayName, FloatRollRangeEntry heartGenerationChance, IntRollRangeEntry heartsGenerated) {
         super(tileEntityGroupId, displayName);
         this.heartGenerationChance = heartGenerationChance;
         this.heartsGenerated = heartsGenerated;
      }

      public FloatRollRangeEntry getHeartGenerationChance() {
         return this.heartGenerationChance;
      }

      public IntRollRangeEntry getHeartsGenerated() {
         return this.heartsGenerated;
      }
   }

   public static class Generator extends ConfigurableAttributeGenerator<HeartFragmentOnLootAttribute, HeartFragmentOnLootAttribute.Config> {
      @Nullable
      @Override
      public Class<HeartFragmentOnLootAttribute.Config> getConfigurationObjectClass() {
         return HeartFragmentOnLootAttribute.Config.class;
      }

      @Nullable
      public MutableComponent getConfigRangeDisplay(
              VaultGearModifierReader<HeartFragmentOnLootAttribute> reader, HeartFragmentOnLootAttribute.Config min, HeartFragmentOnLootAttribute.Config max
      ) {
         return this.getChanceDisplay(min.getHeartGenerationChance().getMin())
            .append("-")
            .append(this.getChanceDisplay(max.getHeartGenerationChance().getMax()))
            .append(", ")
            .append(String.valueOf(min.getHeartsGenerated().getMin()))
            .append("-")
            .append(String.valueOf(max.getHeartsGenerated().getMax()));
      }

      private MutableComponent getChanceDisplay(float value) {
         return new TextComponent(HeartFragmentOnLootAttribute.FORMAT.format(value * 100.0F) + "%");
      }

      @Nullable
      public MutableComponent getConfigDisplay(VaultGearModifierReader<HeartFragmentOnLootAttribute> reader, HeartFragmentOnLootAttribute.Config object) {
         MutableComponent range = this.getConfigRangeDisplay(reader, object);
         MutableComponent display = new TextComponent(object.getDisplayName());
         return new TextComponent("")
            .withStyle(reader.getColoredTextStyle())
            .append(range.withStyle(reader.getColoredTextStyle()))
            .append(" Heart Fragments per ")
            .append(display)
            .append(" looted");
      }

      public HeartFragmentOnLootAttribute generateRandomValue(HeartFragmentOnLootAttribute.Config object, Random random) {
         JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
         float genChance = object.getHeartGenerationChance().getRandom(rand);
         int genAmount = object.getHeartsGenerated().getRandom(rand);
         return new HeartFragmentOnLootAttribute(object.getTileEntityGroupId(), object.getDisplayName(), genChance, genAmount);
      }

      @Override
      public Optional<HeartFragmentOnLootAttribute> getMinimumValue(List<HeartFragmentOnLootAttribute.Config> configurations) {
         Comparator<HeartFragmentOnLootAttribute.Config> cfgCmp = Comparator.comparing(config -> config.getHeartsGenerated().getMin());
         cfgCmp = cfgCmp.thenComparing(config -> config.getHeartGenerationChance().getMin());
         return configurations.stream()
            .min(cfgCmp)
            .map(
               config -> new HeartFragmentOnLootAttribute(
                  config.getTileEntityGroupId(), config.getDisplayName(), config.getHeartGenerationChance().getMin(), config.getHeartsGenerated().getMin()
               )
            );
      }

      @Override
      public Optional<HeartFragmentOnLootAttribute> getMaximumValue(List<HeartFragmentOnLootAttribute.Config> configurations) {
         Comparator<HeartFragmentOnLootAttribute.Config> cfgCmp = Comparator.comparing(config -> config.getHeartsGenerated().getRolledMaximum());
         cfgCmp = cfgCmp.thenComparing(config -> config.getHeartGenerationChance().getRolledMaximum());
         return configurations.stream()
            .max(cfgCmp)
            .map(
               config -> new HeartFragmentOnLootAttribute(
                  config.getTileEntityGroupId(),
                  config.getDisplayName(),
                  config.getHeartGenerationChance().getRolledMaximum(),
                  config.getHeartsGenerated().getRolledMaximum()
               )
            );
      }

      public Optional<Float> getRollPercentage(HeartFragmentOnLootAttribute value, List<HeartFragmentOnLootAttribute.Config> configurations) {
         return MiscUtils.getFloatValueRange(
            value.getHeartGenerationChance(),
            this.getMinimumValue(configurations),
            this.getMaximumValue(configurations),
            HeartFragmentOnLootAttribute::getHeartGenerationChance
         );
      }
   }

   private static class Reader extends VaultGearModifierReader<HeartFragmentOnLootAttribute> {
      protected Reader() {
         super("", 65535);
      }

      private Style getHighlightStyle() {
         return Style.EMPTY.withColor(20479);
      }

      @Nullable
      @Override
      public MutableComponent getDisplay(VaultGearAttributeInstance<HeartFragmentOnLootAttribute> instance, VaultGearModifier.AffixType type) {
         HeartFragmentOnLootAttribute manaLootAttr = instance.getValue();
         MutableComponent valueDisplay = this.getValueDisplay(manaLootAttr);
         if (valueDisplay == null) {
            return null;
         }

         MutableComponent manaGenerated = new TextComponent(String.valueOf(manaLootAttr.getHeartsGenerated()));
         return new TextComponent(type.getAffixPrefix(manaLootAttr.getHeartGenerationChance() >= 0.0F))
            .withStyle(this.getColoredTextStyle())
            .append(valueDisplay.withStyle(this.getHighlightStyle()))
            .append(new TextComponent(" chance to generate ").withStyle(this.getColoredTextStyle()))
            .append(manaGenerated.withStyle(this.getHighlightStyle()))
            .append(new TextComponent(" Heart Fragment per ").withStyle(this.getColoredTextStyle()))
            .append(new TextComponent(manaLootAttr.getDisplayName()).withStyle(this.getHighlightStyle()))
            .append(new TextComponent(" looted").withStyle(this.getColoredTextStyle()));
      }

      @Nullable
      public MutableComponent getValueDisplay(HeartFragmentOnLootAttribute value) {
         return new TextComponent(HeartFragmentOnLootAttribute.FORMAT.format(value.getHeartGenerationChance() * 100.0F) + "%");
      }

      @Override
      protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<HeartFragmentOnLootAttribute> instance, VaultGearModifier.AffixType type) {
         HeartFragmentOnLootAttribute manaLootAttr = instance.getValue();
         MutableComponent valueDisplay = this.getValueDisplay(manaLootAttr);
         if (valueDisplay != null) {
            out.add(type.getAffixPrefix(manaLootAttr.getHeartGenerationChance() >= 0.0F));
            out.add(valueDisplay.getString());
            out.add(" chance to generate ");
            out.add(String.valueOf(manaLootAttr.getHeartsGenerated()));
            out.add(" Heart Fragment per ");
            out.add(manaLootAttr.getDisplayName());
            out.add(" looted");
         }
      }
   }
}
