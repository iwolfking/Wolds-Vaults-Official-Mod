package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rolls greed assassin spawns off a vault mob kill. The crystal's medallion is the whole gate, whatever
 * the killer's rank, and its rank scales the base per-mob-class chance.
 */
public final class GreedAssassinSpawner {
    private static final ResourceLocation GROUP_TANK = VaultMod.id("tank");
    private static final ResourceLocation GROUP_HORDE = VaultMod.id("horde");
    private static final ResourceLocation GROUP_ASSASSIN = VaultMod.id("assassin");
    private static final int MIN_SPAWN_DISTANCE = 24;
    private static final int SPAWN_DISTANCE_RANGE = 8;
    private static final int SPAWN_ATTEMPTS = 40;
    private static final int MAX_Y_SEARCH = 5;
    private static final Map<UUID, SpawnDecayTracker> DECAY_TRACKERS = new ConcurrentHashMap<>();

    private GreedAssassinSpawner() {
    }

    public static void trySpawn(LivingEntity killed, LivingDeathEvent event, ServerLevel level) {
        if (killed instanceof Player) {
            return;
        }
        ServerPlayer player = resolveKiller(event);
        if (player == null) {
            return;
        }
        Optional<Vault> vaultOpt = VaultUtils.getVault(killed.level);
        if (vaultOpt.isEmpty()) {
            return;
        }
        Vault vault = vaultOpt.get();
        Optional<GreedMedallionTier> tierOpt = GreedMedallionVaultState.get(vault);
        if (tierOpt.isEmpty()) {
            return;
        }
        if (VaultUtils.isHeraldVault(vault) || VaultUtils.isRebirthVault(vault)) {
            return;
        }
        GreedMedallionTier tier = tierOpt.get();
        double baseChance = baseSpawnChance(killed);
        if (baseChance <= 0.0D) {
            return;
        }
        double tierChance = baseChance * tierChanceMultiplier(tier);
        SpawnDecayTracker tracker = DECAY_TRACKERS.computeIfAbsent(player.getUUID(), id -> new SpawnDecayTracker());
        double effectiveChance = tracker.applyDecay(tierChance, level.getGameTime());
        if (effectiveChance <= 0.0D || level.random.nextDouble() >= effectiveChance) {
            return;
        }
        if (spawn(vault, tier, level, player)) {
            tracker.onAssassinSpawned(level.getGameTime());
        }
    }

    /**
     * Spawn-rate curve: {@code 1 + rankChanceStep * (rankIndex - 1)}. Values come from
     * {@code greed_medallions.json}.
     */
    private static double tierChanceMultiplier(GreedMedallionTier tier) {
        return 1.0D + GreedMedallionTier.spawnCurve().rankChanceStep * (tier.getRankIndex() - 1);
    }

    /**
     * The base per-mob-class chance, carrying no greed-tier term; the medallion multiplier is the only tier
     * scaling.
     */
    private static double baseSpawnChance(LivingEntity killed) {
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_HORDE, killed)) {
            return GreedMedallionTier.spawnCurve().hordeBaseChance;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_ASSASSIN, killed)
                || ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_TANK, killed)) {
            return GreedMedallionTier.spawnCurve().eliteBaseChance;
        }
        return 0.0D;
    }

    /** The player credited with a kill, following an Eternal back to its owner. */
    public static ServerPlayer resolveKiller(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer instanceof EternalEntity eternal) {
            killer = eternal.getOwner().right().orElse(null);
        }
        return killer instanceof ServerPlayer player ? player : null;
    }

    private static EntityType<?> randomAssassinType(ServerLevel level) {
        return switch (level.random.nextInt(6)) {
            case 0 -> ModEntities.ACID_FIEND;
            case 1 -> ModEntities.ACID_ENFORCER;
            case 2 -> ModEntities.ACID_PURSUER;
            case 3 -> ModEntities.ACID_HUNTER;
            case 4 -> ModEntities.ACID_PRIEST;
            default -> ModEntities.DEATH_GUARD;
        };
    }

    private static boolean spawn(Vault vault, GreedMedallionTier tier, ServerLevel level, ServerPlayer player) {
        EntityType<?> assassinType = randomAssassinType(level);
        Entity created = assassinType.create(level);
        if (!(created instanceof LivingEntity assassin)) {
            WoldsVaults.LOGGER.error("Greed assassin type {} did not create a living entity - skipping this spawn.",
                    assassinType.getRegistryName());
            return false;
        }
        BlockPos spawnPos = findDistantSpawnPos(level, player.blockPosition(), assassinType);
        if (spawnPos == null) {
            assassin.discard();
            return false;
        }
        assassin.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        assassin.addTag(GreedAssassins.GREED_ASSASSIN_TAG);
        GreedAssassins.setTier(assassin, tier);
        if (assassin instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.spawnAnim();
        }
        level.addFreshEntity(assassin);
        EntityScaler.scale(vault, assassin);
        GreedAssassinBehaviors.applyOnSpawn(vault, tier, assassin);
        assassin.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        GreedAssassinRegistry.track(level, assassin.getUUID());
        announce(level, player);
        return true;
    }

    private static void announce(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(new TextComponent("A ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent("Greed Assassin").withStyle(ChatFormatting.GOLD))
                .append((Component) new TextComponent(" is hunting you...").withStyle(ChatFormatting.GRAY)), false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.MOB_TRAP, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private static BlockPos findDistantSpawnPos(ServerLevel level, BlockPos playerPos, EntityType<?> entityType) {
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double angle = Math.PI * 2 * level.random.nextDouble();
            double distance = MIN_SPAWN_DISTANCE + SPAWN_DISTANCE_RANGE * level.random.nextDouble();
            int x = playerPos.getX() + (int) (Math.cos(angle) * distance);
            int z = playerPos.getZ() + (int) (Math.sin(angle) * distance);
            BlockPos found = findValidFloorPos(level, x, z, playerPos.getY(), entityType);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static BlockPos findValidFloorPos(ServerLevel level, int x, int z, int referenceY, EntityType<?> entityType) {
        for (int yOffset = -MAX_Y_SEARCH; yOffset <= MAX_Y_SEARCH; yOffset++) {
            int y = referenceY + yOffset;
            BlockPos candidate = new BlockPos(x, y, z);
            BlockPos floor = candidate.below();
            if (!level.getBlockState(floor).isValidSpawn(level, floor, entityType)) {
                continue;
            }
            AABB box = entityType.getAABB(x + 0.5D, y, z + 0.5D);
            if (!level.noCollision(box)) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    /**
     * Per-player anti-farm curve: each spawn cuts the next roll's odds further, recovering linearly over
     * {@code recoveryTicks}.
     */
    private static final class SpawnDecayTracker {
        private int assassinsSpawned;
        private long lastSpawnTick;

        private double recoveredDecay(long currentTick) {
            return this.lastSpawnTick > 0L
                    ? (currentTick - this.lastSpawnTick) / GreedMedallionTier.spawnCurve().recoveryTicks
                    : 0.0D;
        }

        double applyDecay(double baseChance, long currentTick) {
            double effectiveDecay = Math.max(0.0D, this.assassinsSpawned - recoveredDecay(currentTick));
            return baseChance / (1.0D + effectiveDecay * GreedMedallionTier.spawnCurve().decayFactor);
        }

        void onAssassinSpawned(long currentTick) {
            this.assassinsSpawned = (int) Math.max(0.0D, this.assassinsSpawned - recoveredDecay(currentTick)) + 1;
            this.lastSpawnTick = currentTick;
        }
    }

    public static void clearDecayTrackers() {
        DECAY_TRACKERS.clear();
    }
}
