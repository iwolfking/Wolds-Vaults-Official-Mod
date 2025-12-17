package xyz.iwolfking.woldsvaults.objectives.data;

import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import iskallia.vault.VaultMod;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.ThermalEvents;
import xyz.iwolfking.woldsvaults.objectives.data.builtin.events.WildBackportEvents;


public class EnchantedEventsRegistry {

    private static final WeightedList<VaultEvent> ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> OMEGA_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> POSITIVE_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> NEGATIVE_ENCHANTED_EVENTS = new WeightedList<>();

    private static final WeightedList<VaultEvent> SPAWN_ENTITY_ENCHANTED_EVENTS = new WeightedList<>();
    private static final WeightedList<VaultEvent> MODIFIER_ENCHANTED_EVENTS = new WeightedList<>();

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
                .build("Chaos Modifier", new TextComponent("Adds 5-10 random modifiers from the Chaos modifier pool.")), 12.0);

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
                        .effect(ModEffects.EMPOWER, 255, 600)
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
                .task(new VaultModifierTask(VaultMod.id("leeching"), 1, 2400))
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

        register(WoldsVaults.id("purifying_aura"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .color(TextColor.parseColor("#7D9BFF"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(iskallia.vault.init.ModEffects.PURIFYING_AURA, 0, 1200)
                        .build()
                )
                .build("Purifying Aura", new TextComponent("Become immune to negative effects for a while.")), 8.0);

        register(WoldsVaults.id("turkey_feast"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#8871744"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new PlayerMobEffectTask.Builder()
                        .effect(MobEffects.SATURATION, 3, 2400)
                        .effect(MobEffects.REGENERATION, 1, 1200)
                        .build()
                )
                .task(new PlaySoundTask(SoundEvents.GENERIC_EAT))
                .task(new SpawnMobTask.Builder()
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HATURKIN, 3.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HOSTILE_TURKEY, 6.0)
                        .entity(xyz.iwolfking.woldsvaults.init.ModEntities.HOSTILE_CHICKEN, 4.0)
                        .effect(new Effect(MobEffects.REGENERATION, 9, 1200), 1.0)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 1, 1200), 1.0)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 1, 1200), 1.0)
                        .build())
                .build("Fowl Feast", new TextComponent("Spawns some angry fowls and grants big saturation and regeneration for a while!")), 4.0);

        register(WoldsVaults.id("soul_fest"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#87008E"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierTask(VaultMod.id("soul_fest"), 1, 1200))
                .build("Soul Fest", new TextComponent("Big soul shard boost for a limited time!")), 2.0);

        register(WoldsVaults.id("surprise_boxes"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#884569"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierTask(VaultMod.id("surprise_boxes"), 1, 1600))
                .task(new VaultModifierTask(VaultMod.id("hoard"), 1, 1600))
                .task(new VaultModifierTask(VaultMod.id("fortuitous"), 1, 1600))
                .build("Surprise!?", new TextComponent("Chests spawn nasty mobs and other things but have more stuffs for a limited time!")), 4.0);

        register(WoldsVaults.id("no_fruit_or_temporal"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#676934"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierTask(VaultMod.id("rotten"), 1, 6000))
                .task(new VaultModifierTask(VaultMod.id("no_temporal_shard"), 1, 6000))
                .build("Rotten Core", new TextComponent("Fruit, Temporal Shards, and Companions are temporarily disabled!")), 4.0);

        register(WoldsVaults.id("lunar"), new VaultEvent.Builder()
                .tag(EventTag.POSITIVE)
                .tag(EventTag.OMEGA)
                .tag(EventTag.ADDS_MODIFIER)
                .color(TextColor.parseColor("#676934"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new VaultModifierTask(VaultMod.id("jumpy_deluxe"), 3, 2400))
                .task(new VaultModifierTask(VaultMod.id("lunar"), 1, 2400))
                .build("Lunar Gravity", new TextComponent("Huge jump boost and mobs float for a limited time!")), 4.0);

        register(WoldsVaults.id("strange_times"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#843E34"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new RepeatTask(new WeightedTask.Builder()
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.LEVITATION, 0, 120).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.CONFUSION, 0, 240).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.BLINDNESS, 0, 120).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.HUNGER, 2, 240).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.NIGHT_VISION, 0, 120).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.WITHER, 0, 120).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(ModEffects.QUICKENING, 2, 240).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(ModEffects.EMPOWER, 2, 240).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(MobEffects.DAMAGE_RESISTANCE, 2, 240).build(), 1.0)
                        .task(new PlayerMobEffectTask.Builder().effect(AMEffectRegistry.SOULSTEAL, 0, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(ModEffects.EMPOWER, 2, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.LEVITATION, 0, 120).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.CONFUSION, 0, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.BLINDNESS, 0, 120).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.HUNGER, 2, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.NIGHT_VISION, 0, 120).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.WITHER, 0, 120).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(ModEffects.QUICKENING, 2, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.DAMAGE_RESISTANCE, 2, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.DAMAGE_BOOST, 4, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(MobEffects.MOVEMENT_SPEED, 3, 240).build(), 1.0)
                        .task(new MobMobEffectTask.Builder().effect(AMEffectRegistry.SOULSTEAL, 0, 240).build(), 1.0)
                        .build(), 120, 60))
                .build("Strange Times", new TextComponent("Strange effects happen to you and mobs around you!")), 6.0);

        register(WoldsVaults.id("la_cucaracha"), new VaultEvent.Builder()
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
                                .task(new MessageTask(VaultEvent.EventDisplayType.CHAT_MESSAGE_TARGET, new TranslatableComponent("vault_event.woldsvaults.cockroach_celebrate").withStyle(ChatFormatting.GOLD)))
                                .build(), 1)
                        .task(new TaskGroup.Builder()
                                .task(new ExecuteEventsTask(() -> NEGATIVE_ENCHANTED_EVENTS, 3))
                                .task(new MessageTask(VaultEvent.EventDisplayType.CHAT_MESSAGE_TARGET, new TranslatableComponent("vault_event.woldsvaults.cockroach_hate").withStyle(ChatFormatting.RED)))
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

        if(LoadingModList.get().getModFileById("thermal") != null) {
            ThermalEvents.init();
        }


    }
}
