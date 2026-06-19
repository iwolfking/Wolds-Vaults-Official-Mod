package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityDescriptionsProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.*;

public class ModAbilityDescriptionsProvider extends AbstractAbilityDescriptionsProvider {
    protected ModAbilityDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("chaos_cube", builder -> {
            builder.addDescription("Grenade_Base", jsonElements -> {
               jsonElements.add(JsonDescription.simple("Throw a mysterious, powerful "));
               jsonElements.add(JsonDescription.simple("Chaos Cube ", "$qhighlight"));
                jsonElements.add(JsonDescription.simple("that explodes after a little while, dealing "));
                jsonElements.add(JsonDescription.simple("Attack Damage ", "$damage"));
                jsonElements.add(JsonDescription.simple("scaled damage in a "));
                jsonElements.add(JsonDescription.simple("radius ", "$radius"));
                jsonElements.add(JsonDescription.simple("and activating a random "));
                jsonElements.add(JsonDescription.simple("Effect Cloud ", "$heal"));
                jsonElements.add(JsonDescription.simple("attribute you have equipped."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("damage");
                current.add("adjustedRadius");
                current.add("manaCost");
                current.add("duration");
            }, next -> {
                next.add("damage");
                next.add("adjustedRadius");
                next.add("manaCost");
                next.add("duration");
            });
        });

        add("fireball_volley", builder -> {
            builder.addDescription("Fireball_Volley", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Summon a magical bouncing Fireball that "));
                jsonElements.add(JsonDescription.simple("scorches ", "$ability_power"));
                jsonElements.add(JsonDescription.simple("mobs in its path and explodes in a small radius - dealing an amount of "));
                jsonElements.add(JsonDescription.simple("damage ", "$ability_power"));
                jsonElements.add(JsonDescription.simple("based off your ability power. "));
                jsonElements.add(JsonDescription.simple("After a duration of time, it will stop bouncing and explode a final time. "));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("ability_power");
                current.add("adjustedRadius");
                current.add("cooldown");
                current.add("manaCost");
                current.add("duration");
            }, next -> {
                next.add("ability_power");
                next.add("adjustedRadius");
                next.add("cooldown");
                next.add("manaCost");
                next.add("duration");
            });
        });

        add("wolds_abilities", builder -> {
            builder.addDescription("Colossus", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Makes you considerably bigger, giving you "));
                jsonElements.add(JsonDescription.simple("knockback immunity ", "$knockback"));
                jsonElements.add(JsonDescription.simple("and a boost in "));
                jsonElements.add(JsonDescription.simple("resistance ", "$radius"));
                jsonElements.add(JsonDescription.simple("for a limited duration. "));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("additionalResistance");
                current.add("size");
                current.add("cooldown");
                current.add("manaCost");
                current.add("adjustedDuration");
            }, next -> {
                next.add("additionalResistance");
                next.add("size");
                next.add("cooldown");
                next.add("manaCost");
                next.add("adjustedDuration");
            });

            builder.addDescription("Sneaky_Getaway", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Makes you considerably smaller and increases your "));
                jsonElements.add(JsonDescription.simple("speed ", "$radius"));
                jsonElements.add(JsonDescription.simple("for a limited duration. "));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("speed");
                current.add("size");
                current.add("cooldown");
                current.add("manaCost");
                current.add("adjustedDuration");
            }, next -> {
                next.add("speed");
                next.add("size");
                next.add("cooldown");
                next.add("manaCost");
                next.add("adjustedDuration");
            });

            builder.addDescription("Expunge_Base", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Activates all "));
                jsonElements.add(JsonDescription.simple("Effect Cloud ", "$heal"));
                jsonElements.add(JsonDescription.simple("modifiers you have on your equipped gear where you are standing, and increases their  "));
                jsonElements.add(JsonDescription.simple("duration", "$duration"));
                jsonElements.add(JsonDescription.simple("and "));
                jsonElements.add(JsonDescription.simple("radius ", "$areaOfEffect"));
                jsonElements.add(JsonDescription.simple("."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("durationMultiplier");
                current.add("radiusMultiplier");
                current.add("cooldown");
                current.add("manaCost");
            }, next -> {
                next.add("durationMultiplier");
                next.add("radiusMultiplier");
                next.add("cooldown");
                next.add("manaCost");
            });

            builder.addDescription("Concentrate_Base", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Drains all "));
                jsonElements.add(JsonDescription.simple("Potion Effects ", "$heal"));
                jsonElements.add(JsonDescription.simple("from nearby enemies in a "));
                jsonElements.add(JsonDescription.simple("radius", "$areaOfEffect"));
                jsonElements.add(JsonDescription.simple(". Negative effects are converted into positive ones, and the same effect will always correspond with up to a couple of different effects.\n\n"));
                jsonElements.add(JsonDescription.simple("If you would receive multiple of the same effect, the base duration will be added onto the existing duration for each.\n\n "));
                jsonElements.add(JsonDescription.simple("Depending on your ability level, the level of the effect will also randomly be boosted, making it more potent."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("adjustedRadius");
                current.add("adjustedDuration");
                current.add("baseAmplitude");
                current.add("amplitudeScaleChance");
                current.add("cooldown");
                current.add("manaCost");
            }, next -> {
                next.add("adjustedRadius");
                next.add("adjustedDuration");
                next.add("baseAmplitude");
                next.add("amplitudeScaleChance");
                next.add("cooldown");
                next.add("manaCost");
            });

            builder.addDescription("Fangs_Base", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Summons a ring of Fangs in a "));
                jsonElements.add(JsonDescription.simple("radius ", "$radius"));
                jsonElements.add(JsonDescription.simple("around you dealing damage plus a portion of your "));
                jsonElements.add(JsonDescription.simple("Attack Damage", "$damage"));
                jsonElements.add(JsonDescription.simple(".\n\n"));
                jsonElements.add(JsonDescription.simple("If a target is below a certain percentage of their "));
                jsonElements.add(JsonDescription.simple("health", "green"));
                jsonElements.add(JsonDescription.simple(", they will be "));
                jsonElements.add(JsonDescription.simple("executed ", "red"));
                jsonElements.add(JsonDescription.simple("and drop a  "));
                jsonElements.add(JsonDescription.simple("Heart Fragment", "green"));
                jsonElements.add(JsonDescription.simple("."));
                jsonElements.add(JsonDescription.simple("\n\nApplies On-Hit effects."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("adjustedRadius");
                current.add("damageMultiplier");
                current.add("baseDamage");
                current.add("executeThreshold");
                current.add("cooldown");
                current.add("manaCost");
            }, next -> {
                next.add("adjustedRadius");
                next.add("damageMultiplier");
                next.add("baseDamage");
                next.add("executeThreshold");
                next.add("cooldown");
                next.add("manaCost");
            });

            builder.addDescription("Fangs_Maw", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Summons a line of Fangs in front of you"));
                jsonElements.add(JsonDescription.simple(" dealing damage plus a portion of your "));
                jsonElements.add(JsonDescription.simple("Attack Damage", "$damage"));
                jsonElements.add(JsonDescription.simple(", pulling any target hit towards you.\n\n"));
                jsonElements.add(JsonDescription.simple("Depending on the distance the target was pulled from, they will have "));
                jsonElements.add(JsonDescription.simple("Slowness", "#78329F"));
                jsonElements.add(JsonDescription.simple(","));
                jsonElements.add(JsonDescription.simple(" Weakness", "#783269"));
                jsonElements.add(JsonDescription.simple(", "));
                jsonElements.add(JsonDescription.simple("and "));
                jsonElements.add(JsonDescription.simple("Vulnerability ", "#B13269"));
                jsonElements.add(JsonDescription.simple("applied to them.\n\n"));
                jsonElements.add(JsonDescription.simple("Chance to drop a "));
                jsonElements.add(JsonDescription.simple("Heart Fragment", "green"));
                jsonElements.add(JsonDescription.simple(" per mob hit."));
                jsonElements.add(JsonDescription.simple("\n\nApplies On-Hit effects."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("adjustedRadius");
                current.add("damageMultiplier");
                current.add("baseDamage");
                current.add("heartFragmentChance");
                current.add("effectAmplifier");
                current.add("cooldown");
                current.add("manaCost");
            }, next -> {
                next.add("adjustedRadius");
                next.add("damageMultiplier");
                next.add("baseDamage");
                next.add("heartFragmentChance");
                next.add("effectAmplifier");
                next.add("cooldown");
                next.add("manaCost");
            });

            builder.addDescription("Vein_Miner_Chain", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Changes Vein Miner to mine blocks that are "));
                jsonElements.add(JsonDescription.simple("further apart ", "$distance"));
                jsonElements.add(JsonDescription.simple("at the cost of number of "));
                jsonElements.add(JsonDescription.simple("blocks mined", "$blocks"));
                jsonElements.add(JsonDescription.simple("."));
                jsonElements.add(holdAbility());

            }, current -> {
                current.add("blocks");
                current.add("distance");
            }, next -> {
                next.add("blocks");
                next.add("distance");
            });

            builder.addDescription("Levitate", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Changes Mega Jump into an on-demand levitation effect."));
                jsonElements.add(holdAbility());

            }, current -> {
                current.add("manaCostPerSecond");
                current.add("levitateSpeed");
            }, next -> {
                next.add("manaCostPerSecond");
                next.add("levitateSpeed");
            });


        });
    }

    public JsonObject castAbility() {
        return JsonDescription.simple("\n\n✴ Cast Ability", "$castType");
    }

    public JsonObject holdAbility() {
        return JsonDescription.simple("\n\n● Hold Ability", "$castType");
    }
}
