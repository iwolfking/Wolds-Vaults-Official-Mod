package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.config.ColorsConfig;
import iskallia.vault.config.ModBoxConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.datagen.ExpertisesDescriptionHelper;
import xyz.iwolfking.woldsvaults.api.util.datagen.PrestigePowersDescriptionsHelper;
import xyz.iwolfking.woldsvaults.api.util.datagen.TalentDescriptionsHelper;

import java.util.function.Consumer;

public class ModSkillDescriptionsProvider extends AbstractSkillDescriptionsProvider {
    public ModSkillDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    @Override
    public void registerConfigs() {
        ModConfigs.MOD_BOX = new ModBoxConfig().readConfig();
        ModConfigs.COLORS = new ColorsConfig().readConfig();

        add("talent_overlevels", builder -> {
            TalentDescriptionsHelper.appendOverlevelDescription("Haste", builder, "add", "+10% Mining Speed", "#47b8f5");
            TalentDescriptionsHelper.appendOverlevelDescription("Speed", builder, "add", "+10% Movement Speed", "#ffe400");
            TalentDescriptionsHelper.appendOverlevelDescription("Strength", builder, "add", "+6 Attack Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Intelligence", builder, "add", "+8 Ability Power", "#FF00CB");
            TalentDescriptionsHelper.appendOverlevelDescription("Purist", builder, "add", "+10% Increased Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Last_Stand", builder, "increases the", "Health Threshold by 2%", "#8EEDD9");
            TalentDescriptionsHelper.appendOverlevelDescription("Berserking", builder, "increases the", "Health Threshold by 2%", "#8EEDD9");
            TalentDescriptionsHelper.appendOverlevelDescription("Sorcery", builder, "add", "+10% Increased Mana Regeneration", "#0353d7");
            TalentDescriptionsHelper.appendOverlevelDescription("Prime_Amplification", builder, "add", "+5% Increased Area Of Effect", "#e9c375");
            TalentDescriptionsHelper.appendOverlevelDescription("Bountiful_Harvest", builder, "adds one additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Battle_Trance", builder, "adds one additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Treasure_Seeker", builder, "adds one additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Arcana", builder, "adds one additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Blazing", builder, "adds one additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Lucky_Momentum", builder, "adds two additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Blood_Rush", builder, "adds two additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Frenzy", builder, "adds two additional max", "stack", "#36ffa7");
            TalentDescriptionsHelper.appendOverlevelDescription("Lightning_Damage", builder, "adds", "+10% Increased Lightning Damage", "#ffeb07");
            TalentDescriptionsHelper.appendOverlevelDescription("Lightning_Stun", builder, "adds a small increase to the", "stun chance and duration", "#19A6E4");
            TalentDescriptionsHelper.appendOverlevelDescription("Stoneskin", builder, "decreases the", "Health Threshhold by 2%", "#8EEDD9");
            TalentDescriptionsHelper.appendOverlevelDescription("Lingering_Fumes", builder, "add", "+10% Effect Duration", "#85edfe");
            TalentDescriptionsHelper.appendOverlevelDescription("Clean_Escape", builder, "add", "+10% Movement Speed", "#ffe400");
            TalentDescriptionsHelper.appendOverlevelDescription("Witchery", builder, "add", "+10% Soul Shard Chance", "#4800FF");
            TalentDescriptionsHelper.appendOverlevelDescription("Methodical", builder, "add", "+20% Healing Efficiency", "#7DF587");
            TalentDescriptionsHelper.appendOverlevelDescription("Depleted", builder, "add", "+25% Additional Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Mana_Harvest", builder, "add", "+1 Additional Mana", "#0353d7");
            TalentDescriptionsHelper.appendOverlevelDescription("Fatal_Strike", builder, "add", "+20% Additional Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Heart_Fragments", builder, "add", "+3% Heart Fragment Chance", "#7DF587");
            TalentDescriptionsHelper.appendOverlevelDescription("Mana_Steal", builder, "add", "+0.5% Max Mana Restored", "#19A6E4");
            TalentDescriptionsHelper.appendOverlevelDescription("Life_Steal", builder, "add", "+0.5% Life Leeched", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Cleave", builder, "add", "+1% Additional damage and +5% Additional Range", "#5ae09c");
            TalentDescriptionsHelper.appendOverlevelDescription("Prudent", builder, "add", "+4% Additional Chance", "#5ae09c");
            TalentDescriptionsHelper.appendOverlevelDescription("Ethereal", builder,"add", "+3% Additional Chance", "#5ae09c");
            TalentDescriptionsHelper.appendOverlevelDescription("Quickening", builder, "add", "+10% Mana Refunded", "#19A6E4");
            TalentDescriptionsHelper.appendOverlevelDescription("Trap_Disarm", builder, "add", "+10% Trap Disarm Chance", "#4800FF");
            TalentDescriptionsHelper.appendOverlevelDescription("Executioner", builder, "add", "+5% Additional Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Blizzard", builder, "adds an additional level to the", "Frost Nova", "#2FE1FA");
            TalentDescriptionsHelper.appendOverlevelDescription("Frostbite", builder, "add", "+1% Additional Chance", "#5ae09c");
            TalentDescriptionsHelper.appendOverlevelDescription("Daze", builder, "add", "+20% Additional Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Voltaic_Impact", builder, "add", "+20% Additional Damage", "#C23627");
            TalentDescriptionsHelper.appendOverlevelDescription("Blight", builder, "add", "+2% Damage Reduction", "#ffd800");
            TalentDescriptionsHelper.appendOverlevelDescription("Hunters_Instinct", builder, "add", "+0.2% Chance", "#ffd800");
        });

        add("wolds_talents", builder -> {
            builder.addDescription("Potent_Elixir", description -> {
                description.add(JsonDescription.text("Increases the "));
                description.add(JsonDescription.text("Potency ", "#5ae09c"));
                description.add(JsonDescription.text("of your "));
                description.add(JsonDescription.simple("Vault Potion ", "aqua"));
                description.add(JsonDescription.text(", boosting its effect."));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+2 Absorption Hearts", "gold"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+4% Cooldown Reduction", "aqua"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+1 Casted Ability Levels", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+20 Flat Mana gained", "#0353d7"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+5 second effect duration", "#7024ac"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+1 effect level", "#FFB00F"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+4 Absorption Hearts", "gold"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+8% Cooldown Reduction", "aqua"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+2 Casted Ability Levels", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+40 Flat Mana gained", "#0353d7"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+10 second effect duration", "#7024ac"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+2 effect level", "#FFB00F"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+6 Absorption Hearts", "gold"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+12% Cooldown Reduction", "aqua"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+3 Casted Ability Levels", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+60 Flat Mana gained", "#0353d7"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+15 second effect duration", "#7024ac"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("+3 effect level", "#FFB00F"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Potent Elixir", "add", "+2 Absorption Hearts, +2% Cooldown Reduction, +1 casted Ability Level, +20 Flat Mana Restored, +5% max mana restored, +3 second effect duration, or 1 effect amplifier every 3 levels", "gold", description);
            });
            builder.addDescription("Healthy_Elixir", description -> {
                description.add(JsonDescription.text("Enhances the "));
                description.add(JsonDescription.text("Healthiness ", "#7DF587"));
                description.add(JsonDescription.text("of your "));
                description.add(JsonDescription.text("Vault Potion ", "aqua"));
                description.add(JsonDescription.text(", making it also heal an additional percentage of your "));
                description.add(JsonDescription.text("max health", "#8EEDD9"));
                description.add(JsonDescription.text(".\n\nDepending on the tier, the amount restored will be modified: "));
                description.add(JsonDescription.text("\nVial: 50% reduction"));
                description.add(JsonDescription.text("\nPotion: 25% reduction"));
                description.add(JsonDescription.text("\nMixture: 0% reduction"));
                description.add(JsonDescription.text("\nBrew: 50% increase"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+20%", "#7DF587"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+30%", "#7DF587"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+40%", "#7DF587"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Healthy Elixir", "add", "+3% max health restored", "#7DF587", description);
            });

            builder.addDescription("Blood_Chakra", description -> {
                description.add(JsonDescription.text("Killing a mob grants "));
                description.add(JsonDescription.text("+3% Increased Ability Power", "#bd49bf"));
                description.add(JsonDescription.text(". This effect can "));
                description.add(JsonDescription.text("stack ", "yellow"));
                description.add(JsonDescription.text("and lasts for a short time."));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+4 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("4 seconds", "#7e1c80"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+6 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("6 seconds", "#7e1c80"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+8 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("8 seconds", "#7e1c80"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+10 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("10 seconds", "#7e1c80"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Blood_Chakra", "add", "one additional max stack", "#48a188", description);
            });

            builder.addDescription("Ransack", description -> {
                description.add(JsonDescription.text("Breaking sliceable "));
                description.add(JsonDescription.text("Vault Chests ", "#e88a12"));
                description.add(JsonDescription.text("becomes faster with additional "));
                description.add(JsonDescription.text("Dismantling Chance", "f99b21"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+50% Dismantle Chance", "#f99b21"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+75% Dismantle Chance", "#f99b21"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+100% Dismantle Chance", "#f99b21"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+125% Dismantle Chance", "#f99b21"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Ransack", "add", "+25% Dismantle Chance", "#f99b21", description);
            });

            builder.addDescription("Lunge", description -> {
                description.add(JsonDescription.text("Increases the reach in which you hit with melee weapons with additional "));
                description.add(JsonDescription.text("Attack Range", "#0D7A39"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+0.25 Attack Range", "#0D7A39"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+0.5 Attack Range", "#0D7A39"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+0.75 Attack Range", "#0D7A39"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+1 Attack Range", "#0D7A39"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Lunge", "add", "+0.1 Attack Range", "#0D7A39", description);
            });

            builder.addDescription("Medic", description -> {
                description.add(JsonDescription.text("Improve your healing skills and sources with increased "));
                description.add(JsonDescription.text("Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+10% Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+20% Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+30% Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+40% Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text("\n\n5 "));
                description.add(JsonDescription.text("+50% Healing Effectiveness", "#7DF587"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Medic", "add", "+10% Healing Effectiveness", "#0D7A39", description);

            });
            builder.addDescription("Momentum_Engine", description -> {
                description.add(JsonDescription.text("While moving, builds up stacks of "));
                description.add(JsonDescription.text("Momentum ", "#E9C375"));
                description.add(JsonDescription.text("increasing your "));
                description.add(JsonDescription.text("Movement Speed", "#f6cd0e"));
                description.add(JsonDescription.text(". When at the maximum stacks, the next melee strike consumes all stacks to trigger a guaranteed "));
                description.add(JsonDescription.text("Shocking Hit ", "#ffeb07"));
                description.add(JsonDescription.text("with a modified radius and knockback. Remaining stationary or performing a melee strike will remove stacks."));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("5 max stacks, 4% Movement Speed per stack, stacking every 4 seconds. 6 Radius with 120% Knockback.", "#C7FFE7"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+5 max stacks, 4% Movement Speed per stack, stacking every 3.5 seconds. 7 Radius with 140% Knockback", "#C7FFE7"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("5 max stacks, 4% Movement Speed per stack, stacking every 3 seconds. 8 Radius with 160% Knockback.", "#C7FFE7"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Momentum Engine", "add", "+0.1 second charge reduction and increased knock and size", "#C7FFE7", description);
            });
            builder.addDescription("Healthy_Harvest", description -> {
                description.add(JsonDescription.text("Adds a chance for "));
                description.add(JsonDescription.text("Heart Fragments ", "#14ff02"));
                description.add(JsonDescription.text("to generate when opening "));
                description.add(JsonDescription.text("Vault Chests", "e88a12"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+4%", "#7DF587"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+6%", "#7DF587"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+8%", "#7DF587"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+10%", "#7DF587"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Supply Crates", "add", "+1% additional chance", "#ffeb07", description);
            });
            builder.addDescription("Arcane_Strike", description -> {
                description.add(JsonDescription.text("Enhances your "));
                description.add(JsonDescription.text("Lucky Hits ", "#6DF5A3"));
                description.add(JsonDescription.text("causing them to reduce all of your abilities "));
                description.add(JsonDescription.text("cooldowns by a percentage of their maximum cooldown time"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+2% reduced cooldown", "#cbe6fe"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+3% reduced cooldown", "#cbe6fe"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+4% reduced cooldown", "#cbe6fe"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Arcane Cascade", "add", "+0.5% reduced cooldown", "#cbe6fe", description);
            });
            builder.addDescription("Fanged_Strike", description -> {
                description.add(JsonDescription.text("Enhances your "));
                description.add(JsonDescription.text("Lucky Hits ", "#6DF5A3"));
                description.add(JsonDescription.text("causing them to launch a Fang striking your target from below dealing a small portion of your attack's "));
                description.add(JsonDescription.text("damage ", "#C23627"));
                description.add(JsonDescription.text("and procs any on-hit effects again"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+5% damage", "#C23627"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+7.5% damage", "#C23627"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+10% damage", "#C23627"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Fanged Strike", "add", "+1% increased damage", "#C23627", description);
            });

            builder.addDescription("Execution_Strike", description -> {
                description.add(JsonDescription.text("Enhances your "));
                description.add(JsonDescription.text("Lucky Hits ", "#6DF5A3"));
                description.add(JsonDescription.text("causing them to deal a portion of the target's "));
                description.add(JsonDescription.text("missing health ", "red"));
                description.add(JsonDescription.text("as bonus "));
                description.add(JsonDescription.text("damage ", "red"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+15% of missing health", "#d80000"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Executing Strike", "add", "+1% increased missing health damage", "#d80000", description);
            });

            builder.addDescription("Mind_Meld", description -> {
                description.add(JsonDescription.text("Become one with your inner mana. Your "));
                description.add(JsonDescription.text("Cooldown Reduction ", "#6DF5A3"));
                description.add(JsonDescription.text(" is increased the larger your "));
                description.add(JsonDescription.text("max mana ", "#0353d7"));
                description.add(JsonDescription.text("pool is"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+1% Cooldown Reduction ", "#6DF5A3"));
                description.add(JsonDescription.text("for every "));
                description.add(JsonDescription.text("50 max mana", "#0353d7"));
            });

            builder.addDescription("Cleave", description -> {
                description.add(JsonDescription.text("Enhances your "));
                description.add(JsonDescription.text("Lucky Hits ", "#6DF5A3"));
                description.add(JsonDescription.text("causing them to deal a portion of your "));
                description.add(JsonDescription.text("damage ", "red"));
                description.add(JsonDescription.text("in a radius around the target "));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+40%", "#d80000"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+60%", "#d80000"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+75%", "#d80000"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Cleave", "add", "+5% increased damage and range", "#d80000", description);
            });

            builder.addDescription("Stack_Master", description -> {
                description.add(JsonDescription.text("Enhances your "));
                description.add(JsonDescription.text("Stacking ", "yellow"));
                description.add(JsonDescription.text("talents, adding additional max stacks to any talents that use them"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+10 Additional Stacks", "gold"));
            });

            builder.addDescription("Blazing", description -> {
                description.add(JsonDescription.text("Gain "));
                description.add(JsonDescription.text("+4% Movement Speed ", "speed"));
                description.add(JsonDescription.text("when hitting a mob. This effect can "));
                description.add(JsonDescription.text("stack ", "yellow"));
                description.add(JsonDescription.text("and lasts for a duration of time"));
                description.add(JsonDescription.text(".\n\n1 "));
                description.add(JsonDescription.text("+4 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("4 seconds ", "#4800FF"));
                description.add(JsonDescription.text("wear-off time"));
                description.add(JsonDescription.text("\n\n2 "));
                description.add(JsonDescription.text("+6 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("6 seconds ", "#4800FF"));
                description.add(JsonDescription.text("wear-off time"));
                description.add(JsonDescription.text("\n\n3 "));
                description.add(JsonDescription.text("+8 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("8 seconds ", "#4800FF"));
                description.add(JsonDescription.text("wear-off time"));
                description.add(JsonDescription.text("\n\n4 "));
                description.add(JsonDescription.text("+10 Max Stacks", "yellow"));
                description.add(JsonDescription.text(", "));
                description.add(JsonDescription.text("10 seconds ", "#4800FF"));
                description.add(JsonDescription.text("wear-off time"));
                description.add(JsonDescription.text("\n\n"));
                TalentDescriptionsHelper.appendOverlevelDescription("Blazing", "add one additional max", "stack", "yellow", description);
            });
        });

        add("prestige_overrides", builder -> {
            PrestigePowersDescriptionsHelper.generateDescriptions(builder, getOverridePrestigeDescriptions());
        });

        add("wolds_builtin_prestiges", builder -> {
            PrestigePowersDescriptionsHelper.generateDescriptions(builder, getBuiltInPrestigePowers());
        });

        add("wolds_builtin_expertises_and_overrides", builder -> {
            ExpertisesDescriptionHelper.generateDescriptions(builder, getExpertises());
        });


        add("vanilla_research_overrides", builder ->
                builder.addDescription("Easy Villagers", jsonElements -> modDesc("Easy Villagers and Easy Piglins", "mods", innerDesc -> {
                     innerDesc.add(grantsTransmog());
                     innerDesc.add(JsonDescription.simple("\n"));
                     innerDesc.add(JsonDescription.simple("This mod allows you to store, and interact, with villagers in block form, making it easy and fun to set up trading halls. It can also be used to farm resources with villagers, including iron."));
                     innerDesc.add(JsonDescription.simple("You will also be able to craft a Barterer to make trading with Piglins much easier!"));
                     innerDesc.add(JsonDescription.simple("\nOnly the auto blocks will be locked behind this unlock now, you can make normal Traders cheap and easy, enjoy!"));
                     innerDesc.add(modBox());
                 }).forEach(jsonElements::add))

                 .addDescription("Elevators", jsonElements -> modDesc("Elevators and Travel Anchors", innerDesc -> {
                     innerDesc.add(JsonDescription.simple("This mod provides you with ways of travelling vertically. The basic Elevators, when placed vertically from one another within a certain distance gives you the ability to SHIFT / JUMP on them to go DOWN / UP.", "$text"));
                     innerDesc.add(JsonDescription.simple("\n\nThere are also a Travel Staff and Travel Anchors that let you travel to any Travel Anchor you are looking at! Note, the Teleportation Enchantment is disabled in vaults should you find a way of accessing it...", "$text"));
                 }).forEach(jsonElements::add))

                 .addDescription("Auto Feeding", jsonElements -> {
                     modDesc("Auto Feeding, Refill, Compacting, and Auto-Smelting, Smoking, Blasting", "upgrades",  innerDesc -> {
                         innerDesc.add(JsonDescription.simple("Feeding Upgrades\n\n", "yellow"));
                         innerDesc.add(JsonDescription.simple("can automatically feed you directly from your pouches, belts & backpacks and removes the need to eat food manually.\n\n"));
                         innerDesc.add(JsonDescription.simple("Refill, Compacting, and Auto-Smelting, Smoking, and Blasting", "yellow"));
                         innerDesc.add(JsonDescription.simple("upgrades will let you refill items in your hand from your backpack automatically, compacting items in 2x2 and 3x3 recipes, and automatically process items in your bag!", "yellow"));
                     }).forEach(jsonElements::add);;
                 })

                 .addDescription("Automatic Genius", jsonElements -> {
                            modDesc("Bragging rights", "of unlocking all mods",  innerDesc -> {
                                innerDesc.add(JsonDescription.simple("Big Brain!\n\n", "yellow"));
                                innerDesc.add(JsonDescription.simple("You have completed every research and filled your brain with knowledge!\n\n"));
                                innerDesc.add(JsonDescription.simple("This research is purely for bragging rights, all autocrafting is available within their respective mod without this unlock!\n\n"));
                                innerDesc.add(JsonDescription.simple("All mods with autocrafting respect the research system of Vault Hunters, only allowing crafting items you have unlocked!"));
                            }).forEach(jsonElements::add);;
                })

                .addDescription("Colossal Chests", jsonElements -> {
                    modDesc("Colossal Chests",  innerDesc -> {
                        innerDesc.add(JsonDescription.simple("Colossal chests allows you to build massive chests that can store a lot of items in them.\n\n"));
                        innerDesc.add(JsonDescription.simple("Colossal chests above Iron have been disabled to help prevent issues on servers!"));
                    }).forEach(jsonElements::add);;
                })



                 .addDescription("Junk Management", jsonElements -> modDesc("Junk Management", "research", innerDesc -> {
                     innerDesc.add(JsonDescription.simple("The Void Crucible will let you configure what blocks you don't want to void when using the Void + Pickup Mode on your ", "$text"));
                     innerDesc.add(JsonDescription.simple("Magnet", "aqua"));
                     innerDesc.add(JsonDescription.simple(". The Void Crucible also offers a theme selector, where you can specify items based on themes to not void\n\nThis is a must have for any inventory enjoyer!\n\n", "$text"));
                     innerDesc.add(JsonDescription.simple("Additionally, unlocks the ", "$text"));
                     innerDesc.add(JsonDescription.simple("Filter Necklace ", "yellow"));
                     innerDesc.add(JsonDescription.simple("that lets you use the power of Vault Filters to automatically void items that match the Attribute or List filters in your necklace. ", "$text"));
                     innerDesc.add(JsonDescription.simple("\n\nFurthermore, your ", "$text"));
                     innerDesc.add(JsonDescription.simple("Magnet", "aqua"));
                     innerDesc.add(JsonDescription.simple(" will no longer take durability damage from picking up Vault Junk, lucky you!", "$text"));
                 }).forEach(jsonElements::add))
        );

        add("research_descriptions", builder ->
                builder.addDescription("Ars Nouveau", jsonElements -> modDesc("Ars Nouveau", innerDesc -> {
                    disabledInVaults().forEach(innerDesc::add);
                    innerDesc.add(JsonDescription.simple("Ars Nouveau is a magic mod inspired by Ars Magicka that allows players to craft their own spells, create magical artifacts, perform rituals, and much more!\n\n", "$text"));
                    innerDesc.add(JsonDescription.simple("Documentation is provided entirely in-game by Patchouli. To get started with this mod, craft the Worn Notebook using a book and 1 Lapis Lazuli.\n\n", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Augment Crafting", jsonElements -> modDesc("Augment Assembly Pedestal", "workstation", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("The Augment Assembly Pedestal will allow you to craft Augments using Augment Pieces and other materials, depending on the theme. \n\n", "$text"));
                    innerDesc.add(JsonDescription.simple("You first have to unlock a theme before you can craft it in the Assembly Pedestal. You will automatically do that the first time you enter a theme, you may have seen the messages pop up in chat! Certain themes that are not normally obtainable will be unlocked when doing certain tasks in the vault...", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Companion Workstation", jsonElements -> modDesc("Companion Workstation", "mod", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("The Companion Workstation will allow you to craft Companions, Companion Relics, and Companion Particle Trails from materials acquired from recycling! ", "$text"));
                    innerDesc.add(JsonDescription.simple("You will need to use the Companion Recycler to obtain Companion Fragments and Companion Essence for crafting.\n\n"));
                    innerDesc.add(JsonDescription.simple("Additionally, unlocks the Companion Locker, a useful block to store and search through all your Companions!"));
                }).forEach(jsonElements::add))

                .addDescription("Mod Box Tinkering", jsonElements -> modDesc("Mod Box Tinkering", "workstation", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("The Mod Box Workstation will allow you to craft Targeted Mod Boxes that only drop items and blocks for a certain mod!", "$text"));
                    innerDesc.add(JsonDescription.simple("You have to have the corresponding research first before you can craft a particular box. The cost for each is dependent on the mod."));
                    innerDesc.add(JsonDescription.simple("\n"));
                    innerDesc.add(JsonDescription.simple("This research is unlocked when you have researched 10 different mods!"));
                }).forEach(jsonElements::add))

                .addDescription("Applied Compatability", jsonElements -> modDesc("Applied Botanics and Applied Mekanistics", "addons", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds mana cells and P2P tunnels for storing and interacting with Botania mana, and chemical cells for storing and interacting with Mekanism chemicals!\n\n"));
                }).forEach(jsonElements::add))

                .addDescription("Botanical Machinery", jsonElements -> modDesc("Botanical Machinery", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("For the lazy botanist, this mod adds one-block automation solutions for things like the Runic Altar, Agglomeration Plate, Alfheim Portal, etc. \n\n", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Advanced Peripherals", jsonElements -> modDesc("Advanced Peripherals", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds lots of new blocks that integrate with ComputerCraft such as the Chat Box and Environmental Scanner, among many more! Take your computing to the next level.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("ComputerCraft", jsonElements -> modDesc("ComputerCraft", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod provides you with the amazing power of computing! Conquer the world and improve your automation with this Lua-based machine!... or just have some fun messing around!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Incorporeal", jsonElements -> modDesc("Incorporeal", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds new ways of interacting with your Corporea system, some decorative blocks, and some other nifty and fun little things.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("RFTools Power", jsonElements -> modDesc("RFTools Power", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod generates power through a few different options using fuel.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Create Crafts and Additions", jsonElements -> modDesc("Create Crafts and Additions", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod acts as a bridge between electricity and Create's kinetic energy by adding an electric motor that generates kinetic energy from Forge energy and an alternator that can do the opposite. Also adds a few other useful features!\n\n", "$text"));
                    innerDesc.add(JsonDescription.simple("Also unlocks the ", "$text"));
                    innerDesc.add(JsonDescription.simple("Create Enchantment Industry ", "yellow"));
                    innerDesc.add(JsonDescription.simple("mod! ", "$text"));
                    innerDesc.add(JsonDescription.simple("That lets you enchant and disenchant items with Create!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Better Wallets", jsonElements -> {
                    modDesc("Gold, Emerald, Diamond, and Netherite Wallets", "for Lightman's Currency", innerDesc -> innerDesc.add(JsonDescription.simple("These wallets store even more currency and have some extra features such as magneting coins into them, exchanging between currencies, and accessing your ATM remotely!\n\n", "$text"))).forEach(jsonElements::add);;
                })

                .addDescription("Bonsai Pots", jsonElements -> {
                    modDesc("Bonsai Pots", innerDesc -> innerDesc.add(JsonDescription.simple("These pots can automate any tree in the game very easily by growing them in tiny pots.\n\n", "$text"))).forEach(jsonElements::add);;
                })

                .addDescription("Create Diesel Generators", jsonElements -> modDesc("Create Diesel Generators", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds new blocks and items used to dig up oil from the ground using Pumpjacks. Useful for getting oil for PneumaticCraft or getting kinetic energy in a more interesting way!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Create Ore Excavation", jsonElements -> modDesc("Create Ore Excavation", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds drilling machines for fluids and ores, search for the randomly generated ore veins and generate ores to your hearts content!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Create Mechanical Extruder", jsonElements -> modDesc("Create Mechanical Extruder", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds a new block powered by kinetic energy that can be used to generate cobblestone, obsidian, and other stones. Very useful addition for automation!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Compressed Creativity", jsonElements -> modDesc("Compressed Creativity", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds some new blocks and items that add better integration between Create and PneumaticCraft, such as letting you convert air pressure into SU/RPM.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Immersive Engineering", jsonElements -> modDesc("Immersive Engineering", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod can both produce power, process ores and generate some resources. It uses big multistructures that all consume power (FE) to run.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Dave's Potioneering", jsonElements -> modDesc("Dave's Potioneering", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("Dave's Potioneering is a mod that adds an upgraded Brewing Stand that's faster and has double potion output, a new Reinforced Cauldron that allows you to coat your weapons or tools with potion effects, and a Potioneer Gauntlet that can store up to 6 potion effects and apply them on attack.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("RFTools Utility", jsonElements -> modDesc("RFTools Utility", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod provides you with some good infrastructural options and most importantly it supports wireless redstone signals using infinite different channels. Very useful for making your contraptions neater and more efficient.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Extra Storage", jsonElements -> modDesc("Extra Storage", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds massive disks for storing all your items and fluids in, as well as enhanced importers, exporters, and crafters for your Refined Storage setup!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Functional Storage", jsonElements -> modDesc("Functional Storage", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("Functional Storage is an alternative take on the storage solution of Storage Drawers, with an updated look and additional functionalities.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Hexcasting", jsonElements -> modDesc("Hexcasting", innerDesc -> {
                    innerDesc.add(disabledInVaults());
                    innerDesc.add(JsonDescription.simple("This mod adds stack-based programmable spellcasting. Complicated mod, but you can do some cool stuff with it!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("LaserIO", jsonElements -> modDesc("LaserIO", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("LaserIO is a useful mod that seeks to replicate the mechanics of EnderIO - allowing you to interact with items, fluids, energy, and redstone - all from the same block face! There is limitless potential for logistics around transportation of resources.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Super Factory Manager", jsonElements -> modDesc("Super Factory Manager", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("SFM is a powerful mod that gives you the Factory Manager block, cables to connect to your manager, and a disk you can program scripts into to control your base!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Sophisticated Storage", jsonElements -> modDesc("Sophisticated Storage", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("The ultimate bulk storage mod with massively upgradeable storage and chests and barrels that can be chained together to create an array of storage.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Cable Tiers", jsonElements -> modDesc("Cable Tiers", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod adds enhanced versions of the importer, exporter, disk manipulation, constructor, destructor, and requester. They come in 3 different tiers and are progressively faster and have more filter slots.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Little Logistics", jsonElements -> modDesc("Little Logistics", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod is focused on efficient, affordable, long-distance transport. It adds vessels and locomotives you can use to move item, fluids, and energy around great distances. Additionally, comes with a faster version of hoppers, and a fluid version of hoppers", "$text"));
                    innerDesc.add(JsonDescription.simple("\\n\\nThe mod also comes with Create support so you can put Create contraptions on top of your barges or train cars!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Cloud Storage", jsonElements -> modDesc("Cloud Storage", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("An easy to use storage solution that adds searchable chests that can hold up to 4096 items after floating some storage up to the cloud!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("RFTools Storage", jsonElements -> modDesc("RFTools Storage", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This is a semi basic digital storage solution, that allows you to search items and store them on disk drives. It is far more limiting than it's similarities, Applied Energistics and Refined Storage, but is cheaper to manage early and mid game.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Integrated Dynamics", jsonElements -> modDesc("Integrated Dynamics", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This is a real bang for your buck buy right here, adding cables that you can move items, fluid, and energy with. A terminal you can connect to your storage solution.", "$text"));
                    innerDesc.add(JsonDescription.simple("and a plethora of other features! This one takes some brain power to make the most use of as you'll need to work with variables and operators to get the most value out of", "$text"));
                    innerDesc.add(JsonDescription.simple(" Integrated Dynamics", "yellow"));
                    innerDesc.add(JsonDescription.simple(".", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Industrial Foregoing", jsonElements -> modDesc("Industrial Foregoing", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("This mod contains a ton of useful blocks, useful for mob farming and crop farming. Comes with it's own power solution and storage for items and fluids. Some features of the mod have been disabled such as the ", "$text"));
                    innerDesc.add(JsonDescription.simple("Mob Duplicator, Infinity Hammer, Nuke, Launcher, and Backpack ", "gold"));
                    innerDesc.add(JsonDescription.simple("have been ", "$text"));
                    innerDesc.add(JsonDescription.simple("disabled", "red"));
                    innerDesc.add(JsonDescription.simple("! ", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Occultism", jsonElements -> {
                    modDesc("Occultism", innerDesc -> {
                        disabledInVaults().forEach(innerDesc::add);
                        innerDesc.add(JsonDescription.simple("Occultism is a mod inspired by the world of Jonathan Stroud's Bartimaeus. \nWith the help of occult rituals you will summon spirits, also known as demons, from \"The Other Place\" to aid you in your adventures.\n\n", "$text"));
                        innerDesc.add(JsonDescription.simple("You can perform rituals to summon Familiars and Demons to help you. Mine random ores using a dimensional mineshaft, and crush ores to massively increase the value of them!", "$text"));
                    }).forEach(jsonElements::add);;
                })

                .addDescription("Toms Simple Storage", jsonElements -> {
                    modDesc("Toms Simple Storage", innerDesc -> {
                        innerDesc.add(JsonDescription.simple("Tom's Simple Storage, is a mod that lets you connect up several inventories and browse their content from a central interface digitally, with a search function.\n\n", "$text"));
                    }).forEach(jsonElements::add);;
                })

                .addDescription("Scanner", jsonElements -> {
                    modDesc("Scannable", innerDesc -> innerDesc.add(JsonDescription.simple("Which offers an FE powered device that can scan for entities, blocks, and in this pack, a special Vault Ores module! Consider it like Hunter in a device!", "$text"))).forEach(jsonElements::add);;
                })

                .addDescription("Oops, All Iron Mods", jsonElements -> {
                    modDesc("Iron Generators, Iron Furnaces, and Iron Chests", "mods", innerDesc -> {
                        innerDesc.add(JsonDescription.simple("Iron Generators are a great entry point to", "$text"));
                        innerDesc.add(JsonDescription.simple("Forge Energy (FE)", "aqua"));
                        innerDesc.add(JsonDescription.simple(" that is required by other mods to run. While Iron Generators is fairly basic, it can certainly be the best choice for early to mid game power generation!", "$text"));
                        innerDesc.add(JsonDescription.simple("\n\nIron Chests is a simple way to expand your storage with some upgraded chests.", "$text"));
                        innerDesc.add(JsonDescription.simple("\n\nIron Furnaces provides you with upgraded furnaces, making your smelting processing much quicker.", "$text"));
                    }).forEach(jsonElements::add);;
                })

                .addDescription("PneumaticCraft", jsonElements -> {
                    modDesc("PneumaticCraft", innerDesc -> {
                        innerDesc.add(JsonDescription.simple("PneumaticCraft is a tech mod that is based around air pressure and heat. You can use PneumaticCraft as your mob farm system, plant harvesting, item transferring, and more!", "$text"));
                        innerDesc.add(JsonDescription.simple("It also has some useful tools and armor, although some of the modules have been disabled!", "$text"));
                    }).forEach(jsonElements::add);;
                })

                .addDescription("Time in a Bottle", jsonElements -> {
                   modDesc("Time in a Bottle", innerDesc -> {
                        disabledInVaults().forEach(innerDesc::add);
                        innerDesc.add(JsonDescription.simple("This mod adds an amazing item called the Time in a Bottle that stores time as you play, then you can right-click on a block to speed up its tick time using your stored time! Nifty!\n\n", "$text"));
                        innerDesc.add(JsonDescription.simple("Also unlocks the ", "$text"));
                        innerDesc.add(JsonDescription.simple("Weather Control ", "yellow"));
                        innerDesc.add(JsonDescription.simple("mod! ", "$text"));
                        innerDesc.add(JsonDescription.simple("Which lets you control the time of day and the weather!", "$text"));
                    }).forEach(jsonElements::add);
                })

                .addDescription("Mega Cells", jsonElements -> modDesc("Mega Cells", innerDesc -> innerDesc.add(JsonDescription.simple("This mod adds massive item and fluid cells, crafting storages and units, as well as adding Chemical and Radioactive Chemical cells for Mekanism!", "$text"))).forEach(jsonElements::add))

                .addDescription("Misc Bag Upgrades", jsonElements -> {
                    modDesc("Everlasting, Tool Swapper, Tank, Pump, XP Pump, and Battery Upgrade", "from Sophisticated Backpacks", innerDesc -> {
                        innerDesc.add(JsonDescription.simple("These will come in handy for utilizing your bag in more non-conventional ways... Note: the Tool Swapper upgrade functionality is disabled while in a vault.", "$text"));
                    }).forEach(jsonElements::add);;
                })

                .addDescription("Bag Inception", jsonElements -> modDesc("Inception Upgrade", "from Sophisticated Backpacks", innerDesc -> innerDesc.add(JsonDescription.simple("With your mastery of looting, your accumulation of knowledge, and insane willpower, you finally have the brilliant idea, wondering why it hasn't hit you all this time, to simply put your backpacks inside of other backpacks. A peculiar thought...", "$text"))).forEach(jsonElements::add))

                .addDescription("QuarryPlus", jsonElements -> modDesc("QuarryPlus", innerDesc -> {
                    disabledInVaults().forEach(innerDesc::add);
                    innerDesc.add(JsonDescription.simple("After mastering mining, you can create the ultimate mining machines with the QuarryPlus mod! This adds Quarries that work much like a Buildcraft Quarry and can be enchanted to enhance them. You can also create the coveted Chunk Destroyer that can flatten an entire chunk!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Mystical Agriculture", jsonElements -> modDesc("Mystical Agriculture", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("After mastering the botanical arts, you can now harness the power of mystically infused seeds with this mod. The end-all-be-all production mod, you can create seeds for almost any resource!\n\n", "$text"));
                    innerDesc.add(JsonDescription.simple("Items such as tools and armor are disabled!", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Hostile Neural Networks", jsonElements -> modDesc("Hostile Neural Networks", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("Hostile Neural Networks is a server-friendly mob looting system. Use a Data Model on a mob to make it's Model, then evolve your Data Model using Spawn Eggs of the corresponding type. Very powerful mod!\n\n", "$text"));
                    innerDesc.add(JsonDescription.simple("Note, HNN works differently in this pack! Your models won't upgrade in Simulation Chambers or gain data from kills, you need to evolve them using spawn eggs (Check JEI!)! Also needs power to work.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Psi", jsonElements -> modDesc("Psi", innerDesc -> {
                    disabledInVaults().forEach(innerDesc::add);
                    innerDesc.add(JsonDescription.simple("Psi is a magical tech spell programming mod inspired by Mahouka Kokou no Rettousei. Don't worry if that doesn't make any sense. The core of Psi is a system where you can create action sequences (\\\"Spells\\\") to be executed in the world. This system is followed by a leveling system containing ingame documentation and tutorials of the various concepts and pieces you'll get. The mod makes use of basic mathematical concepts, such as Vectors in its systems, which allows for great flexibility on what spells can do.", "$text"));
                }).forEach(jsonElements::add))

                .addDescription("Draconic Evolution", jsonElements -> modDesc("Draconic Evolution", innerDesc -> {
                     disabledInVaults().forEach(innerDesc::add);
                     innerDesc.add(JsonDescription.simple("Harness immense power with advanced tech, massive energy storage, and endgame gear. Build powerful reactors, upgrade your equipment beyond your limits!", "$text"));
                }).forEach(jsonElements::add))
                .build());
    }

    public SkillDescriptionsConfig getBuiltInPrestigePowers() {
        return new Builder()
                .addDescription("WeaverOfTime", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("Cooldown Reduction ", "#F2CC8C"));
                    jsonElements.add(JsonDescription.simple("Cap."));
                })
                .addDescription("ShieldOfLastingGuard", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("Block Chance ", "#00F5B2"));
                    jsonElements.add(JsonDescription.simple("Cap."));
                })
                .addDescription("BarrierOfResilience", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("Resistance ", "#FEDD00"));
                    jsonElements.add(JsonDescription.simple("Cap."));
                })
                .addDescription("SpiritsHand", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("Reach ", "#82D4FC"));
                    jsonElements.add(JsonDescription.simple("Cap inside vaults."));
                })
                .addDescription("PrismaticPouch", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Unlocks the "));
                    jsonElements.add(JsonDescription.simple("Prismatic Trinket Pouch ", "#82D4FC"));
                    jsonElements.add(JsonDescription.simple("for crafting inside the "));
                    jsonElements.add(JsonDescription.simple("Weaving Station", "aqua"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Toolsmith", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("When crafting "));
                    jsonElements.add(JsonDescription.simple("Vault Tools ", "#82D4FC"));
                    jsonElements.add(JsonDescription.simple("they gain "));
                    jsonElements.add(JsonDescription.simple("75 ", "yellow"));
                    jsonElements.add(JsonDescription.simple("additional capacity."));
                })
                .addDescription("Artificer", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("When you identify "));
                    jsonElements.add(JsonDescription.simple("Vault Gear ", "#82D4FC"));
                    jsonElements.add(JsonDescription.simple("it gains "));
                    jsonElements.add(JsonDescription.simple("100 ", "yellow"));
                    jsonElements.add(JsonDescription.simple("additional "));
                    jsonElements.add(JsonDescription.simple("Crafting Potential", "gold"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("SoulFetcher", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("Soul Shard Chance", "#6D02FB"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("HealthIncrease", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("Health ", "#008700"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("MythicBounty", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Bounties now always roll as "));
                    jsonElements.add(JsonDescription.simple("Legendary bounties", "gold"));
                    jsonElements.add(JsonDescription.simple(".\n\n"));
                    jsonElements.add(JsonDescription.simple("Lost Bounties ", "aqua"));
                    jsonElements.add(JsonDescription.simple("will now roll a "));
                    jsonElements.add(JsonDescription.simple("Mythic bounty", "light_purple"));
                    jsonElements.add(JsonDescription.simple(" with a"));
                    jsonElements.add(JsonDescription.simple(" unique ", "yellow"));
                    jsonElements.add(JsonDescription.simple("reward pool"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .build();
    }

    public SkillDescriptionsConfig getExpertises() {
        return new Builder()
                .addDescription("Companion_Loyalty", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Decreases the "));
                    jsonElements.add(JsonDescription.simple("Cooldown ", "#008080"));
                    jsonElements.add(JsonDescription.simple("of your "));
                    jsonElements.add(JsonDescription.simple("Companions", "#FFFF55"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Grave_Insurance", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("You make a deal with the Reaper and he looks the other way when you slip a few less coins than usual...\nDecreases the cost of your "));
                    jsonElements.add(JsonDescription.simple("Spirit ", "#875FB2"));
                    jsonElements.add(JsonDescription.simple("in the "));
                    jsonElements.add(JsonDescription.simple("Spirit Extractor", "#FFFF55"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Augmentation_Luck", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Improves your luck with rolling special gear modifiers like "));
                    jsonElements.add(JsonDescription.simple("Corrupted", "#A31500"));
                    jsonElements.add(JsonDescription.simple(", "));
                    jsonElements.add(JsonDescription.simple("Frozen", "#0392C4"));
                    jsonElements.add(JsonDescription.simple(", "));
                    jsonElements.add(JsonDescription.simple("Greater", "#039208"));
                    jsonElements.add(JsonDescription.simple(", and "));
                    jsonElements.add(JsonDescription.simple("Unusual", "#039261"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Blessed", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("You become closer to the gods!\n"));
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("affinity rating", "#B2F44B"));
                    jsonElements.add(JsonDescription.simple("for all vault gods, making them more likely to give you a reputation point when completing a god altar."));
                })
                .addDescription("Lucky_Altar", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to have the altar roll Lucky when crafting a crystal, automatically completing the Altar's recipe."));
                })
                .addDescription("Bartering", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain a "));
                    jsonElements.add(JsonDescription.simple("cost reduction ", "yellow"));
                    jsonElements.add(JsonDescription.simple("on items sold in shopping pedestals inside a vault."));
                })
                .addDescription("Artisan", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Reduce the cost of "));
                    jsonElements.add(JsonDescription.simple("Crafting Potential ", "aqua"));
                    jsonElements.add(JsonDescription.simple("by a "));
                    jsonElements.add(JsonDescription.simple("percentage ", "yellow"));
                    jsonElements.add(JsonDescription.simple("when modifying gear in the Artisan Station. "));
                })
                .addDescription("Bounty_Hunter", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Become a master at Bounty Hunting.\n\n"));
                    jsonElements.add(JsonDescription.simple("1 "));
                    jsonElements.add(JsonDescription.simple("Reduce the "));
                    jsonElements.add(JsonDescription.simple("Bounty wait period ", "#FCF5C5"));
                    jsonElements.add(JsonDescription.simple("to "));
                    jsonElements.add(JsonDescription.simple("30 minutes", "#CBE6FE"));
                    jsonElements.add(JsonDescription.simple(".\n"));
                    jsonElements.add(JsonDescription.simple("2 "));
                    jsonElements.add(JsonDescription.simple("Reduce the "));
                    jsonElements.add(JsonDescription.simple("abandon penalty ", "#FCF5C5"));
                    jsonElements.add(JsonDescription.simple("to "));
                    jsonElements.add(JsonDescription.simple("30 minutes", "#CBE6FE"));
                    jsonElements.add(JsonDescription.simple(".\n"));
                    jsonElements.add(JsonDescription.simple("3 "));
                    jsonElements.add(JsonDescription.simple("Have "));
                    jsonElements.add(JsonDescription.simple("2 active bounties ", "#FFEB07"));
                    jsonElements.add(JsonDescription.simple("at a time"));
                    jsonElements.add(JsonDescription.simple(".\n"));
                    jsonElements.add(JsonDescription.simple("4 "));
                    jsonElements.add(JsonDescription.simple("Have "));
                    jsonElements.add(JsonDescription.simple("3 active bounties ", "#FFEB07"));
                    jsonElements.add(JsonDescription.simple("at a time"));
                    jsonElements.add(JsonDescription.simple(".\n"));
                })
                .addDescription("Unbreakable", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the cap for "));
                    jsonElements.add(JsonDescription.simple("Durability Damage Reduction", "#69D68F"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("ShopReroll", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Rough up those Vendor Pedestals and get them to give you the offers you really want!\n\n "));
                    jsonElements.add(JsonDescription.simple("Grants the ability to reroll Shop Pedestal offers (once per pedestal) by sneaking and right-clicking on it."));
                    jsonElements.add(JsonDescription.simple("There is a cooldown in between rerolls."));
                })
                .addDescription("Divine", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("You become closer to the gods!\n\n"));
                    jsonElements.add(JsonDescription.simple("Gives you a chance to earn "));
                    jsonElements.add(JsonDescription.simple("2 reputation points ", "#B2F44B"));
                    jsonElements.add(JsonDescription.simple("when completing a God Altar."));
                })
                .addDescription("Pylon_Pilferer", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("You become better at breaking off beautiful Temporal Relics from Pylons\n\n"));
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("for a Temporal Relic to drop when breaking Pylons."));
                })
                .addDescription("Trinketer", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to not consume a use of your Trinkets, Vault Necklaces, and Vault God Charms when entering a vault."));
                })
                .addDescription("Mystic", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain extra "));
                    jsonElements.add(JsonDescription.simple("capacity ", "yellow"));
                    jsonElements.add(JsonDescription.simple("on "));
                    jsonElements.add(JsonDescription.simple("Vault Crystals ", "aqua"));
                    jsonElements.add(JsonDescription.simple("that you craft, allowing you to modify them further."));
                })
                .addDescription("Surprise_Favors", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to add additional random positive "));
                    jsonElements.add(JsonDescription.simple("Vault Modifiers ", "#B90061"));
                    jsonElements.add(JsonDescription.simple("when entering a Vault."));
                })
                .addDescription("Craftsman", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Become an expert Vault Gear craftsman and enhance the capabilities and versatility of your crafted gear.\n\n "));
                    jsonElements.add(JsonDescription.simple("1 "));
                    jsonElements.add(JsonDescription.simple("Crafted Gear can roll "));
                    jsonElements.add(JsonDescription.simple("Legendary Modifiers ", "gold"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Gambler", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Just one more choice and it will solve everything... just one more!\n\n"));
                    jsonElements.add(JsonDescription.simple("1 "));
                    jsonElements.add(JsonDescription.simple("Add an additional "));
                    jsonElements.add(JsonDescription.simple("choice ", "gold"));
                    jsonElements.add(JsonDescription.simple("when opening "));
                    jsonElements.add(JsonDescription.simple("Booster Packs", "green"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Deck_Master", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Become a master at handling decks!\n\n"));
                    jsonElements.add(JsonDescription.simple("1 "));
                    jsonElements.add(JsonDescription.simple("Add a random "));
                    jsonElements.add(JsonDescription.simple("Lesser ", "yellow"));
                    jsonElements.add(JsonDescription.simple("deck modifier and an additional deck socket "));
                    jsonElements.add(JsonDescription.simple("when crafting "));
                    jsonElements.add(JsonDescription.simple("Card Decks ", "gold"));
                    jsonElements.add(JsonDescription.simple("in the "));
                    jsonElements.add(JsonDescription.simple("Deck Station ", "aqua"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Infuser", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("When applying an "));
                    jsonElements.add(JsonDescription.simple("Infused Vault Catalyst ", "#B90061"));
                    jsonElements.add(JsonDescription.simple("to a "));
                    jsonElements.add(JsonDescription.simple("Vault Crystal ", "aqua"));
                    jsonElements.add(JsonDescription.simple(", there is a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to not apply a negative modifier."));
                })
                .addDescription("Experienced", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increase the "));
                    jsonElements.add(JsonDescription.simple("value ", "#D9FF00"));
                    jsonElements.add(JsonDescription.simple("of "));
                    jsonElements.add(JsonDescription.simple("Vault Experience ", "#C1B72A"));
                    jsonElements.add(JsonDescription.simple("you gain from all sources."));
                })
                .addDescription("Fortunate", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain passive "));
                    jsonElements.add(JsonDescription.simple("Fortune ", "#8BD0C9"));
                    jsonElements.add(JsonDescription.simple("levels that are applied whenever you use a tool enchanted with "));
                    jsonElements.add(JsonDescription.simple("Fortune ", "#8BD0C9"));
                    jsonElements.add(JsonDescription.simple("to break blocks."));
                })
                .addDescription("Jeweler", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Allows the ability to craft random jewels in the Jewel Crafting Station using gemstones and other materials. Adds an additional jewel choice when opening Jewel Pouches."));
                })
                .addDescription("Marketer", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Unlock an additional "));
                    jsonElements.add(JsonDescription.simple("Black Market ", "#4800FF"));
                    jsonElements.add(JsonDescription.simple("slot per level. "));
                })
                .addDescription("Fortuitous_Finesse", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of a gear piece getting a "));
                    jsonElements.add(JsonDescription.simple("Legendary Modifier ", "gold"));
                    jsonElements.add(JsonDescription.simple("when being identified. "));
                })



                .build();
    }

    public SkillDescriptionsConfig getOverridePrestigeDescriptions() {
        return new Builder()
                .addDescription("BlackMarketRerolls", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Adds a  "));
                    jsonElements.add(JsonDescription.simple("25% ", "#8000A8"));
                    jsonElements.add(JsonDescription.simple("chance to consume no "));
                    jsonElements.add(JsonDescription.simple("Soul Ichor ", "#8000A8"));
                    jsonElements.add(JsonDescription.simple("when rerolling."));
                })
                .addDescription("PickupRange", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("The effect of "));
                    jsonElements.add(JsonDescription.simple("Copiously ", "#f4467e"));
                    jsonElements.add(JsonDescription.simple("on your "));
                    jsonElements.add(JsonDescription.simple("Magnet ", "aqua"));
                    jsonElements.add(JsonDescription.simple("is "));
                    jsonElements.add(JsonDescription.simple("doubled", "yellow"));
                    jsonElements.add(JsonDescription.simple(".\n\n"));
                    jsonElements.add(JsonDescription.simple("Additionally, your "));
                    jsonElements.add(JsonDescription.simple("Magnet ", "aqua"));
                    jsonElements.add(JsonDescription.simple("gains additional "));
                    jsonElements.add(JsonDescription.simple("Pickup Range ", "yellow"));
                    jsonElements.add(JsonDescription.simple(". "));
                })
                .addDescription("PickupRange", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("The effect of "));
                    jsonElements.add(JsonDescription.simple("Copiously", "#f4467e"));
                    jsonElements.add(JsonDescription.simple("on your "));
                    jsonElements.add(JsonDescription.simple("Magnet ", "aqua"));
                    jsonElements.add(JsonDescription.simple("is "));
                    jsonElements.add(JsonDescription.simple("doubled", "yellow"));
                    jsonElements.add(JsonDescription.simple(".\n\n"));
                    jsonElements.add(JsonDescription.simple("Additionally, your "));
                    jsonElements.add(JsonDescription.simple("Magnet ", "aqua"));
                    jsonElements.add(JsonDescription.simple("gains additional "));
                    jsonElements.add(JsonDescription.simple("Pickup Range ", "yellow"));
                })
                .addDescription("Shielded", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Grants you an "));
                    jsonElements.add(JsonDescription.simple("Absorption shield ", "#FFD700"));
                    jsonElements.add(JsonDescription.simple("when entering a vault, equal to a percentage of your "));
                    jsonElements.add(JsonDescription.simple("max health. ", "green"));
                })
                .addDescription("ChampionsDamage", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Doubles the loot dropped by "));
                    jsonElements.add(JsonDescription.simple("Champions", "#f4467e"));
                    jsonElements.add(JsonDescription.simple(".\n\n"));
                    jsonElements.add(JsonDescription.simple("Additionally, increases all your "));
                    jsonElements.add(JsonDescription.simple("damage dealt ", "#B22222"));
                    jsonElements.add(JsonDescription.simple("to "));
                    jsonElements.add(JsonDescription.simple("Champions", "#f4467e"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Fruity", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the time granted by any "));
                    jsonElements.add(JsonDescription.simple("Vault Fruit", "#FFD700"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("SuperCrystals", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the capacity of any "));
                    jsonElements.add(JsonDescription.simple("Vault Crystals", "aqua"));
                    jsonElements.add(JsonDescription.simple("you craft, allowing you to modify them further."));
                })
                .addDescription("TemporalShardChance", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of finding ", "yellow"));
                    jsonElements.add(JsonDescription.simple("Temporal Relics ", "#00FFFF"));
                    jsonElements.add(JsonDescription.simple("when breaking "));
                    jsonElements.add(JsonDescription.simple("Pylons", "#FFD700"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("OriginalWardPower", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("While not taking or dealing damage for a certain "));
                    jsonElements.add(JsonDescription.simple("duration ", "#FFD700"));
                    jsonElements.add(JsonDescription.simple("you will generate an "));
                    jsonElements.add(JsonDescription.simple("Absorption shield ", "#FFD700"));
                    jsonElements.add(JsonDescription.simple("equal to your "));
                    jsonElements.add(JsonDescription.simple("max health", "#32CD32"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("TreasureHunterPower", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Looting a chest increases your "));
                    jsonElements.add(JsonDescription.simple("Item Quantity ", "#FFA500"));
                    jsonElements.add(JsonDescription.simple("and "));
                    jsonElements.add(JsonDescription.simple("Item Rarity", "#FFD700"));
                    jsonElements.add(JsonDescription.simple(".\n\n"));
                    jsonElements.add(JsonDescription.simple("This effect can stack up to 20 times and wears off while not looting a chest or taking damage."));
                })
                .addDescription("CommanderPower", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the duration of your "));
                    jsonElements.add(JsonDescription.simple("Companion's ", "#FFD700"));
                    jsonElements.add(JsonDescription.simple(" "));
                    jsonElements.add(JsonDescription.simple("Temporal Modifier", "#00FFFF"));
                })
                .build();
    }

    public SkillDescriptionsConfig getTalentDescriptions() {
        return new Builder()
                .addDescription("Intelligence", jsonElements -> {
                   jsonElements.add(JsonDescription.simple("Increase your "));
                   jsonElements.add(JsonDescription.simple("Ability Power ", "#FF00CB"));
                })
                .addDescription("Haste", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("mining speed ", "#47B8F5"));
                    jsonElements.add(JsonDescription.simple("by applying the "));
                    jsonElements.add(JsonDescription.simple("Haste ", "green"));
                    jsonElements.add(JsonDescription.simple("effect permanently\n\n"));
                    jsonElements.add(JsonDescription.simple("Every level of the Haste effect increases mining speed by "));
                    jsonElements.add(JsonDescription.simple("20%", "#47B8F5"));
                })
                .addDescription("Speed", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("movement speed ", "#F6CD0E"));
                    jsonElements.add(JsonDescription.simple("by applying the "));
                    jsonElements.add(JsonDescription.simple("Speed ", "green"));
                    jsonElements.add(JsonDescription.simple("effect permanently\n\n"));
                    jsonElements.add(JsonDescription.simple("Every level of the Speed effect increases movement speed by "));
                    jsonElements.add(JsonDescription.simple("20%", "#F6CD0E"));
                })
                .addDescription("Strength", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("attack damage ", "#F6CD0E"));
                    jsonElements.add(JsonDescription.simple("by applying the "));
                    jsonElements.add(JsonDescription.simple("Strength ", "green"));
                    jsonElements.add(JsonDescription.simple("effect permanently\n\n"));
                    jsonElements.add(JsonDescription.simple("Every level of the Strength effect increases attack damage by "));
                    jsonElements.add(JsonDescription.simple("+3", "#F6CD0E"));
                })
                .addDescription("Purist", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Deal "));
                    jsonElements.add(JsonDescription.simple("extra damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("based on the amount of "));
                    jsonElements.add(JsonDescription.simple("Scrappy ", "343434"));
                    jsonElements.add(JsonDescription.simple("armor pieces you wear."));
                })
                .addDescription("Nether_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Dungeon mobs.\nThe total can be seen in your statistics tab under Dungeon Damage. "));
                })
                .addDescription("Undead_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Horde mobs.\nThe total can be seen in your statistics tab under Horde Damage. "));
                })
                .addDescription("Arthropod_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Assassin mobs.\nThe total can be seen in your statistics tab under Assassin Damage. "));
                })
                .addDescription("Illager_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Vault Dwellers mobs.\nThe total can be seen in your statistics tab under Dweller Damage. "));
                })
                .addDescription("Champion_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Champion mobs.\nThe total can be seen in your statistics tab under Champion Damage. "));
                })
                .addDescription("Tank_Mastery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C21A00"));
                    jsonElements.add(JsonDescription.simple("dealt when fighting Tank mobs.\nThe total can be seen in your statistics tab under Tank Damage. "));
                })
                .addDescription("Last_Stand", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra resistance ", "#09BFB8"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% ", "#7DF587"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Berserking", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% ", "#7DF587"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Methodical", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra healing efficiency ", "#7DF587"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% mana ", "#0353D7"));
                })
                .addDescription("Sorcery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra mana regeneration ", "#0353D7"));
                    jsonElements.add(JsonDescription.simple("while above "));
                    jsonElements.add(JsonDescription.simple("80% ", "#0353D7"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Prime_Amplification", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("increased area of effect ", "#E9C375"));
                    jsonElements.add(JsonDescription.simple("while above "));
                    jsonElements.add(JsonDescription.simple("80% ", "#0353D7"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Bountiful_Harvest", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra item quantity ", "#ffaa3d"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% ", "#7DF587"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Lightning_Damage", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("minimum damage ", "#FF00CB"));
                    jsonElements.add(JsonDescription.simple("by a percentage when using Lightning Abilities."));
                })
                .addDescription("Lightning_Stun", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gives you a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to "));
                    jsonElements.add(JsonDescription.simple("stun ", "#19A6E4"));
                    jsonElements.add(JsonDescription.simple("enemies hit by Lightning Abilities."));
                })
                .addDescription("Treasure_Seeker", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("extra item rarity ", "#EAFF00"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% ", "#7DF587"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Stoneskin", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("increased knockback resistance ", "#E9C375"));
                    jsonElements.add(JsonDescription.simple("while above "));
                    jsonElements.add(JsonDescription.simple("80% ", "#0353D7"));
                    jsonElements.add(JsonDescription.simple("max health."));
                })
                .addDescription("Witchery", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("increased soul chance ", "#4800FF"));
                    jsonElements.add(JsonDescription.simple("while above "));
                    jsonElements.add(JsonDescription.simple("80% max mana", "#0353D7"));
                })
                .addDescription("Depleted", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain "));
                    jsonElements.add(JsonDescription.simple("increased damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("while below "));
                    jsonElements.add(JsonDescription.simple("20% max mana", "#0353D7"));
                })
                .addDescription("Fatal_Strike", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Adds Fatal Strike to your "));
                    jsonElements.add(JsonDescription.simple("Lucky Hits ", "#6DF5A3"));
                    jsonElements.add(JsonDescription.simple("causing you to deal "));
                    jsonElements.add(JsonDescription.simple("increased damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("with your hit. "));
                })
                .addDescription("Mana_Steal", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Adds Mana Steal to your "));
                    jsonElements.add(JsonDescription.simple("Lucky Hits ", "#6DF5A3"));
                    jsonElements.add(JsonDescription.simple("causing you to gain back part of your "));
                    jsonElements.add(JsonDescription.simple("max mana ", "#0353D7"));
                    jsonElements.add(JsonDescription.simple("with your hit. "));
                })
                .addDescription("Life_Steal", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Adds Life Steal to your "));
                    jsonElements.add(JsonDescription.simple("Lucky Hits ", "#6DF5A3"));
                    jsonElements.add(JsonDescription.simple("causing you to gain back part of your "));
                    jsonElements.add(JsonDescription.simple("max health ", "#CF0000"));
                    jsonElements.add(JsonDescription.simple("with your hit, as long as your hit deals the same amount of damage, percentage wise, to the target's total health."));
                })
                .addDescription("Cleave", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Adds Cleave to your "));
                    jsonElements.add(JsonDescription.simple("Lucky Hits ", "#6DF5A3"));
                    jsonElements.add(JsonDescription.simple(", causing you to deal "));
                    jsonElements.add(JsonDescription.simple("part of your damage", "#CF0000"));
                    jsonElements.add(JsonDescription.simple("in a 5 by 5 area around the target."));
                })
                .addDescription("Prudent", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gives you a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple(" to not consume a potion charge when drinking any "));
                    jsonElements.add(JsonDescription.simple("Vault Potion", "aqua"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Blizzard", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Hitting a "));
                    jsonElements.add(JsonDescription.simple("chilled ", "#2FE1FA"));
                    jsonElements.add(JsonDescription.simple("mob has a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of causing a "));
                    jsonElements.add(JsonDescription.simple("Frost Nova", "#23D2BB"));
                    jsonElements.add(JsonDescription.simple("to be cast around the target. \nThe Frost Nova scales in"));
                    jsonElements.add(JsonDescription.simple("level ", "#c1579d"));
                    jsonElements.add(JsonDescription.simple("with this talent and does not require you to have the ability learnt."));
                })
                .addDescription("Frostbite", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Hitting a "));
                    jsonElements.add(JsonDescription.simple("chilled ", "#2FE1FA"));
                    jsonElements.add(JsonDescription.simple("mob has a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of causing them to get "));
                    jsonElements.add(JsonDescription.simple("Frostbitten ", "#23D2BB"));
                    jsonElements.add(JsonDescription.simple("for "));
                    jsonElements.add(JsonDescription.simple("3 seconds ", "#7024AC"));
                    jsonElements.add(JsonDescription.simple(", and if hit again they will shatter and die instantly, regardless of their health."));
                })
                .addDescription("Daze", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Deal increased "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("to stunned enemies on hit."));
                })
                .addDescription("Nucleus", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Killing a "));
                    jsonElements.add(JsonDescription.simple("stunned ", "#2FE1FA"));
                    jsonElements.add(JsonDescription.simple("mob has a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of causing an explosive reaction, casting a Nova of your level around the killed mob."));
                    jsonElements.add(JsonDescription.simple(" This talent requires levels in the ability Nova."));
                })
                .addDescription("Blight", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Apply "));
                    jsonElements.add(JsonDescription.simple("Weakness ", "#AFB477"));
                    jsonElements.add(JsonDescription.simple("to your targets while hitting them, lowering their "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("for a "));
                    jsonElements.add(JsonDescription.simple("duration ", "#7024AC"));
                    jsonElements.add(JsonDescription.simple("of time, if they are affected by "));
                    jsonElements.add(JsonDescription.simple("Poison ", "#6fe95a"));
                })
                .addDescription("Hunters_Instinct", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Killing a mob grants a chance of having their death cause your "));
                    jsonElements.add(JsonDescription.simple("Hunter senses ", "#FCF5C5"));
                    jsonElements.add(JsonDescription.simple("to trigger, revealing all POI's in a radius around the killed mob. "));
                    jsonElements.add(JsonDescription.simple("This talent requires a level in "));
                    jsonElements.add(JsonDescription.simple("Hunter", "#FCF5C5"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Arcana", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain 20% "));
                    jsonElements.add(JsonDescription.simple("Mana Regeneration ", "#00FFFF"));
                    jsonElements.add(JsonDescription.simple("when killing a mob. This effect can "));
                    jsonElements.add(JsonDescription.simple("stack ", "#ffeb07"));
                    jsonElements.add(JsonDescription.simple("and lasts for a duration of time."));
                })
                .addDescription("Blazing", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain 5% "));
                    jsonElements.add(JsonDescription.simple("Movement Speed ", "#FFE068"));
                    jsonElements.add(JsonDescription.simple("when killing a mob. This effect can "));
                    jsonElements.add(JsonDescription.simple("stack ", "#ffeb07"));
                    jsonElements.add(JsonDescription.simple("and lasts for a duration of time."));
                })
                .addDescription("Lucky_Momentum", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain 1% "));
                    jsonElements.add(JsonDescription.simple("Lucky Hit ", "#6DF5A3"));
                    jsonElements.add(JsonDescription.simple("when killing a mob. This effect can "));
                    jsonElements.add(JsonDescription.simple("stack ", "#ffeb07"));
                    jsonElements.add(JsonDescription.simple("and lasts for a duration of time."));
                })
                .addDescription("Frenzy", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gain 2.5% "));
                    jsonElements.add(JsonDescription.simple("Attack Speed ", "#FFE068"));
                    jsonElements.add(JsonDescription.simple("when killing a mob. This effect can "));
                    jsonElements.add(JsonDescription.simple("stack ", "#ffeb07"));
                    jsonElements.add(JsonDescription.simple("and lasts for a duration of time."));
                })
                .addDescription("Toxic_Reaction", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Killing a "));
                    jsonElements.add(JsonDescription.simple("poisoned ", "#6fe95a"));
                    jsonElements.add(JsonDescription.simple("mob has a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("of casting a "));
                    jsonElements.add(JsonDescription.simple("Poison Nova", "#6fe95a"));
                    jsonElements.add(JsonDescription.simple(", of your level, when the mob dies. This effect can chain indefinitely, but requires you to have levels in"));
                    jsonElements.add(JsonDescription.simple("Poison Nova", "#6fe95a"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Javelin_Throw_Power", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Enhances the "));
                    jsonElements.add(JsonDescription.simple("projectile speed ", "#F6CD0E"));
                    jsonElements.add(JsonDescription.simple("of your "));
                    jsonElements.add(JsonDescription.simple("Javelins", "#FFE068"));
                })
                .addDescription("Javelin_Damage", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Enhances the "));
                    jsonElements.add(JsonDescription.simple("damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("of your "));
                    jsonElements.add(JsonDescription.simple("Javelins", "#FFE068"));
                })
                .addDescription("Javelin_Conduct", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gives your "));
                    jsonElements.add(JsonDescription.simple("Javelins ", "#FFE068"));
                    jsonElements.add(JsonDescription.simple("Conducting Power ", "#DF750E"));
                    jsonElements.add(JsonDescription.simple(", making it able to transfer any on-hit effect, except Lucky Hit, to the throw. On-hit effects include "));
                    jsonElements.add(JsonDescription.simple("Clouds", "#DF0EC4"));
                    jsonElements.add(JsonDescription.simple(", "));
                    jsonElements.add(JsonDescription.simple("Stunning ", "#19A6E4"));
                    jsonElements.add(JsonDescription.simple("and "));
                    jsonElements.add(JsonDescription.simple("Shocking", "#FFFB81"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .addDescription("Javelin_Frugal", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Gives your "));
                    jsonElements.add(JsonDescription.simple("Javelins ", "#FFE068"));
                    jsonElements.add(JsonDescription.simple("a "));
                    jsonElements.add(JsonDescription.simple("chance ", "yellow"));
                    jsonElements.add(JsonDescription.simple("to consume no "));
                    jsonElements.add(JsonDescription.simple("mana", "#0353D7"));
                    jsonElements.add(JsonDescription.simple(" for the throw."));
                })
                .addDescription("Farmer_Twerker", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Allows you to hold down shift to increase growth speed of nearby crops and animals. Works on most planted crops, as well as Cactus, Melon, Pumpkin, Sugar Cane and Nether Wart.\n\nLevels increase "));
                    jsonElements.add(JsonDescription.simple("Radius ", "#33ff6d"));
                    jsonElements.add(JsonDescription.simple("and "));
                    jsonElements.add(JsonDescription.simple("Growth Speed ", "#f6cd0e"));
                    jsonElements.add(JsonDescription.simple("."));
                })
                .build();
    }

    public JsonArray modDescOpening(String modName, String modTextOverride) {
        JsonArray descriptions = new JsonArray();
        descriptions.add(JsonDescription.simple("Unlocks the ", "$text"));
        descriptions.add(JsonDescription.simple(modName, "$yellow"));
        descriptions.add(JsonDescription.simple(" " + modTextOverride + "!\n\n", "$text"));
        return descriptions;
    }

    public JsonArray disabledInVaults() {
        JsonArray descriptions = new JsonArray();
        descriptions.add(JsonDescription.simple("⚠", "red"));
        descriptions.add(JsonDescription.simple("Disabled In Vaults!\n\n", "dark_red"));
        return descriptions;
    }

    public JsonObject grantsTransmog() {
        return JsonDescription.simple("Grants Transmog!\n", "#90FF00");
    }

    public JsonArray modDescClosing(String modName) {
        if(!ModConfigs.MOD_BOX.POOL.containsKey(modName)) {
            return null;
        }

        return modBox();
    }

    public JsonArray modBox() {
        JsonArray descriptions = new JsonArray();
        descriptions.add(JsonDescription.simple("\n\n", "$text"));
        descriptions.add(JsonDescription.simple("Adds rewards to ", "$text"));
        descriptions.add(JsonDescription.simple("Mod Boxes ", "aqua"));
        descriptions.add(JsonDescription.simple("!", "$text"));
        return descriptions;
    }

    public JsonArray modDesc(String modName, String modTextOverride, Consumer<JsonArray> innerDescConsumer) {
        JsonArray jsonArray = new JsonArray();
        modDescOpening(modName, modTextOverride).forEach(jsonArray::add);
        innerDescConsumer.accept(jsonArray);
        JsonArray closing = modDescClosing(modName);
        if(closing != null) {
            closing.forEach(jsonArray::add);
        }
        return jsonArray;
    }

    public JsonArray modDesc(String modName, Consumer<JsonArray> innerDescConsumer) {
        return modDesc(modName, "mod", innerDescConsumer);
    }

}
