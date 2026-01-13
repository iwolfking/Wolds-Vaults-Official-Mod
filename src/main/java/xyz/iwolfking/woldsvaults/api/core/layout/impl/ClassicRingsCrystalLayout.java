package xyz.iwolfking.woldsvaults.api.core.layout.impl;

import com.google.gson.JsonObject;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class ClassicRingsCrystalLayout extends ClassicInfiniteCrystalLayout {
   protected int radius;
   protected int ringInterval;

   public ClassicRingsCrystalLayout() {
   }

   public ClassicRingsCrystalLayout(int tunnelSpan, int width, int height) {
      super(tunnelSpan);
      this.radius = width;
      this.ringInterval = height;
   }

   @Override
   public void configure(Vault vault, RandomSource random, String sigil) {
      vault.getOptional(Vault.WORLD).map(world -> world.get(WorldManager.GENERATOR)).ifPresent(generator -> {
         if (generator instanceof GridGenerator grid) {
            grid.set(GridGenerator.LAYOUT, new ClassicRingsLayout(this.tunnelSpan, this.radius, this.ringInterval));
         }
      });
   }

   @Override
   public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
      tooltip.add(new TextComponent("Layout: ").append(new TextComponent("Rings").withStyle(ChatFormatting.LIGHT_PURPLE)));
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         nbt.putInt("radius", this.radius);
         nbt.putInt("ringInterval", this.ringInterval);
         return nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.radius = nbt.getInt("radius");
      this.ringInterval = nbt.getInt("ringInterval");
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         json.addProperty("radius", this.radius);
         json.addProperty("ringInterval", this.ringInterval);
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.radius = json.get("radius").getAsInt();
      this.ringInterval = json.get("ringInterval").getAsInt();
   }

   public int getRadius() {
      return radius;
   }

   public int getRingInterval() {
      return ringInterval;
   }


}