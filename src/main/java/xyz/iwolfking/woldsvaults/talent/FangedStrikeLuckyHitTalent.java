package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.LuckyHitDamageParticleMessage;
import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.entities.projectiles.CustomFangEntity;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

import java.util.Optional;

public class FangedStrikeLuckyHitTalent extends LuckyHitTalent {
   private float damageIncrease;

   public FangedStrikeLuckyHitTalent(int unlockLevel, int learnPointCost, int regretPointCost, float damageIncrease) {
      super(unlockLevel, learnPointCost, regretPointCost);
      this.damageIncrease = damageIncrease;
   }

   public FangedStrikeLuckyHitTalent() {
   }

   @Override
   public void onLuckyHit(LivingHurtEvent event) {
      if(event.getSource().getEntity() instanceof ServerPlayer serverPlayer && WoldEventHelper.isNormalAttack()) {
         procFangedStrike(serverPlayer, event.getEntityLiving(), event.getAmount() * damageIncrease);
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

   public static void procFangedStrike(ServerPlayer player, LivingEntity target, float baseFangDamage) {
      if (player == null || target == null || !player.isAlive()) return;

      if(WoldActiveFlags.IS_FANG_ATTACKING.isSet()) {
         return;
      }

      ServerLevel level = player.getLevel();

      CustomFangEntity fang = new CustomFangEntity(
              level,
              target.getX(),
              target.getY(),
              target.getZ(),
              player.getYRot(),
              player,
              baseFangDamage,
              0.0F,
              0,
              0,
              false
      );

      level.addFreshEntity(fang);

      level.playSound(
              null,
              target.blockPosition(),
              SoundEvents.EVOKER_FANGS_ATTACK,
              SoundSource.PLAYERS,
              1.0F,
              0.8F + (baseFangDamage * 0.1F)
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
         return nbt;
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
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.damageIncrease = Adapters.FLOAT.readJson(json.get("damageIncrease")).orElseThrow();
   }
}
