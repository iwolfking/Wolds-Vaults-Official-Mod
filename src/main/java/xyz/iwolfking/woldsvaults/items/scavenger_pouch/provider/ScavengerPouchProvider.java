
package xyz.iwolfking.woldsvaults.items.scavenger_pouch.provider;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.scavenger_pouch.menu.ScavengerPouchContainer;

public class ScavengerPouchProvider implements MenuProvider {
    private final ServerPlayer player;
    private final int slot;
    private final ItemStack scavengerPouch;

    public ScavengerPouchProvider(ServerPlayer player, int slot, ItemStack scavengerPouch) {
        this.player = player;
        this.slot = slot;
        this.scavengerPouch = scavengerPouch;
    }

    public Component getDisplayName() {
        return this.scavengerPouch.getDisplayName();
    }

    public Consumer<FriendlyByteBuf> extraDataWriter() {
        return (buffer) -> buffer.writeInt(this.slot);
    }

    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ScavengerPouchContainer(id, this.player.getInventory(), this.slot);
    }
}
