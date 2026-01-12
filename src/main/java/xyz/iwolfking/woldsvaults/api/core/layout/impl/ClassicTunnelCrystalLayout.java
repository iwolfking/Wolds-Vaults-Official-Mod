package xyz.iwolfking.woldsvaults.api.core.layout.impl;

import com.google.gson.JsonObject;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.generator.GridGenerator;

import java.util.List;
import java.util.Optional;

import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

public class ClassicTunnelCrystalLayout extends ClassicInfiniteCrystalLayout {
   protected int width;
   protected int height;
   protected int branchInterval;

   public ClassicTunnelCrystalLayout() {
   }

   public ClassicTunnelCrystalLayout(int tunnelSpan, int width, int height, int branchInterval) {
      super(tunnelSpan);
      this.width = width;
      this.height = height;
      this.branchInterval = branchInterval;
   }

   @Override
   public void configure(Vault vault, RandomSource random, String sigil) {
      vault.getOptional(Vault.WORLD).map(world -> world.get(WorldManager.GENERATOR)).ifPresent(generator -> {
         if (generator instanceof GridGenerator grid) {
            grid.set(GridGenerator.LAYOUT, new ClassicTunnelLayout(this.tunnelSpan, this.width, this.height, this.branchInterval));
         }
      });
   }

   @Override
   public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
      tooltip.add(new TextComponent("Layout: ").append(new TextComponent("Tunnels").withStyle(ChatFormatting.DARK_PURPLE)));
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         nbt.putInt("width", this.width);
         nbt.putInt("height", this.height);
         nbt.putInt("branchInterval", this.branchInterval);
         return nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.width = nbt.getInt("width");
      this.height = nbt.getInt("height");
      this.branchInterval = nbt.getInt("branchInterval");
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         json.addProperty("width", this.width);
         json.addProperty("height", this.height);
         json.addProperty("branchInterval", this.branchInterval);
         return json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.width = json.get("width").getAsInt();
      this.height = json.get("height").getAsInt();
      this.branchInterval = json.get("branchInterval").getAsInt();
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   public int getBranchInterval() {
      return branchInterval;
   }


}