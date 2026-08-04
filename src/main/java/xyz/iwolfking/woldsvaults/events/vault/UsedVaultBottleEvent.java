package xyz.iwolfking.woldsvaults.events.vault;

import iskallia.vault.core.event.Event;
import iskallia.vault.item.bottle.BottleEffect;
import iskallia.vault.item.bottle.BottleItem;
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

    public UsedVaultBottleEvent.Data invoke(Level world, BlockPos pos, ServerPlayer player, ItemStack bottleStack, BottleEffect effect, BottleItem.Type bottleType) {
        return this.invoke(new UsedVaultBottleEvent.Data(world, pos, player, bottleStack, effect, bottleType));
    }

    public static class Data {
        private final Level world;
        private final BlockPos pos;
        private final ItemStack bottleStack;
        private BottleEffect effect;
        private final BottleItem.Type type;
        private final ServerPlayer player;
        private boolean cancelled = false;
        private boolean consumeCharge = true;

        public Data(Level world, BlockPos pos, ServerPlayer player, ItemStack bottleStack, BottleEffect effect, BottleItem.Type bottleType) {
            this.world = world;
            this.pos = pos;
            this.player = player;
            this.bottleStack = bottleStack;
            this.effect = effect;
            this.type = bottleType;
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

        public Optional<BottleEffect> getEffect() {
            return Optional.ofNullable(effect);
        }

        public void setEffect(BottleEffect effect) {
             this.effect = effect;
        }

        public Optional<BottleItem.Type> getType() {
            return Optional.ofNullable(type);
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