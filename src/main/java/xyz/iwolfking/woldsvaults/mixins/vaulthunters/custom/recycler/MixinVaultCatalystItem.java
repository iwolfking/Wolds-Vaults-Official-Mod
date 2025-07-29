package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.recycler;

import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.item.InfusedCatalystItem;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.core.VaultLevelItem;
import iskallia.vault.item.gear.RecyclableItem;
import iskallia.vault.item.render.core.IManualModelLoading;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.config.fake.CatalystVaultRecylerConfig;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = InfusedCatalystItem.class, remap = false)
public abstract class MixinVaultCatalystItem extends Item implements VaultLevelItem, IManualModelLoading, DataInitializationItem, RecyclableItem {

    public MixinVaultCatalystItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isValidInput(ItemStack itemStack) {
        return true;
    }

    @Override
    public VaultRecyclerConfig.RecyclerOutput getOutput(ItemStack itemStack) {
        return CatalystVaultRecylerConfig.CATALYST_VAULT_RECYCLER_OUTPUT;
    }

    @Override
    public float getResultPercentage(ItemStack itemStack) {
        return 1.0F;
    }

    @Override
    public Optional<UUID> getUuid(ItemStack itemStack) {
        return Optional.of(UUID.randomUUID());
    }
}
