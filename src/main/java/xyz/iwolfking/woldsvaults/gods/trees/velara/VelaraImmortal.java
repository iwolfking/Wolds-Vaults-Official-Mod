package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
 * Immortal's behavioural legs: the 300 second self-revive and the x0.5 damage penalty.
 *
 * <p>The stat legs are elsewhere - health and armour ride vanilla attribute modifiers in
 * {@link VelaraModifiers}, healing efficiency and regeneration ride {@link VelaraStatBus}.
 *
 * <p>The x0.5 goes through {@link GlobalDamageMultiplierRegistry} rather than a vanilla
 * {@code ATTACK_DAMAGE} modifier, which covers ability power as well as weapon damage in one
 * place. It is multiplicative with every other global factor, which is the intended reading of
 * "have your attack and ability power multiplied by 0.5x".
 *
 * <p>The revive cooldown is a tick stamp in the shared {@link GodNodeState} scratch, so it is
 * torn down on logout and on leaving a vault like every other node's state.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraImmortal {
    /** The base mod's own "already revived this death" tag; setting it makes phoenix and downed stand down. */
    private static final String VAULT_REVIVE_TAG = "the_vault_revived_tick";
    private static final ResourceLocation DAMAGE_FACTOR_KEY = WoldsVaults.id("velara_immortal");

    private VelaraImmortal() {
    }

    /**
     * Runs above the base mod's death handler so Immortal resolves before phoenix gear and before
     * the downed system, which would otherwise knock the player down instead of reviving them.
     */
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
        MinecraftServer server = player.getServer();
        if (server == null) {
            WoldsVaults.LOGGER.error("Immortal could not read the server tick for {}; the self-revive was skipped.",
                    player.getGameProfile().getName());
            return;
        }
        long now = server.getTickCount();
        Long last = GodNodeState.<Long>peek(player.getUUID(), VelaraNodes.IMMORTAL).orElse(null);
        if (last != null && now - last < VelaraValues.immortalReviveCooldownTicks()) {
            return;
        }
        GodNodeState.put(player.getUUID(), VelaraNodes.IMMORTAL, now);
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
