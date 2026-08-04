package xyz.iwolfking.woldsvaults.talent.loot_trigger;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TickingSkill;
import iskallia.vault.skill.talent.GearAttributeSkill;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.modifiers.gear.HeartFragmentOnLootAttribute;

public class HeartFragmentPerLootedContainerTalent extends LearnableSkill implements GearAttributeSkill, TickingSkill {
   private ResourceLocation tileGroupId;
   private String displayName;
   private float heartFragmentChance;

   public HeartFragmentPerLootedContainerTalent(
      int unlockLevel, int learnPointCost, int regretPointCost, ResourceLocation tileGroupId, String displayName, int heartFragmentChance
   ) {
      super(unlockLevel, learnPointCost, regretPointCost);
      this.tileGroupId = tileGroupId;
      this.displayName = displayName;
      this.heartFragmentChance = heartFragmentChance;
   }

   public HeartFragmentPerLootedContainerTalent() {
   }

   @Override
   public void onAdd(SkillContext context) {
      context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
   }

   @Override
   public void onRemove(SkillContext context) {
      context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
   }

   @Override
   public void onTick(SkillContext context) {
      if (!this.isUnlocked()) {
         this.onRemoveModifiers(context);
      } else {
         this.onAddModifiers(context);
      }
   }

   @Override
   public Stream<VaultGearAttributeInstance<?>> getGearAttributes(SkillContext context) {
      return !this.isUnlocked()
         ? Stream.empty()
         : Stream.of(
            new VaultGearAttributeInstance<>(
                    ModGearAttributes.HEART_FRAGMENT_ON_LOOT, new HeartFragmentOnLootAttribute(this.tileGroupId, this.displayName, heartFragmentChance, 1)
            )
         );
   }

   @Override
   public void writeBits(BitBuffer buffer) {
      super.writeBits(buffer);
      Adapters.IDENTIFIER.writeBits(this.tileGroupId, buffer);
      Adapters.UTF_8.writeBits(this.displayName, buffer);
      Adapters.FLOAT.writeBits(this.heartFragmentChance, buffer);
   }

   @Override
   public void readBits(BitBuffer buffer) {
      super.readBits(buffer);
      this.tileGroupId = Adapters.IDENTIFIER.readBits(buffer).orElseThrow();
      this.displayName = Adapters.UTF_8.readBits(buffer).orElseThrow();
      this.heartFragmentChance = Adapters.FLOAT.readBits(buffer).orElseThrow();
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         Adapters.IDENTIFIER.writeNbt(this.tileGroupId).ifPresent(tag -> nbt.put("tileGroupId", tag));
         Adapters.UTF_8.writeNbt(this.displayName).ifPresent(tag -> nbt.put("displayName", tag));
         Adapters.FLOAT.writeNbt(this.heartFragmentChance).ifPresent(tag -> nbt.put("heartFragmentChance", tag));
         return nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.tileGroupId = Adapters.IDENTIFIER.readNbt(nbt.get("tileGroupId")).orElseThrow();
      this.displayName = Adapters.UTF_8.readNbt(nbt.get("displayName")).orElseThrow();
      this.heartFragmentChance = Adapters.FLOAT.readNbt(nbt.get("heartFragmentChance")).orElseThrow();
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         Adapters.IDENTIFIER.writeJson(this.tileGroupId).ifPresent(element -> json.add("tileGroupId", element));
         Adapters.UTF_8.writeJson(this.displayName).ifPresent(element -> json.add("displayName", element));
         Adapters.FLOAT.writeJson(this.heartFragmentChance).ifPresent(element -> json.add("heartFragmentChance", element));
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.tileGroupId = Adapters.IDENTIFIER.readJson(json.get("tileGroupId")).orElseThrow();
      this.displayName = Adapters.UTF_8.readJson(json.get("displayName")).orElseThrow();
      this.heartFragmentChance = Adapters.FLOAT.readJson(json.get("heartFragmentChance")).orElseThrow();
   }
}
