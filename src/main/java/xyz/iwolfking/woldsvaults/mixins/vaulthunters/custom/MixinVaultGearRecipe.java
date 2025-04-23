package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.crafting.recipe.GearForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.world.data.PlayerGreedData;
import iskallia.vault.world.data.PlayerStatsData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

@Mixin(value = GearForgeRecipe.class, remap = false)
public abstract class MixinVaultGearRecipe extends VaultForgeRecipe {

    @Unique
    private static final ResourceLocation ETCHING_LOCATION = new ResourceLocation("the_vault", "etching");
    @Unique
    private static final ResourceLocation MAP_LOCATION = new ResourceLocation("the_vault", "map");
    private static final ResourceLocation ZEPHYR_LOCATION = new ResourceLocation("woldsvaults", "zephyr_charm");

    protected MixinVaultGearRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }

    /**
     * @author iwolfking
     * @reason Lock etching crafting behind Herald completion.
     */
    @Override
    public boolean canCraft(Player player) {
        if(!this.getId().equals(ETCHING_LOCATION) && !this.getId().equals(MAP_LOCATION) && !this.getId().equals(ZEPHYR_LOCATION)) {
            return true;
        }
        else {
           return PlayerGreedData.get().get(player).hasCompletedHerald() || player.isCreative();
        }
    }

    /**
     * @author iwolfking
     * @reason Allow non-gear items to display in Vault Forge.
     */
    @Overwrite
    public ItemStack getDisplayOutput(int vaultLevel) {
        ItemStack out = super.getDisplayOutput(vaultLevel);
        if(!(out.getItem() instanceof VaultGearItem)) {
            return out;
        }

        VaultGearData data = VaultGearData.read(out);
        data.setState(VaultGearState.IDENTIFIED);
        data.setItemLevel(vaultLevel);
        data.write(out);
        return out;
    }
}
