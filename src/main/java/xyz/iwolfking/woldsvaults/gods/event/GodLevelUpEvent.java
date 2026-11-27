package xyz.iwolfking.woldsvaults.gods.event;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/** Fired on the Forge event bus, server side, once per alignment level a player gains with a god. */
public class GodLevelUpEvent extends Event {
    private final ServerPlayer player;
    private final VaultGod god;
    private final int newLevel;

    public GodLevelUpEvent(ServerPlayer player, VaultGod god, int newLevel) {
        this.player = player;
        this.god = god;
        this.newLevel = newLevel;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public VaultGod getGod() {
        return this.god;
    }

    public int getNewLevel() {
        return this.newLevel;
    }
}
