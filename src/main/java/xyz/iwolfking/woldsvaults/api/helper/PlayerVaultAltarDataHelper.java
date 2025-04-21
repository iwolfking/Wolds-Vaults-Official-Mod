package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.altar.RequiredItems;
import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PlayerVaultAltarDataAccessor;

import java.util.List;

public class PlayerVaultAltarDataHelper {
    public static AltarInfusionRecipe generateGreedRecipe(ServerPlayer player, BlockPos pos, boolean isPogInfused) {
        PlayerVaultAltarData data = PlayerVaultAltarData.get();
        List<RequiredItems> items = ModConfigs.GREED_VAULT_ALTAR_INGREDIENTS.getIngredients(player, pos);
        AltarInfusionRecipe recipe = ((PlayerVaultAltarDataAccessor)data).getPlayerMap().computeIfAbsent(player.getUUID(), (k) -> new AltarInfusionRecipe(player.getUUID(), items, items, isPogInfused));
        data.addRecipe(player.getUUID(), recipe);
        data.setDirty();
        return recipe;
    }
}
