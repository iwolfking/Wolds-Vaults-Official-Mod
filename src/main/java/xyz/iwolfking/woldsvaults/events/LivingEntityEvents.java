package xyz.iwolfking.woldsvaults.events;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import cofh.core.init.CoreMobEffects;
import iskallia.vault.block.CoinPileBlock;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.entity.entity.elite.EliteDrownedEntity;
import iskallia.vault.entity.entity.elite.EliteEndermanEntity;
import iskallia.vault.entity.entity.elite.EliteHuskEntity;
import iskallia.vault.entity.entity.elite.EliteSpiderEntity;
import iskallia.vault.entity.entity.elite.EliteStrayEntity;
import iskallia.vault.entity.entity.elite.EliteWitchEntity;
import iskallia.vault.entity.entity.elite.EliteWitherSkeleton;
import iskallia.vault.entity.entity.elite.EliteZombieEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.gear.trinket.effects.MultiJumpTrinket;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.ThornsHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.WoldAttributeHelper;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.data.HexEffects;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.effect.mobeffects.EchoingPotionEffect;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;
import xyz.iwolfking.woldsvaults.objectives.data.bosses.WoldBoss;
import xyz.iwolfking.woldsvaults.util.WoldEventHelper;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID
)
public class LivingEntityEvents {

    private static final Random random = new Random();

    private static SoundEvent ANCHOR_SLAM_SOUND = null;

    public static void init() {
         ANCHOR_SLAM_SOUND  = Registry.SOUND_EVENT.get(new ResourceLocation("bettercombat:anchor_slam"));
    }

    @SubscribeEvent
    public static void dodge(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (!(entity instanceof Player))
            return;

        DamageSource source = event.getSource();

        if (!(source instanceof EntityDamageSource) || source.isExplosion() || source.isBypassInvul())
            return;

        float dodgeChance = AttributeSnapshotHelper.getInstance().getSnapshot(entity).getAttributeValue(ModGearAttributes.DODGE_PERCENT, VaultGearAttributeTypeMerger.floatSum());
        boolean dodge = entity.getRandom().nextDouble() < dodgeChance;

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

    //Handles Trinket Pouch saving all equipped trinkets.
    @SubscribeEvent
    public static void curioChange(CurioChangeEvent event) {
        ItemStack fromStack = event.getFrom();
        if (!(fromStack.getItem() instanceof TrinketPouchItem)) return;
        if(event.getFrom().getItem().equals(event.getTo().getItem()) && !event.getTo().getOrCreateTag().contains("StoredCurios")) return;
        if(event.getFrom().getItem() instanceof TrinketPouchItem && !event.getTo().is(Items.AIR)) {
            return;
        }
        if (event.getEntityLiving().level.isClientSide) return;

        LivingEntity entity = event.getEntityLiving();
        ListTag storedList = new ListTag();

        CuriosApi.getCuriosHelper().getCuriosHandler(entity).ifPresent(handler -> {
            for (String slotId : TrinketPouchItem.getSlotTypes(event.getFrom())) {
                handler.getStacksHandler(slotId).ifPresent(slotHandler -> {
                    IItemHandlerModifiable slots = slotHandler.getStacks();
                    for (int i = 0; i < slots.getSlots(); i++) {
                        ItemStack trinket = slots.getStackInSlot(i);
                        if (!trinket.isEmpty()) {
                            CompoundTag tag = new CompoundTag();
                            tag.putString("Slot", slotId);
                            tag.putInt("Index", i);
                            trinket.save(tag);
                            storedList.add(tag);
                            if(trinket.getItem() instanceof TrinketItem) {
                                TrinketItem.getTrinket(trinket).ifPresent((trinketEffect) -> trinketEffect.onUnEquip(entity, trinket));
                            }
                            slots.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                });
            }

            if (!storedList.isEmpty()) {
                ItemStack updatedStack = fromStack.copy();
                updatedStack.getOrCreateTag().put("StoredCurios", storedList);
                Player player = (Player) entity;
                // Try replacing the carried item (player's cursor)
                ItemStack carried = player.containerMenu.getCarried();
                if (ItemStack.isSameItemSameTags(carried, fromStack)) {
                    player.containerMenu.setCarried(updatedStack);
                } else {
                    // Fallback: try replacing in inventory
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (ItemStack.isSameItemSameTags(invStack, fromStack)) {
                            player.getInventory().setItem(i, updatedStack);
                            break;
                        }
                    }

                    // If we still can't find it, drop it
                    if (!player.addItem(updatedStack)) {
                        entity.spawnAtLocation(updatedStack);
                    }
                }
            }
        });
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
    public static void reavingDamage(LivingHurtEvent event) {
        //Prevent an entity from being reaved more than once or applying to non-melee strikes.
        if(event.getEntityLiving().hasEffect(ModEffects.REAVING) || !WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float reavingDamage = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.REAVING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
            if(reavingDamage != 0) {
                if(WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
                    WoldsVaults.LOGGER.debug("[WOLD'S VAULTS] Added " + (event.getEntityLiving().getMaxHealth() * reavingDamage) + " bonus reaving damage to attack.");
                }
                event.getEntityLiving().addEffect(new MobEffectInstance(ModEffects.REAVING, Integer.MAX_VALUE, 0));
                event.getEntityLiving().addEffect(new MobEffectInstance(iskallia.vault.init.ModEffects.NO_AI, 20, 0));

                if(ChampionLogic.isChampion(event.getEntityLiving()) || InfernalMobsCore.getMobModifiers(event.getEntityLiving()) != null || event.getEntityLiving() instanceof VaultBoss || event.getEntityLiving() instanceof VaultBossEntity || event.getEntityLiving() instanceof EliteDrownedEntity || event.getEntityLiving() instanceof EliteWitherSkeleton || event.getEntityLiving() instanceof EliteEndermanEntity || event.getEntityLiving() instanceof EliteHuskEntity || event.getEntityLiving() instanceof EliteSpiderEntity || event.getEntityLiving() instanceof  EliteStrayEntity || event.getEntityLiving() instanceof  EliteZombieEntity || event.getEntityLiving() instanceof EliteWitchEntity) {
                    event.setAmount(event.getAmount() + (event.getEntityLiving().getMaxHealth() * reavingDamage * 0.5F));
                }
                else {
                    event.setAmount(event.getAmount() + (event.getEntityLiving().getMaxHealth() * reavingDamage));
                }

                if(ANCHOR_SLAM_SOUND == null) {
                    WoldsVaults.LOGGER.debug("Anchor Slam Sound was null, Better Combat mod is missing.");
                    return;
                }
                player.getLevel().playSound(null, event.getEntity(), ANCHOR_SLAM_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void executionDamage(LivingHurtEvent event) {
        //Prevent an entity from being reaved more than once or applying to non-melee strikes.
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }


        if(event.getSource().getEntity() instanceof Player player) {
            float executionDamage = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.EXECUTION_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
            if(executionDamage != 0) {
                if(WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
                    WoldsVaults.LOGGER.debug("[WOLD'S VAULTS] Added " + ((event.getEntityLiving().getMaxHealth() - event.getEntityLiving().getHealth()) * executionDamage) + " bonus execution damage to attack.");
                }

                event.setAmount(event.getAmount() + ((event.getEntityLiving().getMaxHealth() - event.getEntityLiving().getHealth()) * executionDamage));

            }
        }
    }

    @SubscribeEvent
    public static void thornsScalingDamage(LivingHurtEvent event) {
        //Prevent an entity from being reaved more than once or applying to non-melee strikes.
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof VaultGearItem) {
            VaultGearData data = VaultGearData.read(player.getMainHandItem().copy());
            if(data != null) {
                float thornsScalingPercent = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.THORNS_SCALING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
                if(thornsScalingPercent <= 0F) {
                    return;
                }

                float thornsDamage = ThornsHelper.getAdditionalThornsFlatDamage(player);
                event.setAmount(event.getAmount() + (thornsDamage * thornsScalingPercent));

            }
        }
    }


    @SubscribeEvent
    public static void apScalingDamage(LivingHurtEvent event) {
        //Prevent an entity from being reaved more than once or applying to non-melee strikes.
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof VaultGearItem) {
            VaultGearData data = VaultGearData.read(player.getMainHandItem().copy());
            if(data != null) {
                float apScalingPercent = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.AP_SCALING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
                if(apScalingPercent <= 0F) {
                    return;
                }

                float abilityPower = WoldAttributeHelper.getAdditionalAbilityPower(player);
                event.setAmount(event.getAmount() + (abilityPower * apScalingPercent));

            }
        }
    }

    @SubscribeEvent
    public static void hexingHit(LivingHurtEvent event) {
        //Prevent an entity from being reaved more than once or applying to non-melee strikes.
        if(!WoldEventHelper.isNormalAttack()) {
            return;
        }

        if(event.getSource().isProjectile()) {
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float hexingChance = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.HEXING_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            if(hexingChance != 0) {
                if(player.level.random.nextFloat() <= hexingChance) {
                    MobEffectInstance instance = HexEffects.HEX_EFFECTS.getRandom(player.getRandom());
                    if(instance == null){
                        return;
                    }

                    event.getEntityLiving().addEffect(new MobEffectInstance(instance));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void echoingHit(LivingHurtEvent event) {

        if(event.getSource().isProjectile()) {
            return;
        }

        if(WoldActiveFlags.IS_ECHOING_ATTACKING.isSet() && (ActiveFlags.IS_AOE_ATTACKING.isSet() || ActiveFlags.IS_CHAINING_ATTACKING.isSet())){
            return;
        }


        if(ActiveFlags.IS_DOT_ATTACKING.isSet()
        || ActiveFlags.IS_LEECHING.isSet()
//        || ActiveFlags.IS_AOE_ATTACKING.isSet()
        || ActiveFlags.IS_REFLECT_ATTACKING.isSet()
//        || ActiveFlags.IS_TOTEM_ATTACKING.isSet()
        || ActiveFlags.IS_CHARMED_ATTACKING.isSet()
        || ActiveFlags.IS_EFFECT_ATTACKING.isSet()
//        || ActiveFlags.IS_JAVELIN_ATTACKING.isSet()
        || ActiveFlags.IS_SMITE_ATTACKING.isSet()
//        || ActiveFlags.IS_SMITE_BASE_ATTACKING.isSet()
//        || ActiveFlags.IS_CHAINING_ATTACKING.isSet()
//        || ActiveFlags.IS_THORNS_REFLECTING.isSet()
//        || ActiveFlags.IS_FIRESHOT_ATTACKING.isSet()
//        || ActiveFlags.IS_GLACIAL_SHATTER_ATTACKING.isSet()
//        || ActiveFlags.IS_AP_ATTACKING.isSet()
        ){
            return;
        }

        if(event.getSource().getEntity() instanceof Player player) {
            float echoingChance = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ECHOING_CHANCE, VaultGearAttributeTypeMerger.floatSum());
            float echoingDamage = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ECHOING_DAMAGE, VaultGearAttributeTypeMerger.floatSum());
            if(echoingChance != 0) {
                if (WoldActiveFlags.IS_ECHOING_ATTACKING.isSet())
                    echoingChance = (float) Math.sqrt(echoingChance);

                if(player.level.random.nextFloat() <= echoingChance) {
                    EchoingPotionEffect newEffect = (EchoingPotionEffect) ModEffects.ECHOING;

                    if (WoldActiveFlags.IS_ECHOING_ATTACKING.isSet() && event.getEntityLiving().hasEffect(ModEffects.ECHOING)) {
                        newEffect = (EchoingPotionEffect) event.getEntityLiving().getEffect(ModEffects.ECHOING).getEffect();
                        ////[[DEBUG]]
                        //WoldsVaults.LOGGER.info("[WOLD'S VAULTS] Added a {} damage echo to attack from a previous echo.", newEffect.getDamage());
                    }
                    else {
                        newEffect.setDamage(event.getAmount());
                        newEffect.setAttacker(player);
                        newEffect.setSource(event.getSource());
                    }

                    float damage = newEffect.getDamage() * 0.667f;

                    if(echoingDamage != 0)
                        damage *= 1 + echoingDamage;

                    if(damage > 1.0f) {
                        newEffect.setDamage(damage);
                        int duration = EffectDurationHelper.adjustEffectDurationFloor(player, 1) * 10;
                        event.getEntityLiving().addEffect(new MobEffectInstance(newEffect, duration, 0));

                        ////[[DEBUG]]
                        //WoldsVaults.LOGGER.info("[WOLD'S VAULTS] Added a {} damage echo to attack.", damage);
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
                if (!ServerVaults.get(world).isEmpty() || !(offHand.getItem() instanceof VaultGearItem)) {
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

    /**
     * Handles the reduction/cancellation of fall damage for players with the Feather Trinket equipped.
     * <p>
     * If the player has an active and usable {@code MultiJumpTrinket}:
     * - Fall damage is canceled if the fall distance is less than 5.0 blocks to prevent damage ticking when taking no damage.
     * - Otherwise, the fall distance is reduced by 2.0 blocks to mitigate fall damage.
     * <p>
     * Called whenever a LivingEntity falls
     */
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
