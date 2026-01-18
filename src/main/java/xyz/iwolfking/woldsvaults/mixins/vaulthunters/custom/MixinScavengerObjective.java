package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.ScavengerAltarTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.ScavengerObjective;
import iskallia.vault.core.vault.objective.scavenger.ScavengerGoal;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.items.ItemScavengerPouch;

import java.util.List;

import static iskallia.vault.core.vault.objective.ScavengerObjective.GOALS;

@Mixin(value = ScavengerObjective.class, remap = false)
public abstract class MixinScavengerObjective extends Objective {
    @Inject(method = "initServer", at = @At("TAIL"))
    private void addScavBagHandling(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.SCAVENGER_ALTAR_CONSUME.register(this, data -> {
            if (!(data.getTile() instanceof ScavengerAltarTileEntity entity)) return;
            if (!(entity.getHeldItem().getItem() instanceof ItemScavengerPouch)) return;
            ItemStack pouch = entity.getHeldItem();
            OverSizedInventory inv = ItemScavengerPouch.getInventory(pouch);

            List<ScavengerGoal> goals = this.get(GOALS)
                    .get(entity.getItemPlacedBy());

            boolean changed = false;

            for(int i = 0; i < inv.getOverSizedContents().size(); i++) {
                OverSizedItemStack os = inv.getOverSizedContents().get(i);
                if (os.isEmpty()) continue;

                ItemStack temp = os.overSizedStack().copy();

                if (!temp.hasTag()) {
                    continue;
                }

                if(!temp.getTag().contains("VaultId")) {
                    continue;
                }

                Listener listener = vault.get(Vault.LISTENERS).get(entity.getItemPlacedBy());



                if(!temp.getTag().getString("VaultId").equals(vault.get(Vault.ID).toString())) {
                    if(!listener.getPlayer().map(ServerPlayer::isCreative).orElse(false)) {
                        System.out.println("Not creative and ID doesn't match!");
                        continue;
                    }
                }

                int before = temp.getCount();
                for (ScavengerGoal goal : goals) {
                    goal.consume(temp);
                }

                int consumed = before - temp.getCount();
                if (consumed > 0) {
                    System.out.println("Item was consumed!");
                    ItemScavengerPouch.getInventory(pouch).removeItem(i, consumed);
                    changed = true;
                }
            }

            if (changed) {
                System.out.println("Saving inventory!");
                ItemScavengerPouch.getInventory(pouch).save();
            }
        });

    }
}
