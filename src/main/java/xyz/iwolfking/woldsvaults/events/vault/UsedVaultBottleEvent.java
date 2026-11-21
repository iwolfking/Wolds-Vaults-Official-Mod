package xyz.iwolfking.woldsvaults.events.vault;

import iskallia.vault.core.event.Event;
import iskallia.vault.item.bottle.PotionEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Optional;

public class UsedVaultBottleEvent extends Event<UsedVaultBottleEvent, UsedVaultBottleEvent.Data> {
    public UsedVaultBottleEvent() {
    }

    protected UsedVaultBottleEvent(UsedVaultBottleEvent parent) {
        super(parent);
    }

    public UsedVaultBottleEvent createChild() {
        return new UsedVaultBottleEvent(this);
    }

    public UsedVaultBottleEvent.Data invoke(Level world, BlockPos pos, ServerPlayer player, ItemStack bottleStack, PotionEffect effect) {
        return this.invoke(new UsedVaultBottleEvent.Data(world, pos, player, bottleStack, effect));
    }

    public static class Data {
        private final Level world;
        private final BlockPos pos;
        private final ItemStack bottleStack;
        private PotionEffect effect;
        private final ServerPlayer player;
        private boolean cancelled = false;
        private boolean consumeCharge = true;

        public Data(Level world, BlockPos pos, ServerPlayer player, ItemStack bottleStack, PotionEffect effect) {
            this.world = world;
            this.pos = pos;
            this.player = player;
            this.bottleStack = bottleStack;
            this.effect = effect;
        }

        public Level getWorld() {
            return this.world;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public ItemStack getStack() {
            return this.bottleStack;
        }

        public Optional<PotionEffect> getEffect() {
            return Optional.ofNullable(effect);
        }

        public void setEffect(PotionEffect effect) {
             this.effect = effect;
        }

        public ServerPlayer getDrinker() {
            return player;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public void setShouldConsumeCharge(boolean shouldConsumeCharge) {
            this.consumeCharge = shouldConsumeCharge;
        }

        public boolean isCancelled() {
            return this.cancelled;
        }

        public boolean shouldConsumeCharge() {
            return this.consumeCharge;
        }
    }
}