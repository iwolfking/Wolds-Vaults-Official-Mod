package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.crafting.recipe.GearForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.util.SidedHelper;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.ClientPlayerGreedData;
import xyz.iwolfking.woldsvaults.items.AirMobilityItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

import java.util.List;

@Mixin(value = GearForgeRecipe.class, remap = false)
public abstract class MixinVaultGearRecipe extends VaultForgeRecipe {

    @Unique
    private static final ResourceLocation ETCHING_LOCATION = VaultMod.id("etching");
    @Unique
    private static final ResourceLocation MAP_LOCATION = VaultMod.id("map");
    private static final ResourceLocation ZEPHYR_LOCATION = WoldsVaults.id("zephyr_charm");

    protected MixinVaultGearRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }

    /**
     * @author iwolfking
     * @reason Lock etching crafting behind Herald completion.
     */
    @Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
    public void canCraft(Player player, int level, CallbackInfoReturnable<Boolean> cir) {
        if(this.getId().equals(MAP_LOCATION) || this.getId().equals(ZEPHYR_LOCATION)) {
            if (player instanceof ServerPlayer sPlayer) {
                PlayerGreedData greedData = PlayerGreedData.get(sPlayer.server);
                cir.setReturnValue(greedData.get(player).hasCompletedHerald());
            }
            else if(ClientGreedData.isCompletedHerald()) {
                cir.setReturnValue(true);
            }

            cir.setReturnValue(false);
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

    @Inject(method = "getDisabledText", at = @At("HEAD"), cancellable = true)
    public void getDisabledText(CallbackInfoReturnable<List<Component>> cir) {
        if(this.output.getItem() instanceof AirMobilityItem || this.output.getItem() instanceof VaultMapItem) {
            cir.setReturnValue(List.of(new TextComponent("Defeat The Herald by collecting all 25 Artifacts to unlock.")));
        }
    }
}
