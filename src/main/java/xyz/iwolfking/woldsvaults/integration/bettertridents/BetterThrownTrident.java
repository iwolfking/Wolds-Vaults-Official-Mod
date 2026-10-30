package xyz.iwolfking.woldsvaults.integration.bettertridents;

import fuzs.bettertridents.BetterTridents;
import fuzs.bettertridents.config.ServerConfig;
import fuzs.bettertridents.init.ModRegistry;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRIDENT_LOYALTY;

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

    @Override
    protected void outOfWorld() {
        if (!BetterTridents.CONFIG.get(ServerConfig.class).returnTridentFromVoid) {
            super.outOfWorld();
            return;
        }
        VaultGearData data = VaultGearData.read(this.getPickupItem());
        if (data.get(TRIDENT_LOYALTY, VaultGearAttributeTypeMerger.intSum()) >= 1 && this.canReturn()) {
            this.setNoPhysics(true);
            this.playerTouch((Player) this.getOwner());
            return;
        }
        super.outOfWorld();
    }

    private boolean canReturn() {
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        } else {
            return false;
        }
    }
}
