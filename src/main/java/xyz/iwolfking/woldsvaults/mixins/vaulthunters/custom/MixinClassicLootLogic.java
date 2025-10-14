package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.ShopPedestalConfig;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.ShopPedestalGenerationEvent;
import iskallia.vault.core.vault.ClassicLootLogic;
import iskallia.vault.core.vault.LootLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.objective.ArchitectObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.helper.ShopPedestalHelper;

import java.util.List;

@Mixin(value = ClassicLootLogic.class, remap = false)
public abstract class MixinClassicLootLogic extends LootLogic {

    /**
     * @author iwolfking
     * @reason Generate test pedestal
     */
    @Overwrite
    protected void onShopPedestalGenerate(VirtualWorld world, Vault vault, ShopPedestalGenerationEvent.Data data) {
        int level = (Integer) vault.getOptional(Vault.LEVEL).map(VaultLevel::get).orElse(0);
        ShopPedestalConfig.ShopOffer offer;
        offer = ShopPedestalHelper.generatePedestalOffer(data.getState(), world, vault, data.getRandom());


        if (offer != null && !offer.isEmpty()) {
            ItemStack stack = this.initializeLoot(vault, offer.offer().copy(), data.getPos(), data.getRandom());
            data.getTileEntity().setOffer(stack, OverSizedItemStack.of(offer.currency().overSizedStack()));
        }

        data.getTileEntity().setInitialized(true);
        data.getTileEntity().setChanged();
        world.sendBlockUpdated(data.getPos(), data.getState(), data.getState(), 3);
    }

    @Redirect(method = "generateCatalystFragments", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0))
    protected boolean generateCatalystFragments(List instance) {
        return true;
    }
}
