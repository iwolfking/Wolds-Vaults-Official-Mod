package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class PlayerStatModifierSettable extends SettableValueVaultModifier<PlayerStatModifierSettable.Properties> {
    public PlayerStatModifierSettable(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
        MinecraftForge.EVENT_BUS.register(this);
    }


    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.PLAYER_STAT.register(context.getUUID(), (data) -> {
            if (data.getStat() == (this.properties.getStat())) {
                if (((Listeners)vault.get(Vault.LISTENERS)).contains(data.getEntity().getUUID())) {
                    if (!context.hasTarget() || context.getTarget().equals(data.getEntity().getUUID())) {
                        data.setValue(data.getValue() + (this.properties).getValue());
                    }
                }
            }
        });
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final PlayerStat stat;

        public Properties(PlayerStat stat) {
            this.stat = stat;
        }

        public PlayerStat getStat() {
            return this.stat;
        }
    }
}
