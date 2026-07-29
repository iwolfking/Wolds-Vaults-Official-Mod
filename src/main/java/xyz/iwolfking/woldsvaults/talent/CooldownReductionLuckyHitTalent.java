package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.LuckyHitDamageParticleMessage;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class CooldownReductionLuckyHitTalent extends LuckyHitTalent {
   private float cooldownDecrease;

   public CooldownReductionLuckyHitTalent(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownDecrease) {
      super(unlockLevel, learnPointCost, regretPointCost);
      this.cooldownDecrease = cooldownDecrease;
   }

   public CooldownReductionLuckyHitTalent() {
   }

   @Override
   public void onLuckyHit(LivingHurtEvent event) {
      if(event.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
         PlayerAbilitiesData.get(serverPlayer.getLevel()).getAbilities(serverPlayer).skills.stream().filter(skill -> skill instanceof Ability ability && ability.isUnlocked() && ability.isOnCooldown()).map(Ability.class::cast).toList().forEach(ability -> {
            ability.getCooldown().ifPresent(cooldown -> {
               ability.reduceCooldownBy((int) (cooldown.getMaxTicks() * cooldownDecrease));
            });
         });
      }
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

   public float getCooldownDecrease() {
      return this.cooldownDecrease;
   }

   @Override
   public void writeBits(BitBuffer buffer) {
      super.writeBits(buffer);
      Adapters.FLOAT.writeBits(this.cooldownDecrease, buffer);
   }

   @Override
   public void readBits(BitBuffer buffer) {
      super.readBits(buffer);
      this.cooldownDecrease = Adapters.FLOAT.readBits(buffer).orElseThrow();
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         Adapters.FLOAT.writeNbt(this.cooldownDecrease).ifPresent(tag -> nbt.put("cooldownDecrease", tag));
         return nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.cooldownDecrease = Adapters.FLOAT.readNbt(nbt.get("cooldownDecrease")).orElseThrow();
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         Adapters.FLOAT.writeJson(this.cooldownDecrease).ifPresent(element -> json.add("cooldownDecrease", element));
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.cooldownDecrease = Adapters.FLOAT.readJson(json.get("cooldownDecrease")).orElseThrow();
   }
}
