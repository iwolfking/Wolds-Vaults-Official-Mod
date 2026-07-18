package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModEffects;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * The hyper vault's always-on gameplay rules that must run on the Forge bus (they intercept
 * events VH's own CommonEvents never see). Everything here is hyper-vault-gated at runtime;
 * for non-hyper worlds the handlers cost an instanceof / early return.
 *
 * <p>The per-hit damage diagnostics that used to share a class with these live in
 * {@link HyperBossDamageInstrumentation}, gated behind the debug-mode config.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperVaultEvents {
    private HyperVaultEvents() {
    }

    static boolean isInHyperVault(LivingEntity entity) {
        return ServerVaults.get(entity.level)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
    }

    /**
     * Checks whether the entity is the hyper objective's boss: a rune boss entity inside a
     * hyper vault.
     */
    public static boolean isHyperBoss(LivingEntity entity) {
        return entity instanceof VaultBossEntity && isInHyperVault(entity);
    }

    /**
     * The hyperboss takes no %-max-health damage-over-time: with hyper-scaled health pools a
     * single Bleed tick (amplifier × 2.5% of max health, see MixinBleedEffect) deals billions.
     * Bleed is denied at application here; Reaving's on-hit proc is neutralized by pre-applying
     * its own once-per-mob latch effect in HyperBossManager.
     */
    @SubscribeEvent
    public static void denyBleedOnHyperboss(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.BLEED) {
            return;
        }
        if (isHyperBoss(event.getEntityLiving())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * No mob may ever be immortal in a hyper vault. The known offender is VH's elite zombie
     * (a brutal-roster boss): EliteZombieEntity self-applies IMMORTALITY every 10 ticks while
     * its raised minions live — and ImmortalityEffect.onAdded carves an explicit exemption for
     * it, so nothing upstream stops it. With hyper-scaled minion health the "kill the minions
     * first" window stretches toward forever. Denying the effect keeps the minion-raising
     * flavor while the boss stays damageable; players are not Mobs and keep any immortality of
     * their own. This also covers the champion "Immune" potion aura (the aura applies
     * IMMORTALITY to nearby mobs), which is why the aura could return to champions.json for
     * every non-hyper vault.
     */
    @SubscribeEvent
    public static void denyMobImmortalityInHyperVaults(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.IMMORTALITY) {
            return;
        }
        if (!(event.getEntityLiving() instanceof Mob mob) || !isInHyperVault(mob)) {
            return;
        }
        event.setResult(Event.Result.DENY);
        if (mob.addTag("woldsvaults_hyper_immortality_denied")) {
            WoldsVaults.LOGGER.info("Denied Immortality on {} in a hyper vault (mob immortality is disabled here).",
                    mob.getType().getRegistryName());
        }
    }

    /**
     * The floating lava/void pools are placed as bare liquid source blocks; explosions that
     * eat them (or whatever invisible casing sits around them) spill the pool across the
     * room floor. In hyper vaults explosions therefore cannot affect a pool fluid or any
     * block within one step of one.
     */
    @SubscribeEvent
    public static void protectPoolsFromExplosions(ExplosionEvent.Detonate event) {
        if (!(event.getWorld() instanceof ServerLevel level)) {
            return;
        }
        if (ServerVaults.get(level)
                .map(vault -> vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(true)) {
            return;
        }
        int before = event.getAffectedBlocks().size();
        event.getAffectedBlocks().removeIf(pos -> isPoolOrCasing(level, pos));
        int removed = before - event.getAffectedBlocks().size();
        if (removed > 0) {
            WoldsVaults.LOGGER.info("Shielded {} pool block(s) from an explosion in a hyper vault.", removed);
        }
    }

    private static boolean isPoolOrCasing(Level level, BlockPos pos) {
        if (isPoolFluid(level.getBlockState(pos))) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (isPoolFluid(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPoolFluid(BlockState state) {
        return state.getBlock() == Blocks.LAVA
                || state.getBlock() == ModBlocks.VOID_LIQUID_BLOCK;
    }
}
