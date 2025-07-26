package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.RoyaleDraftContainer;
import iskallia.vault.container.spi.AbstractElementContainer;
import iskallia.vault.gear.trinket.TrinketEffectRegistry;
import iskallia.vault.init.ModItems;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = RoyaleDraftContainer.class, remap = false)
public abstract class MixinRoyaleDraftContainer extends AbstractElementContainer {
    @Shadow @Final private List<ResourceLocation> blueTrinkets;
    @Shadow @Final private List<ResourceLocation> redTrinkets;

    public MixinRoyaleDraftContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }


    /**
     * @author iwolfking
     * @reason Set trinket to appropriate slot index
     */
    @Overwrite
    public boolean selectTrinket(ResourceLocation trinket, boolean isBlue) {
        List<ResourceLocation> targetList = isBlue ? this.blueTrinkets : this.redTrinkets;
        if (!targetList.contains(trinket)) {
            return false;
        } else {
            Player var5 = this.player;
            if (var5 instanceof ServerPlayer sPlayer) {
                ItemStack stack = TrinketItem.createBaseTrinket(TrinketEffectRegistry.getEffect(trinket));
                String identifier = TrinketItem.getSlotIdentifier(stack).orElse(null);
                if(identifier == null) {
                    return false;
                }

                if(IntegrationCurios.getCurioItemStack(sPlayer, identifier, 0).isEmpty()) {
                    IntegrationCurios.setCurioItemStack(sPlayer, new ItemStack(ModItems.TRINKET), identifier, 0);
                }
                else {
                    IntegrationCurios.setCurioItemStack(sPlayer, new ItemStack(ModItems.TRINKET), identifier, 1);
                }

                return true;
            } else {
                return false;
            }
        }
    }
}
