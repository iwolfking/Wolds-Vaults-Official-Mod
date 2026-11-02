package xyz.iwolfking.woldsvaults.events;

import cofh.core.init.CoreMobEffects;
import com.mojang.math.Vector3f;
import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.entity.champion.IChampionAffix;
import iskallia.vault.entity.champion.PotionAuraAffix;
import iskallia.vault.entity.entity.elite.EliteDrownedEntity;
import iskallia.vault.entity.entity.elite.EliteEndermanEntity;
import iskallia.vault.entity.entity.elite.EliteHuskEntity;
import iskallia.vault.entity.entity.elite.EliteSpiderEntity;
import iskallia.vault.entity.entity.elite.EliteStrayEntity;
import iskallia.vault.entity.entity.elite.EliteWitchEntity;
import iskallia.vault.entity.entity.elite.EliteWitherSkeleton;
import iskallia.vault.entity.entity.elite.EliteZombieEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.ActiveFlagsCheck;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.gear.trinket.effects.MultiJumpTrinket;
import iskallia.vault.item.gear.VaultAxeItem;
import iskallia.vault.skill.ability.effect.RampageAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.talent.type.JavelinConductTalent;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.*;
import iskallia.vault.util.damage.DamageUtil;
import iskallia.vault.world.data.PlayerTalentsData;
import iskallia.vault.world.data.ServerVaults;
import xyz.iwolfking.woldsvaults.gods.combat.RampageAccess;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraCounterstrike;
import xyz.iwolfking.woldsvaults.milestones.MilestoneIds;
import xyz.iwolfking.woldsvaults.milestones.Milestones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.SneakyGetawayAbility;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.*;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.api.data.HexEffects;
import xyz.iwolfking.woldsvaults.api.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.effect.mobeffects.BleedOverrideEffect;
import xyz.iwolfking.woldsvaults.effect.mobeffects.EchoingEffectInstance;
import xyz.iwolfking.woldsvaults.effect.mobeffects.PercentBurnEffect;
import xyz.iwolfking.woldsvaults.effect.trinkets.EffectOnHitTakenEffect;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRuntime;
import xyz.iwolfking.woldsvaults.objectives.data.bosses.WoldBoss;
import xyz.iwolfking.woldsvaults.talent.special.DebuffDamageBonusTalent;
import xyz.iwolfking.woldsvaults.talent.special.WoldsAxeSpecializationTalent;

import java.util.List;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID
)
public class LivingEntityEvents {

    private static final Random random = new Random();

    private static SoundEvent ANCHOR_SLAM_SOUND = null;

    private static final TargetingConditions AURA_TARGETING = TargetingConditions.forCombat().range(64.0);

    public static void init() {
         ANCHOR_SLAM_SOUND  = Registry.SOUND_EVENT.get(ResourceLocation.parse("bettercombat:anchor_slam"));
         registerLeechHealingEfficiencyExclusion();
         MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, false, LivingHurtEvent.class, LivingEntityEvents::applyRiptideDamageMore);
    }

    /** Clamps healing effectiveness to 1.0 while {@link ActiveFlags#IS_LEECHING} is set, at lowest priority. */
    private static void registerLeechHealingEfficiencyExclusion() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LivingEntityEvents.class, data -> {
            if(ActiveFlags.IS_LEECHING.isSet()) {
                data.setValue(Math.min(data.getValue(), 1.0F));
            }
        }, Integer.MIN_VALUE);
    }

    @SubscribeEvent
    public static void onElementalAttack(LivingHurtEvent event) {
        if (WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer player) {
            ElementHelper.applyElementalEffects(event.getEntityLiving(), player, event.getAmount());
        }

        //Handle special projectile cases
        if (attacker instanceof VaultFireball fireball && fireball instanceof IWoldAbilityEnhancements woldAbilityEnhancements) {
            ElementHelper.applyElementalEffects(event.getEntityLiving(), fireball.getThrower(), event.getAmount(), woldAbilityEnhancements.getElementTypes(), true);
        }
    }

    @SubscribeEvent
    public static void reduceHealingFromHealBlock(LivingHealEvent event) {
        if(event.getEntityLiving().hasEffect(ModEffects.HEALING_BLOCK)) {
            MobEffectInstance effectInstance = event.getEntityLiving().getEffect(ModEffects.HEALING_BLOCK);
            if(effectInstance != null) {
                float reduction = (event.getEntityLiving().getEffect(ModEffects.HEALING_BLOCK).getAmplifier() + 1) * 0.1F;
                event.setAmount(event.getAmount() * reduction);
            }
        }
    }

    @SubscribeEvent
    public static void onScepterHitSparkles(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (ActiveFlags.IS_AP_ATTACKING.isSet() || WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) return;

        ItemStack heldItem = player.getMainHandItem();
        GearDataCache gearDataCache = GearDataCache.of(heldItem);
        if (!gearDataCache.hasAttribute(ModGearAttributes.SCEPTER_SPARKLES)) {
            return;
        }

        LivingEntity victim = event.getEntityLiving();
        Level level = player.getLevel();

        if (!(level instanceof ServerLevel serverLevel)) return;

        float playerAP = AbilityPowerHelper.getAbilityPower(player);
        float aoeBonus = AreaOfEffectHelper.getAreaOfEffectUnlimited(player);
        double radius = 3.5 * (1.0F + aoeBonus);

        AABB searchBox = victim.getBoundingBox().inflate(radius);
        List<LivingEntity> nearbyTargets = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, e ->
                !(e instanceof Player) && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof PetEntity)
        );

        if (nearbyTargets.isEmpty()) return;

        float sparkleDamage = playerAP * (gearDataCache.getRarity().equals(VaultGearRarity.valueOf("MYTHIC")) ? 0.2F : 0.1F);

        Vector3f startColor = new Vector3f(0.8F, 0.2F, 1.0F);
        Vector3f endColor = new Vector3f(0.3F, 0.9F, 1.0F);

        for (LivingEntity target : nearbyTargets) {
            Vec3 originPos = victim.position().add(0, victim.getBbHeight() * 0.5, 0);

            spawnSparkleParticleLine(
                    serverLevel,
                    originPos,
                    () -> target.position().add(0, target.getBbHeight() * 0.5, 0),
                    startColor,
                    endColor,
                    8,
                    1,
                    () -> {
                        if (target.isAlive()) {
                            WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE.runWithFlag(() -> {
                                ActiveFlags.IS_AP_ATTACKING.push();
                                try {
                                    DamageSource sparkleSource = DamageSource.indirectMagic(player, player);
                                    WoldActiveFlags.IS_SCEPTER_CAST_ATTACKING.runWithFlag(() -> target.hurt(sparkleSource, sparkleDamage));
                                } finally {
                                    ActiveFlags.IS_AP_ATTACKING.pop();
                                }
                            });

                            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                    target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                                    12, 0.2, 0.2, 0.2, 0.4);
                        }
                    }
            );
        }
    }


    private static void spawnSparkleParticleLine(
            ServerLevel level,
            Vec3 from,
            Supplier<Vec3> toSupplier,
            Vector3f startColor,
            Vector3f endColor,
            int steps,
            int ticksBetween,
            Runnable onArrival
    ) {
        Vec3 mid = from.add(0, 0.5, 0);
        Vec3 controlOffset = new Vec3(
                (level.random.nextDouble() - 0.5) * 2.0,
                1.0 + level.random.nextDouble(),
                (level.random.nextDouble() - 0.5) * 2.0
        );

        for (int i = 0; i <= steps; i++) {
            final int stepIndex = i;
            double t = (double) stepIndex / steps;

            DelayedExecutionHelper.schedule(level, stepIndex * ticksBetween, () -> {
                Vec3 to = toSupplier.get();
                Vec3 control = mid.add(controlOffset);

                Vec3 pos = from.scale((1 - t) * (1 - t))
                        .add(control.scale(2 * (1 - t) * t))
                        .add(to.scale(t * t));

                float r = (float) (startColor.x() + t * (endColor.x() - startColor.x()));
                float g = (float) (startColor.y() + t * (endColor.y() - startColor.y()));
                float b = (float) (startColor.z() + t * (endColor.z() - startColor.z()));
                DustParticleOptions particle = new DustParticleOptions(new Vector3f(r, g, b), 1.2F);

                level.sendParticles(particle, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);

                if (stepIndex == steps && onArrival != null) {
                    onArrival.run();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onChampionDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (entity.level.isClientSide() || !(entity instanceof ChampionLogic.IChampionLogicHolder holder)) {
            return;
        }

        if (ChampionLogic.isChampion(entity)) {
            ChampionLogic logic = holder.getChampionLogic();
            if (logic == null) return;

            for (IChampionAffix affix : logic.getAffixes()) {
                if (affix instanceof PotionAuraAffix potionAura) {
                    if (potionAura.getMobEffect() != null) {
                        entity.level.getEntities(
                                EntityTypeTest.forClass(LivingEntity.class),
                                entity.getBoundingBox().inflate(potionAura.getRange() * 2),
                                e -> e != entity && !(e instanceof Player)
                        ).forEach(mob -> {
                            if (mob.hasEffect(potionAura.getMobEffect())) {
                                mob.removeEffect(potionAura.getMobEffect());
                            }
                        });
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHit(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (!(entity instanceof Player))
            return;

        DamageSource source = event.getSource();

        if (!(source instanceof EntityDamageSource) || source.isExplosion() || source.isBypassInvul())
            return;

        TrinketHelper.getTrinkets(entity, EffectOnHitTakenEffect.class).forEach(effectOnHitTakenEffectTrinketStack -> {
            if(entity.getRandom().nextFloat() <= effectOnHitTakenEffectTrinketStack.trinket().getConfig().getChance() && !entity.hasEffect(effectOnHitTakenEffectTrinketStack.trinket().getConfig().getEffect())) {
                entity.addEffect(new MobEffectInstance(effectOnHitTakenEffectTrinketStack.trinket().getConfig().getEffectInstance()));
            }
        });

        float dodgeChance = AttributeSnapshotHelper.getInstance().getSnapshot(entity).getAttributeValue(ModGearAttributes.DODGE_PERCENT, VaultGearAttributeTypeMerger.floatSum());
        if(entity.hasEffect(ModEffects.SNEAKY_GETAWAY)) {
            dodgeChance += SneakyGetawayAbility.SneakyGetawayEffect.getSneakyEtchingDodgeChance(entity);
        }
        boolean dodge = entity.getRandom().nextDouble() <= Math.min(LuckHelper.getLuckAffectedChance(dodgeChance, entity), 0.75);

        if (dodge && entity instanceof ServerPlayer dodgingPlayer) {
            Milestones.advance(dodgingPlayer, MilestoneIds.WOOSH, 1L);
            VelaraCounterstrike.onDefended(dodgingPlayer, source);
        }

        event.setCanceled(dodge);
    }

    @SubscribeEvent
    public static void unlockLightTrinketPouch(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker))
            return;

        if(event.getEntityLiving() instanceof WoldBoss) {
            Vault vault = ServerVaults.get(attacker.level).orElse(null);
            if(vault != null) {
                if(vault.get(Vault.LEVEL).get(VaultLevel.VALUE) >= 100) {
                    if(attacker.getLevel() instanceof ServerLevel sLevel) {
                        if(DiscoveredRecipesData.get(sLevel).hasDiscovered(attacker, WoldsVaults.id("light_trinket_pouch"))) {
                            return;
                        }

                        DiscoveredRecipesData.get(sLevel).discoverRecipeAndBroadcast(WoldsVaults.id("light_trinket_pouch"), attacker);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void unlockSlayerPouch(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker))
            return;

        if(event.getEntityLiving() instanceof VaultBossEntity) {
            Vault vault = ServerVaults.get(attacker.level).orElse(null);
            if(vault != null) {
                if(vault.get(Vault.LEVEL).get(VaultLevel.VALUE) >= 100) {
                    if(attacker.getLevel() instanceof ServerLevel sLevel) {
                        if(DiscoveredRecipesData.get(sLevel).hasDiscovered(attacker, WoldsVaults.id("slayer_trinket_pouch"))) {
                            return;
                        }

                        DiscoveredRecipesData.get(sLevel).discoverRecipeAndBroadcast(WoldsVaults.id("slayer_trinket_pouch"), attacker);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void soulLeech(LivingDeathEvent event) {
        if(event.getSource().isProjectile()) {
            return;
        }

        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker))
            return;

        int soulLeechValue = AttributeSnapshotHelper.getInstance().getSnapshot(attacker).getAttributeValue(ModGearAttributes.SOUL_LEECH_FLAT, VaultGearAttributeTypeMerger.intSum());

        attacker.heal(soulLeechValue);
    }

    @SubscribeEvent
    public static void curioChange(CurioChangeEvent event) {
        if (event.getEntityLiving() instanceof Player player
                && (event.getFrom().getItem() instanceof TrinketPouchItem || event.getTo().getItem() instanceof TrinketPouchItem)) {
            PouchRuntime.update(player);
        }
    }

    @SubscribeEvent
    public static void capLightningDamageInVaults(LivingHurtEvent event) {
        if(event.getEntityLiving() instanceof Player player && event.getSource().equals(DamageSource.LIGHTNING_BOLT)) {
            ServerVaults.get(player.level).ifPresent(vault -> {
                event.setAmount( Math.min(event.getAmount(), player.getMaxHealth() * 0.1F));
                player.addEffect(new MobEffectInstance(CoreMobEffects.SHOCKED.get(), 80, 0));
            });
        }
    }

    @SubscribeEvent
    public static void burningHit(LivingHurtEvent event) {
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float burnChance = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.BURNING_HIT_CHANCE, VaultGearAttributeTypeMerger.floatSum());

            if(burnChance != 0) {

                if(random.nextFloat() < LuckHelper.getLuckAffectedChance(burnChance, player)) {
                    PercentBurnEffect.applyPercentBurn(event.getEntityLiving(), player, 200);
                }

                player.getLevel().playSound(null, event.getEntity(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void drippingLava(LivingHurtEvent event) {
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            boolean drippingLava = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.DRIPPING_LAVA, VaultGearAttributeTypeMerger.anyTrue());
            if(drippingLava) {
                tryPlaceLava(event.getEntityLiving());
            }
        }
    }

    private static void tryPlaceLava(LivingEntity target) {
        if (!(target.level instanceof ServerLevel level)) return;

        if (LivingEntityEvents.random.nextFloat() >= 0.01f) return;

        BlockPos basePos = target.blockPosition();

        BlockPos pos = basePos.offset(
                LivingEntityEvents.random.nextInt(3) - 1,
                0,
                LivingEntityEvents.random.nextInt(3) - 1
        );

        if (!level.getBlockState(pos).isAir()) return;

        BlockPos below = pos.below();
        if (!level.getBlockState(below).isSolidRender(level, below)) return;

        level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
    }

    /**
     * Stores echoes, then applies Spectral Strike and the flat bonuses (cleaving, reaving) in a fixed order ahead
     * of every damage multiplier. Separate same-priority handlers ran in an order Forge does not define, so a flat
     * add could land before or after damage increase depending on the session. Echoes are stored before Spectral
     * Strike, so they do not carry its bonus. Thorns and ability power scaling are added later, by
     * {@link PostMultiplierFlatAdds}.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preMultiplierHit(LivingHurtEvent event) {
        echoingHit(event);
        SpectralStrikeDamage.apply(event);
        cleavingDamage(event);
        reavingDamage(event);
    }

    private static void cleavingDamage(LivingHurtEvent event) {
        if(event.getSource().getEntity() instanceof Player player && WoldEventHelper.isNormalAttack() && !WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            if(player.getMainHandItem().getItem() instanceof VaultAxeItem) {
                event.setAmount(event.getAmount() + WoldsAxeSpecializationTalent.applyCleavingDamageBonus(player, event.getEntityLiving()));
            }
        }
    }

    private static void reavingDamage(LivingHurtEvent event) {
        if(event.getEntityLiving().hasEffect(ModEffects.REAVING) || !WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float reavingDamage = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.REAVING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
            if(reavingDamage != 0) {
                WoldLogger.debug("Added " + (event.getEntityLiving().getMaxHealth() * reavingDamage) + " bonus reaving damage to attack.");
                event.getEntityLiving().addEffect(new MobEffectInstance(ModEffects.REAVING, Integer.MAX_VALUE, 0));
                event.getEntityLiving().addEffect(new MobEffectInstance(iskallia.vault.init.ModEffects.NO_AI, 20, 0));

                event.setAmount(MaxHealthDamageHelper.applyScaledMaxHealthDamageBonus(event.getEntityLiving(), event.getAmount(), reavingDamage));

                EtchingHelper.getEtchings(player, ModEtchingGearAttributes.REAVING_HEMMORAGE).stream().findFirst().ifPresent(reavingHemmorageAttribute -> {
                    event.getEntityLiving().addEffect(new MobEffectInstance(iskallia.vault.init.ModEffects.BLEED, 160, reavingHemmorageAttribute.getValue()));
                    BleedOverrideEffect.registerSource(event.getEntityLiving(), event.getSource().getEntity());
                });

                if(ANCHOR_SLAM_SOUND == null) {
                    WoldLogger.debug("Anchor Slam Sound was null, Better Combat mod is missing.");
                    return;
                }
                player.getLevel().playSound(null, event.getEntity(), ANCHOR_SLAM_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void bonusDebuffDamage(LivingHurtEvent event) {
        if(event.getSource().getEntity() instanceof ServerPlayer player) {
            if(ActiveFlags.IS_AP_ATTACKING.isSet() || WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) {
                if(event.getEntity() instanceof LivingEntity livingEntity) {
                    if(!MobEffectHelper.hasNegativeEffect(livingEntity)) {
                        return;
                    }

                    Optional<DebuffDamageBonusTalent> debuffDamageTalent = TalentHelper.getTalent(player, DebuffDamageBonusTalent.class);
                    debuffDamageTalent.ifPresent(debuffDamageBonusTalent -> event.setAmount(event.getAmount() * (1.0F + debuffDamageBonusTalent.getDamageIncrease())));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if(WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE.isSet()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hexingHit(LivingHurtEvent event) {
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float hexingChance = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.HEXING_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            if(hexingChance != 0) {
                if(player.level.random.nextFloat() <= LuckHelper.getLuckAffectedChance(hexingChance, player)) {
                    MobEffectInstance instance = HexEffects.HEX_EFFECTS.getRandom(player.getRandom());
                    if(instance == null){
                        return;
                    }

                    if(instance.getEffect().equals(iskallia.vault.init.ModEffects.BLEED)) {
                        BleedOverrideEffect.registerSource(event.getEntityLiving(), player);
                    }

                    event.getEntityLiving().addEffect(new MobEffectInstance(instance));

                }
            }
        }
    }

    private static void echoingHit(LivingHurtEvent event) {

        if(event.getSource().isProjectile()) {
            return;
        }

        if(WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet() || WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) {
            return;
        }

        if(ActiveFlags.IS_DOT_ATTACKING.isSet()
        || ActiveFlags.IS_LEECHING.isSet()
        || ActiveFlags.IS_AOE_ATTACKING.isSet()
        || ActiveFlags.IS_REFLECT_ATTACKING.isSet()
        || ActiveFlags.IS_CHARMED_ATTACKING.isSet()
        || ActiveFlags.IS_EFFECT_ATTACKING.isSet()
        || (ActiveFlags.IS_AP_ATTACKING.isSet()
            && !(ActiveFlags.IS_FIRESHOT_ATTACKING.isSet()
                || ActiveFlags.IS_ARCANE_RAIL_ATTACKING.isSet()
                || ActiveFlags.IS_TOTEM_ATTACKING.isSet()
                || ActiveFlags.IS_SMITE_BASE_ATTACKING.isSet()))
        ){
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            if(ActiveFlags.IS_JAVELIN_ATTACKING.isSet()) {
                if (player instanceof ServerPlayer sPlayer) {
                    TalentTree talents = PlayerTalentsData.get(sPlayer.getLevel()).getTalents(sPlayer);

                    boolean hasConduct = false;
                    for (JavelinConductTalent talent : talents.getAll(JavelinConductTalent.class, Skill::isUnlocked)) {
                        hasConduct = true;
                    }
                    if (!hasConduct)
                        return;
                }
            }

            float echoingChance = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ECHOING_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            float echoingDamage = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ECHOING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
            if(echoingChance != 0) {
                if (WoldActiveFlags.IS_ECHOING_ATTACKING.isSet())
                    echoingChance = (float) Math.sqrt(echoingChance);

                if(player.level.random.nextFloat() <= LuckHelper.getLuckAffectedChance(echoingChance, player)) {
                    LivingEntity target = event.getEntityLiving();

                    float newDamage;
                    DamageSource newSource;
                    int newDuration;
                    float newDecay;
                    EnumSet<ActiveFlags> newFlags;

                    EchoingEffectInstance oldInstance = (EchoingEffectInstance) target.getEffect(ModEffects.ECHOING);
                    if (WoldActiveFlags.IS_ECHOING_ATTACKING.isSet()) {
                        if(oldInstance == null)
                            return;

                        newDamage = event.getAmount() * oldInstance.getDecay();
                        newSource = oldInstance.getSource();
                        newDuration = EffectDurationHelper.adjustEffectDurationFloor(player, 1) * 10;
                        newDecay = oldInstance.getDecay() * 0.95F - 0.05F;
                        newFlags = oldInstance.getFlags();
                    } else {
                        newDamage = event.getAmount() * (1+echoingDamage) * 0.667F;
                        if (player instanceof ServerPlayer sPlayer && RampageAbility.isDirectMeleeHit(event)) {
                            newDamage *= 1.0F + RampageAccess.effectiveDamageIncrease(sPlayer);
                        }
                        newSource = event.getSource();
                        newDuration = EffectDurationHelper.adjustEffectDurationFloor(player, 1) * 10;
                        newDecay = 1.0F;
                        newFlags = EchoingEffectInstance.captureFlags();
                    }

                    if(newDamage > 1.0f) {
                        boolean noLuck = ActiveFlagsCheck.isAnyFlagActiveLuckyHit();
                        boolean noCleave = ActiveFlags.IS_TOTEM_ATTACKING.isSet();
                        if(oldInstance != null
                        && WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.REVERBERATION)) {

                            DamageSource oSource = oldInstance.getSource();
                            EnumSet<ActiveFlags> oFlags = oldInstance.getFlags();
                            float oDamage = oldInstance.getDamage() * oldInstance.getDecay();
                            oldInstance.setDamage(oDamage);

                            oldInstance.doDecay();
                            oldInstance.reverberate();

                            if(oDamage > 1.0F) {
                                WoldActiveFlags.IS_AOE2_ATTACK.maybeRunWithFlag(noCleave, () ->
                                    WoldActiveFlags.IS_UNLUCKY_ATTACK.runWithFlag(() ->
                                        WoldActiveFlags.IS_ECHOING_ATTACKING.runWithFlag(() ->
                                            EchoingEffectInstance.runWithFlags(oFlags, () -> DamageUtil.shotgunAttack(target, e -> e.hurt(oSource, oDamage)))
                                        )
                                    )
                                );
                            }
                        }

                        target.addEffect(new EchoingEffectInstance(player, newDamage, newSource, newDuration, newDecay, noLuck, noCleave, newFlags));
                    }
                }
            }
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOW
    )
    public static void onDamageTotem(LivingHurtEvent event) {
        Level world = event.getEntity().getCommandSenderWorld();
        if (!world.isClientSide() && world instanceof ServerLevel) {
            if (event.getEntityLiving() instanceof Player player) {
                if (!event.getSource().isBypassArmor()) {
                    ItemStack offHand = event.getEntityLiving().getOffhandItem();
                    if (!ServerVaults.get(world).isEmpty() || !(offHand.getItem() instanceof VaultGearItem)) {
                        if (offHand.getItem() instanceof VaultPlushieItem) {
                            int damage = (int) CommonEvents.PLAYER_STAT.invoke(PlayerStat.DURABILITY_DAMAGE, player, Math.max(1.0F, event.getAmount() / 6.0F)).getValue();
                            if (damage <= 1) {
                                damage = 1;
                            }

                            offHand.hurtAndBreak(damage, event.getEntityLiving(), entity -> entity.broadcastBreakEvent(EquipmentSlot.OFFHAND));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(
            priority = EventPriority.LOW
    )
    public static void onBreakVaultBlock(BlockEvent.BreakEvent event) {

        Level world = event.getPlayer().getCommandSenderWorld();
        if (!world.isClientSide() && world instanceof ServerLevel) {
            if (event.getState().getBlock() instanceof VaultChestBlock || event.getState().getBlock() instanceof CoinPileBlock || event.getState().getBlock() instanceof VaultOreBlock) {
                ItemStack offHand = event.getPlayer().getOffhandItem();
                if (ServerVaults.get(world).isPresent() || !(offHand.getItem() instanceof VaultGearItem)) {
                    if (offHand.getItem() instanceof VaultLootSackItem) {
                        if(event.getState().getBlock() instanceof VaultChestBlock chestBlock && world.getBlockEntity(event.getPos()) instanceof VaultChestTileEntity chest) {
                            if(chestBlock.hasStepBreaking(chest)) {
                                if(random.nextFloat() < 0.75F) {
                                    return;
                                }
                            }
                        }

                        int damage = (int)CommonEvents.PLAYER_STAT.invoke(PlayerStat.DURABILITY_DAMAGE, event.getPlayer(), 1.0F).getValue();
                        if (damage <= 1) {
                            damage = 1;
                        }

                        offHand.hurtAndBreak(damage, event.getPlayer(), entity -> entity.broadcastBreakEvent(EquipmentSlot.OFFHAND));
                    }
                }
            }
        }
    }

    /** With a usable {@code MultiJumpTrinket}, cancels falls under 5.0 blocks and cuts 2.0 off the rest. */
    @SubscribeEvent
    public static void multiJumpTrinketFallReductionEvent(LivingFallEvent event) {
        if(event.getEntityLiving() instanceof ServerPlayer player) {
            if (TrinketHelper.getTrinkets(player, MultiJumpTrinket.class).stream().anyMatch(trinket -> trinket.isUsable(player))) {
                if(event.getDistance() < 5.0F) {
                    event.setCanceled(true);
                } else {
                    event.setDistance(event.getDistance() - 2.0F);
                }
            }
        }
    }

    /**
     * Sets the base damage of hits a player lands while spinning with a riptide vault trident: twice the player's
     * attack damage attribute, for riptide contacts and the channeling hits the spin calls alike. Runs first so every
     * later stage of the hurt chain builds on this base.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (WoldActiveFlags.IS_FANG_ATTACKING.isSet()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (isVaultRiptideSpin(player, stack)) {
            double d = 2 * player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            if(!ActiveFlags.IS_CHAINING_ATTACKING.isSet()) {
                ActiveFlags.IS_CHAINING_ATTACKING.runIfNotSet(() -> VaultTridentItem.triggerChannelingRiptide(stack, player.getLevel(), player));
            }
            event.setAmount((float) d);
        }
    }

    /**
     * Multiplies a riptide contact by its More Riptide Damage. Registered at HIGH after the base mod's own HIGH
     * listeners, so the additions they make to the contact (Battle Cry) are multiplied too.
     */
    private static void applyRiptideDamageMore(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)
                || WoldActiveFlags.IS_FANG_ATTACKING.isSet()
                || ActiveFlags.IS_CHAINING_ATTACKING.isSet()
                || !RiptideDash.isContact(event.getEntityLiving())
                || !isVaultRiptideSpin(player, player.getMainHandItem())
                || !AttributeSnapshotHelper.canHaveSnapshot(player)) {
            return;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float more = snapshot.getAttributeValue(ModGearAttributes.RIPTIDE_DAMAGE_MORE, VaultGearAttributeTypeMerger.floatSum());
        if (more != 0.0F) {
            event.setAmount(event.getAmount() * (1.0F + more));
        }
    }

    private static boolean isVaultRiptideSpin(Player player, ItemStack stack) {
        return stack.getItem() instanceof VaultTridentItem && player.isAutoSpinAttack() && VaultTridentItem.isVaultTridentRiptide(stack);
    }

    private static void withSnapshot(LivingEvent event, boolean serverOnly, BiConsumer<LivingEntity, AttributeSnapshot> fn) {
        withSnapshot(event.getEntityLiving(), serverOnly, fn);
    }

    private static void withSnapshot(LivingEntity entity, boolean serverOnly, BiConsumer<LivingEntity, AttributeSnapshot> fn) {
        if (AttributeSnapshotHelper.canHaveSnapshot(entity)) {
            if (!serverOnly || !entity.getCommandSenderWorld().isClientSide()) {
                fn.accept(entity, AttributeSnapshotHelper.getInstance().getSnapshot(entity));
            }
        }
    }

}
