package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.recipe.ConfigDeckRecipe;
import iskallia.vault.config.entry.recipe.ConfigForgeRecipe;
import iskallia.vault.config.recipe.DeckCraftingRecipesConfig;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.task.Task;
import iskallia.vault.task.util.TaskProgress;
import iskallia.vault.world.data.DeckRecipeTaskData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mixin(value = DeckRecipeTaskData.class, remap = false)
public abstract class MixinDeckRecipeTaskData extends SavedData {

    @Shadow
    public static DeckRecipeTaskData get(MinecraftServer server) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    @Final
    private Map<UUID, DeckRecipeTaskData.PlayerEntry> playerEntries;

    @Shadow
    private static CompoundTag getTaskDefinition(Task task) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public abstract void ensureTracking(ServerPlayer player);

    /**
     * @author iwolfking
     * @reason Don't run this, he be broken
     */
    @Overwrite
    public static void onConfigReload(DeckCraftingRecipesConfig config) {

    }
}
