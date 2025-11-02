package xyz.iwolfking.woldsvaults.objectives.data;

import cofh.thermal.core.init.TCoreEntities;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.LoadingModList;
import samebutdifferent.ecologics.registry.ModMobEffects;
import vazkii.quark.content.mobs.module.WraithModule;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.*;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.CloudStorageEvents;
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.WildBackportEvents;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.LaCucarachaSpecialVaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.PlayerSwapVaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

import java.util.List;


public class EnchantedEventsRegistry {

    private static final WeightedList<BasicVaultEvent> ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<BasicVaultEvent> OMEGA_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<BasicVaultEvent> POSITIVE_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<BasicVaultEvent> NEGATIVE_ENCHANTED_EVENTS = new WeightedList<>();

    private static final WeightedList<BasicVaultEvent> SPAWN_ENTITY_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<BasicVaultEvent> MODIFIER_ENCHANTED_EVENTS = new WeightedList<>();


    public static final VaultModifierVaultEvent COMMON_POSITIVE_MODIFER_EVENT;
    public static final VaultModifierVaultEvent LEECHING_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent COMMON_NEGATIVE_MODIFER_EVENT;
    public static final VaultModifierVaultEvent RARE_NEGATIVE_MODIFER_EVENT;
    public static final VaultModifierVaultEvent RARE_POSITIVE_MODIFER_EVENT;
    public static final VaultModifierVaultEvent OMEGA_NEGATIVE_MODIFER_EVENT;
    public static final VaultModifierVaultEvent OMEGA_POSITIVE_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent OMEGA_GOD_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent MOB_ONHITS_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent CHAOS_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent CURSES_MODIFIER_EVENT;
    public static final VaultModifierVaultEvent I_CAN_SEE_FOREVER_EVENT;
    public static final VaultModifierVaultEvent HUNTERS_EVENT;
    public static final VaultModifierVaultEvent BINGO_EVENT;
    public static final VaultModifierVaultEvent CASCADING_CHESTS_MODFIER_EVENT;
    public static final PotionEffectVaultEvent SLIPPERY_FLOORS_EVENT;
    public static final PotionEffectVaultEvent SUNBIRD_CURSE_EVENT;
    public static final PotionEffectVaultEvent SUNBIRD_BLESSING_EVENT;
    public static final PotionEffectVaultEvent CLINGING_EVENT;
    public static final PotionEffectVaultEvent INSTAKILL_EVENT;
    public static final PotionEffectVaultEvent LEVITATION_EVENT;
    public static final PotionEffectVaultEvent SHRINKING_EVENT;

    public static final MultiPotionEffectVaultEvent BOLSTERED_EVENT;
    public static final MultiPotionEffectVaultEvent CHEMICAL_BATH_EVENT;
    public static final MultiPotionEffectVaultEvent HOLY_BLESSING_EVENT;

    public static final MultiPotionEffectVaultEvent HYPERSPEED_EVENT;
    public static final SpawnEntityVaultEvent COW_EVENT;
    public static final SpawnEntityVaultEvent BAT_EVENT;
    public static final SpawnEntityVaultEvent CREEPER_EVENT;
    public static final SpawnEntityVaultEvent ZOMBOID_EVENT;
    public static final SpawnEntityVaultEvent BUNFUNGUS_EVENT;
    public static final SpawnEntityVaultEvent ARACHNOPHOBIA_EVENT;
    public static final SpawnEntityVaultEvent GHOSTY_EVENT;
    public static final SpawnEntityVaultEvent TNT_EVENT;
    public static final SpawnEntityVaultEvent TURTLES_EVENT;
    public static final SpawnEntityVaultEvent ZOO_EVENT;
    public static final SpawnEntityVaultEvent VOID_ZOO_EVENT;
    public static final SpawnEntityVaultEvent DWELLER_EVENT;
    public static final SpawnEntityVaultEvent THERMAL_EXPANSION;
    public static final SpawnEntityVaultEvent LA_CUCARACHA_EVENT;
    public static final GiftItemVaultEvent WOLD_SANTA_BOX_EVENT;
    public static final GiftItemVaultEvent CONSOLATION_PRIZE;
    public static final InceptionVaultEvent PANDAMONIUM_EVENT;
    public static final InceptionVaultEvent X_RANDOM_EVENT;
    public static final InceptionVaultEvent X_OMEGA_RANDOM_EVENT;
    public static final InceptionVaultEvent X_MODIFIER_RANDOM_EVENT;
    public static final InceptionVaultEvent HORDE_EVENT;
    public static final InceptionVaultEvent VAMPIRE_SURVIVORS;
    public static final PlayerSwapVaultEvent TELESWAP_EVENT;
    public static final LaCucarachaSpecialVaultEvent LA_CUCARACHA_RANDOM_EVENT;

    public static final BuffEntityInAreaVaultEvent MOB_VIGOR_EVENT;
    public static final BuffEntityInAreaVaultEvent MOB_DOWNGRADE_EVENT;
    public static final BuffEntityInAreaVaultEvent DISAPPEAR_MOBS_EVENT;
    public static final BuffEntityInAreaVaultEvent MOB_RESISTANCE_EVENT;
    public static final BuffEntityInAreaVaultEvent RANDOM_MOB_BUFFS;
    public static final BuffEntityInAreaVaultEvent MOB_MINIMIZER_EVENT;
    public static final BuffEntityInAreaVaultEvent MOB_MAXIMIZER_EVENT;


    public static void addEvents() {
        //Vault Modifier events
        register(COMMON_POSITIVE_MODIFER_EVENT, 60.0, false, true);
        register(COMMON_NEGATIVE_MODIFER_EVENT, 60.0, false, false);
        register(RARE_POSITIVE_MODIFER_EVENT, 35.0, false, true);
        register(RARE_NEGATIVE_MODIFER_EVENT, 35.0, false, false);
        register(OMEGA_POSITIVE_MODIFIER_EVENT, 6.0, true, true);
        register(OMEGA_NEGATIVE_MODIFER_EVENT, 6.0, true, false);
        register(OMEGA_GOD_MODIFIER_EVENT, 3.0, true, true);
        register(CHAOS_MODIFIER_EVENT, 6.0, true, true);
        register(MOB_ONHITS_MODIFIER_EVENT, 6.0, false, false);
        register(CURSES_MODIFIER_EVENT, 1.0, true, false);
        register(I_CAN_SEE_FOREVER_EVENT, 1.0, true, true);
        register(HUNTERS_EVENT, 12.0, true, true);
        register(BINGO_EVENT, 3.0, true, true);
        register(CASCADING_CHESTS_MODFIER_EVENT, 10.0, true, true);

        //Potion Effect events
        register(SLIPPERY_FLOORS_EVENT, 22.0, false, false);
        register(SUNBIRD_CURSE_EVENT, 12.0, false, false);
        register(SUNBIRD_BLESSING_EVENT, 12.0, false, true);
        register(CLINGING_EVENT, 12.0, false, false);
        register(INSTAKILL_EVENT, 16.0, false, true);
        register(LEVITATION_EVENT, 12.0, false, false);
        register(SHRINKING_EVENT, 6.0, false, false);

        //Multi-Potion Effect events
        register(BOLSTERED_EVENT, 16.0, false, true);
        register(CHEMICAL_BATH_EVENT, 7.0, false, false);
        register(HOLY_BLESSING_EVENT, 14.0, false, true);
        register(HYPERSPEED_EVENT, 16.0, false, true);

        //Mob Spawn events
        register(COW_EVENT, 12.0, false, false);
        register(CREEPER_EVENT, 16.0, false, false);
        register(ZOMBOID_EVENT, 16.0, false, false);
        register(BUNFUNGUS_EVENT, 12.0, false, false);
        register(ARACHNOPHOBIA_EVENT, 16.0, false, false);
        register(GHOSTY_EVENT, 16.0, false, false);
        register(ZOO_EVENT, 5.0, false, false);
        register(DWELLER_EVENT, 16.0, false, false);
        register(VOID_ZOO_EVENT, 6.0, false, false);
        register(TURTLES_EVENT, 16.0, false, false);
        register(THERMAL_EXPANSION, 8.0, false, false);
        register(LA_CUCARACHA_EVENT, 4.0, true, false);

        //Mob Buff Events
        register(MOB_VIGOR_EVENT, 16.0, false, false);
        register(MOB_DOWNGRADE_EVENT, 16.0, false, true);
        register(DISAPPEAR_MOBS_EVENT, 16.0, false, false);
        register(MOB_RESISTANCE_EVENT, 16.0, false, false);
        register(RANDOM_MOB_BUFFS, 16.0, false, false);
        register(MOB_MINIMIZER_EVENT, 6.0, false, false);
        register(MOB_MAXIMIZER_EVENT, 8.0, false, false);

        //Item Gift events
        register(WOLD_SANTA_BOX_EVENT, 16.0, true, true);
        register(CONSOLATION_PRIZE, 16.0, false, true);

        //Inception Events
        register(PANDAMONIUM_EVENT, 6.0, false, false);
        register(HORDE_EVENT, 8.0, true, false);
        register(X_RANDOM_EVENT, 8.0, true, true);
        register(X_MODIFIER_RANDOM_EVENT, 8.0, true, true);
        register(X_OMEGA_RANDOM_EVENT, 1.0, true, true);
        register(VAMPIRE_SURVIVORS, 3.0, false, true);

        //Unique Events
        register(TELESWAP_EVENT, 12.0, false, false);

        //Events that rely on mods
        if(LoadingModList.get().getModFileById("wildbackport") != null) {
            WildBackportEvents.init();
        }

        if(LoadingModList.get().getModFileById("cloudstorage") != null) {
            CloudStorageEvents.init();
        }

        ENCHANTED_EVENTS.forEach((basicEnchantedEvent, aDouble) -> {
            if(basicEnchantedEvent instanceof SpawnEntityVaultEvent) {
                SPAWN_ENTITY_ENCHANTED_EVENTS.add(basicEnchantedEvent, aDouble);
            }
        });

        ENCHANTED_EVENTS.forEach((basicEnchantedEvent, aDouble) -> {
            if(basicEnchantedEvent instanceof VaultModifierVaultEvent) {
                MODIFIER_ENCHANTED_EVENTS.add(basicEnchantedEvent, aDouble);
            }
        });
    }

    public static void register(BasicVaultEvent event, Double weight, boolean isOmega, boolean isPositive) {
        ENCHANTED_EVENTS.add(event, weight);
        if(isOmega) {
            OMEGA_ENCHANTED_EVENTS.add(event, 1.0);
        }

        if(isPositive) {
            POSITIVE_ENCHANTED_EVENTS.add(event, weight);
        }
        else {
            NEGATIVE_ENCHANTED_EVENTS.add(event, weight);
        }
    }

    public static WeightedList<BasicVaultEvent> getEvents() {
        return ENCHANTED_EVENTS;
    }

    public static WeightedList<BasicVaultEvent> getOmegaEvents() {
        return OMEGA_ENCHANTED_EVENTS;
    }
    public static WeightedList<BasicVaultEvent> getPositiveEvents() {
        return POSITIVE_ENCHANTED_EVENTS;
    }

    public static WeightedList<BasicVaultEvent> getNegativeEvents() {
        return NEGATIVE_ENCHANTED_EVENTS;
    }


    static {
        COMMON_POSITIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Common Positive", "Adds a modifier from a common pool of positive modifiers.", "#cccc00",  "basic_positive");
        LEECHING_MODIFIER_EVENT = new VaultModifierVaultEvent("Leeching", "Adds leeching vault modifier.", "#800000",  "leeching");
        COMMON_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Common Negative", "Adds a modifier from a pool of common negatives modifiers.", "#ff1a1a",  "basic_negative");
        RARE_POSITIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Rare Positive", "Adds a modifier from a pool of rare positive modifiers", "#8cff1a",  "medium_positive");
        RARE_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Rare Negative", "Adds a modifier from a pool of rare negative modifiers", "#660000", "medium_negative");
        OMEGA_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Omega Negative", "Adds a modifier from a pool of omega negative modifiers", "#330000", "omega_negative");
        OMEGA_POSITIVE_MODIFIER_EVENT = new VaultModifierVaultEvent("Omega Positive", "Adds one very good random modifier.", "#77ff33", "omega_positive");
        OMEGA_GOD_MODIFIER_EVENT = new VaultModifierVaultEvent("God's Blessing", "Adds one favour modifier from the gods.", "#ff80d5", "gods_omega_blessing");
        MOB_ONHITS_MODIFIER_EVENT = new VaultModifierVaultEvent("Mob On-hits", "Adds one modifier that adds an on-hit effect to mobs.",  "#00a3cc", "mob_onhits");
        CHAOS_MODIFIER_EVENT = new VaultModifierVaultEvent("Chaos", "Adds a modifier from the chaos pool.", "#4d4dff", "chaos_enchanted");
        CURSES_MODIFIER_EVENT = new VaultModifierVaultEvent("Curses", "Adds one modifier from a pool of curses.", "#3d0099",  "curses");
        CASCADING_CHESTS_MODFIER_EVENT = new VaultModifierVaultEvent("Cascading", "Adds one modifier from a pool of cascading modifiers.", "#d5ff80",  "enchanted_cascade");
        I_CAN_SEE_FOREVER_EVENT = new VaultModifierVaultEvent("I Can See Forever", "Adds All Hunter modifiers.", "#8585ad",  "hunters_enchanted");
        HUNTERS_EVENT = new VaultModifierVaultEvent("Hunter", "Adds a modifier from the pool of Hunter modifiers.", "#8585ad",  "hunters_enchanted_random");
        BINGO_EVENT = new VaultModifierVaultEvent("Bingo!?", "Adds a modifier from the pool of Bingo modifiers.", "#4d4dff",  "bingos_enchanted");
        SLIPPERY_FLOORS_EVENT = new PotionEffectVaultEvent("Slippery Floors", "Who forgot to dry the floor!?", "#adebeb",   ModMobEffects.SLIPPERY.get(), 1800, 10);
        SUNBIRD_CURSE_EVENT = new PotionEffectVaultEvent("Curse of the Sunbird", "It seems gravity is higher...", "#804000", AMEffectRegistry.SUNBIRD_CURSE, 1200, 1);
        SUNBIRD_BLESSING_EVENT = new PotionEffectVaultEvent("Blessing of the Sunbird", "Glide to safety", "#ff9900", AMEffectRegistry.SUNBIRD_BLESSING, 1800, 0);
        CLINGING_EVENT = new PotionEffectVaultEvent("Topsy Turvy", "Walk on ceilings o.o", "#bfff00",   AMEffectRegistry.CLINGING, 1200, 0);
        LEVITATION_EVENT = new PotionEffectVaultEvent("Leviosa", "Up up and away", "#b300b3", MobEffects.LEVITATION, 300, 0);
        INSTAKILL_EVENT = new PotionEffectVaultEvent("Insta-Kill", "Grants strength x 255", "#ff8c1a",   MobEffects.DAMAGE_BOOST, 600 , 255);
        HYPERSPEED_EVENT = new MultiPotionEffectVaultEvent("Hyper-Speed", "Grants speed x 10", "#e6e600",   List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED), 900, 9);
        BOLSTERED_EVENT = new MultiPotionEffectVaultEvent("Barricade", "I feel a lot tougher!", "#804000",  List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE), 900, 2);
        CHEMICAL_BATH_EVENT = new MultiPotionEffectVaultEvent("Chemical Bath", "A nasty brew of potion effects!", "#333300", List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, MobEffects.REGENERATION, MobEffects.WEAKNESS, MobEffects.GLOWING, AMEffectRegistry.BUG_PHEROMONES, MobEffects.BLINDNESS, MobEffects.ABSORPTION, MobEffects.CONFUSION), 300, 1);
        HOLY_BLESSING_EVENT = new MultiPotionEffectVaultEvent("Holy Blessing", "A myriad of positive potion effects!", "#ffd966", List.of(MobEffects.GLOWING, MobEffects.REGENERATION, MobEffects.SATURATION, MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED, MobEffects.MOVEMENT_SPEED, MobEffects.NIGHT_VISION, MobEffects.FIRE_RESISTANCE), 600, 4);
        COW_EVENT = new SpawnEntityVaultEvent("Barnyard Bash", "Attack of the cows", "#666633", new WeightedList<EntityType<?>>().add(ModEntities.AGGRESSIVE_COW, 6.0), new WeightedList<Integer>().add(4, 15).add(6, 12).add(8, 10));
        BAT_EVENT = new SpawnEntityVaultEvent("Bat Swarm", "Bats everywhere!?", "#c2c2a3",  new WeightedList<EntityType<?>>().add(EntityType.BAT, 6.0), new WeightedList<Integer>().add(3, 10).add(6, 10));
        CREEPER_EVENT = new SpawnEntityVaultEvent("Jeepers Creepers", "Attack of the creepers", "#1aff66", new WeightedList<EntityType<?>>().add(ModEntities.T1_CREEPER, 6.0).add(ModEntities.T2_CREEPER, 4.0).add(ModEntities.T3_CREEPER, 2.0), new WeightedList<Integer>().add(5, 10).add(7, 10).add(8, 8));
        ZOMBOID_EVENT = new SpawnEntityVaultEvent("Project Zomboid", "Attack of the zombies", "#006600",  new WeightedList<EntityType<?>>().add(ModEntities.T1_ZOMBIE, 6.0).add(ModEntities.T2_ZOMBIE, 4.0).add(ModEntities.T3_ZOMBIE, 2.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(3, 6).add(9, 5));
        BUNFUNGUS_EVENT = new SpawnEntityVaultEvent("Bunfungus Amongus", "I am Joseph and I like bunnies", "#ffb3b3",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.BUNFUNGUS.get(), 6.0).add(AMEntityRegistry.MUNGUS.get(), 1.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(3, 6).add(9, 5));
        ARACHNOPHOBIA_EVENT = new SpawnEntityVaultEvent("Arachnophobia", "Attack of the spidders", "#0d0033",  new WeightedList<EntityType<?>>().add(ModEntities.VAULT_SPIDER, 6.0).add(ModEntities.VAULT_SPIDER_BABY, 4.0).add(ModEntities.DUNGEON_SPIDER, 2.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(8, 10));
        GHOSTY_EVENT = new SpawnEntityVaultEvent("Happy Halloween", "All ghost costumes this year?", "#ff471a",  new WeightedList<EntityType<?>>().add(WraithModule.wraithType, 6.0), new WeightedList<Integer>().add(8, 10).add(12, 10));
        TNT_EVENT = new SpawnEntityVaultEvent("Whoops", "Who left all this tnt around?", "#000000", new WeightedList<EntityType<?>>().add(EntityType.TNT, 6.0), new WeightedList<Integer>().add(8, 10).add(12, 10));
        TURTLES_EVENT = new SpawnEntityVaultEvent("Ninja Turtles", "Attack of the turtles", "#40bf40", new WeightedList<EntityType<?>>().add(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE.get(), 6.0), new WeightedList<Integer>().add(7, 10).add(9, 10));
        ZOO_EVENT = new SpawnEntityVaultEvent("Escaped Zoo", "Who's Alex anyway?", "#85e085", new WeightedList<EntityType<?>>().add(AMEntityRegistry.CROCODILE.get(), 6.0).add(AMEntityRegistry.GORILLA.get(), 6.0).add(AMEntityRegistry.TIGER.get(), 6.0).add(AMEntityRegistry.ELEPHANT.get(), 4.0).add(AMEntityRegistry.KOMODO_DRAGON.get(), 2.0).add(AMEntityRegistry.SNOW_LEOPARD.get(), 3.0), new WeightedList<Integer>().add(6, 10).add(8, 10));
        VOID_ZOO_EVENT = new SpawnEntityVaultEvent("Void Invasion", "Ender what now?", "#bf00ff",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.ENDERIOPHAGE.get(), 12.0).add(AMEntityRegistry.VOID_WORM.get(), 3.0).add(AMEntityRegistry.COSMIC_COD.get(), 4.0).add(AMEntityRegistry.MIMICUBE.get(), 5.0).add(ModEntities.T1_ENDERMAN, 7.0).add(ModEntities.T2_ENDERMAN, 9.0), new WeightedList<Integer>().add(6, 10).add(8, 10));
        DWELLER_EVENT = new SpawnEntityVaultEvent("Dweller Duel", "Attack of the dwellers", "#ff6666",  new WeightedList<EntityType<?>>().add(ModEntities.VAULT_FIGHTER_TYPES.get(1), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(2), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(3), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(4), 6.0), new WeightedList<Integer>().add(6, 10).add(10, 10));
        THERMAL_EXPANSION = new SpawnEntityVaultEvent("Thermal Expansion", "Thermally expand these mobs into your face", "#8ab97d",  new WeightedList<EntityType<?>>().add(TCoreEntities.BLITZ.get(), 6.0).add(TCoreEntities.BLIZZ.get(), 6.0).add(TCoreEntities.BASALZ.get(), 6.0), new WeightedList<Integer>().add(6, 10).add(10, 10).add(8, 10));
        LA_CUCARACHA_EVENT = new SpawnEntityVaultEvent("La Cucaracha", "Cha Cha Cha", "#ff6666",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.COCKROACH.get(), 6.0), new WeightedList<Integer>().add(2, 10).add(3, 10), AMItemRegistry.MARACA.get().getDefaultInstance());
        WOLD_SANTA_BOX_EVENT = new GiftItemVaultEvent("Wold's Box Giveaway", "And they said I wasn't nice", "#4d94ff", new WeightedList<ItemStack>().add(new ItemStack(ModItems.SUPPLY_BOX.asItem(), 1), 4.0).add(new ItemStack(ModItems.GEM_BOX.asItem(), 1), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.MOD_BOX.asItem(), 1), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.MYSTERY_BOX.asItem(), 1), 4.0).add(new ItemStack(ModItems.AUGMENT_BOX), 4.0));
        CONSOLATION_PRIZE = new GiftItemVaultEvent("Consolation Prize", "At least you tried...", "#7979d2", new WeightedList<ItemStack>().add(new ItemStack(iskallia.vault.init.ModItems.VELVET.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.ORNATE_INGOT.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.GILDED_INGOT.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.VAULT_MEAT.asItem(), 8), 4.0));
        PANDAMONIUM_EVENT = new InceptionVaultEvent("PANDAMONIUM", "Unleash the horde", "#4dffff",  SPAWN_ENTITY_ENCHANTED_EVENTS, true, 5);
        X_RANDOM_EVENT = new InceptionVaultEvent("5 Random Events", "Open pandora's box", "#66b3ff",  ENCHANTED_EVENTS, true, 5);
        X_OMEGA_RANDOM_EVENT = new InceptionVaultEvent("3 Random Omega Events", "3 random omega events, did you get lucky?", "#0088cc", OMEGA_ENCHANTED_EVENTS, true, 3);
        X_MODIFIER_RANDOM_EVENT = new InceptionVaultEvent("3 Random Modifier Events", "3 random modifier events", "#ff66b3",  MODIFIER_ENCHANTED_EVENTS, true, 3);
        HORDE_EVENT = new InceptionVaultEvent("Horde Night", "3 Random Mob spawn events", "#5c5cd6",  SPAWN_ENTITY_ENCHANTED_EVENTS, true, 3);
        VAMPIRE_SURVIVORS = new InceptionVaultEvent("Vampire Survivors", "Grants leeching and spawns bats", "#ffc34d",  new WeightedList<BasicVaultEvent>().add(LEECHING_MODIFIER_EVENT, 1.0).add(BAT_EVENT, 1.0), false, 0);
        TELESWAP_EVENT = new PlayerSwapVaultEvent("Teleswap", "Swap places with another player", "#cc6699");
        LA_CUCARACHA_RANDOM_EVENT = new LaCucarachaSpecialVaultEvent("Cockroach's Judgement", "Gives a random negative or postive event to each player.", "#eab676", 0.25F);
        MOB_VIGOR_EVENT = new BuffEntityInAreaVaultEvent("Mob Boost", "Gives speed and strength to nearby mobs", "#789D00", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 1 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 1), 1.0), true, 1, 32);
        MOB_DOWNGRADE_EVENT = new BuffEntityInAreaVaultEvent("Mob Downgrade", "Gives slowness and weakness to nearby mobs", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.WEAKNESS, 3600, 5 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3600, 5), 1.0), true, 1, 32);
        DISAPPEAR_MOBS_EVENT = new BuffEntityInAreaVaultEvent("Mob Invisibility", "Makes mobs in the area around you invisible", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.INVISIBILITY, 7200, 0 ), 1.0), true, 1, 32);
        MOB_RESISTANCE_EVENT = new BuffEntityInAreaVaultEvent("Mob Resistance", "Makes mobs in the area around you much tougher", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 5 ), 1.0), true, 1, 32);
        RANDOM_MOB_BUFFS = new BuffEntityInAreaVaultEvent("Random Mob Buffs", "Grants random potion effects to nearby mobs", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 3 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1 ), 1.0).add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2 ), 1.0).add(new MobEffectInstance(MobEffects.REGENERATION, 3600, 4 ), 1.0).add(new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600, 3 ), 1.0).add(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4 ), 1.0), true, 1, 32);
        MOB_MINIMIZER_EVENT = new BuffEntityInAreaVaultEvent("Mob Minimizer", "Makes nearby mobs eenie weenie", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(ModEffects.SHRINKING, 3600, 3 ), 1.0), true, 1, 32);
        MOB_MAXIMIZER_EVENT  = new BuffEntityInAreaVaultEvent("Mob Maximizer", "Makes nearby mobs large and in charge", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(ModEffects.GROWING, 3600, 0 ), 1.0), true, 1, 3);
        SHRINKING_EVENT = new PotionEffectVaultEvent("Shrink Ray", "Honey I shrunk the vaulters", "#ff8c1a",   ModEffects.SHRINKING, 600 , 0);
    }
}
