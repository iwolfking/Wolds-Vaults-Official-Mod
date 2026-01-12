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

public class ClassicWaveCrystalLayout extends ClassicInfiniteCrystalLayout {
   protected int length;
   protected int amplitude;
   protected double frequency;

   public ClassicWaveCrystalLayout() {
   }

   public ClassicWaveCrystalLayout(int tunnelSpan, int width, int height, double frequency) {
      super(tunnelSpan);
      this.length = width;
      this.amplitude = height;
      this.frequency = frequency;
   }

   @Override
   public void configure(Vault vault, RandomSource random, String sigil) {
      vault.getOptional(Vault.WORLD).map(world -> world.get(WorldManager.GENERATOR)).ifPresent(generator -> {
         if (generator instanceof GridGenerator grid) {
            grid.set(GridGenerator.LAYOUT, new ClassicWaveLayout(this.tunnelSpan, this.length, this.amplitude, this.frequency));
         }
      });
   }

   @Override
   public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
      tooltip.add(new TextComponent("Layout: ").append(new TextComponent("Wave").withStyle(ChatFormatting.BLUE)));
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         nbt.putInt("length", this.length);
         nbt.putInt("amplitude", this.amplitude);
         nbt.putDouble("frequency", this.frequency);
         return nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.length = nbt.getInt("length");
      this.amplitude = nbt.getInt("amplitude");
      this.frequency = nbt.getDouble("frequency");
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         json.addProperty("length", this.length);
         json.addProperty("amplitude", this.amplitude);
         json.addProperty("frequency", this.frequency);
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.length = json.get("length").getAsInt();
      this.amplitude = json.get("amplitude").getAsInt();
      this.frequency = json.get("frequency").getAsDouble();
   }

   public int getLength() {
      return length;
   }

   public int getAmplitude() {
      return amplitude;
   }

   public double getFrequency() {
      return frequency;
   }


}