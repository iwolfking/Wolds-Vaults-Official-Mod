package xyz.iwolfking.woldsvaults.effect.traps;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.vault.chest.VaultChestEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MobBuffTrapEffect extends VaultChestEffect {
   @Expose private final List<String> effects;
   @Expose private final double radius;
   @Expose private final int duration;
   @Expose private final int amplifier;

   public MobBuffTrapEffect(String name, double radius, int duration, int amplifier, List<MobEffect> effects) {
      super(name);
      this.radius = radius;
      this.duration = duration;
      this.amplifier = amplifier;
      this.effects = effects.stream()
         .map(effect -> effect.getRegistryName().toString())
         .collect(Collectors.toList());
   }

   public List<MobEffect> getEffects() {
      return this.effects.stream()
         .map(s -> Registry.MOB_EFFECT.getOptional(ResourceLocation.parse(s)).orElse(null))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   @Override
   public void apply(VirtualWorld world, Vault vault, ServerPlayer player, BlockPos chestPos) {
      List<MobEffect> mobEffects = this.getEffects();
      if (mobEffects.isEmpty()) return;

      AABB area = new AABB(chestPos).inflate(this.radius);
      List<Mob> mobs = world.getEntitiesOfClass(Mob.class, area);

      for (Mob mob : mobs) {
         for (MobEffect effect : mobEffects) {
            mob.addEffect(new MobEffectInstance(effect, this.duration, this.amplifier));
         }

      }

      if (!mobs.isEmpty()) {
         world.playSound(null, chestPos, SoundEvents.WOLF_HOWL, SoundSource.HOSTILE, 0.25F, 0.5F);
      }
   }
}