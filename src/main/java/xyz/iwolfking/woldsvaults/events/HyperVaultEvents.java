package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModEffects;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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

    /** True when the entity stands in a vault carrying the hyper Radar modifier. */
    public static boolean hasRadar(LivingEntity entity) {
        return ServerVaults.get(entity.level)
                .map(vault -> vault.get(Vault.MODIFIERS).hasModifier(WoldsVaults.id("radar")))
                .orElse(false);
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

    /**
     * Radar's teeth: Ghost Walk and Spirit Walk stealth exist purely as their MobEffects (the
     * Targeting immunity and the damage-cancel are both gated on the effect being active), so
     * denying the application makes the cast spend its mana and do nothing — the mobs' radar
     * still finds the player. Shadow Cloak's passive effect is denied silently for the same
     * reason; its targeting immunity is additionally disabled in MixinShadowCloakTrinket.
     */
    @SubscribeEvent
    public static void denyStealthUnderRadar(PotionEvent.PotionApplicableEvent event) {
        MobEffect effect = event.getPotionEffect().getEffect();
        boolean castStealth = effect == ModEffects.GHOST_WALK || effect == ModEffects.GHOST_WALK_SPIRIT_WALK;
        if (!castStealth && effect != ModEffects.SHADOW_CLOAK) {
            return;
        }
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || !hasRadar(player)) {
            return;
        }
        event.setResult(Event.Result.DENY);
        if (castStealth) {
            player.displayClientMessage(new TextComponent("The Vault's radar reveals you — stealth has no effect!")
                    .withStyle(ChatFormatting.RED), true);
            WoldsVaults.LOGGER.info("Radar denied {} on {}.", effect.getRegistryName(), player.getGameProfile().getName());
        }
    }

    /**
     * Radar defense-in-depth: strips a stealth effect that was already running when Radar
     * landed at cycle 3 — the deny above only covers applications made after that moment.
     */
    @SubscribeEvent
    public static void stripStealthUnderRadar(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || event.player.tickCount % 20 != 0) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player) || !hasRadar(player)) {
            return;
        }
        boolean stripped = player.removeEffect(ModEffects.GHOST_WALK);
        stripped |= player.removeEffect(ModEffects.GHOST_WALK_SPIRIT_WALK);
        stripped |= player.removeEffect(ModEffects.SHADOW_CLOAK);
        if (stripped) {
            player.displayClientMessage(new TextComponent("The Vault's radar reveals you — stealth has no effect!")
                    .withStyle(ChatFormatting.RED), true);
            WoldsVaults.LOGGER.info("Radar stripped an active stealth effect from {}.", player.getGameProfile().getName());
        }
    }

    /**
     * Hyperboss ability damage is physical by decree. VH's rune WaveBlast builds its source
     * as armor-bypassing magic, which at hyper escalation (six-figure raw hits from cycle 1)
     * deletes armor-stacked players from full health no matter their gear. Any
     * armor-bypassing or magic hit dealt DIRECTLY by a hyperboss is cancelled here and
     * re-dealt as a plain mob attack for the same amount, so VH's armor and dodge pipeline
     * applies in full. The replacement carries neither flag and passes straight through on
     * re-entry; shared constant sources (poison, wither...) have no attached entity and are
     * structurally excluded; indirect sources (fireballs, missiles) keep their own semantics.
     */
    @SubscribeEvent
    public static void normalizeHyperbossAbilityDamage(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
            return;
        }
        DamageSource source = event.getSource();
        if (!source.isBypassArmor() && !source.isMagic()) {
            return;
        }
        if (!(source.getEntity() instanceof VaultBossEntity boss) || source.getDirectEntity() != boss) {
            return;
        }
        if (!isInHyperVault(boss)) {
            return;
        }
        event.setCanceled(true);
        float amount = event.getAmount();
        player.hurt(DamageSource.mobAttack(boss), amount);
        WoldsVaults.LOGGER.info("Normalized a hyperboss ability hit on {} to physical ({} raw damage — armor applies now).",
                player.getGameProfile().getName(), Math.round(amount));
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
