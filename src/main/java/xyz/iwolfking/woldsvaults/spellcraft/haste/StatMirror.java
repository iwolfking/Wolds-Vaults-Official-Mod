package xyz.iwolfking.woldsvaults.spellcraft.haste;

import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.StatMirrorMessage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-computed values of the player stats whose bonuses come from server-only listeners (Mind Meld's spell haste,
 * vault modifiers, god and charm hooks), mirrored to the owning client. The client's stat helpers return the mirrored
 * value for the local player, so the statistics tab and spell previews show the same numbers the server uses.
 */
public final class StatMirror {
    public static final int HASTE = 0;
    public static final int AREA = 1;
    public static final int ABILITY_POWER = 2;
    private static final int COUNT = 3;
    private static final Map<UUID, float[]> SENT = new HashMap<>();
    private static UUID clientPlayer;
    private static float[] clientValues;

    private StatMirror() {
    }

    /** Sends the player's current values when they changed since the last send. */
    public static void sync(ServerPlayer player) {
        float[] values = new float[COUNT];
        values[HASTE] = CooldownHelper.getCooldownMultiplierUnlimited(player);
        values[AREA] = AreaOfEffectHelper.getAreaOfEffectUnlimited(player);
        values[ABILITY_POWER] = AbilityPowerHelper.getAbilityPower(player);
        float[] previous = SENT.get(player.getUUID());
        if (previous == null || !Arrays.equals(previous, values)) {
            SENT.put(player.getUUID(), values);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StatMirrorMessage(player.getUUID(), values));
        }
    }

    public static void forget(UUID player) {
        SENT.remove(player);
    }

    /** Called on the client when a mirror arrives. */
    public static void receive(UUID player, float[] values) {
        clientPlayer = player;
        clientValues = values;
    }

    public static void clearClient() {
        clientPlayer = null;
        clientValues = null;
    }

    /** The mirrored value of a stat for this entity on the client, or null when there is none to use. */
    @Nullable
    public static Float client(Entity entity, int stat) {
        if (entity == null || !entity.level.isClientSide() || clientValues == null || !entity.getUUID().equals(clientPlayer)) {
            return null;
        }
        return clientValues[stat];
    }
}
