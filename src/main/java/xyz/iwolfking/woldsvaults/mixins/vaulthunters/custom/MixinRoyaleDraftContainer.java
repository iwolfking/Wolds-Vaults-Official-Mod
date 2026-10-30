package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.RoyaleDraftContainer;
import iskallia.vault.container.spi.AbstractElementContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRoyale;

@Mixin(value = RoyaleDraftContainer.class, remap = false)
public abstract class MixinRoyaleDraftContainer extends AbstractElementContainer {
    @Shadow public abstract int getEntityId();

    protected MixinRoyaleDraftContainer(MenuType<?> menuType, int id, Player player) {
        super(menuType, id, player);
    }

    /**
     * @author iwolfking
     * @reason Insert the chosen trinket into the Royale pouch
     */
    @Overwrite
    public boolean selectTrinket(ResourceLocation trinket, boolean isBlue) {
        return player instanceof ServerPlayer serverPlayer && PouchRoyale.select(serverPlayer, trinket, getEntityId(), isBlue);
    }
}
