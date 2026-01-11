package xyz.iwolfking.woldsvaults.api.core.layout.impl;

import com.google.gson.JsonObject;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.ClassicCircleLayout;
import java.util.List;
import java.util.Optional;

import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

public class ClassicTestCrystalLayout extends ClassicInfiniteCrystalLayout {
   protected int width;
   protected int rowStep;
   protected int branchInterval;

   public ClassicTestCrystalLayout() {
   }

   public ClassicTestCrystalLayout(int tunnelSpan, int width, int rowStep, int branchInterval) {
      super(tunnelSpan);
      this.width = width;
      this.rowStep= rowStep;
      this.branchInterval = branchInterval;
   }

   @Override
   public void configure(Vault vault, RandomSource random, String sigil) {
      vault.getOptional(Vault.WORLD).map(world -> world.get(WorldManager.GENERATOR)).ifPresent(generator -> {
         if (generator instanceof GridGenerator grid) {
            grid.set(GridGenerator.LAYOUT, new ClassicSerpentineLayout(this.tunnelSpan, this.width, this.rowStep, this.branchInterval));
         }
      });
   }

   @Override
   public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
      tooltip.add(new TextComponent("Layout: ").append(new TextComponent("Serpentine").withStyle(ChatFormatting.GREEN)));
   }

   @Override
   public Optional<CompoundTag> writeNbt() {
      return super.writeNbt().map(nbt -> {
         nbt.putInt("width", this.width);
         nbt.putInt("rowStep", this.rowStep);
         nbt.putInt("branchInterval", this.branchInterval);
         return (CompoundTag)nbt;
      });
   }

   @Override
   public void readNbt(CompoundTag nbt) {
      super.readNbt(nbt);
      this.width = nbt.getInt("width");
      this.rowStep = nbt.getInt("rowStep");
      this.branchInterval = nbt.getInt("branchInterval");
   }

   @Override
   public Optional<JsonObject> writeJson() {
      return super.writeJson().map(json -> {
         json.addProperty("width", this.width);
         json.addProperty("rowStep", this.width);
         json.addProperty("branchInterval", this.width);
         return (JsonObject)json;
      });
   }

   @Override
   public void readJson(JsonObject json) {
      super.readJson(json);
      this.width = json.get("width").getAsInt();
      this.width = json.get("rowStep").getAsInt();
      this.width = json.get("branchInterval").getAsInt();
   }

   public int getWidth() {
      return width;
   }

   public int getRowStep() {
      return rowStep;
   }

   public int getBranchInterval() {
      return branchInterval;
   }


}