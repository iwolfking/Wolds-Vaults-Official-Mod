package xyz.iwolfking.woldsvaults.talent.lucky_hit;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.LuckyHitDamageParticleMessage;
import java.util.Optional;

import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.api.util.MissingHealthDamageHelper;

public class ExecutionDamageLuckyHitTalent extends LuckyHitTalent {
   private float damageIncrease;

   public ExecutionDamageLuckyHitTalent(int unlockLevel, int learnPointCost, int regretPointCost, float damageIncrease) {
      super(unlockLevel, learnPointCost, regretPointCost);
      this.damageIncrease = damageIncrease;
   }

   public ExecutionDamageLuckyHitTalent() {
   }

   @Override
   public void onLuckyHit(LivingHurtEvent event) {
      event.setAmount(event.getAmount() + (MissingHealthDamageHelper.getExecutionDamage(this.damageIncrease, event.getEntityLiving())));
      ModNetwork.CHANNEL
         .send(
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(event::getEntity),
            new LuckyHitDamageParticleMessage(
               new Vec3(event.getEntity().position().x, event.getEntity().position().y + event.getEntity().getBbHeight() / 2.0F, event.getEntity().position().z),
               new Vec3(event.getEntity().getBbWidth() / 2.0F, event.getEntity().getBbHeight() / 2.0F, event.getEntity().getBbWidth() / 2.0F),
               event.getEntity().getId()
            )
         );
   }

   public float getDamageIncrease() {
      return this.damageIncrease;
   }

   @Override
   public void writeBits(BitBuffer buffer) {
      super.writeBits(buffer);
      Adapters.FLOAT.writeBits(this.damageIncrease, buffer);
   }

   @Override
   public void readBits(BitBuffer buffer) {
      super.readBits(buffer);
      this.damageIncrease = Adapters.FLOAT.readBits(buffer).orElseThrow();
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         Adapters.FLOAT.writeNbt(this.damageIncrease).ifPresent(tag -> nbt.put("damageIncrease", tag));
         return (CompoundTag)nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.damageIncrease = Adapters.FLOAT.readNbt(nbt.get("damageIncrease")).orElseThrow();
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         Adapters.FLOAT.writeJson(this.damageIncrease).ifPresent(element -> json.add("damageIncrease", element));
         return (JsonObject)json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.damageIncrease = Adapters.FLOAT.readJson(json.get("damageIncrease")).orElseThrow();
   }
}
