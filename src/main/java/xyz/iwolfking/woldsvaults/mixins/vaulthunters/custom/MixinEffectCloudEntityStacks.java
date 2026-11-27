package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.init.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckEffectCloudStacks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mixin(value = EffectCloudEntity.class, remap = false)
public abstract class MixinEffectCloudEntityStacks implements DuckEffectCloudStacks {
    @Unique private static final int MIN_REAPPLICATION_DELAY = 2;

    @Shadow private Potion potion;
    @Shadow @Final private List<MobEffectInstance> effects;
    @Shadow @Final private Map<Entity, Integer> reapplicationDelayMap;
    @Shadow private int duration;
    @Shadow private int waitTime;
    @Shadow private int reapplicationDelay;
    @Shadow private boolean affectsOwner;
    @Shadow private boolean affectsPlayers;
    @Shadow private int durationOnUse;
    @Shadow private float radiusOnUse;
    @Shadow private float radiusPerTick;
    @Shadow private UUID ownerUniqueId;

    @Shadow public abstract float getRadius();
    @Shadow public abstract void setRadius(float radiusIn);
    @Shadow public abstract int getColor();
    @Shadow public abstract ParticleOptions getParticleData();
    @Shadow public abstract boolean shouldIgnoreRadius();

    @Unique private List<int[]> woldsvaults$stacks;
    @Unique private int woldsvaults$baseDelay;
    @Unique private int woldsvaults$activeStacks = 1;

    @Override
    public UUID woldsvaults$getOwnerId() {
        return this.ownerUniqueId;
    }

    @Override
    public boolean woldsvaults$canAbsorb(EffectCloudEntity other) {
        if (((Object) this).getClass() != ((Object) other).getClass() || this.ownerUniqueId == null) {
            return false;
        }
        MixinEffectCloudEntityStacks that = (MixinEffectCloudEntityStacks) (Object) other;
        if (!this.ownerUniqueId.equals(that.ownerUniqueId) || this.woldsvaults$changesOnUse() || that.woldsvaults$changesOnUse()) {
            return false;
        }
        if (this.potion != that.potion || this.affectsOwner != that.affectsOwner || this.affectsPlayers != that.affectsPlayers) {
            return false;
        }
        if (this.getColor() != that.getColor() || this.getParticleData().getType() != that.getParticleData().getType()) {
            return false;
        }
        if (this.effects.size() != that.effects.size()) {
            return false;
        }
        for (int i = 0; i < this.effects.size(); i++) {
            MobEffectInstance mine = this.effects.get(i);
            MobEffectInstance theirs = that.effects.get(i);
            if (mine.getEffect() != theirs.getEffect() || mine.getAmplifier() != theirs.getAmplifier() || mine.getDuration() != theirs.getDuration()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the other cloud as a stack on this one. The stack starts after the other cloud's wait time and ends when the
     * other cloud would have expired; this cloud's lifetime and radius grow to cover it.
     */
    @Override
    public void woldsvaults$absorb(EffectCloudEntity other) {
        MixinEffectCloudEntityStacks that = (MixinEffectCloudEntityStacks) (Object) other;
        if (this.woldsvaults$stacks == null) {
            this.woldsvaults$stacks = new ArrayList<>();
            this.woldsvaults$stacks.add(new int[]{this.waitTime, this.waitTime + this.duration, 1});
            this.woldsvaults$baseDelay = this.reapplicationDelay;
        }
        int start = ((Entity) (Object) this).tickCount + that.waitTime;
        int end = start + that.duration;
        this.woldsvaults$stacks.add(new int[]{start, end, 0});
        this.duration = Math.max(this.duration, end - this.waitTime);
        if (other.getRadius() > this.getRadius()) {
            this.setRadius(other.getRadius());
        }
    }

    @Unique
    private boolean woldsvaults$changesOnUse() {
        return this.durationOnUse != 0 || this.radiusOnUse != 0.0F || this.radiusPerTick != 0.0F;
    }

    /**
     * Counts the live stacks and shortens the reapplication delay to {@code baseDelay / stacks} (at least
     * {@link #MIN_REAPPLICATION_DELAY} ticks), so a stacked cloud applies as often as its separate copies would have.
     * A stack that just started clears the delay map, as a freshly spawned cloud would hit everything at once.
     */
    @Inject(method = "tick", at = @At("HEAD"), remap = true)
    private void woldsvaults$updateStacks(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (this.woldsvaults$stacks == null || self.level.isClientSide) {
            return;
        }
        int now = self.tickCount;
        int active = 0;
        boolean started = false;
        Iterator<int[]> iterator = this.woldsvaults$stacks.iterator();
        while (iterator.hasNext()) {
            int[] stack = iterator.next();
            if (now >= stack[1]) {
                iterator.remove();
            } else if (now >= stack[0]) {
                active++;
                if (stack[2] == 0) {
                    stack[2] = 1;
                    started = true;
                }
            }
        }
        if (started) {
            this.reapplicationDelayMap.clear();
        }
        this.woldsvaults$activeStacks = Math.max(1, active);
        this.reapplicationDelay = Math.max(MIN_REAPPLICATION_DELAY, Mth.ceil(this.woldsvaults$baseDelay / (float) this.woldsvaults$activeStacks));
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 5), remap = true)
    private int woldsvaults$checkEveryTickWhenStacked(int interval) {
        return this.woldsvaults$activeStacks > 1 ? 1 : interval;
    }

    /**
     * @author PoorMansPhysicist
     * @reason Spawn the same expected number of particles without rolling once per square block of cloud area every tick.
     */
    @Overwrite
    private void tickParticles() {
        Entity self = (Entity) (Object) this;
        Random random = self.level.random;
        ParticleOptions particle = this.getParticleData();
        boolean tinted = particle.getType() == ParticleTypes.ENTITY_EFFECT || particle.getType() == ModParticles.CLOUD_EFFECT.get();
        if (this.shouldIgnoreRadius()) {
            if (!random.nextBoolean()) {
                return;
            }
            for (int i = 0; i < 2; i++) {
                if (random.nextInt(25) != 0) {
                    continue;
                }
                int color = random.nextBoolean() ? 0xFFFFFF : this.getColor();
                this.woldsvaults$spawnParticle(self, random, particle, tinted, 0.2F, color, false);
            }
            return;
        }
        float radius = this.getRadius();
        int cells = Mth.ceil((float) Math.PI * radius * radius);
        int count = cells / 25 + (random.nextInt(25) < cells % 25 ? 1 : 0);
        for (int i = 0; i < count; i++) {
            this.woldsvaults$spawnParticle(self, random, particle, tinted, radius, this.getColor(), true);
        }
    }

    @Unique
    private void woldsvaults$spawnParticle(Entity self, Random random, ParticleOptions particle, boolean tinted, float radius, int color, boolean drift) {
        float angle = random.nextFloat() * ((float) Math.PI * 2.0F);
        float distance = Mth.sqrt(random.nextFloat()) * radius;
        double x = self.getX() + Mth.cos(angle) * distance;
        double z = self.getZ() + Mth.sin(angle) * distance;
        if (tinted) {
            self.level.addParticle(particle, x, self.getY(), z, (color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        } else if (drift) {
            self.level.addParticle(particle, x, self.getY(), z, (0.5 - random.nextDouble()) * 0.15, 0.01F, (0.5 - random.nextDouble()) * 0.15);
        } else {
            self.level.addParticle(particle, x, self.getY(), z, 0.0, 0.0, 0.0);
        }
    }
}
