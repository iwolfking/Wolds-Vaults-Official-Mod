package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.TrapDisarmChanceHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class MindMeldTalent extends LearnableSkill {
   private float manaStep = 10F;
   private float bonusPerStep;

   public MindMeldTalent(int unlockLevel, int learnPointCost, int regretPointCost, float manaStep, float bonusPerStep) {
      super(unlockLevel, learnPointCost, regretPointCost);
      this.manaStep = manaStep;
      this.bonusPerStep = bonusPerStep;
   }

   public MindMeldTalent() {
   }

   public float getManaStep() {
      return this.manaStep;
   }

   public float getBonusPerStep() {
      return this.bonusPerStep;
   }

   private static void applyBonus(PlayerStatEvent.Data data) {
      if (data.getEntity() instanceof ServerPlayer player) {
         if (player.getServer() != null) {
            TalentTree tree = PlayerTalentsData.get(player.getServer()).getTalents(player);

            for (MindMeldTalent talent : tree.getAll(MindMeldTalent.class, Skill::isUnlocked)) {
               float step = talent.getManaStep();
               if (!(step <= 0.0F) && !(talent.getBonusPerStep() <= 0.0F)) {
                  if(player instanceof ManaPlayer manaPlayer) {
                     float maxMana = manaPlayer.getManaMax();
                     int steps = Mth.floor(maxMana / step + 1.0E-4F);
                     if (steps > 0) {
                        data.setValue(data.getValue() + steps * talent.getBonusPerStep());
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void writeBits(BitBuffer buffer) {
      super.writeBits(buffer);
      Adapters.FLOAT.writeBits(this.manaStep, buffer);
      Adapters.FLOAT.writeBits(this.bonusPerStep, buffer);
   }

   @Override
   public void readBits(BitBuffer buffer) {
      super.readBits(buffer);
      this.manaStep = Adapters.FLOAT.readBits(buffer).orElseThrow();
      this.bonusPerStep = Adapters.FLOAT.readBits(buffer).orElseThrow();
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         Adapters.FLOAT.writeNbt(this.manaStep).ifPresent(tag -> nbt.put("manaStep", tag));
         Adapters.FLOAT.writeNbt(this.bonusPerStep).ifPresent(tag -> nbt.put("bonusPerStep", tag));
         return (CompoundTag)nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.manaStep = Adapters.FLOAT.readNbt(nbt.get("manaStep")).orElse(0.1F);
      this.bonusPerStep = Adapters.FLOAT.readNbt(nbt.get("bonusPerStep")).orElse(0.0F);
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         Adapters.FLOAT.writeJson(this.manaStep).ifPresent(element -> json.add("manaStep", element));
         Adapters.FLOAT.writeJson(this.bonusPerStep).ifPresent(element -> json.add("bonusPerStep", element));
         return (JsonObject)json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.manaStep = Adapters.FLOAT.readJson(json.get("manaStep")).orElse(0.1F);
      this.bonusPerStep = Adapters.FLOAT.readJson(json.get("bonusPerStep")).orElse(0.0F);
   }

   static {
      CommonEvents.PLAYER_STAT.of(PlayerStat.COOLDOWN_REDUCTION).register(MindMeldTalent.class, MindMeldTalent::applyBonus);
   }
}