package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.world.data.DownedPlayerManager;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.ultimates.DownedPlayerManagerInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Velara's ultimate. A vault-wide shockwave that heals, shields and protects every runner in the
 * vault, and pulls downed teammates back to their feet.
 *
 * <p>The roster is {@code Vault.LISTENERS}' runners rather than the {@code /party} roster: the
 * spec says "all players in the vault", and a party member in a different vault plainly should not
 * be healed by a shockwave they are not standing in. Cast outside a vault it still works, on the
 * caster alone.
 *
 * <p>The revive half is best-effort by construction. The base mod's {@code completeRevive} is
 * private and reached through an invoker mixin, so if that mixin is ever missing the heal, shield
 * and protection still land and only the revive is skipped, with a logged error. The downed system
 * itself only ever engages in multiplayer, so a solo caster never has anyone to raise.
 */
public class SaviorAbility extends GodUltimateAbility {
    public SaviorAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public SaviorAbility() {
    }

    @Override
    public VaultGod getGod() {
        return VaultGod.VELARA;
    }

    @Override
    protected ActionResult doUltimate(ServerPlayer player, int level) {
        UltimateLevels.Savior values = UltimateLevels.savior(level);
        Optional<Vault> vault = ServerVaults.get(player.level);
        List<ServerPlayer> targets = collectTargets(player, vault.orElse(null));
        for (ServerPlayer target : targets) {
            if (DownedPlayerManager.isPlayerDowned(target)) {
                continue;
            }
            target.heal(values.healing());
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), values.absorption()));
            SaviorState.apply(target, values.resistance(), values.durationTicks());
        }
        vault.ifPresent(activeVault -> reviveDowned(player, activeVault, targets, values));
        return ActionResult.successCooldownImmediate();
    }

    private static List<ServerPlayer> collectTargets(ServerPlayer caster, Vault vault) {
        List<ServerPlayer> targets = new ArrayList<>();
        if (vault == null) {
            targets.add(caster);
            return targets;
        }
        Listeners listeners = vault.get(Vault.LISTENERS);
        if (listeners == null) {
            targets.add(caster);
            return targets;
        }
        for (Runner runner : listeners.getAll(Runner.class)) {
            runner.getPlayer().ifPresent(targets::add);
        }
        if (!targets.contains(caster)) {
            targets.add(caster);
        }
        return targets;
    }

    private static void reviveDowned(ServerPlayer caster, Vault vault, List<ServerPlayer> targets,
                                     UltimateLevels.Savior values) {
        UUID vaultId = vault.get(Vault.ID);
        if (vaultId == null || caster.getServer() == null) {
            return;
        }
        UltimateReviveData revives = UltimateReviveData.get(caster.getServer());
        for (ServerPlayer target : targets) {
            if (!DownedPlayerManager.isPlayerDowned(target) || revives.hasClaimed(vaultId, target.getUUID())) {
                continue;
            }
            try {
                DownedPlayerManagerInvoker.woldsvaults$completeRevive(caster, target);
            } catch (RuntimeException | LinkageError e) {
                WoldsVaults.LOGGER.error("Savior could not revive {}: the DownedPlayerManager invoker is unavailable. "
                        + "The heal, absorption and resistance still applied.", target.getGameProfile().getName(), e);
                return;
            }
            revives.claim(vaultId, target.getUUID());
            target.heal(values.healing());
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), values.absorption()));
            SaviorState.apply(target, values.resistance(), values.durationTicks());
        }
    }
}
