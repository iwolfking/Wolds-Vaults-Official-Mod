package xyz.iwolfking.woldsvaults.objectives.data;

import cofh.thermal.core.init.TCoreEntities;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import iskallia.vault.VaultMod;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.LoadingModList;
import samebutdifferent.ecologics.registry.ModMobEffects;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.*;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.CloudStorageEvents;
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.WildBackportEvents;
import xyz.iwolfking.woldsvaults.api.core.vault_events.LegacyVaultEvent;


public class EnchantedEventsRegistry {

    private static final WeightedList<VaultEvent> ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> OMEGA_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> POSITIVE_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> NEGATIVE_ENCHANTED_EVENTS = new WeightedList<>();

    private static final WeightedList<VaultEvent> SPAWN_ENTITY_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> MODIFIER_ENCHANTED_EVENTS = new WeightedList<>();


//    public static final VaultModifierVaultEvent COMMON_POSITIVE_MODIFER_EVENT;
//    public static final VaultModifierVaultEvent LEECHING_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent COMMON_NEGATIVE_MODIFER_EVENT;
//    public static final VaultModifierVaultEvent RARE_NEGATIVE_MODIFER_EVENT;
//    public static final VaultModifierVaultEvent RARE_POSITIVE_MODIFER_EVENT;
//    public static final VaultModifierVaultEvent OMEGA_NEGATIVE_MODIFER_EVENT;
//    public static final VaultModifierVaultEvent OMEGA_POSITIVE_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent OMEGA_GOD_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent MOB_ONHITS_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent CHAOS_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent CURSES_MODIFIER_EVENT;
//    public static final VaultModifierVaultEvent I_CAN_SEE_FOREVER_EVENT;
//    public static final VaultModifierVaultEvent HUNTERS_EVENT;
//    public static final VaultModifierVaultEvent BINGO_EVENT;
//    public static final VaultModifierVaultEvent CASCADING_CHESTS_MODFIER_EVENT;
//    public static final PotionEffectVaultEvent SLIPPERY_FLOORS_EVENT;
//    public static final PotionEffectVaultEvent SUNBIRD_CURSE_EVENT;
//    public static final PotionEffectVaultEvent SUNBIRD_BLESSING_EVENT;
//    public static final PotionEffectVaultEvent CLINGING_EVENT;
//    public static final PotionEffectVaultEvent INSTAKILL_EVENT;
//    public static final PotionEffectVaultEvent LEVITATION_EVENT;
//    public static final PotionEffectVaultEvent SHRINKING_EVENT;
//
//    public static final MultiPotionEffectVaultEvent BOLSTERED_EVENT;
//    public static final MultiPotionEffectVaultEvent CHEMICAL_BATH_EVENT;
//    public static final MultiPotionEffectVaultEvent HOLY_BLESSING_EVENT;
//
//    public static final MultiPotionEffectVaultEvent HYPERSPEED_EVENT;
//    public static final SpawnEntityVaultEvent COW_EVENT;
//    public static final SpawnEntityVaultEvent BAT_EVENT;
//    public static final SpawnEntityVaultEvent CREEPER_EVENT;
//    public static final SpawnEntityVaultEvent ZOMBOID_EVENT;
//    public static final SpawnEntityVaultEvent BUNFUNGUS_EVENT;
//    public static final SpawnEntityVaultEvent ARACHNOPHOBIA_EVENT;
//    public static final SpawnEntityVaultEvent GHOSTY_EVENT;
//    public static final SpawnEntityVaultEvent TNT_EVENT;
//    public static final SpawnEntityVaultEvent TURTLES_EVENT;
//    public static final SpawnEntityVaultEvent ZOO_EVENT;
//    public static final SpawnEntityVaultEvent VOID_ZOO_EVENT;
//    public static final SpawnEntityVaultEvent DWELLER_EVENT;
//    public static final SpawnEntityVaultEvent THERMAL_EXPANSION;
//    public static final SpawnEntityVaultEvent LA_CUCARACHA_EVENT;
//    public static final GiftItemVaultEvent WOLD_SANTA_BOX_EVENT;
//    public static final GiftItemVaultEvent CONSOLATION_PRIZE;
//    public static final InceptionVaultEvent PANDAMONIUM_EVENT;
//    public static final InceptionVaultEvent X_RANDOM_EVENT;
//    public static final InceptionVaultEvent X_OMEGA_RANDOM_EVENT;
//    public static final InceptionVaultEvent X_MODIFIER_RANDOM_EVENT;
//    public static final InceptionVaultEvent HORDE_EVENT;
//    public static final InceptionVaultEvent VAMPIRE_SURVIVORS;
//    public static final PlayerSwapVaultEvent TELESWAP_EVENT;
//    public static final LaCucarachaSpecialVaultEvent LA_CUCARACHA_RANDOM_EVENT;
//
//    public static final BuffEntityInAreaVaultEvent MOB_VIGOR_EVENT;
//    public static final BuffEntityInAreaVaultEvent MOB_DOWNGRADE_EVENT;
//    public static final BuffEntityInAreaVaultEvent DISAPPEAR_MOBS_EVENT;
//    public static final BuffEntityInAreaVaultEvent MOB_RESISTANCE_EVENT;
//    public static final BuffEntityInAreaVaultEvent RANDOM_MOB_BUFFS;
//    public static final BuffEntityInAreaVaultEvent MOB_MINIMIZER_EVENT;
//    public static final BuffEntityInAreaVaultEvent MOB_MAXIMIZER_EVENT;


    public static void addEvents() {
        //Vault Modifier events
//        register(COMMON_POSITIVE_MODIFER_EVENT, 60.0, false, true);
//        register(COMMON_NEGATIVE_MODIFER_EVENT, 60.0, false, false);
//        register(RARE_POSITIVE_MODIFER_EVENT, 35.0, false, true);
//        register(RARE_NEGATIVE_MODIFER_EVENT, 35.0, false, false);
//        register(OMEGA_POSITIVE_MODIFIER_EVENT, 6.0, true, true);
//        register(OMEGA_NEGATIVE_MODIFER_EVENT, 6.0, true, false);
//        register(OMEGA_GOD_MODIFIER_EVENT, 3.0, true, true);
//        register(CHAOS_MODIFIER_EVENT, 6.0, true, true);
//        register(MOB_ONHITS_MODIFIER_EVENT, 6.0, false, false);
//        register(CURSES_MODIFIER_EVENT, 1.0, true, false);
//        register(I_CAN_SEE_FOREVER_EVENT, 1.0, true, true);
//        register(HUNTERS_EVENT, 12.0, true, true);
//        register(BINGO_EVENT, 3.0, true, true);
//        register(CASCADING_CHESTS_MODFIER_EVENT, 10.0, true, true);
//
//        //Potion Effect events
//        register(SLIPPERY_FLOORS_EVENT, 22.0, false, false);
//        register(SUNBIRD_CURSE_EVENT, 12.0, false, false);
//        register(SUNBIRD_BLESSING_EVENT, 12.0, false, true);
//        register(CLINGING_EVENT, 12.0, false, false);
//        register(INSTAKILL_EVENT, 16.0, false, true);
//        register(LEVITATION_EVENT, 12.0, false, false);
//        register(SHRINKING_EVENT, 6.0, false, false);
//
//        //Multi-Potion Effect events
//        register(BOLSTERED_EVENT, 16.0, false, true);
//        register(CHEMICAL_BATH_EVENT, 7.0, false, false);
//        register(HOLY_BLESSING_EVENT, 14.0, false, true);
//        register(HYPERSPEED_EVENT, 16.0, false, true);
//
//        //Mob Spawn events
//        register(COW_EVENT, 12.0, false, false);
//        register(CREEPER_EVENT, 16.0, false, false);
//        register(ZOMBOID_EVENT, 16.0, false, false);
//        register(BUNFUNGUS_EVENT, 12.0, false, false);
//        register(ARACHNOPHOBIA_EVENT, 16.0, false, false);
//        register(GHOSTY_EVENT, 16.0, false, false);
//        register(ZOO_EVENT, 5.0, false, false);
//        register(DWELLER_EVENT, 16.0, false, false);
//        register(VOID_ZOO_EVENT, 6.0, false, false);
//        register(TURTLES_EVENT, 16.0, false, false);
//        register(THERMAL_EXPANSION, 8.0, false, false);
//        register(LA_CUCARACHA_EVENT, 4.0, true, false);
//
//        //Mob Buff Events
//        register(MOB_VIGOR_EVENT, 16.0, false, false);
//        register(MOB_DOWNGRADE_EVENT, 16.0, false, true);
//        register(DISAPPEAR_MOBS_EVENT, 16.0, false, false);
//        register(MOB_RESISTANCE_EVENT, 16.0, false, false);
//        register(RANDOM_MOB_BUFFS, 16.0, false, false);
//        register(MOB_MINIMIZER_EVENT, 6.0, false, false);
//        register(MOB_MAXIMIZER_EVENT, 8.0, false, false);
//
//        //Item Gift events
//        register(WOLD_SANTA_BOX_EVENT, 16.0, true, true);
//        register(CONSOLATION_PRIZE, 16.0, false, true);
//
//        //Inception Events
//        register(PANDAMONIUM_EVENT, 6.0, false, false);
//        register(HORDE_EVENT, 8.0, true, false);
//        register(X_RANDOM_EVENT, 8.0, true, true);
//        register(X_MODIFIER_RANDOM_EVENT, 8.0, true, true);
//        register(X_OMEGA_RANDOM_EVENT, 1.0, true, true);
//        register(VAMPIRE_SURVIVORS, 3.0, false, true);
//
//        //Unique Events
//        register(TELESWAP_EVENT, 12.0, false, false);
//
//        //Events that rely on mods
//        if(LoadingModList.get().getModFileById("wildbackport") != null) {
//            WildBackportEvents.init();
//        }
//
//        if(LoadingModList.get().getModFileById("cloudstorage") != null) {
//            CloudStorageEvents.init();
//        }
//
//        ENCHANTED_EVENTS.forEach((basicEnchantedEvent, aDouble) -> {
//            if(basicEnchantedEvent instanceof SpawnEntityVaultEvent) {
//                SPAWN_ENTITY_ENCHANTED_EVENTS.add(basicEnchantedEvent, aDouble);
//            }
//        });
//
//        ENCHANTED_EVENTS.forEach((basicEnchantedEvent, aDouble) -> {
//            if(basicEnchantedEvent instanceof VaultModifierVaultEvent) {
//                MODIFIER_ENCHANTED_EVENTS.add(basicEnchantedEvent, aDouble);
//            }
//        });
    }

//    public static void register(LegacyVaultEvent event, Double weight, boolean isOmega, boolean isPositive) {
//        ENCHANTED_EVENTS.add(event, weight);
//        if(isOmega) {
//            OMEGA_ENCHANTED_EVENTS.add(event, 1.0);
//        }
//
//        if(isPositive) {
//            POSITIVE_ENCHANTED_EVENTS.add(event, weight);
//        }
//        else {
//            NEGATIVE_ENCHANTED_EVENTS.add(event, weight);
//        }
//    }

    public static void register(ResourceLocation id, VaultEvent event, Double weight) {
        VaultEventSystem.register(id, event);
        ENCHANTED_EVENTS.add(event, weight);
        if(event.getEventTags().contains(EventTag.OMEGA)) {
            OMEGA_ENCHANTED_EVENTS.add(event, weight);
        }

        if(event.getEventTags().contains(EventTag.POSITIVE)) {
            POSITIVE_ENCHANTED_EVENTS.add(event, weight);
        }

        if(event.getEventTags().contains(EventTag.NEGATIVE)) {
            NEGATIVE_ENCHANTED_EVENTS.add(event, weight);
        }

        if(event.getEventTags().contains(EventTag.SPAWN_MOB)) {
            SPAWN_ENTITY_ENCHANTED_EVENTS.add(event, weight);
        }

        if(event.getEventTags().contains(EventTag.ADDS_MODIFIER)) {
            MODIFIER_ENCHANTED_EVENTS.add(event, weight);
        }
    }

    public static WeightedList<VaultEvent> getEvents() {
        return ENCHANTED_EVENTS;
    }

    public static WeightedList<VaultEvent> getOmegaEvents() {
        return OMEGA_ENCHANTED_EVENTS;
    }
    public static WeightedList<VaultEvent> getPositiveEvents() {
        return POSITIVE_ENCHANTED_EVENTS;
    }

    public static WeightedList<VaultEvent> getNegativeEvents() {
        return NEGATIVE_ENCHANTED_EVENTS;
    }

    public static void registerAllBuiltInEvents() {
        register(WoldsVaults.id("common_positive_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#cccc00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("basic_positive")))
                .build("Common Positive Modifier", new TextComponent("Adds a modifier from a common pool of positive modifiers.")), 60.0);

        register(WoldsVaults.id("rare_positive_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#8cff1a"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("medium_positive")))
                .build("Rare Positive Modifier", new TextComponent("Adds a modifier from a rare pool of positive modifiers.")), 35.0);

        register(WoldsVaults.id("omega_positive_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#77ff33"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("omega_positive")))
                .build("Omegas Positive Modifier", new TextComponent("Adds a modifier from an omega pool of positive modifiers.")), 6.0);

        register(WoldsVaults.id("gods_blessing_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#ff80d5"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("gods_omega_blessing")))
                .build("God's Blessing", new TextComponent("The God's bless your with one of their omega favour modifiers.")), 3.0);

        register(WoldsVaults.id("common_negative_modifier"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#ff1a1a"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("basic_negative")))
                .build("Common Negative Modifier", new TextComponent("Adds a modifier from a common pool of negative modifiers.")), 60.0);

        register(WoldsVaults.id("rare_negative_modifier"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#660000"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("medium_negative")))
                .build("Rare Negative Modifier", new TextComponent("Adds a modifier from a rare pool of negative modifiers.")), 35.0);

        register(WoldsVaults.id("omega_negative_modifier"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#330000"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("omega_negative")))
                .build("Rare Positive Modifier", new TextComponent("Adds a modifier from an omega pool of negative modifiers.")), 6.0);

        register(WoldsVaults.id("mob_onhits_modifier"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#00a3cc"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("mob_onhits")))
                .build("Mob On-Hits Modifier", new TextComponent("Adds a nasty modifier that adds on-hit effects to mobs.")), 6.0);

        register(WoldsVaults.id("chaos_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#4d4dff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("chaos_enchanted")))
                .build("Chaos Modifier", new TextComponent("Adds a random modifier from the Chaos modifier pool.")), 12.0);

        register(WoldsVaults.id("curses_modifier"), new VaultEvent.Builder()
                .tag(EventTag.OMEGA)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#3d0099"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("curses")))
                .build("Curses!", new TextComponent("Adds a random modifier from the Curses modifier pool.")), 1.0);

        register(WoldsVaults.id("cascading_chests_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#d5ff80"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("enchanted_cascade")))
                .build("Cascading Chests", new TextComponent("Adds a random modifier from the Cascading Chests modifier pool.")), 10.0);

        register(WoldsVaults.id("i_can_see_forever"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#8585ad"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("hunters_enchanted")))
                .build("I Can See Forever", new TextComponent("Adds all Hunters modifiers.")), 1.0);

        register(WoldsVaults.id("random_hunter_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#8585ad"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("hunters_enchanted_random")))
                .build("Hunter Modifier", new TextComponent("Adds a random Hunter modifier.")), 10.0);

        register(WoldsVaults.id("bingo_modifier"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#4d4dff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierFromPoolTask(VaultMod.id("bingos_enchanted")))
                .build("Bingo!?", new TextComponent("Adds a random Bingo modifier.")), 3.0);

        register(WoldsVaults.id("slippery_floors"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#adebeb"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(ModMobEffects.SLIPPERY.get(), 10, 1800)
                        .build()
                )
                .build("Slippery Floors", new TextComponent("Who forgot to dry the floor!? Makes movement slippery.")), 16.0);

        register(WoldsVaults.id("curse_of_the_sunbird"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#804000"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(AMEffectRegistry.SUNBIRD_CURSE, 0, 1800)
                        .build()
                )
                .build("Curse of the Sunbird", new TextComponent("It seems gravity is higher...")), 9.0);

        register(WoldsVaults.id("blessing_of_the_sunbird"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#ff9900"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(AMEffectRegistry.SUNBIRD_BLESSING, 0, 1800)
                        .build()
                )
                .build("Blessing of the Sunbird", new TextComponent("Glide to safety on feathered wing.")), 10.0);

        register(WoldsVaults.id("clinging_effect"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#bfff00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(AMEffectRegistry.CLINGING, 0, 1200)
                        .build()
                )
                .build("Topsy Turvy", new TextComponent("Walk on ceilings o.o")), 3.0);

        register(WoldsVaults.id("levitation"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#b300b3"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.LEVITATION, 0, 400)
                        .build()
                )
                .build("Leviosa", new TextComponent("Up up and away")), 10.0);

        register(WoldsVaults.id("insta_kill"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#ff8c1a"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_BOOST, 255, 600)
                        .effect(ModEffects.EMPOWER, 100, 600)
                        .build()
                )
                .build("Insta-Kill", new TextComponent("Grants Max Strength + Empower")), 14.0);

        register(WoldsVaults.id("hyper_speed"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#e6e600"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(ModEffects.QUICKENING, 9, 900)
                        .effect(MobEffects.DIG_SPEED, 9, 900)
                        .build()
                )
                .build("Hyperspeed", new TextComponent("Grants big speed boost!")), 14.0);

        register(WoldsVaults.id("bolstered"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#804000"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_RESISTANCE, 2, 900)
                        .effect(MobEffects.FIRE_RESISTANCE, 4, 900)
                        .effect(AMEffectRegistry.POISON_RESISTANCE, 9, 900)
                        .build()
                )
                .build("Bolstered", new TextComponent("Grants Damage resistance, fire resistance, and poison resistance.")), 16.0);

        register(WoldsVaults.id("chemical_bath"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#333300"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.WITHER, 1, 300)
                        .effect(MobEffects.POISON, 1, 300)
                        .effect(iskallia.vault.init.ModEffects.BLEED, 1, 300)
                        .effect(iskallia.vault.init.ModEffects.VULNERABLE, 0, 900)
                        .effect(MobEffects.BLINDNESS, 0, 600)
                        .effect(MobEffects.HUNGER, 2, 400)
                        .effect(MobEffects.GLOWING, 0, 300)
                        .effect(MobEffects.WEAKNESS, 2, 600)
                        .effect(MobEffects.CONFUSION, 0, 360)
                        .effect(MobEffects.REGENERATION, 1, 360)
                        .effect(MobEffects.ABSORPTION, 1, 300)
                        .effect(AMEffectRegistry.BUG_PHEROMONES, 1, 600)
                        .build()
                )
                .build("Chemical Bath", new TextComponent("A nasty brew of potion effects.")), 5.0);

        register(WoldsVaults.id("holy_blessing"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#ffd966"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_RESISTANCE, 3, 400)
                        .effect(MobEffects.FIRE_RESISTANCE, 4, 400)
                        .effect(MobEffects.WATER_BREATHING, 4, 400)
                        .effect(ModEffects.QUICKENING, 5, 400)
                        .effect(ModEffects.EMPOWER, 5, 400)
                        .effect(MobEffects.DIG_SPEED, 4, 400)
                        .effect(MobEffects.SATURATION, 3, 400)
                        .effect(MobEffects.GLOWING, 0, 400)
                        .effect(MobEffects.REGENERATION, 2, 400)
                        .effect(MobEffects.ABSORPTION, 2, 400)
                        .effect(MobEffects.NIGHT_VISION, 0, 400)
                        .build()
                )
                .build("Holy Concoction", new TextComponent("A beautiful brew of potion effects.")), 8.0);

        register(WoldsVaults.id("barnyard_bash"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#666633"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.AGGRESSIVE_COW, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HOSTILE_CHICKEN, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HOSTILE_SHEEP, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HOSTILE_PIG, 3.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 1, 300), 1)
                        .build()
                )
                .build("Barnyard Bash", new TextComponent("Attack of the farm animals!")), 9.0);

        register(WoldsVaults.id("creeper_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#1aff66"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.T1_CREEPER, 9.0)
                        .entity(ModEntities.T2_CREEPER, 6.0)
                        .entity(ModEntities.T3_CREEPER, 3.0)
                        .amount(5,  15)
                        .amount(7, 10)
                        .amount(9, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 1, 300), 1)
                        .build()
                )
                .build("Jeepers Creepers!", new TextComponent("Attack of the creepers!")), 12.0);

        register(WoldsVaults.id("zombie_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#006600"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.T1_ZOMBIE, 9.0)
                        .entity(ModEntities.T2_ZOMBIE, 6.0)
                        .entity(ModEntities.T3_ZOMBIE, 3.0)
                        .amount(5,  15)
                        .amount(7, 10)
                        .amount(9, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Project Zomboid", new TextComponent("Attack of the zombies!")), 12.0);

        register(WoldsVaults.id("bunfungus_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#ffb3b3"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(AMEntityRegistry.BUNFUNGUS.get(), 9.0)
                        .entity(AMEntityRegistry.MUNGUS.get(), 3.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Bunfungus Amongus", new TextComponent("I am Joseph and I like bunnies")), 12.0);

        register(WoldsVaults.id("spider_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#0d0033"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.VAULT_SPIDER, 9.0)
                        .entity(ModEntities.VAULT_SPIDER_BABY, 3.0)
                        .entity(ModEntities.DUNGEON_SPIDER, 5.0)
                        .entity(ModEntities.DUNGEON_BLACK_WIDOW_SPIDER, 1.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Arachno-no-no", new TextComponent("They are crawling everywhere! NO NO NO!!!")), 12.0);

        register(WoldsVaults.id("ghost_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#ff471a"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.VAULT_WRAITH_WHITE, 8.0)
                        .entity(ModEntities.VAULT_WRAITH_YELLOW, 2.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.BLUE_GHOST, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.RED_GHOST, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.GREEN_GHOST, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.BROWN_GHOST, 1.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.BLACK_GHOST, 1.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.PURPLE_GHOST, 1.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Happy Halloween", new TextComponent("Poltergeists appear to pummel you.")), 10.0);

        register(WoldsVaults.id("turtle_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#40bf40"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE.get(), 1.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Ninja Turtles", new TextComponent("Turtles come to trounce you.")), 10.0);

        register(WoldsVaults.id("zoo_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#85e085"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE.get(), 1.0)
                        .entity(AMEntityRegistry.CROCODILE.get(), 3.0)
                        .entity(AMEntityRegistry.TIGER.get(), 6.0)
                        .entity(AMEntityRegistry.KOMODO_DRAGON.get(), 4.0)
                        .entity(AMEntityRegistry.ELEPHANT.get(), 1.0)
                        .entity(AMEntityRegistry.SNOW_LEOPARD.get(), 4.0)
                        .spawnRanges(13, 23)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Escaped Zoo", new TextComponent("Who's Alex anyway?")), 5.0);

        register(WoldsVaults.id("void_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#bf00ff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(AMEntityRegistry.ENDERIOPHAGE.get(), 9.0)
                        .entity(AMEntityRegistry.COSMIC_COD.get(), 1.0)
                        .entity(AMEntityRegistry.MIMICUBE.get(), 3.0)
                        .entity(ModEntities.T2_ENDERMAN, 6.0)
                        .spawnRanges(13, 23)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Void Invasion", new TextComponent("No really, WHO IS ALEX!?")), 3.0);

        register(WoldsVaults.id("dweller_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#ff6666"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.VAULT_FIGHTER_TYPES.get(1), 15.0)
                        .entity(ModEntities.VAULT_FIGHTER_TYPES.get(2), 9.0)
                        .entity(ModEntities.VAULT_FIGHTER_TYPES.get(3), 6.0)
                        .entity(ModEntities.VAULT_FIGHTER_TYPES.get(4), 3.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Dweller Duel", new TextComponent("Attack of the Vault Dwellers")), 12.0);

        register(WoldsVaults.id("thermal_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#8ab97d"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(TCoreEntities.BASALZ.get(), 1.0)
                        .entity(TCoreEntities.BLIZZ.get(), 1.0)
                        .entity(TCoreEntities.BLITZ.get(), 1.0)
                        .entity(EntityType.BLAZE, 1.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Thermal Expansion", new TextComponent("Thermally expand these mobs into your face.")), 10.0);

        register(WoldsVaults.id("box_giveaway"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#4d94ff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ItemRewardTask.Builder()
                        .item(ModItems.SUPPLY_BOX, 4, 5.0)
                        .item(ModItems.AUGMENT_BOX, 1, 5.0)
                        .item(ModItems.CATALYST_BOX, 1, 3.0)
                        .item(ModItems.GEM_BOX, 4, 5.0)
                        .item(ModItems.OMEGA_BOX, 1, 1.0)
                        .item(iskallia.vault.init.ModItems.MOD_BOX, 1, 5.0)
                        .item(iskallia.vault.init.ModItems.MYSTERY_BOX, 8, 5.0)
                        .build()
                )
                .build("Wolf's Box Giveaway", new TextComponent("Never tell me I don't do anything for you.")), 5.0);

        register(WoldsVaults.id("sweet_eats"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#7979d2"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ItemRewardTask.Builder()
                        .item(ModItems.VAULT_ROCK_CANDY, 8, 5.0)
                        .build()
                )
                .build("Wolf's Sweet Surprise", new TextComponent("Enjoy your delicious rock-hard candy!")), 8.0);

        register(WoldsVaults.id("pandamonium"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#4dffff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ExecuteEventsTask(() -> SPAWN_ENTITY_ENCHANTED_EVENTS, 5))
                .build("PANDAMONIUM!", new TextComponent("Unleash the Horde!")), 1.0);

        register(WoldsVaults.id("horde_night"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#5c5cd6"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ExecuteEventsTask(() -> SPAWN_ENTITY_ENCHANTED_EVENTS, 3))
                .build("Horde Night", new TextComponent("Unleash the Horde!")), 5.0);


        register(WoldsVaults.id("five_random_events"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#66b3ff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ExecuteEventsTask(() -> ENCHANTED_EVENTS, 5))
                .build("5 Random Events", new TextComponent("Let's see what you get...")), 8.0);

        register(WoldsVaults.id("three_random_omega"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#66b3ff"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ExecuteEventsTask(() -> OMEGA_ENCHANTED_EVENTS, 3))
                .build("3 Random Omega Events", new TextComponent("3 random Omega events... did you get lucky?")), 1.0);

        register(WoldsVaults.id("three_random_modifier"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#ff66b3"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new ExecuteEventsTask(() -> MODIFIER_ENCHANTED_EVENTS, 3))
                .build("3 Random Modifier Events", new TextComponent("3 random Modifier events... did you get lucky?")), 8.0);

        register(WoldsVaults.id("vampire_survivors"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#ffc34d"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierTask(VaultMod.id("leeching"), 1, 900))
                        .task(new SpawnMobTask.Builder()
                                .entity(EntityType.BAT, 1.0)
                                .spawnRanges(13.0, 25.0)
                                .amount(8, 1.0)
                                .build()
                        )
                .build("Vampire Survivors", new TextComponent("Grants leeching for a period and spawns bats!")), 2.0);

        register(WoldsVaults.id("teleswap"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#cc6699"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerSwapTask())
                        .task(new PlayerMobEffectTask.Builder()
                                .effect(MobEffects.CONFUSION, 0, 120)
                                .build())
                .build("Teleswap", new TextComponent("Swaps you and another player's location!")), 10.0);

        register(WoldsVaults.id("mob_vigor"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_BOOST, 3, 3600)
                        .effect(MobEffects.MOVEMENT_SPEED, 3, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Invigoration", new TextComponent("Gives nearby mobs a damage and speed boost!")), 12.0);

        register(WoldsVaults.id("mob_downgrade"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(MobEffects.WEAKNESS, 5, 1200)
                        .effect(MobEffects.MOVEMENT_SLOWDOWN, 5, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Downgrade", new TextComponent("Gives nearby mobs a damage and speed downgrade!")), 12.0);

        register(WoldsVaults.id("mob_invisibility"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(MobEffects.INVISIBILITY, 0, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Invisibility", new TextComponent("Makes nearby mobs disappear!")), 12.0);

        register(WoldsVaults.id("mob_resistance"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_RESISTANCE, 3, 3600)
                        .effect(AMEffectRegistry.POISON_RESISTANCE, 3, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Resistance", new TextComponent("Nearby mobs get a heap of damage resistance!")), 12.0);

        register(WoldsVaults.id("random_mob_buffs"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(MobEffects.DAMAGE_RESISTANCE, 1, 3600)
                        .effect(AMEffectRegistry.POISON_RESISTANCE, 1, 3600)
                        .effect(AMEffectRegistry.SOULSTEAL, 1, 3600)
                        .effect(MobEffects.MOVEMENT_SPEED, 4, 3600)
                        .effect(MobEffects.DAMAGE_BOOST, 4, 3600)
                        .effect(MobEffects.REGENERATION, 4, 3600)
                        .amount(3)
                        .grantAll()
                        .build()
                )
                .build("Mob Resistance", new TextComponent("Nearby mobs get random buffs!")), 12.0);

        register(WoldsVaults.id("minimize_mobs"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(ModEffects.SHRINKING, 3, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Minimizer", new TextComponent("Nearby mobs get teeny weeny!")), 8.0);

        register(WoldsVaults.id("maximize_mobs"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#789D00"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new MobMobEffectTask.Builder()
                        .effect(ModEffects.GROWING, 1, 3600)
                        .grantAll()
                        .build()
                )
                .build("Mob Maximizer", new TextComponent("Nearby mobs get a growth spurt!")), 8.0);

        register(WoldsVaults.id("shrink_ray"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#ff8c1a"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(ModEffects.SHRINKING, 1, 600)
                        .build()
                )
                .build("Shrink Ray", new TextComponent("Honey I shrunk the vaulters!")), 3.0);

        register(WoldsVaults.id("la_cucharacha"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .color(TextColor.parseColor("#ff6666"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(AMEntityRegistry.COCKROACH.get(), 1.0)
                        .amount(4, 1.0)
                        .heldStack(new ItemStack(AMItemRegistry.MARACA.get()))
                        .build()
                )
                .task(new PlaySoundTask(AMSoundRegistry.LA_CUCARACHA))
                .task(new DelayTask(100))
                .task(new WeightedTask.Builder()
                        .task(new TaskGroup.Builder()
                                .task(new ExecuteEventsTask(() -> POSITIVE_ENCHANTED_EVENTS, 3))
                                .task(new MessageTask(VaultEvent.EventDisplayType.CHAT_MESSAGE_TARGET, (TextComponent) new TextComponent("The dancing cockroaches are fond towards you and bless each player with 3 random positive events!").withStyle(ChatFormatting.GOLD)))
                                .build(), 1)
                        .task(new TaskGroup.Builder()
                                .task(new ExecuteEventsTask(() -> NEGATIVE_ENCHANTED_EVENTS, 3))
                                .task(new MessageTask(VaultEvent.EventDisplayType.CHAT_MESSAGE_TARGET, (TextComponent) new TextComponent("The dancing cockroaches hate your guts and curse everyone in the vault with 3 random negative events!").withStyle(ChatFormatting.RED)))
                                .build(), 1)
                        .build()
                )
                .build("La Cucaracha", new TextComponent("Cha Cha Cha")), 3.0);

        if(LoadingModList.get().getModFileById("cloudstorage") != null) {
            CloudStorageEvents.init();
        }

        if(LoadingModList.get().getModFileById("wildbackport") != null) {
            WildBackportEvents.init();
        }


    }

    static {

        //COMMON_POSITIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Common Positive", "Adds a modifier from a common pool of positive modifiers.", "#cccc00",  "basic_positive");
        //LEECHING_MODIFIER_EVENT = new VaultModifierVaultEvent("Leeching", "Adds leeching vault modifier.", "#800000",  "leeching");
        //COMMON_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Common Negative", "Adds a modifier from a pool of common negatives modifiers.", "#ff1a1a",  "basic_negative");
        //RARE_POSITIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Rare Positive", "Adds a modifier from a pool of rare positive modifiers", "#8cff1a",  "medium_positive");
        //RARE_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Rare Negative", "Adds a modifier from a pool of rare negative modifiers", "#660000", "medium_negative");
        //OMEGA_NEGATIVE_MODIFER_EVENT = new VaultModifierVaultEvent("Omega Negative", "Adds a modifier from a pool of omega negative modifiers", "#330000", "omega_negative");
        //OMEGA_POSITIVE_MODIFIER_EVENT = new VaultModifierVaultEvent("Omega Positive", "Adds one very good random modifier.", "#77ff33", "omega_positive");
        //OMEGA_GOD_MODIFIER_EVENT = new VaultModifierVaultEvent("God's Blessing", "Adds one favour modifier from the gods.", "#ff80d5", "gods_omega_blessing");
        //MOB_ONHITS_MODIFIER_EVENT = new VaultModifierVaultEvent("Mob On-hits", "Adds one modifier that adds an on-hit effect to mobs.",  "#00a3cc", "mob_onhits");
        //CHAOS_MODIFIER_EVENT = new VaultModifierVaultEvent("Chaos", "Adds a modifier from the chaos pool.", "#4d4dff", "chaos_enchanted");
        //CURSES_MODIFIER_EVENT = new VaultModifierVaultEvent("Curses", "Adds one modifier from a pool of curses.", "#3d0099",  "curses");
        //CASCADING_CHESTS_MODFIER_EVENT = new VaultModifierVaultEvent("Cascading", "Adds one modifier from a pool of cascading modifiers.", "#d5ff80",  "enchanted_cascade");
        //I_CAN_SEE_FOREVER_EVENT = new VaultModifierVaultEvent("I Can See Forever", "Adds All Hunter modifiers.", "#8585ad",  "hunters_enchanted");
        //HUNTERS_EVENT = new VaultModifierVaultEvent("Hunter", "Adds a modifier from the pool of Hunter modifiers.", "#8585ad",  "hunters_enchanted_random");
        //BINGO_EVENT = new VaultModifierVaultEvent("Bingo!?", "Adds a modifier from the pool of Bingo modifiers.", "#4d4dff",  "bingos_enchanted");
        //SLIPPERY_FLOORS_EVENT = new PotionEffectVaultEvent("Slippery Floors", "Who forgot to dry the floor!?", "#adebeb",   ModMobEffects.SLIPPERY.get(), 1800, 10);
        //SUNBIRD_CURSE_EVENT = new PotionEffectVaultEvent("Curse of the Sunbird", "It seems gravity is higher...", "#804000", AMEffectRegistry.SUNBIRD_CURSE, 1200, 1);
        //SUNBIRD_BLESSING_EVENT = new PotionEffectVaultEvent("Blessing of the Sunbird", "Glide to safety", "#ff9900", AMEffectRegistry.SUNBIRD_BLESSING, 1800, 0);
        //CLINGING_EVENT = new PotionEffectVaultEvent("Topsy Turvy", "Walk on ceilings o.o", "#bfff00",   AMEffectRegistry.CLINGING, 1200, 0);
        //LEVITATION_EVENT = new PotionEffectVaultEvent("Leviosa", "Up up and away", "#b300b3", MobEffects.LEVITATION, 300, 0);
        //INSTAKILL_EVENT = new PotionEffectVaultEvent("Insta-Kill", "Grants strength x 255", "#ff8c1a",   MobEffects.DAMAGE_BOOST, 600 , 255);
        //HYPERSPEED_EVENT = new MultiPotionEffectVaultEvent("Hyper-Speed", "Grants speed x 10", "#e6e600",   List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED), 900, 9);
        //BOLSTERED_EVENT = new MultiPotionEffectVaultEvent("Barricade", "I feel a lot tougher!", "#804000",  List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE), 900, 2);
        //CHEMICAL_BATH_EVENT = new MultiPotionEffectVaultEvent("Chemical Bath", "A nasty brew of potion effects!", "#333300", List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.HUNGER, MobEffects.REGENERATION, MobEffects.WEAKNESS, MobEffects.GLOWING, AMEffectRegistry.BUG_PHEROMONES, MobEffects.BLINDNESS, MobEffects.ABSORPTION, MobEffects.CONFUSION), 300, 1);
        //HOLY_BLESSING_EVENT = new MultiPotionEffectVaultEvent("Holy Blessing", "A myriad of positive potion effects!", "#ffd966", List.of(MobEffects.GLOWING, MobEffects.REGENERATION, MobEffects.SATURATION, MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST, MobEffects.DIG_SPEED, MobEffects.MOVEMENT_SPEED, MobEffects.NIGHT_VISION, MobEffects.FIRE_RESISTANCE), 600, 4);
        //COW_EVENT = new SpawnEntityVaultEvent("Barnyard Bash", "Attack of the cows", "#666633", new WeightedList<EntityType<?>>().add(ModEntities.AGGRESSIVE_COW, 6.0), new WeightedList<Integer>().add(4, 15).add(6, 12).add(8, 10));
        //BAT_EVENT = new SpawnEntityVaultEvent("Bat Swarm", "Bats everywhere!?", "#c2c2a3",  new WeightedList<EntityType<?>>().add(EntityType.BAT, 6.0), new WeightedList<Integer>().add(3, 10).add(6, 10));
        //CREEPER_EVENT = new SpawnEntityVaultEvent("Jeepers Creepers", "Attack of the creepers", "#1aff66", new WeightedList<EntityType<?>>().add(ModEntities.T1_CREEPER, 6.0).add(ModEntities.T2_CREEPER, 4.0).add(ModEntities.T3_CREEPER, 2.0), new WeightedList<Integer>().add(5, 10).add(7, 10).add(8, 8));
        //ZOMBOID_EVENT = new SpawnEntityVaultEvent("Project Zomboid", "Attack of the zombies", "#006600",  new WeightedList<EntityType<?>>().add(ModEntities.T1_ZOMBIE, 6.0).add(ModEntities.T2_ZOMBIE, 4.0).add(ModEntities.T3_ZOMBIE, 2.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(3, 6).add(9, 5));
        //BUNFUNGUS_EVENT = new SpawnEntityVaultEvent("Bunfungus Amongus", "I am Joseph and I like bunnies", "#ffb3b3",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.BUNFUNGUS.get(), 6.0).add(AMEntityRegistry.MUNGUS.get(), 1.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(3, 6).add(9, 5));
        //ARACHNOPHOBIA_EVENT = new SpawnEntityVaultEvent("Arachnophobia", "Attack of the spidders", "#0d0033",  new WeightedList<EntityType<?>>().add(ModEntities.VAULT_SPIDER, 6.0).add(ModEntities.VAULT_SPIDER_BABY, 4.0).add(ModEntities.DUNGEON_SPIDER, 2.0), new WeightedList<Integer>().add(6, 10).add(7, 10).add(8, 10));
        //GHOSTY_EVENT = new SpawnEntityVaultEvent("Happy Halloween", "All ghost costumes this year?", "#ff471a",  new WeightedList<EntityType<?>>().add(WraithModule.wraithType, 6.0), new WeightedList<Integer>().add(8, 10).add(12, 10));
        //TNT_EVENT = new SpawnEntityVaultEvent("Whoops", "Who left all this tnt around?", "#000000", new WeightedList<EntityType<?>>().add(EntityType.TNT, 6.0), new WeightedList<Integer>().add(8, 10).add(12, 10));
        //TURTLES_EVENT = new SpawnEntityVaultEvent("Ninja Turtles", "Attack of the turtles", "#40bf40", new WeightedList<EntityType<?>>().add(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE.get(), 6.0), new WeightedList<Integer>().add(7, 10).add(9, 10));
        //ZOO_EVENT = new SpawnEntityVaultEvent("Escaped Zoo", "Who's Alex anyway?", "#85e085", new WeightedList<EntityType<?>>().add(AMEntityRegistry.CROCODILE.get(), 6.0).add(AMEntityRegistry.GORILLA.get(), 6.0).add(AMEntityRegistry.TIGER.get(), 6.0).add(AMEntityRegistry.ELEPHANT.get(), 4.0).add(AMEntityRegistry.KOMODO_DRAGON.get(), 2.0).add(AMEntityRegistry.SNOW_LEOPARD.get(), 3.0), new WeightedList<Integer>().add(6, 10).add(8, 10));
        //VOID_ZOO_EVENT = new SpawnEntityVaultEvent("Void Invasion", "Ender what now?", "#bf00ff",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.ENDERIOPHAGE.get(), 12.0).add(AMEntityRegistry.VOID_WORM.get(), 3.0).add(AMEntityRegistry.COSMIC_COD.get(), 4.0).add(AMEntityRegistry.MIMICUBE.get(), 5.0).add(ModEntities.T1_ENDERMAN, 7.0).add(ModEntities.T2_ENDERMAN, 9.0), new WeightedList<Integer>().add(6, 10).add(8, 10));
        //DWELLER_EVENT = new SpawnEntityVaultEvent("Dweller Duel", "Attack of the dwellers", "#ff6666",  new WeightedList<EntityType<?>>().add(ModEntities.VAULT_FIGHTER_TYPES.get(1), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(2), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(3), 6.0).add(ModEntities.VAULT_FIGHTER_TYPES.get(4), 6.0), new WeightedList<Integer>().add(6, 10).add(10, 10));
        //THERMAL_EXPANSION = new SpawnEntityVaultEvent("Thermal Expansion", "Thermally expand these mobs into your face", "#8ab97d",  new WeightedList<EntityType<?>>().add(TCoreEntities.BLITZ.get(), 6.0).add(TCoreEntities.BLIZZ.get(), 6.0).add(TCoreEntities.BASALZ.get(), 6.0), new WeightedList<Integer>().add(6, 10).add(10, 10).add(8, 10));
        //LA_CUCARACHA_EVENT = new SpawnEntityVaultEvent("La Cucaracha", "Cha Cha Cha", "#ff6666",  new WeightedList<EntityType<?>>().add(AMEntityRegistry.COCKROACH.get(), 6.0), new WeightedList<Integer>().add(2, 10).add(3, 10), AMItemRegistry.MARACA.get().getDefaultInstance());
        //WOLD_SANTA_BOX_EVENT = new GiftItemVaultEvent("Wold's Box Giveaway", "And they said I wasn't nice", "#4d94ff", new WeightedList<ItemStack>().add(new ItemStack(ModItems.SUPPLY_BOX.asItem(), 1), 4.0).add(new ItemStack(ModItems.GEM_BOX.asItem(), 1), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.MOD_BOX.asItem(), 1), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.MYSTERY_BOX.asItem(), 1), 4.0).add(new ItemStack(ModItems.AUGMENT_BOX), 4.0));
        //CONSOLATION_PRIZE = new GiftItemVaultEvent("Consolation Prize", "At least you tried...", "#7979d2", new WeightedList<ItemStack>().add(new ItemStack(iskallia.vault.init.ModItems.VELVET.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.ORNATE_INGOT.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.GILDED_INGOT.asItem(), 8), 4.0).add(new ItemStack(iskallia.vault.init.ModItems.VAULT_MEAT.asItem(), 8), 4.0));
        //PANDAMONIUM_EVENT = new InceptionVaultEvent("PANDAMONIUM", "Unleash the horde", "#4dffff",  SPAWN_ENTITY_ENCHANTED_EVENTS, true, 5);
        //X_RANDOM_EVENT = new InceptionVaultEvent("5 Random Events", "Open pandora's box", "#66b3ff",  ENCHANTED_EVENTS, true, 5);
        //X_OMEGA_RANDOM_EVENT = new InceptionVaultEvent("3 Random Omega Events", "3 random omega events, did you get lucky?", "#0088cc", OMEGA_ENCHANTED_EVENTS, true, 3);
        //X_MODIFIER_RANDOM_EVENT = new InceptionVaultEvent("3 Random Modifier Events", "3 random modifier events", "#ff66b3",  MODIFIER_ENCHANTED_EVENTS, true, 3);
        //HORDE_EVENT = new InceptionVaultEvent("Horde Night", "3 Random Mob spawn events", "#5c5cd6",  SPAWN_ENTITY_ENCHANTED_EVENTS, true, 3);
        //VAMPIRE_SURVIVORS = new InceptionVaultEvent("Vampire Survivors", "Grants leeching and spawns bats", "#ffc34d",  new WeightedList<LegacyVaultEvent>().add(LEECHING_MODIFIER_EVENT, 1.0).add(BAT_EVENT, 1.0), false, 0);
        //TELESWAP_EVENT = new PlayerSwapVaultEvent("Teleswap", "Swap places with another player", "#cc6699");
        //LA_CUCARACHA_RANDOM_EVENT = new LaCucarachaSpecialVaultEvent("Cockroach's Judgement", "Gives a random negative or postive event to each player.", "#eab676", 0.25F);
        //MOB_VIGOR_EVENT = new BuffEntityInAreaVaultEvent("Mob Boost", "Gives speed and strength to nearby mobs", "#789D00", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 1 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 1), 1.0), true, 1, 32);
        //MOB_DOWNGRADE_EVENT = new BuffEntityInAreaVaultEvent("Mob Downgrade", "Gives slowness and weakness to nearby mobs", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.WEAKNESS, 3600, 5 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3600, 5), 1.0), true, 1, 32);
        //DISAPPEAR_MOBS_EVENT = new BuffEntityInAreaVaultEvent("Mob Invisibility", "Makes mobs in the area around you invisible", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.INVISIBILITY, 7200, 0 ), 1.0), true, 1, 32);
        //MOB_RESISTANCE_EVENT = new BuffEntityInAreaVaultEvent("Mob Resistance", "Makes mobs in the area around you much tougher", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 5 ), 1.0), true, 1, 32);
        //RANDOM_MOB_BUFFS = new BuffEntityInAreaVaultEvent("Random Mob Buffs", "Grants random potion effects to nearby mobs", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 3 ), 1.0).add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1 ), 1.0).add(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2 ), 1.0).add(new MobEffectInstance(MobEffects.REGENERATION, 3600, 4 ), 1.0).add(new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600, 3 ), 1.0).add(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4 ), 1.0), true, 1, 32);
        //MOB_MINIMIZER_EVENT = new BuffEntityInAreaVaultEvent("Mob Minimizer", "Makes nearby mobs eenie weenie", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(ModEffects.SHRINKING, 3600, 3 ), 1.0), true, 1, 32);
        //MOB_MAXIMIZER_EVENT  = new BuffEntityInAreaVaultEvent("Mob Maximizer", "Makes nearby mobs large and in charge", "#789DCD", new WeightedList<MobEffectInstance>().add(new MobEffectInstance(ModEffects.GROWING, 3600, 0 ), 1.0), true, 1, 3);
        //SHRINKING_EVENT = new PotionEffectVaultEvent("Shrink Ray", "Honey I shrunk the vaulters", "#ff8c1a",   ModEffects.SHRINKING, 600 , 0);
    }
}
