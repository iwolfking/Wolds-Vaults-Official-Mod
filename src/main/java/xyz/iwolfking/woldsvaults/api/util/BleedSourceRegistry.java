package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BleedSourceRegistry {
    private static final Map<UUID, UUID> bleedSources = new HashMap<>();

    private static final DamageSource UNATTRIBUTED_BLEED = new UnattributedBleedSource();

    /**
     * Records the entity responsible for the most recent Bleed application on the target so
     * Bleed damage ticks can be attributed to it for kill credit.
     */
    public static void registerSource(LivingEntity target, Entity source) {
        if (source != null) {
            bleedSources.put(target.getUUID(), source.getUUID());
        }
    }

    public static void removeSource(LivingEntity target) {
        bleedSources.remove(target.getUUID());
    }

    /**
     * Resolves the damage source for a Bleed tick, attributed to the registered applier when
     * one is known and still loaded. Every returned source bypasses armor and magic reduction
     * so percent Bleed always deals its full listed damage.
     */
    public static DamageSource resolveDamageSource(LivingEntity entity) {
        UUID sourceId = bleedSources.get(entity.getUUID());
        if (sourceId != null && entity.level instanceof ServerLevel serverLevel) {
            Entity sourceEntity = serverLevel.getEntity(sourceId);
            if (sourceEntity instanceof LivingEntity living) {
                return new BleedDamageSource(living instanceof Player ? "player" : "mob", living);
            }
        }
        return UNATTRIBUTED_BLEED;
    }

    private static final class BleedDamageSource extends EntityDamageSource {
        private BleedDamageSource(String msgId, Entity source) {
            super(msgId, source);
            this.bypassArmor();
            this.bypassMagic();
        }
    }

    private static final class UnattributedBleedSource extends DamageSource {
        private UnattributedBleedSource() {
            super("generic");
            this.bypassArmor();
            this.bypassMagic();
        }
    }
}
