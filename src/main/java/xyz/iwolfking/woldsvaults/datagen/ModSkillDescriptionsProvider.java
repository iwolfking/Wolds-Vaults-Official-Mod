package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.config.ModBoxConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.config.TalentsConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.talent.type.EffectTalent;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import iskallia.vault.skill.talent.type.PuristTalent;
import iskallia.vault.skill.talent.type.VanillaAttributeTalent;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.vhapi.mixin.accessors.SkillDescriptionsConfigAccessor;
import xyz.iwolfking.vhapi.mixin.accessors.TieredSkillAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VanillaAttributeTalentAccessor;

import java.util.function.Consumer;

public class ModSkillDescriptionsProvider extends AbstractSkillDescriptionsProvider {
    public ModSkillDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    public static JsonArray toJsonArray(Component component) {
        JsonArray array = new JsonArray();
        collectParts(component, array);
        return array;
    }

    private static void collectParts(Component component, JsonArray array) {

        String ownText = component.getContents();

        if (!ownText.isEmpty()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("text", ownText);

            TextColor color = component.getStyle().getColor();
            if (color != null) {
                obj.addProperty("color", color.serialize());
            }

            array.add(obj);
        }

        for (Component child : component.getSiblings()) {
            collectParts(child, array);
        }
    }



    @Override
    public void registerConfigs() {
        ModConfigs.MOD_BOX = new ModBoxConfig().readConfig();
        ModConfigs.TALENTS = new TalentsConfig().readConfig();
        SkillDescriptionsConfig talentsDescriptions = getTalentDescriptions();
        add("talent_overlevels", builder -> {
            ModConfigs.TALENTS.tree.skills.forEach(skill -> {
                builder.addDescription(skill.getId(), jsonElements -> {
                    toJsonArray(talentsDescriptions.getDescriptionFor(skill.getId())).forEach(jsonElements::add);

                    jsonElements.add(JsonDescription.simple("\n\n"));
                    if(skill instanceof TieredSkill tieredSkill) {
                        for(int i = 0; i < tieredSkill.getTiers().size(); i++) {
                            LearnableSkill tier = tieredSkill.getTiers().get(i);
                            jsonElements.add(JsonDescription.simple(i + 1 + " "));
                            if(tier instanceof GearAttributeTalent gearAttributeTalent) {
                                jsonElements.add(JsonDescription.simple("+" + gearAttributeTalent.getValue() + "\n", "#FF00CB"));
                            }
                            if(tier instanceof EffectTalent effectTalent) {
                                jsonElements.add(JsonDescription.simple("+" + effectTalent.getAmplifier()+ "\n", "green"));
                            }
                            if(tier instanceof PuristTalent puristTalent) {
                                jsonElements.add(JsonDescription.simple("+" + (puristTalent.getDamageIncrease() * 100) + "% damage per piece\n", "#C23627"));
                            }
                            if(tier instanceof VanillaAttributeTalent vanillaAttributeTalent) {
                                jsonElements.add(JsonDescription.simple("+" + (((VanillaAttributeTalentAccessor)vanillaAttributeTalent).getAmount() * 100) + "%\n", "#90FF00"));
                            }
                            if(i + 1 == tieredSkill.getMaxLearnableTier()) {
                                jsonElements.add(JsonDescription.simple("Overlevels\n", "#EFBF04"));
                                jsonElements.add(JsonDescription.simple("------------", "#EFBF04"));
                                jsonElements.add(JsonDescription.simple("\n"));
                            }
                        }
                    }
                });
            });
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
                         innerDesc.add(JsonDescription.simple("can automatically feed you directly from your pouches, belts & backpacks and removes the need to eat food manually.\\n\\n"));
                         innerDesc.add(JsonDescription.simple("Refill, Compacting, and Auto-Smelting, Smoking, and Blasting", "yellow"));
                         innerDesc.add(JsonDescription.simple("upgrades will let you refill items in your hand from your backpack automatically, compacting items in 2x2 and 3x3 recipes, and automatically process items in your bag!", "yellow"));
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

                .addDescription("Mod Box Tinkering", jsonElements -> modDesc("Mod Box Tinkering", "workstation", innerDesc -> {
                    innerDesc.add(JsonDescription.simple("The Mod Box Workstation will allow you to craft Targeted Mod Boxes that only drop items and blocks for a certain mod!", "$text"));
                    innerDesc.add(JsonDescription.simple("You have to have the corresponding research first before you can craft a particular box. The cost for each is dependent on the mod."));
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
                        innerDesc.add(JsonDescription.simple("You can perform rituals to summon Familiars and Demons to help you. Mine random ores using a dimensional mineshaft, and crush ores to massively increase the value of them!", "$text"));
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

                .addDescription("Inception Upgrade", jsonElements -> modDesc("Inception Upgrade", "from Sophisticated Backpacks", innerDesc -> innerDesc.add(JsonDescription.simple("With your mastery of looting, your accumulation of knowledge, and insane willpower, you finally have the brilliant idea, wondering why it hasn't hit you all this time, to simply put your backpacks inside of other backpacks. A peculiar thought...", "$text"))).forEach(jsonElements::add))

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
                .build());
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
                    jsonElements.add(JsonDescription.simple("effect permanently\n\n "));
                    jsonElements.add(JsonDescription.simple("Every level of the Haste effect increases mining speed by "));
                    jsonElements.add(JsonDescription.simple("20%", "#47B8F5"));
                })
                .addDescription("Strength", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your "));
                    jsonElements.add(JsonDescription.simple("movement speed ", "#F6CD0E"));
                    jsonElements.add(JsonDescription.simple("by applying the "));
                    jsonElements.add(JsonDescription.simple("Speed ", "green"));
                    jsonElements.add(JsonDescription.simple("effect permanently\n\n "));
                    jsonElements.add(JsonDescription.simple("Every level of the Speed effect increases movement speed by "));
                    jsonElements.add(JsonDescription.simple("20%", "#F6CD0E"));
                })
                .addDescription("Purist", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Deal "));
                    jsonElements.add(JsonDescription.simple("extra damage ", "#C23627"));
                    jsonElements.add(JsonDescription.simple("based on the amount of "));
                    jsonElements.add(JsonDescription.simple("Scrappy ", "343434"));
                    jsonElements.add(JsonDescription.simple("armor pieces you wear."));
                    jsonElements.add(JsonDescription.simple("20%", "#F6CD0E"));
                })
                .addDescription("Stone_Skin", jsonElements -> {
                    jsonElements.add(JsonDescription.simple("Increases your knockback resistance, reducing the amount of knockback of projectiles and melee hits. "));
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
