package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperModifierPolicy;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class VaultModifierFromPoolTask implements VaultEventTask {

    /**
     * Hyper vaults curate their negative modifiers (no CDR drains, no ethereal, no fading...).
     * Enchanted events on the crystal would otherwise pull from these raw pools and smuggle
     * every banned modifier back in; in a hyper vault the pull is redirected to the filtered
     * union of exactly these pools.
     */
    private static final Set<ResourceLocation> HYPER_REDIRECTED_POOLS = Set.of(
            VaultMod.id("basic_negative"), VaultMod.id("medium_negative"), VaultMod.id("omega_negative"));

    private final ResourceLocation modifierPoolId;

    public VaultModifierFromPoolTask(ResourceLocation modifierId) {
        this.modifierPoolId = modifierId;
    }


    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        ResourceLocation poolId = this.modifierPoolId;
        if (HYPER_REDIRECTED_POOLS.contains(poolId)
                && !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty()) {
            WoldsVaults.LOGGER.info("Enchanted event redirected its {} modifier pull to the filtered hyper pool.", poolId);
            // Hyper stack caps (frenzy, mana leak, ...) must hold on this path too; a capped
            // roll is simply skipped (the event fizzles, matching the other hyper add paths).
            List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS
                    .getRandom(HyperVaultObjective.CHAOS_POOL_TIMER_EVENTS, 0, JavaRandom.ofNanoTime());
            for (VaultModifier<?> modifier : modifiers) {
                if (HyperModifierPolicy.isStackCapped(vault, modifier)) {
                    continue;
                }
                VaultModifierUtils.addModifier(vault, modifier.getId(), 1);
            }
            return;
        }
        VaultModifierUtils.addModifierFromPool(vault, poolId);
    }
}
