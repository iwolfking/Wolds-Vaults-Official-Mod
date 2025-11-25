package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonArray;
import iskallia.vault.config.ModBoxConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.function.Consumer;

public class ModSkillDescriptionsProvider extends AbstractSkillDescriptionsProvider {
    public ModSkillDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.MOD_BOX = new ModBoxConfig().readConfig();
        add("research_descriptions", builder -> {
            builder.addDescription("Ars Nouveau", jsonElements -> {
                        modDesc("Ars Nouveau", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Ars Nouveau is a magic mod inspired by Ars Magicka that allows players to craft their own spells, create magical artifacts, perform rituals, and much more!\n\n", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Documentation is provided entirely in-game by Patchouli. To get started with this mod, craft the Worn Notebook using a book and 1 Lapis Lazuli.\n\n", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Bonsai Pots", jsonElements -> {
                        modDesc("Bonsai Pots", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("These pots can automate any tree in the game very easily by growing them in tiny pots.\n\n", "$text"));
                        }).forEach(jsonElements::add);;
                    })
                    .addDescription("Dave's Potioneering", jsonElements -> {
                        modDesc("Dave's Potioneering", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Dave's Potioneering is a mod that adds an upgraded Brewing Stand that's faster and has double potion output, a new Reinforced Cauldron that allows you to coat your weapons or tools with potion effects, and a Potioneer Gauntlet that can store up to 6 potion effects and apply them on attack.", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Occultism", jsonElements -> {
                        modDesc("Occultism", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Occultism is a mod inspired by the world of Jonathan Stroud's Bartimaeus. \nWith the help of occult rituals you will summon spirits, also known as demons, from \"The Other Place\" to aid you in your adventures.\n\n", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("You can perform rituals to summon Familiars and Demons to help you. Mine random ores using a dimensional mineshaft, and crush ores to massively increase the value of them!", "$text"));
                        }).forEach(jsonElements::add);;
                    })
                    .addDescription("Functional Storage", jsonElements -> {
                        modDesc("Functional Storage", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Functional Storage is an alternative take on the storage solution of Storage Drawers, with an updated look and additional functionalities.", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Time in a Bottle", jsonElements -> {
                       modDesc("Time in a Bottle", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("This mod adds an amazing item called the Time in a Bottle that stores time as you play, then you can right-click on a block to speed up its tick time using your stored time! Nifty!\n\n", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Also unlocks the ", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Weather Control ", "yellow"));
                            innerDesc.add(JsonDescription.simpleDescription("mod! ", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Which lets you control the time of day and the weather!", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Misc Bag Upgrades", jsonElements -> {
                        modDesc("Everlasting, Tool Swapper, Tank, Pump, XP Pump, and Battery Upgrade from the Sophisticated Backpacks", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("These will come in handy for utilizing your bag in more non-conventional ways... Note: the Tool Swapper upgrade functionality is disabled while in a vault.", "$text"));
                        }).forEach(jsonElements::add);;
                    })
                    .addDescription("Inception Upgrade", jsonElements -> {
                        modDesc("Inception Upgrade from the Sophisticated Backpacks", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("With your mastery of looting, your accumulation of knowledge, and insane willpower, you finally have the brilliant idea, wondering why it hasn't hit you all this time, to simply put your backpacks inside of other backpacks. A peculiar thought...", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("QuarryPlus", jsonElements -> {
                        modDesc("QuarryPlus", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("After mastering mining, you can create the ultimate mining machines with the QuarryPlus mod! This adds Quarries that work much like a Buildcraft Quarry and can be enchanted to enhance them. You can also create the coveted Chunk Destroyer that can flatten an entire chunk!", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Mystical Agriculture", jsonElements -> {
                        modDesc("Mystical Agriculture", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("After mastering the botanical arts, you can now harness the power of mystically infused seeds with this mod. The end-all-be-all production mod, you can create seeds for almost any resource!\n\n", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Items such as tools and armor are disabled!", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Hostile Neural Networks", jsonElements -> {
                        modDesc("Hostile Neural Networks", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Hostile Neural Networks is a server-friendly mob looting system. Use a Data Model on a mob to make it's Model, then evolve your Data Model using Spawn Eggs of the corresponding type. Very powerful mod!\n\n", "$text"));
                            innerDesc.add(JsonDescription.simpleDescription("Note, HNN works differently in this pack! Your models won't upgrade in Simulation Chambers or gain data from kills, you need to evolve them using spawn eggs (Check JEI!)! Also needs power to work.", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .addDescription("Psi", jsonElements -> {
                        modDesc("Psi", innerDesc -> {
                            innerDesc.add(JsonDescription.simpleDescription("Psi is a magical tech spell programming mod inspired by Mahouka Kokou no Rettousei. Don't worry if that doesn't make any sense. The core of Psi is a system where you can create action sequences (\\\"Spells\\\") to be executed in the world. This system is followed by a leveling system containing ingame documentation and tutorials of the various concepts and pieces you'll get. The mod makes use of basic mathematical concepts, such as Vectors in its systems, which allows for great flexibility on what spells can do.", "$text"));
                        }).forEach(jsonElements::add);
                    })
                    .build();
        });
    }

    public JsonArray modDescOpening(String modName) {
        JsonArray descriptions = new JsonArray();
        descriptions.add(JsonDescription.simpleDescription("Unlocks the ", "$text"));
        descriptions.add(JsonDescription.simpleDescription(modName, "$yellow"));
        descriptions.add(JsonDescription.simpleDescription(" mod!\n\n", "$text"));
        return descriptions;
    }

    public JsonArray modDescClosing(String modName) {
        if(!ModConfigs.MOD_BOX.POOL.containsKey(modName)) {
            return null;
        }

        return modBox();
    }

    public JsonArray modBox() {
        JsonArray descriptions = new JsonArray();
        descriptions.add(JsonDescription.simpleDescription("\n\n", "$text"));
        descriptions.add(JsonDescription.simpleDescription("Adds rewards to ", "$text"));
        descriptions.add(JsonDescription.simpleDescription("Mod Boxes ", "aqua"));
        descriptions.add(JsonDescription.simpleDescription("!", "$text"));
        return descriptions;
    }

    public JsonArray modDesc(String modName, Consumer<JsonArray> innerDescConsumer) {
        JsonArray jsonArray = new JsonArray();
        modDescOpening(modName).forEach(jsonArray::add);
        innerDescConsumer.accept(jsonArray);
        JsonArray closing = modDescClosing(modName);
        if(closing != null) {
            closing.forEach(jsonArray::add);
        }
        return jsonArray;
    }

}
