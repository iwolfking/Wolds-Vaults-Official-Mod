package xyz.iwolfking.woldsvaults.prestige;

import com.google.gson.JsonObject;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.api.data.discovery.DiscoveredRecipesData;

import java.util.Optional;

public class CraftingRecipePower extends PrestigePower {
    private ResourceLocation recipeUnlock;

    public CraftingRecipePower() {
    }

    @Override
    public void learn(SkillContext context) {
        context.getSource().as(LivingEntity.class).ifPresent(entity -> {
            if(entity instanceof ServerPlayer player) {
                if(!DiscoveredRecipesData.get(player.getLevel()).hasDiscovered(player, recipeUnlock)) {
                    DiscoveredRecipesData.get(player.getLevel()).discoverRecipeAndBroadcast(recipeUnlock, player);
                }
            }
        });
        super.learn(context);
    }

    public CraftingRecipePower(ResourceLocation recipeUnlock) {
        this.recipeUnlock = recipeUnlock;
    }

    public ResourceLocation getRecipeUnlock() {
        return recipeUnlock;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            nbt.putString("recipeUnlock", recipeUnlock.toString());
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        String recipeUnlockString = nbt.getString("recipeUnlock");
        this.recipeUnlock = ResourceLocation.parse(recipeUnlockString);
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            json.addProperty("recipeUnlock", recipeUnlock.toString());
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        String recipeUnlockString = json.get("recipeUnlock").getAsString();
        this.recipeUnlock = ResourceLocation.parse(recipeUnlockString);
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        buffer.writeString(this.recipeUnlock.toString());
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.recipeUnlock = ResourceLocation.parse(buffer.readString());
    }
}
