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

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class HyperVaultEvents {
    private HyperVaultEvents() {
    }

    public static boolean isInHyperVault(LivingEntity entity) {
        return ServerVaults.get(entity.level)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
    }

    public static boolean isHyperBoss(LivingEntity entity) {
        return entity instanceof VaultBossEntity && isInHyperVault(entity);
    }

    @SubscribeEvent
    public static void denyBleedOnHyperboss(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() != ModEffects.BLEED) {
            return;
        }
        if (isHyperBoss(event.getEntityLiving())) {
            event.setResult(Event.Result.DENY);
        }
    }

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
