package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.EntityScaler;
import iskallia.vault.init.ModEntities;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;


/**
 * Places the squads {@link GreedAssassinFury} arms around the player, each assassin stamped with the medallion tier
 * it was spawned under.
 */
public final class GreedAssassinSpawner {
    private static final int SPAWN_ATTEMPTS = 40;
    private static final int MAX_Y_SEARCH = 5;

    private GreedAssassinSpawner() {
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

    static boolean spawn(Vault vault, GreedMedallionTier tier, ServerLevel level, ServerPlayer player) {
        EntityType<?> assassinType = randomAssassinType(level);
        Entity created = assassinType.create(level);
        if (!(created instanceof LivingEntity assassin)) {
            WoldLogger.error("Greed assassin type {} did not create a living entity - skipping this spawn.",
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
        return true;
    }

    static void announce(ServerLevel level, ServerPlayer player, int count) {
        TextComponent message = count == 1
                ? new TextComponent("A ")
                : new TextComponent("A squad of " + count + " ");
        message.withStyle(ChatFormatting.GRAY);
        message.append(new TextComponent(count == 1 ? "Greed Assassin" : "Greed Assassins").withStyle(ChatFormatting.GOLD))
                .append((Component) new TextComponent(count == 1 ? " is hunting you..." : " are hunting you...")
                        .withStyle(ChatFormatting.GRAY));
        player.displayClientMessage(message, false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.MOB_TRAP, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private static BlockPos findDistantSpawnPos(ServerLevel level, BlockPos playerPos, EntityType<?> entityType) {
        GreedMedallionsConfig.Assassins settings = GreedMedallionTier.assassinSettings();
        if (settings == null) {
            return null;
        }
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double angle = Math.PI * 2 * level.random.nextDouble();
            double distance = settings.spawnDistanceMin + settings.spawnDistanceRange * level.random.nextDouble();
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
}
