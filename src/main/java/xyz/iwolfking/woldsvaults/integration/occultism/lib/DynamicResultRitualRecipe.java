package xyz.iwolfking.woldsvaults.integration.occultism.lib;

import com.github.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.CrystalDataUtils;
import xyz.iwolfking.woldsvaults.integration.occultism.impl.VaultCrystalRitual;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DynamicResultRitualRecipe extends RitualRecipe {
    private final ResourceLocation customId;
    private final Supplier<RecipeSerializer<?>> serializerSupplier;
    private final BiFunction<ResourceLocation, ItemStack, ItemStack> resultFactory;

    public DynamicResultRitualRecipe(RitualRecipe base, ResourceLocation customId, 
                                     Supplier<RecipeSerializer<?>> serializerSupplier, 
                                     BiFunction<ResourceLocation, ItemStack, ItemStack> resultFactory) {
        super(base.getId(), base.getGroup(), base.getPentacleId(), base.getRitualType(), 
              base.getRitualDummy(), resultFactory.apply(customId, ItemStack.EMPTY), base.getEntityToSummon(), 
              base.getEntityNbt(), base.getActivationItem(), base.getIngredients(), 
              base.getDuration(), base.getSpiritMaxAge(), base.getSpiritJobType(), 
              base.getEntityToSacrifice(), base.getEntityToSacrificeDisplayName(), 
              base.getItemToUse(), base.getCommand());
        
        this.customId = customId;
        this.serializerSupplier = serializerSupplier;
        this.resultFactory = resultFactory;
    }

    public ResourceLocation getCustomId() {
        return this.customId;
    }

    public ItemStack getResultItem(ItemStack activationItem) {
        return this.resultFactory.apply(this.customId, activationItem);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return this.serializerSupplier.get();
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return getResultItem(getActivationItem().getItems()[0]);
    }

    @Override
    public boolean matches(Level level, BlockPos goldenBowlPosition, ItemStack activationItem) {
        if (activationItem.getItem() instanceof VaultCrystalItem) {
            CrystalData data = CrystalData.read(activationItem);
            if (data.getProperties().isUnmodifiable()) {
                return false;
            }

            if(this.customId.equals(VaultCrystalRitual.RANDOM_ZEALOT_VAULT) && CrystalDataUtils.isModified(data)) {
                return false;
            }
        }

        return super.matches(level, goldenBowlPosition, activationItem);
    }

    public static class Serializer extends RitualRecipe.Serializer {
        private final String jsonKey;
        private final Supplier<RecipeSerializer<?>> serializerSupplier;
        private final BiFunction<ResourceLocation, ItemStack, ItemStack> resultFactory;

        public Serializer(String jsonKey, Supplier<RecipeSerializer<?>> serializerSupplier, BiFunction<ResourceLocation, ItemStack, ItemStack> resultFactory) {
            this.jsonKey = jsonKey;
            this.serializerSupplier = serializerSupplier;
            this.resultFactory = resultFactory;
        }

        @Override
        public DynamicResultRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            RitualRecipe base = super.fromJson(recipeId, json);
            if (!json.has(this.jsonKey)) {
                throw new JsonParseException("Ritual recipe is missing its mandatory '" + this.jsonKey + "' property!");
            }
            ResourceLocation customId = ResourceLocation.parse(GsonHelper.getAsString(json, this.jsonKey));
            return new DynamicResultRitualRecipe(base, customId, serializerSupplier, resultFactory);
        }

        @Override
        public DynamicResultRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            RitualRecipe base = super.fromNetwork(recipeId, buffer);
            ResourceLocation customId = buffer.readResourceLocation();
            return new DynamicResultRitualRecipe(base, customId, serializerSupplier, resultFactory);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RitualRecipe recipe) {
            super.toNetwork(buffer, recipe);
            if (recipe instanceof DynamicResultRitualRecipe dynamicRecipe) {
                buffer.writeResourceLocation(dynamicRecipe.getCustomId());
            }
        }
    }
}