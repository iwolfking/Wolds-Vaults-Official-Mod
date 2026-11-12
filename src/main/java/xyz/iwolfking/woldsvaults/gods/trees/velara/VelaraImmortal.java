package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

/**
 * Immortal's self-revive and damage penalty. The revive cooldown is a wall-clock deadline in
 * {@link GodNodeState#persistent}, re-armed on vault exit.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraImmortal {
    /** The base mod's "already revived this death" tag; setting it stands phoenix and downed down. */
    private static final String VAULT_REVIVE_TAG = "the_vault_revived_tick";
    private static final String READY_AT_KEY = "revive_ready_at";
    private static final long MILLIS_PER_TICK = 50L;
    private static final ResourceLocation DAMAGE_FACTOR_KEY = WoldsVaults.id("velara_immortal");

    private VelaraImmortal() {
    }

    /** Runs above the base mod's death handler, so Immortal resolves before phoenix gear and downed. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || player.getLevel().isClientSide()) {
            return;
        }
        if (event.isCanceled() || player.getTags().contains(VAULT_REVIVE_TAG)) {
            return;
        }
        if (!VelaraNodes.isActive(player, VelaraNodes.IMMORTAL) || event.getSource().isBypassInvul()) {
            return;
        }
        CompoundTag cooldown = GodNodeState.persistent(player, VelaraNodes.IMMORTAL);
        long now = System.currentTimeMillis();
        if (cooldown.contains(READY_AT_KEY, Tag.TAG_LONG) && now < cooldown.getLong(READY_AT_KEY)) {
            return;
        }
        cooldown.putLong(READY_AT_KEY, now + VelaraValues.immortalReviveCooldownTicks() * MILLIS_PER_TICK);
        revive(player);
        player.addTag(VAULT_REVIVE_TAG);
        event.setCanceled(true);
        VelaraSacrificeFlocks.rebuildFor(player);
    }

    private static void revive(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 2));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.getLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    static void updateGlobalFactor(ServerPlayer player) {
        if (VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            if (GlobalDamageMultiplierRegistry.getFactor(player, DAMAGE_FACTOR_KEY) != VelaraValues.immortalDamageMultiplier()) {
                GlobalDamageMultiplierRegistry.register(player, DAMAGE_FACTOR_KEY, VelaraValues.immortalDamageMultiplier());
            }
        } else {
            GlobalDamageMultiplierRegistry.remove(player, DAMAGE_FACTOR_KEY);
        }
    }
}
