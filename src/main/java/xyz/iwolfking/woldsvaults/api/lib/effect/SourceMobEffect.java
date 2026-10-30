package xyz.iwolfking.woldsvaults.api.lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SourceMobEffect extends MobEffect {

    private static final Map<UUID, UUID> sources = new HashMap<>();

    protected SourceMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public static void registerSource(LivingEntity target, Entity source) {
        if (source != null) {
            sources.put(target.getUUID(), source.getUUID());
        }
    }

    public static void removeSource(LivingEntity target) {
        sources.remove(target.getUUID());
    }

    public static Map<UUID, UUID> getSources() {
        return sources;
    }
}
