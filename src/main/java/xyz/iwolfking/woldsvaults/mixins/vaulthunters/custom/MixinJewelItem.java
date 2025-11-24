package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.config.entry.ChanceItemStackEntry;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.models.Jewels;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = JewelItem.class, remap = false)
public abstract class MixinJewelItem extends Item implements VaultGearItem, DataInitializationItem, DataTransferItem {

    private ServerPlayer player;

    public MixinJewelItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(this.player == null && pEntity instanceof ServerPlayer player) {
            this.player = player;
        }
    }


    @Override
    public Optional<? extends DynamicModel<?>> resolveDynamicModel(ItemStack stack, ResourceLocation key) {
        return Jewels.REGISTRY.get(key);
    }

    /**
     * @author iwolfking
     * @reason Try stuff
     */
    @Overwrite
    public void initialize(ItemStack stack, RandomSource random) {
        if(player != null) {
            this.instantIdentify(player, stack);
        }
        else {
            this.instantIdentify(null, stack);
        }

    }
}
