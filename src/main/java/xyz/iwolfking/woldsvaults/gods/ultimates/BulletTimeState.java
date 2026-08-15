package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRate;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wendarr's ultimate, minus the ability shell: the attack- and movement-speed buffs, the dodge
 * roll, and the stretched vault clock.
 *
 * <p>The clock half rides the wave-1 {@code VaultClockRate} primitive. The sheet's "vault timer
 * stretch" is applied as a rate <em>factor</em> of {@code 1 + stretch}, i.e. a vault second becomes
 * that many times longer. Taking the sheet's percentages as a direct subtraction instead would
 * freeze the timer at 100% and run it backwards at 120%, turning the ultimate into a time faucet;
 * the factor reading gives 0.667x clock speed at level 1 and 0.455x at level 8 and can never go
 * negative. Note the vault clock is shared, so in a party the stretch benefits everyone in the run.
 *
 * <p>Both speed buffs are transient attribute modifiers with fixed UUIDs, removed on expiry, on
 * death, on logout and on leaving the dimension, so no modifier can survive its buff the way the
 * U33 talent modifiers did.
 */
public final class BulletTimeState {
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private BulletTimeState() {
    }

    /** Starts (or restarts) bullet time on this player. */
    public static void begin(ServerPlayer player, UltimateLevels.BulletTime values) {
        end(player);
        State state = new State(values.dodgeChance(), values.durationTicks());
        ServerVaults.get(player.level).ifPresent(vault -> {
            state.vaultId = vault.get(Vault.ID);
            VaultClockRate.setRateFactor(vault, UltimateIds.BULLET_TIME_CLOCK_FACTOR, 1.0F + values.timerStretch());
        });
        addModifier(player, Attributes.ATTACK_SPEED, UltimateIds.BULLET_TIME_ATTACK_SPEED, "WoldsVaultsBulletTimeAttackSpeed",
                values.attackSpeed());
        addModifier(player, Attributes.MOVEMENT_SPEED, UltimateIds.BULLET_TIME_MOVEMENT_SPEED, "WoldsVaultsBulletTimeMovementSpeed",
                values.movementSpeed());
        STATES.put(player.getUUID(), state);
    }

    /**
     * The bullet-time dodge chance for this player, or zero. It is deliberately an independent roll
     * rather than an addition to the gear dodge stat: gear dodge is clamped at 0.95 and a geared
     * player would sit on that cap already, which would make every level of the ultimate's own
     * 0.70-0.85 ladder worthless.
     */
    public static float getDodgeChance(Player player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0.0F : state.dodgeChance;
    }

    public static boolean isActive(Player player) {
        return STATES.containsKey(player.getUUID());
    }

    public static void tick(MinecraftServer server) {
        if (STATES.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, State>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, State> entry = iterator.next();
            if (--entry.getValue().remainingTicks > 0) {
                continue;
            }
            iterator.remove();
            revert(server, entry.getKey(), entry.getValue());
        }
    }

    public static void end(Player player) {
        State state = STATES.remove(player.getUUID());
        removeModifiers(player);
        if (state != null && player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
            clearClock(serverPlayer.getServer(), state);
        }
    }

    private static void revert(MinecraftServer server, UUID playerId, State state) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            removeModifiers(player);
        }
        clearClock(server, state);
    }

    private static void clearClock(MinecraftServer server, State state) {
        if (state.vaultId == null) {
            return;
        }
        ServerVaults.get(state.vaultId)
                .ifPresent(vault -> VaultClockRate.removeRateFactor(vault, UltimateIds.BULLET_TIME_CLOCK_FACTOR));
    }

    private static void addModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                    UUID id, String name, float amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    private static void removeModifiers(Player player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(UltimateIds.BULLET_TIME_ATTACK_SPEED);
        }
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(UltimateIds.BULLET_TIME_MOVEMENT_SPEED);
        }
    }

    private static final class State {
        private final float dodgeChance;
        private int remainingTicks;
        private UUID vaultId;

        private State(float dodgeChance, int remainingTicks) {
            this.dodgeChance = dodgeChance;
            this.remainingTicks = remainingTicks;
        }
    }
}
