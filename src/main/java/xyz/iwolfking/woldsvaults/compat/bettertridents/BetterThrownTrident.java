package xyz.iwolfking.woldsvaults.compat.bettertridents;

import fuzs.bettertridents.BetterTridents;
import fuzs.bettertridents.config.ServerConfig;
import fuzs.bettertridents.init.ModRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BetterThrownTrident extends ThrownTrident {
    public BetterThrownTrident(Level level, Player player, ItemStack stack) {
        super(level, player, stack);

        if (!BetterTridents.CONFIG.get(ServerConfig.class).returnTridentToSlot)
            return;

        if (player.getUseItem() == stack) {
            ModRegistry.TRIDENT_SLOT_CAPABILITY.maybeGet(this).ifPresent(capability -> {
                if (player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                    capability.setSlot(40);
                } else {
                    capability.setSlot(player.getInventory().selected);
                }
            });
        }
    }
}
