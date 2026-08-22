package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobAttributeModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.champion.ChampionPromoter;
import iskallia.vault.init.ModCapabilities;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ScaleDataSyncMessage;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.MobAttributeModifierSettable;

import java.util.UUID;

/**
 * Builds a Vault Champion and puts it in the world.
 *
 * <p>The sequence follows {@code KillBossObjective#spawnBoss} and the assassin spawner's ring
 * placement, with two deliberate departures. It pre-tags {@code vault_scaled}, which opts the
 * Champion out of the automatic {@code EntityScaler} pass entirely, so its health and damage are the
 * authored numbers from {@code greed_champion.json} rather than the vault mob curve layered on top of
 * them; and it tags {@code no_champion} so the elite promoter never rolls a champion elite on top of
 * a boss that is already the Champion.
 *
 * <p>It also spawns dormant. The Vessel's dormancy suppresses its AI, its damage ramp and its ability
 * to be hurt, which makes it exactly the right primitive for a two-second arrival: the fight starts
 * when the boss wakes, not when the entity appears.</p>
 */
public final class VaultChampionSpawner {
    private static final int SPAWN_ATTEMPTS = 40;
    private static final int MAX_Y_SEARCH = 6;

    private VaultChampionSpawner() {
    }

    /**
     * Spawns a Champion hunting {@code summoner}, or returns null if no valid position was found in
     * forty attempts. The caller owns the state bookkeeping; this only builds the entity.
     */
    public static TheVesselEntity spawn(Vault vault, GreedMedallionTier tier, ServerLevel level, ServerPlayer summoner) {
        GreedChampionConfig config = VaultChampion.config();
        GreedChampionConfig.Rank stats = config.getRank(tier.getRankIndex());
        if (stats == null) {
            WoldsVaults.LOGGER.error("Greed medallion rank {} passes the champion gate but greed_champion.json has no "
                    + "stat block for it - no champion was spawned.", tier.getRankIndex());
            return null;
        }
        BlockPos spawnPos = findSpawnPos(level, summoner.blockPosition(), config.getHunt());
        if (spawnPos == null) {
            WoldsVaults.LOGGER.warn("No valid Vault Champion spawn position was found near {} - skipping this spawn.",
                    summoner.getGameProfile().getName());
            return null;
        }
        TheVesselEntity champion = ModEntities.THE_VESSEL.create(level);
        if (champion == null) {
            WoldsVaults.LOGGER.error("The Vessel entity type did not create an entity - the Vault Champion cannot spawn.");
            return null;
        }
        champion.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        champion.setDormant(true);
        champion.addTag(VaultChampion.CHAMPION_TAG);
        champion.addTag(ChampionPromoter.NO_CHAMPION_TAG);
        EntityScaler.setScaled(champion);
        VaultChampion.stamp(champion, tier, summoner.getUUID());
        applyStats(vault, level, tier, stats, config, champion);
        champion.setPersistenceRequired();
        VaultChampionKills.setWakeTime(champion, level.getGameTime() + config.getHunt().dormantTicks);
        level.addFreshEntity(champion);
        champion.setTarget(summoner);
        champion.setHealth(champion.getMaxHealth());
        applySize(champion, config.getScaling().sizeMultiplier);
        announce(level, vault, spawnPos);
        WoldsVaults.LOGGER.info("Vault Champion spawned at rank {} for {}: pool {}, attack damage {}, speed {}.",
                tier.getRankIndex(), summoner.getGameProfile().getName(),
                VaultChampionKills.poolOf(champion), attributeOf(champion, Attributes.ATTACK_DAMAGE),
                attributeOf(champion, Attributes.MOVEMENT_SPEED));
        return champion;
    }

    /**
     * Resolves the whole stat block and writes it onto the entity. Health and the damage pool are the
     * same number: the Vessel's health is clamped at a floor of one and can never reach zero, so the
     * pool is what actually ends the fight, but mirroring it into max health keeps percent-of-max
     * damage proportional to the fight rather than to the Vessel's arena value.
     */
    private static void applyStats(Vault vault, ServerLevel level, GreedMedallionTier tier,
                                   GreedChampionConfig.Rank stats, GreedChampionConfig config,
                                   TheVesselEntity champion) {
        GreedChampionConfig.Scaling scaling = config.getScaling();
        double levelFactor = levelFactor(vault, scaling);
        VaultDifficulty difficulty = difficultyOf(vault, level);
        double medallionFactor = 1.0D + tier.getMobHealthDamageBonus() / 100.0D;

        double pool = stats.healthPool * levelFactor * difficulty.getBossHealthMultiplier() * medallionFactor;
        double damage = scaling.baseAttackDamage * levelFactor * stats.damageMultiplier
                * difficulty.getBossDamageMultiplier() * medallionFactor;
        double speed = Math.min(scaling.movementSpeedCap,
                baseValue(champion, Attributes.MOVEMENT_SPEED) * stats.movementSpeedMultiplier);

        setBase(champion, Attributes.MAX_HEALTH, pool);
        setBase(champion, Attributes.ATTACK_DAMAGE, damage);
        setBase(champion, Attributes.MOVEMENT_SPEED, speed);
        setBase(champion, Attributes.FOLLOW_RANGE, Math.max(baseValue(champion, Attributes.FOLLOW_RANGE),
                config.getHunt().aggroRadius + 16.0D));
        VaultChampionKills.setPool(champion, pool);

        if (scaling.inheritVaultModifiers) {
            inheritVaultModifiers(vault, champion);
        }
    }

    /**
     * Replays the crystal's mob attribute modifiers by hand. A hand-spawned entity never fires the
     * Forge spawn event those modifiers ride, so without this the Champion would be the only mob in
     * the vault carrying none of them.
     *
     * <p>Max-health modifiers are refused. The pool is already the product of the rank table, the
     * level, the difficulty and the medallion; letting a crystal's health modifiers compound on top
     * is the same chain that produced the hundred-billion-health boss the hyper manager documents,
     * and unlike damage there is no play reason to want it.</p>
     */
    private static void inheritVaultModifiers(Vault vault, TheVesselEntity champion) {
        if (!vault.has(Vault.MODIFIERS)) {
            return;
        }
        Modifiers modifiers = vault.get(Vault.MODIFIERS);
        double health = champion.getMaxHealth();
        int applied = 0;
        for (Modifiers.Entry entry : modifiers.getEntries()) {
            VaultModifier<?> modifier = entry.getModifier().orElse(null);
            ModifierContext context = modifiers.getContext(entry);
            if (modifier instanceof MobAttributeModifier mob) {
                mob.applyToEntity(champion, context.getUUID(), context);
                applied++;
            } else if (modifier instanceof MobAttributeModifierSettable settable) {
                settable.applyToEntity(champion, context.getUUID(), context);
                applied++;
            }
        }
        AttributeInstance maxHealth = champion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getValue() != health) {
            new java.util.ArrayList<>(maxHealth.getModifiers()).forEach(maxHealth::removeModifier);
            champion.setHealth(champion.getMaxHealth());
        }
        WoldsVaults.LOGGER.debug("Vault Champion inherited {} vault mob attribute modifiers.", applied);
    }

    private static double levelFactor(Vault vault, GreedChampionConfig.Scaling scaling) {
        if (scaling.levelAnchor <= 0 || !vault.has(Vault.LEVEL)) {
            return 1.0D;
        }
        return Math.max(0.01D, vault.get(Vault.LEVEL).get() / (double) scaling.levelAnchor);
    }

    private static VaultDifficulty difficultyOf(Vault vault, ServerLevel level) {
        UUID owner = vault.has(Vault.OWNER) ? vault.get(Vault.OWNER) : null;
        if (owner == null) {
            return VaultDifficulty.NORMAL;
        }
        VaultDifficulty difficulty = WorldSettings.get(level).getPlayerDifficulty(owner);
        return difficulty == null ? VaultDifficulty.NORMAL : difficulty;
    }

    private static void setBase(TheVesselEntity champion, net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                double value) {
        AttributeInstance instance = champion.getAttribute(attribute);
        if (instance == null) {
            WoldsVaults.LOGGER.error("The Vessel has no {} attribute; the Vault Champion is using its default instead.",
                    attribute.getDescriptionId());
            return;
        }
        instance.setBaseValue(value);
    }

    private static double baseValue(TheVesselEntity champion,
                                    net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        AttributeInstance instance = champion.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getBaseValue();
    }

    private static double attributeOf(TheVesselEntity champion,
                                      net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        AttributeInstance instance = champion.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getValue();
    }

    /**
     * Grows the Champion through the base mod's entity-scale capability - the one the Grow effect
     * drives. It is the only route that moves both halves: a client mixin on the GeckoLib renderer
     * reads it for the model, and one on {@code getDimensions} reads it for the hitbox. Setting the
     * hitbox alone would leave a boss that fills a doorway while still looking its old size.
     *
     * <p>Sent to trackers and to the entity itself, because the capability is not synced on its own
     * and a client that never hears about it draws the Champion at 1.0.</p>
     */
    private static void applySize(TheVesselEntity champion, float multiplier) {
        if (multiplier <= 0.0F || multiplier == 1.0F) {
            return;
        }
        champion.getCapability(ModCapabilities.ENTITY_SCALE).ifPresent(scale -> {
            scale.setScale(multiplier, 1);
            ModNetwork.sendTrackingAndSelf(new ScaleDataSyncMessage(champion, scale.getTargetScale(),
                    scale.getTransitionDuration()), champion);
        });
    }

    private static void announce(ServerLevel level, Vault vault, BlockPos pos) {
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            runner.displayClientMessage(new TextComponent("The ").withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent("Vault Champion").withStyle(ChatFormatting.DARK_PURPLE))
                    .append((Component) new TextComponent(" has found you.").withStyle(ChatFormatting.GRAY)), false);
        }
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0F, 1.0F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D,
                60, 0.6D, 1.2D, 0.6D, 0.02D);
    }

    /**
     * Lands the Champion on the player.
     *
     * <p>It arrives on top of you rather than somewhere across the room, which is the whole point of a
     * hunter: an ambush you have to react to, not a boss you have to go looking for. The ring is only
     * a fallback for the case where there is genuinely nowhere to stand at your feet - it widens a
     * block at a time and takes the first spot that fits, so even then the Champion is as close as the
     * geometry allows.</p>
     */
    private static BlockPos findSpawnPos(ServerLevel level, BlockPos playerPos, GreedChampionConfig.Hunt hunt) {
        BlockPos onPlayer = fitAt(level, playerPos.getX(), playerPos.getZ(), playerPos.getY());
        if (onPlayer != null) {
            return onPlayer;
        }
        int maxRadius = (int) Math.max(1.0D, hunt.spawnRingMax);
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                double angle = Math.PI * 2 * level.random.nextDouble();
                int x = playerPos.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = playerPos.getZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos found = fitAt(level, x, z, playerPos.getY());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** The first height near {@code referenceY} where the Champion's hitbox fits without collision. */
    private static BlockPos fitAt(ServerLevel level, int x, int z, int referenceY) {
        EntityType<?> type = ModEntities.THE_VESSEL;
        for (int yOffset = 0; yOffset <= MAX_Y_SEARCH; yOffset++) {
            for (int sign = 1; sign >= -1; sign -= 2) {
                int y = referenceY + yOffset * sign;
                if (level.noCollision(type.getAABB(x + 0.5D, y, z + 0.5D))) {
                    return new BlockPos(x, y, z);
                }
                if (yOffset == 0) {
                    break;
                }
            }
        }
        return null;
    }
}
