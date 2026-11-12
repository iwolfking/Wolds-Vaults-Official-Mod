package xyz.iwolfking.woldsvaults.entities.extraction;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.MysticExpertise;
import iskallia.vault.skill.prestige.SuperCrystalsPrestigePower;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import top.theillusivec4.curios.api.CuriosApi;
import xyz.iwolfking.woldsvaults.blocks.containers.ExtractorCardMenu;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.extraction.jobs.GreedCardUpgrades;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;
import xyz.iwolfking.woldsvaults.objectives.ExtractionObjective;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@SuppressWarnings({"deprecated", "removal"})
public class ExtractorEntity extends LivingEntity implements IAnimatable, RigTarget {
    private static final EntityDataAccessor<Boolean> DATA_DRILLING = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_JOB_DONE = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_VARIANTS = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_ARMED = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FLUID_MB = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLUID_LOCK = SynchedEntityData.defineId(ExtractorEntity.class, EntityDataSerializers.INT);
    public static final int CARD_CONTAINER_SIZE = ExtractionObjectiveConfig.GreedInfuserEntry.SLOT_LIMIT;
    private static final double MENU_RANGE_SQ = 8.0D * 8.0D;

    public static final String[] ALL_VARIANT_BONES = {
            "chassis_s1_wooden", "chassis_s2_chromatic", "chassis_s3_suspension", "chassis_s4_armored",
            "chassis_s5_echosteel", "legs_s1_wooden", "legs_s2_chromatic", "legs_s3_suspension",
            "legs_s4_armored", "legs_s5_echosteel", "core_s1_combustion", "core_s2_turbine",
            "core_s3_fusion", "core_s4_etg", "core_s5_echofluid", "radiator_s1_grating",
            "radiator_s2_silkvent", "radiator_s3_painite", "radiator_s4_heatsink",
            "radiator_s5_evaporator", "shield_s1_projector", "shield_s2_barrier",
            "shield_s3_voidbender", "shield_s4_impact", "shield_s5_echoflare", "scanner_s1_probe",
            "scanner_s2_calibrator", "scanner_s3_wutodite", "scanner_s4_vaulterite",
            "scanner_s5_echolocator", "drill_standard", "drill_laser", "drill_greed",
            "drill_archeology", "drill_excavator", "drill_refiner", "drill_heavy", "drill_juicer",
            "drill_glue", "drill_crystal", "drill_containment", "attach_aux_radiator_s1",
            "attach_shield_charger_s1", "attach_driver_override_s1", "attach_extra_plating_s1",
            "attach_fuel_injector_s1", "attach_battery_array_s1", "attach_ore_cleaner_s1",
            "attach_defense_turret_s1", "attach_system_reinforcement_s1", "attach_nanobots_s1",
            "attach_aux_radiator_s2", "attach_shield_charger_s2", "attach_driver_override_s2",
            "attach_extra_plating_s2", "attach_fuel_injector_s2", "attach_battery_array_s2",
            "attach_ore_cleaner_s2", "attach_defense_turret_s2", "attach_system_reinforcement_s2",
            "attach_nanobots_s2", "attach_aux_radiator_s3", "attach_shield_charger_s3",
            "attach_driver_override_s3", "attach_extra_plating_s3", "attach_fuel_injector_s3",
            "attach_battery_array_s3", "attach_ore_cleaner_s3", "attach_defense_turret_s3",
            "attach_system_reinforcement_s3", "attach_nanobots_s3", "attach_aux_radiator_s4",
            "attach_shield_charger_s4", "attach_driver_override_s4", "attach_extra_plating_s4",
            "attach_fuel_injector_s4", "attach_battery_array_s4", "attach_ore_cleaner_s4",
            "attach_defense_turret_s4", "attach_system_reinforcement_s4", "attach_nanobots_s4",
            "attach_aux_radiator_s5", "attach_shield_charger_s5", "attach_driver_override_s5",
            "attach_extra_plating_s5", "attach_fuel_injector_s5", "attach_battery_array_s5",
            "attach_ore_cleaner_s5", "attach_defense_turret_s5", "attach_system_reinforcement_s5",
            "attach_nanobots_s5"
    };
    /** Attachments are optional and up to three are installed at once, so they are
     * absent here: an extractor with none shows none. */
    public static final String DEFAULT_VARIANT_BONES = "chassis_s1_wooden,legs_s1_wooden,core_s1_combustion,radiator_s1_grating,shield_s1_projector,scanner_s1_probe,drill_standard";
    private static final int LOOT_ROWS = 6;
    private static final int SHIELD_REACTIVATION_TICKS = 40;

    private final AnimationFactory factory = new AnimationFactory(this);
    private AnimationBuilder mainChain;
    private AnimationBuilder drillChain;
    private boolean mainChainDrilling;
    private boolean drillChainDrilling;

    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private final SimpleContainer loot = new SimpleContainer(9 * LOOT_ROWS);

    /** Client-only blend between idle and drilling for the procedural motion in
     * {@link xyz.iwolfking.woldsvaults.entities.extraction.client.ExtractorGeoModel};
     * mirrors motion.py RAMP_SECONDS. Never synced -- it is derived from DATA_DRILLING. */
    private static final float RAMP_SECONDS = 0.8F;
    private float drillRamp;

    private float shield;
    private float energy;
    private float currentDurability;
    private float maxShield;
    private float armor;
    private float shieldToughness;
    private float shieldRegenPerTick;
    private int shieldDownTicks;
    private int power;
    private int speed;
    private float energyCapacity;
    private float energyCostPerSecond;
    private float energyPerCell;
    private float drillQuantity;
    private float drillRarity;
    private int maxDurability;
    private float durabilityDrainMult = 1.0F;
    private float wearPerTick;
    private float[] attachmentDurability = new float[0];
    private float healthRegenPerTick;
    private String jobId = "";
    private int siteIndex = -1;
    private UUID owner;

    private final SimpleContainer cards = new SimpleContainer(CARD_CONTAINER_SIZE);
    private int cardSlots;
    private String cardTargetClass = "";
    private int placerCapacityFlat;
    private float placerCapacityMultiplier = 1.0F;
    private final FluidTank heldFluid = new FluidTank(Integer.MAX_VALUE);
    private int fluidLockTicks;

    public ExtractorEntity(EntityType<? extends ExtractorEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DRILLING, false);
        this.entityData.define(DATA_JOB_DONE, false);
        this.entityData.define(DATA_VARIANTS, DEFAULT_VARIANT_BONES);
        this.entityData.define(DATA_ARMED, false);
        this.entityData.define(DATA_FLUID_MB, 0);
        this.entityData.define(DATA_FLUID_LOCK, 0);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "main", 2, this::mainPredicate));
        animationData.addAnimationController(new AnimationController<>(this, "drill", 0, this::drillPredicate));
    }

    private <E extends IAnimatable> PlayState mainPredicate(AnimationEvent<E> event) {
        boolean drilling = this.isDrillingAnim();
        if (this.mainChain == null) {
            this.mainChain = drilling
                    ? new AnimationBuilder().addAnimation("deploy", false).addAnimation("active", true)
                    : new AnimationBuilder().addAnimation("idle", true);
            this.mainChainDrilling = drilling;
        } else if (drilling != this.mainChainDrilling) {
            this.mainChain = drilling
                    ? new AnimationBuilder().addAnimation("deploy", false).addAnimation("active", true)
                    : new AnimationBuilder().addAnimation("retract", false).addAnimation("idle", true);
            this.mainChainDrilling = drilling;
        }
        event.getController().setAnimation(this.mainChain);
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState drillPredicate(AnimationEvent<E> event) {
        boolean drilling = this.isDrillingAnim();
        if (drilling != this.drillChainDrilling || (this.drillChain == null && drilling)) {
            this.drillChain = drilling
                    ? new AnimationBuilder().addAnimation("spin_wind_up", false).addAnimation("spin_fast", true)
                    : new AnimationBuilder().addAnimation("spin_wind_down", false);
            this.drillChainDrilling = drilling;
        }
        if (this.drillChain == null) {
            return PlayState.STOP;
        }
        event.getController().setAnimation(this.drillChain);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public String[] getEquippedVariantBones() {
        return this.entityData.get(DATA_VARIANTS).split(",");
    }

    /** Client-side display copies (the Assembler vitrine) set their loadout directly; nothing syncs. */
    public void setPreviewVariants(String variants) {
        this.entityData.set(DATA_VARIANTS, sanitizeVariants(variants));
    }

    private static final java.util.Set<String> KNOWN_VARIANT_BONES = new java.util.HashSet<>(java.util.Arrays.asList(ALL_VARIANT_BONES));

    /**
     * Drops bone names the current rig does not know (extractors built before the art rework carry
     * frame_a_braced-style names) and falls back to the default loadout when nothing survives, so a
     * stale item renders a whole machine instead of an empty skeleton.
     */
    public static String sanitizeVariants(String variants) {
        if (variants == null || variants.isEmpty()) {
            return DEFAULT_VARIANT_BONES;
        }
        java.util.List<String> kept = new java.util.ArrayList<>();
        java.util.List<String> dropped = new java.util.ArrayList<>();
        for (String raw : variants.split(",")) {
            String bone = raw.trim();
            if (bone.isEmpty()) {
                continue;
            }
            (KNOWN_VARIANT_BONES.contains(bone) ? kept : dropped).add(bone);
        }
        if (!dropped.isEmpty()) {
            WoldLogger.error("Extractor variant bones {} do not exist on the current rig and were dropped{}.", dropped,
                    kept.isEmpty() ? "; the default loadout is rendered instead" : "");
        }
        return kept.isEmpty() ? DEFAULT_VARIANT_BONES : String.join(",", kept);
    }

    /** Snapshots the deploying item's stat block onto this entity and charges the initial energy. */
    public void initFromItem(ItemStack stack, Player deployer, int siteIndex, float initialEnergyFraction) {
        CompoundTag data = ExtractorItem.getData(stack);
        this.maxShield = data.getFloat("Shield");
        this.shield = this.maxShield;
        this.armor = data.getFloat("Armor");
        this.shieldToughness = data.getFloat("ShieldToughness");
        this.shieldRegenPerTick = data.getFloat("ShieldRegen");
        this.power = data.getInt("Power");
        this.speed = data.getInt("Speed");
        this.energyCapacity = data.getFloat("EnergyCapacity");
        this.energyCostPerSecond = data.getFloat("EnergyCost");
        this.energyPerCell = data.getFloat("EnergyPerCell");
        this.drillQuantity = data.getFloat("Quantity");
        this.drillRarity = data.getFloat("Rarity");
        this.currentDurability = data.getInt("Durability");
        this.maxDurability = data.getInt("MaxDurability");
        this.jobId = data.getString("Job");
        this.durabilityDrainMult = data.contains("DurabilityDrainMult") ? data.getFloat("DurabilityDrainMult") : 1.0F;
        this.healthRegenPerTick = data.getFloat("HealthRegen");
        this.attachmentDurability = readAttachmentDurability(stack);
        this.recomputeWearPerTick();
        this.siteIndex = siteIndex;
        this.owner = deployer.getUUID();
        if (deployer instanceof ServerPlayer serverPlayer) {
            this.snapshotPlacerCapacity(serverPlayer.server);
        }
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0F, data.getFloat("MaxHealth")));
        this.setHealth(this.getMaxHealth());
        this.entityData.set(DATA_VARIANTS, sanitizeVariants(data.getString("Variants")));
        this.energy = this.energyCapacity * initialEnergyFraction;
        this.setYRot(0.0F);
        this.setYHeadRot(0.0F);
        this.yBodyRot = 0.0F;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (this.level.isClientSide() || this.isRemoved()) {
            return false;
        }
        if (source == DamageSource.OUT_OF_WORLD) {
            return super.hurt(source, amount);
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker instanceof Player) {
            return false;
        }
        if (attacker.getTags().contains(xyz.iwolfking.woldsvaults.objectives.extraction.ExtractionSpawnManager.TAG_RAM)) {
            this.applyRamHit(attacker, this.ramHitDamage(ModConfigs.EXTRACTION_OBJECTIVE.WAVES.ram.killSeconds));
            return true;
        }
        if (this.shield > 0.0F) {
            float absorbed = CombatRules.getDamageAfterAbsorb(amount, this.shieldToughness, 0.0F);
            float taken = Math.min(this.shield, absorbed);
            this.shield -= taken;
            this.invulnerableTime = 0;
            if (this.shield <= 0.0F) {
                this.shield = 0.0F;
                this.shieldDownTicks = SHIELD_REACTIVATION_TICKS;
                this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
            } else {
                this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.6F, 1.4F);
            }
            float overflow = absorbed <= 0.0F ? 0.0F : amount * (1.0F - taken / absorbed);
            if (overflow > 0.0F) {
                this.hurtHull(source, overflow);
            }
            return true;
        }
        return this.hurtHull(source, amount);
    }

    /** Damage that reaches the hull: armor-mitigated, charged against durability, then vanilla health. */
    private boolean hurtHull(DamageSource source, float amount) {
        float mitigated = CombatRules.getDamageAfterAbsorb(amount, this.armor, 0.0F);
        float hpDamagePerDurability = ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY.hpDamagePerDurabilityPoint;
        if (hpDamagePerDurability > 0.0F) {
            this.currentDurability = Math.max(0.0F, this.currentDurability - mitigated / hpDamagePerDurability * this.durabilityDrainMult);
        }
        boolean result = super.hurt(source, mitigated);
        this.invulnerableTime = 0;
        return result;
    }

    @Override
    public void die(@Nonnull DamageSource source) {
        if (!this.level.isClientSide()) {
            float loss = ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY.destructionLossFraction * this.maxDurability;
            int remaining = (int) Math.max(0.0F, this.currentDurability - loss);
            writeBackToItem(this.level.getServer(), this.getUUID(), remaining, this.getMaxHealth(), this.attachmentDurability);
            this.dropLoot();
            this.dropCards();
            VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get)
                    .ifPresentOrElse(objective -> objective.onExtractorDestroyed(this),
                            () -> WoldLogger.error("Extractor entity {} died outside an extraction objective; site state cannot be updated.", this.getUUID()));
        }
        super.die(source);
    }

    private void dropLoot() {
        for (int i = 0; i < this.loot.getContainerSize(); i++) {
            ItemStack stack = this.loot.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), stack);
            }
        }
    }

    /**
     * Finds the deploying item (whoever now holds it) and writes the session's durability and health
     * results onto it, re-arming it. Falls back to the item's own timed self-reset when nobody online
     * holds it.
     */
    public static void writeBackToItem(MinecraftServer server, UUID entityId, int durability, float hp, float[] attachmentDurability) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof ExtractorItem && entityId.equals(ExtractorItem.getDeployedEntity(stack))) {
                    ExtractorItem.armWithResults(stack, durability, hp, attachmentDurability);
                    return;
                }
            }
        }
        WoldLogger.info("No online player holds the extractor item keyed to entity {}; relying on the item's timed self-reset.", entityId);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 localHit, InteractionHand hand) {
        if (this.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean ownItem = held.getItem() instanceof ExtractorItem && this.getUUID().equals(ExtractorItem.getDeployedEntity(held));
        if (this.isArmed()) {
            if (ownItem) {
                this.pickUpArmed(player, held);
            } else if (player instanceof ServerPlayer serverPlayer) {
                this.openCardMenu(serverPlayer);
            }
            return InteractionResult.CONSUME;
        }
        if (this.isJobDone()) {
            if (ownItem) {
                if (this.hasFluid() && this.fluidLockTicks > 0) {
                    player.displayClientMessage(new TextComponent(String.format("%,d mB of Prismatic Glue is still inside - collect it with a tank, or wait %ds to discard it.",
                            this.getFluidAmount(), (this.fluidLockTicks + 19) / 20)).withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResult.CONSUME;
                }
                this.pickUp(player, held);
                return InteractionResult.CONSUME;
            }
            if (this.hasFluid() && held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
                this.fillHeldContainer(player, hand, held);
                return InteractionResult.CONSUME;
            }
            if (!this.cards.isEmpty()) {
                int count = this.giveCardsTo(player);
                player.displayClientMessage(new TextComponent(String.format("Collected %d greed card%s.", count, count == 1 ? "" : "s")).withStyle(ChatFormatting.GREEN), true);
                this.level.playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
                return InteractionResult.CONSUME;
            }
            if (isLootBoxHit(localHit)) {
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, this.loot, LOOT_ROWS),
                        new TextComponent("Extractor Storage")));
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(new TextComponent("Pick up the extractor by right-clicking it with its item in hand!").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }
        int deposited = this.depositCellsFrom(player);
        if (deposited > 0) {
            player.displayClientMessage(new TextComponent(String.format("Deposited %d power cell%s (+%,.0f energy).",
                    deposited, deposited == 1 ? "" : "s", deposited * this.energyPerCell)).withStyle(ChatFormatting.YELLOW), true);
            this.level.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8F, 0.6F);
        } else {
            player.displayClientMessage(new TextComponent(String.format("Energy %,.0f / %,.0f - deposit power cells to keep it drilling.",
                    this.energy, this.energyCapacity)).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }

    private void pickUp(Player player, ItemStack held) {
        ExtractorItem.armWithResults(held, (int) this.currentDurability, this.getHealth(), this.attachmentDurability);
        this.dropLoot();
        this.dropCards();
        int voided = this.getFluidAmount();
        if (voided > 0) {
            this.heldFluid.setFluid(FluidStack.EMPTY);
            this.syncFluid();
            player.displayClientMessage(new TextComponent(String.format("%,d mB of Prismatic Glue was left in the extractor and lost.", voided)).withStyle(ChatFormatting.RED), false);
        }
        player.displayClientMessage(new TextComponent("Extractor collected.").withStyle(ChatFormatting.GREEN), true);
        VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get)
                .ifPresent(objective -> objective.onExtractorCollected(this));
        this.discard();
    }

    /** Takes back a placed rig that was never started: its inserted cards go back to the player and the site frees up. */
    private void pickUpArmed(Player player, ItemStack held) {
        ExtractorItem.armWithResults(held, (int) this.currentDurability, this.getHealth(), this.attachmentDurability);
        int returned = this.giveCardsTo(player);
        player.displayClientMessage(new TextComponent(returned > 0
                ? String.format("Extractor collected; %d card%s returned.", returned, returned == 1 ? "" : "s")
                : "Extractor collected.").withStyle(ChatFormatting.GREEN), true);
        this.closeCardMenus();
        VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get)
                .ifPresentOrElse(objective -> objective.onArmedExtractorCollected(this),
                        () -> WoldLogger.error("Armed extractor {} was collected outside an extraction objective; its site state was not reset.", this.getUUID()));
        this.discard();
    }

    private static boolean isLootBoxHit(Vec3 localHit) {
        return localHit.x < -0.85D && localHit.y >= 2.2D && localHit.y <= 3.1D;
    }

    @Override
    public int getPadIndex() {
        return 0;
    }

    @Override
    public boolean isRigTargetable() {
        return this.isAlive() && !this.isJobDone() && !this.isArmed();
    }

    @Override
    public float ramHitDamage(float killSeconds) {
        return this.shieldRegenPerTick * 20.0F + (this.maxShield + this.getMaxHealth()) / Math.max(1.0F, killSeconds);
    }

    @Override
    public void applyRamHit(LivingEntity ram, float damage) {
        float taken = Math.min(this.shield, damage);
        this.shield -= taken;
        if (taken > 0.0F && this.shield <= 0.0F) {
            this.shield = 0.0F;
            this.shieldDownTicks = SHIELD_REACTIVATION_TICKS;
            this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 0.8F, 0.9F);
        }
        float overflow = damage - taken;
        if (overflow > 0.0F) {
            float hpDamagePerDurability = ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY.hpDamagePerDurabilityPoint;
            if (hpDamagePerDurability > 0.0F) {
                this.currentDurability = Math.max(0.0F, this.currentDurability - overflow / hpDamagePerDurability * this.durabilityDrainMult);
            }
            super.hurt(DamageSource.mobAttack(ram), overflow);
            this.invulnerableTime = 0;
        }
        this.level.playSound(null, this.blockPosition(), SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    /** Charges a share of the destruction loss to the hull's durability: a wrecked thumper costs the main rig this. */
    public float chargeDestructionPenalty(float fractionOfDestructionLoss) {
        float loss = fractionOfDestructionLoss * ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY.destructionLossFraction * this.maxDurability;
        this.currentDurability = Math.max(0.0F, this.currentDurability - loss);
        return loss;
    }

    public int getMaxDurability() {
        return this.maxDurability;
    }

    public float getEnergyPerCell() {
        return this.energyPerCell;
    }

    /** Moves power cells from the player's inventory, backpacks and curios into the energy pool until it is full. */
    public int depositCellsFrom(Player player) {
        int consumed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize() && this.energy < this.energyCapacity; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.POWER_CELL) {
                while (!stack.isEmpty() && this.energy < this.energyCapacity) {
                    stack.shrink(1);
                    this.energy = Math.min(this.energyCapacity, this.energy + this.energyPerCell);
                    consumed++;
                }
            } else {
                consumed += this.depositFromBackpack(stack);
            }
        }
        consumed += this.depositFromCurios(player);
        return consumed;
    }

    private int depositFromBackpack(ItemStack candidate) {
        if (this.energy >= this.energyCapacity || candidate.isEmpty()) {
            return 0;
        }
        try {
            IItemHandler handler = candidate
                    .getCapability(net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper.getCapabilityInstance())
                    .map(wrapper -> (IItemHandler) wrapper.getInventoryHandler())
                    .orElse(null);
            return handler == null ? 0 : this.depositFromHandler(handler);
        } catch (Throwable t) {
            WoldLogger.error("Sophisticated Backpacks cell sweep failed; only main-inventory cells were deposited.", t);
            return 0;
        }
    }

    private int depositFromCurios(Player player) {
        try {
            return CuriosApi.getCuriosHelper().getEquippedCurios(player).map(curios -> {
                int consumed = 0;
                for (int i = 0; i < curios.getSlots(); i++) {
                    consumed += this.depositFromBackpack(curios.getStackInSlot(i));
                }
                return consumed;
            }).orElse(0);
        } catch (Throwable t) {
            WoldLogger.error("Curios cell sweep failed; equipped backpacks were skipped.", t);
            return 0;
        }
    }

    private int depositFromHandler(IItemHandler handler) {
        int consumed = 0;
        for (int slot = 0; slot < handler.getSlots() && this.energy < this.energyCapacity; slot++) {
            while (this.energy < this.energyCapacity && handler.getStackInSlot(slot).getItem() == ModItems.POWER_CELL
                    && !handler.extractItem(slot, 1, false).isEmpty()) {
                this.energy = Math.min(this.energyCapacity, this.energy + this.energyPerCell);
                consumed++;
            }
        }
        return consumed;
    }

    /** Rolls the payout into the on-board storage, heals the configured fraction and powers down. */
    public void completeJob(NonNullList<ItemStack> payout) {
        for (ItemStack stack : payout) {
            ItemStack remainder = this.loot.addItem(stack);
            if (!remainder.isEmpty()) {
                Containers.dropItemStack(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), remainder);
            }
        }
        this.heal(this.getMaxHealth() * ModConfigs.EXTRACTION_OBJECTIVE.DEPLOYMENT.healFractionOnJobComplete);
        this.setJobDone(true);
        this.setDrillingAnim(false);
        this.level.playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.7F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level.isClientSide()) {
            float target = this.isDrillingAnim() ? 1.0F : 0.0F;
            float step = 1.0F / (RAMP_SECONDS * 20.0F);
            this.drillRamp = this.drillRamp < target
                    ? Math.min(target, this.drillRamp + step)
                    : Math.max(target, this.drillRamp - step);
        }
        if (!this.level.isClientSide() && this.fluidLockTicks > 0) {
            this.fluidLockTicks--;
            if (this.fluidLockTicks % 20 == 0) {
                this.entityData.set(DATA_FLUID_LOCK, this.fluidLockTicks / 20);
            }
        }
        if (!this.level.isClientSide() && this.tickCount % 100 == 0 && VaultUtils.getVault(this.level).isEmpty()) {
            WoldLogger.info("Extractor entity {} found itself outside a vault and self-destructed.", this.getUUID());
            this.discard();
        }
    }

    @Override
    protected void tickDeath() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public void travel(@Nonnull Vec3 input) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean canBeAffected(@Nonnull MobEffectInstance effect) {
        return false;
    }

    @Override
    protected int getExperienceReward(@Nonnull Player player) {
        return 0;
    }

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(@Nonnull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("WvShield", this.shield);
        tag.putFloat("WvMaxShield", this.maxShield);
        tag.putFloat("WvEnergy", this.energy);
        tag.putFloat("WvDurability", this.currentDurability);
        tag.putInt("WvMaxDurability", this.maxDurability);
        tag.putFloat("WvArmor", this.armor);
        tag.putFloat("WvShieldToughness", this.shieldToughness);
        tag.putFloat("WvShieldRegen", this.shieldRegenPerTick);
        tag.putInt("WvShieldDown", this.shieldDownTicks);
        tag.putInt("WvPower", this.power);
        tag.putInt("WvSpeed", this.speed);
        tag.putFloat("WvEnergyCapacity", this.energyCapacity);
        tag.putFloat("WvEnergyCost", this.energyCostPerSecond);
        tag.putFloat("WvEnergyPerCell", this.energyPerCell);
        tag.putFloat("WvQuantity", this.drillQuantity);
        tag.putFloat("WvRarity", this.drillRarity);
        tag.putString("WvJob", this.jobId);
        tag.putFloat("WvDurabilityDrainMult", this.durabilityDrainMult);
        tag.putFloat("WvHealthRegen", this.healthRegenPerTick);
        tag.putInt("WvSiteIndex", this.siteIndex);
        tag.putBoolean("WvJobDone", this.isJobDone());
        if (this.owner != null) {
            tag.putUUID("WvOwner", this.owner);
        }
        tag.putString("WvVariants", this.entityData.get(DATA_VARIANTS));
        tag.put("WvLoot", this.loot.createTag());
        ListTag attachmentWear = new ListTag();
        for (float value : this.attachmentDurability) {
            attachmentWear.add(FloatTag.valueOf(value));
        }
        tag.put("WvAttachmentDurability", attachmentWear);
        tag.putBoolean("WvArmed", this.isArmed());
        tag.put("WvCards", this.cards.createTag());
        tag.putInt("WvCardSlots", this.cardSlots);
        tag.putString("WvCardTarget", this.cardTargetClass);
        tag.putInt("WvPlacerCapacityFlat", this.placerCapacityFlat);
        tag.putFloat("WvPlacerCapacityMult", this.placerCapacityMultiplier);
        tag.put("WvFluid", this.heldFluid.writeToNBT(new CompoundTag()));
        tag.putInt("WvFluidLock", this.fluidLockTicks);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.shield = tag.getFloat("WvShield");
        this.maxShield = tag.getFloat("WvMaxShield");
        this.energy = tag.getFloat("WvEnergy");
        this.currentDurability = tag.getFloat("WvDurability");
        this.maxDurability = tag.getInt("WvMaxDurability");
        this.armor = tag.getFloat("WvArmor");
        this.shieldToughness = tag.getFloat("WvShieldToughness");
        this.shieldRegenPerTick = tag.getFloat("WvShieldRegen");
        this.shieldDownTicks = tag.getInt("WvShieldDown");
        this.power = tag.getInt("WvPower");
        this.speed = tag.getInt("WvSpeed");
        this.energyCapacity = tag.getFloat("WvEnergyCapacity");
        this.energyCostPerSecond = tag.getFloat("WvEnergyCost");
        this.energyPerCell = tag.getFloat("WvEnergyPerCell");
        this.drillQuantity = tag.getFloat("WvQuantity");
        this.drillRarity = tag.getFloat("WvRarity");
        this.jobId = tag.getString("WvJob");
        this.durabilityDrainMult = tag.contains("WvDurabilityDrainMult") ? tag.getFloat("WvDurabilityDrainMult") : 1.0F;
        this.healthRegenPerTick = tag.getFloat("WvHealthRegen");
        this.siteIndex = tag.getInt("WvSiteIndex");
        this.setJobDone(tag.getBoolean("WvJobDone"));
        if (tag.hasUUID("WvOwner")) {
            this.owner = tag.getUUID("WvOwner");
        }
        this.entityData.set(DATA_VARIANTS, sanitizeVariants(tag.getString("WvVariants")));
        this.loot.fromTag(tag.getList("WvLoot", 10));
        ListTag attachmentWear = tag.getList("WvAttachmentDurability", Tag.TAG_FLOAT);
        this.attachmentDurability = new float[attachmentWear.size()];
        for (int i = 0; i < attachmentWear.size(); i++) {
            this.attachmentDurability[i] = attachmentWear.getFloat(i);
        }
        this.recomputeWearPerTick();
        this.entityData.set(DATA_ARMED, tag.getBoolean("WvArmed"));
        this.cards.fromTag(tag.getList("WvCards", Tag.TAG_COMPOUND));
        this.cardSlots = tag.getInt("WvCardSlots");
        this.cardTargetClass = tag.getString("WvCardTarget");
        this.placerCapacityFlat = tag.getInt("WvPlacerCapacityFlat");
        this.placerCapacityMultiplier = tag.contains("WvPlacerCapacityMult") ? tag.getFloat("WvPlacerCapacityMult") : 1.0F;
        this.heldFluid.readFromNBT(tag.getCompound("WvFluid"));
        this.fluidLockTicks = tag.getInt("WvFluidLock");
        this.syncFluid();
    }

    /** 0 while idle, 1 while drilling, eased across {@value #RAMP_SECONDS} seconds. */
    public float getDrillRamp() {
        return this.drillRamp;
    }

    public boolean isDrillingAnim() {
        return this.entityData.get(DATA_DRILLING);
    }

    public void setDrillingAnim(boolean drilling) {
        this.entityData.set(DATA_DRILLING, drilling);
    }

    public boolean isJobDone() {
        return this.entityData.get(DATA_JOB_DONE);
    }

    public void setJobDone(boolean done) {
        this.entityData.set(DATA_JOB_DONE, done);
    }

    public float getShield() {
        return this.shield;
    }

    public void setShield(float shield) {
        this.shield = Math.min(this.maxShield, Math.max(0.0F, shield));
    }

    public float getMaxShield() {
        return this.maxShield;
    }

    public float getShieldRegenPerTick() {
        return this.shieldRegenPerTick;
    }

    /** Shield regeneration; a shield that was knocked down stays offline for two seconds before it starts recharging. */
    public void tickShieldRegen() {
        if (this.shieldDownTicks > 0) {
            this.shieldDownTicks--;
            if (this.shieldDownTicks == 0 && this.maxShield > 0.0F) {
                this.level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.5F, 1.6F);
            }
            return;
        }
        if (this.shield < this.maxShield) {
            this.setShield(this.shield + this.shieldRegenPerTick);
        }
    }

    public boolean isShieldDown() {
        return this.shieldDownTicks > 0;
    }

    public float getEnergy() {
        return this.energy;
    }

    public void setEnergy(float energy) {
        this.energy = Math.min(this.energyCapacity, Math.max(0.0F, energy));
    }

    public float getEnergyCapacity() {
        return this.energyCapacity;
    }

    public float getEnergyCostPerSecond() {
        return this.energyCostPerSecond;
    }

    public float getCurrentDurability() {
        return this.currentDurability;
    }

    public void drainDurability(float amount) {
        this.currentDurability = Math.max(0.0F, this.currentDurability - amount * this.durabilityDrainMult);
    }

    /**
     * The hull wear one drilling tick costs before {@code durabilityDrainMult}: a stronger rig burns
     * itself out faster, following {@code wearCoefficient x max(1, Power)^wearPowerExponent} per second.
     * Cached because the power stat is fixed for the whole deployment.
     */
    public float getWearPerTick() {
        return this.wearPerTick;
    }

    private void recomputeWearPerTick() {
        ExtractionObjectiveConfig.DurabilityEntry cfg = ModConfigs.EXTRACTION_OBJECTIVE.DURABILITY;
        if (cfg.wearCoefficient <= 0.0F || !Float.isFinite(cfg.wearCoefficient) || !Float.isFinite(cfg.wearPowerExponent)) {
            WoldLogger.error("extraction_objective.json DURABILITY has an unusable wear curve (coefficient {}, exponent {}); extractor {} will take no drilling wear.",
                    cfg.wearCoefficient, cfg.wearPowerExponent, this.getUUID());
            this.wearPerTick = 0.0F;
            return;
        }
        this.wearPerTick = (float) (cfg.wearCoefficient * Math.pow(Math.max(1, this.power), cfg.wearPowerExponent)) / 20.0F;
    }

    /**
     * Wears every installed attachment's own pool by {@code amount}. Attachment pools are entirely
     * separate from the hull's: they never feed it, and {@code durabilityDrainMult} does not touch them.
     */
    public void drainAttachmentDurability(float amount) {
        for (int i = 0; i < this.attachmentDurability.length; i++) {
            if (this.attachmentDurability[i] > 0.0F) {
                this.attachmentDurability[i] = Math.max(0.0F, this.attachmentDurability[i] - amount);
            }
        }
    }

    public float[] getAttachmentDurability() {
        return this.attachmentDurability;
    }

    private static float[] readAttachmentDurability(ItemStack stack) {
        List<PartData> attachments = ExtractorItem.getBuildAttachments(stack);
        float[] values = new float[attachments.size()];
        for (int i = 0; i < attachments.size(); i++) {
            values[i] = attachments.get(i).durability;
        }
        return values;
    }

    /** Passive health regeneration granted by attachments; runs only while the extractor is drilling. */
    public void tickHealthRegen() {
        if (this.healthRegenPerTick > 0.0F && this.getHealth() < this.getMaxHealth()) {
            this.heal(this.healthRegenPerTick);
        }
    }

    public float getDurabilityDrainMult() {
        return this.durabilityDrainMult;
    }

    public int getPower() {
        return this.power;
    }

    public int getDrillSpeed() {
        return this.speed;
    }

    public float getDrillQuantity() {
        return this.drillQuantity;
    }

    public float getDrillRarity() {
        return this.drillRarity;
    }

    public String getJobId() {
        return this.jobId;
    }

    public int getSiteIndex() {
        return this.siteIndex;
    }

    public Optional<UUID> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    /** Places the rig idle for a job that needs inputs first: {@code slots} card slots upgrading to {@code targetClass}. */
    public void arm(int slots, String targetClass) {
        this.cardSlots = Math.max(1, Math.min(CARD_CONTAINER_SIZE, slots));
        this.cardTargetClass = targetClass == null ? "" : targetClass;
        this.entityData.set(DATA_ARMED, true);
    }

    public boolean isArmed() {
        return this.entityData.get(DATA_ARMED);
    }

    public SimpleContainer getCards() {
        return this.cards;
    }

    public int getCardSlots() {
        return this.cardSlots;
    }

    public String getCardTargetClass() {
        return this.cardTargetClass;
    }

    public boolean isUsableBy(Player player) {
        return this.isAlive() && this.isArmed() && player.distanceToSqr(this) <= MENU_RANGE_SQ;
    }

    private void openCardMenu(ServerPlayer player) {
        if (this.cardTargetClass.isEmpty()) {
            WoldLogger.error("Armed extractor {} has no greed card target class; its insert screen cannot open.", this.getUUID());
            player.displayClientMessage(new TextComponent("This Greed Infuser has no card class to upgrade to.").withStyle(ChatFormatting.RED), true);
            return;
        }
        NetworkHooks.openGui(player, new SimpleMenuProvider((id, inventory, p) -> ExtractorCardMenu.server(id, inventory, this),
                new TextComponent("Greed Infuser")), buffer -> {
            buffer.writeVarInt(this.getId());
            buffer.writeVarInt(this.cardSlots);
            buffer.writeUtf(this.cardTargetClass);
        });
    }

    private void closeCardMenus() {
        if (this.level.getServer() == null) {
            return;
        }
        for (ServerPlayer player : this.level.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof ExtractorCardMenu menu && menu.isFor(this)) {
                player.closeContainer();
            }
        }
    }

    /** The insert screen's Start button: switches an armed rig with at least one card into a normal drilling job. */
    public boolean start(ServerPlayer player) {
        if (!this.isArmed()) {
            return false;
        }
        if (this.cards.isEmpty()) {
            player.displayClientMessage(new TextComponent("Insert at least one greed card first.").withStyle(ChatFormatting.RED), true);
            return false;
        }
        Optional<ExtractionObjective> objective = VaultUtils.getVault(this.level).flatMap(ExtractionObjective::get);
        if (objective.isEmpty()) {
            WoldLogger.error("Armed extractor {} was started outside an extraction objective; it stays armed.", this.getUUID());
            return false;
        }
        if (!objective.get().startArmedExtractor(this)) {
            return false;
        }
        this.entityData.set(DATA_ARMED, false);
        this.closeCardMenus();
        this.level.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        player.displayClientMessage(new TextComponent("Greed Infuser started - defend it while it works!").withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    /** Upgrades every inserted card in place to the placer's class; cards that cannot be matched stay as they are. */
    public int upgradeCards() {
        int upgraded = 0;
        for (int i = 0; i < this.cards.getContainerSize(); i++) {
            ItemStack stack = this.cards.getItem(i);
            if (!stack.isEmpty() && GreedCardUpgrades.upgrade(stack, this.cardTargetClass)) {
                upgraded++;
            }
        }
        this.cards.setChanged();
        return upgraded;
    }

    /** Hands every held card to the player (inventory first, the ground at their feet otherwise) and returns the count. */
    public int giveCardsTo(Player player) {
        int count = 0;
        for (int i = 0; i < this.cards.getContainerSize(); i++) {
            ItemStack stack = this.cards.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                count += stack.getCount();
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
        this.cards.setChanged();
        return count;
    }

    private void dropCards() {
        for (int i = 0; i < this.cards.getContainerSize(); i++) {
            ItemStack stack = this.cards.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), stack);
            }
        }
    }

    /** Takes a fluid payout on board and starts the pickup lock. */
    public void storeFluid(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return;
        }
        this.heldFluid.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        this.fluidLockTicks = Math.max(0, ModConfigs.EXTRACTION_OBJECTIVE.FLUID_PAYOUT.pickupLockSeconds) * 20;
        this.syncFluid();
    }

    public boolean hasFluid() {
        return !this.heldFluid.getFluid().isEmpty();
    }

    public int getFluidAmount() {
        return this.level.isClientSide() ? this.entityData.get(DATA_FLUID_MB) : this.heldFluid.getFluidAmount();
    }

    /** Seconds left on the pickup lock, as synced to clients. */
    public int getFluidLockSeconds() {
        return this.entityData.get(DATA_FLUID_LOCK);
    }

    private void syncFluid() {
        this.entityData.set(DATA_FLUID_MB, this.heldFluid.getFluidAmount());
        this.entityData.set(DATA_FLUID_LOCK, (this.fluidLockTicks + 19) / 20);
    }

    private void fillHeldContainer(Player player, InteractionHand hand, ItemStack held) {
        int before = this.heldFluid.getFluidAmount();
        FluidActionResult result = FluidUtil.tryFillContainerAndStow(held, this.heldFluid, new PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) {
            player.displayClientMessage(new TextComponent("That container cannot take any more Prismatic Glue.").withStyle(ChatFormatting.RED), true);
            return;
        }
        player.setItemInHand(hand, result.getResult());
        this.syncFluid();
        int moved = before - this.heldFluid.getFluidAmount();
        this.level.playSound(null, this.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(new TextComponent(String.format("Collected %,d mB of Prismatic Glue (%,d mB left).", moved, this.heldFluid.getFluidAmount()))
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    /**
     * Records the placer's own crystal-capacity bonuses the way the Vault Altar applies them (Mystic expertise and the
     * Super Crystals prestige powers), so crystal payouts never need the placer online.
     */
    private void snapshotPlacerCapacity(MinecraftServer server) {
        try {
            this.placerCapacityFlat = PlayerExpertisesData.get(server).getExpertises(this.owner)
                    .getAll(MysticExpertise.class, Skill::isUnlocked).stream().mapToInt(MysticExpertise::getAdditionalCrystalVolume).sum();
            List<SuperCrystalsPrestigePower> powers = PlayerPrestigePowersData.get(server).getPowers(this.owner).getAll(SuperCrystalsPrestigePower.class, Skill::isUnlocked);
            this.placerCapacityFlat += powers.stream().filter(power -> !power.isMultiplyMode()).mapToInt(SuperCrystalsPrestigePower::getExtraCapacity).sum();
            float multiplier = 1.0F;
            for (SuperCrystalsPrestigePower power : powers) {
                if (power.isMultiplyMode()) {
                    multiplier *= power.getCapacityMultiplier();
                }
            }
            this.placerCapacityMultiplier = multiplier;
        } catch (Throwable t) {
            WoldLogger.error("Could not read the crystal-capacity bonuses of extractor placer {}; ancient crystals from this rig get no placer bonus.", this.owner, t);
            this.placerCapacityFlat = 0;
            this.placerCapacityMultiplier = 1.0F;
        }
    }

    public int getPlacerCapacityFlat() {
        return this.placerCapacityFlat;
    }

    public float getPlacerCapacityMultiplier() {
        return this.placerCapacityMultiplier;
    }
}
