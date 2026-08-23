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

        add("god_ultimates", builder -> {
            builder.addDescription("Stirrings_Of_Power_Dormant", jsonElements -> {
                jsonElements.add(JsonDescription.simple("The gods stir, but none of them answer you yet.\n\n"));
                jsonElements.add(JsonDescription.simple("Equip a "));
                jsonElements.add(JsonDescription.simple("god charm ", "$qhighlight"));
                jsonElements.add(JsonDescription.simple("and reach "));
                jsonElements.add(JsonDescription.simple("god level 5 ", "$level"));
                jsonElements.add(JsonDescription.simple("with that god, and this becomes their ultimate. Swap charms and it changes with you."));
                jsonElements.add(castAbility());
            }, current -> {
            }, next -> {
            });

            builder.addDescription("Cope_De_Grace", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Infuse yourself with the power of "));
                jsonElements.add(JsonDescription.simple("Idona", "$idona"));
                jsonElements.add(JsonDescription.simple(", multiplying your "));
                jsonElements.add(JsonDescription.simple("ability power ", "$ability_power"));
                jsonElements.add(JsonDescription.simple("and "));
                jsonElements.add(JsonDescription.simple("attack damage ", "$damage"));
                jsonElements.add(JsonDescription.simple("while gaining huge amounts of "));
                jsonElements.add(JsonDescription.simple("damage resistance", "$resistance"));
                jsonElements.add(JsonDescription.simple(", stacking multiplicatively with all other boosts.\n\n"));
                jsonElements.add(JsonDescription.simple("At the end of the infusion, "));
                jsonElements.add(JsonDescription.simple("dash forward ", "$force"));
                jsonElements.add(JsonDescription.simple("in a brutal explosion of power, dealing huge damage to everything in your way. Every hit landed during the infusion increases the damage of this final dash."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("attackDamageMultiplier");
                current.add("abilityPowerMultiplier");
                current.add("resistance");
                current.add("damageCompounding");
                current.add("duration");
                current.add("ultimateCooldown");
            }, next -> {
                next.add("attackDamageMultiplier");
                next.add("abilityPowerMultiplier");
                next.add("resistance");
                next.add("damageCompounding");
                next.add("duration");
                next.add("ultimateCooldown");
            });

            builder.addDescription("Savior", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Infuse yourself with the power of "));
                jsonElements.add(JsonDescription.simple("Velara", "$velara"));
                jsonElements.add(JsonDescription.simple(", sending a shockwave across the whole vault that "));
                jsonElements.add(JsonDescription.simple("heals ", "$heal"));
                jsonElements.add(JsonDescription.simple("and "));
                jsonElements.add(JsonDescription.simple("shields ", "$absorb"));
                jsonElements.add(JsonDescription.simple("every runner in it and grants them "));
                jsonElements.add(JsonDescription.simple("resistance", "$resistance"));
                jsonElements.add(JsonDescription.simple(".\n\nIt also raises downed teammates, once per person per vault.\n\n"));
                jsonElements.add(JsonDescription.simple("Granted by Velara at god level 5. Its cooldown cannot be reduced."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("heal");
                current.add("absorb");
                current.add("resistance");
                current.add("duration");
                current.add("ultimateCooldown");
            }, next -> {
                next.add("heal");
                next.add("absorb");
                next.add("resistance");
                next.add("duration");
                next.add("ultimateCooldown");
            });

            builder.addDescription("Eyes_Of_God", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Infuse yourself with the power of "));
                jsonElements.add(JsonDescription.simple("Tenos", "$tenos"));
                jsonElements.add(JsonDescription.simple(", instantly discovering every room in a "));
                jsonElements.add(JsonDescription.simple("radius ", "$radius"));
                jsonElements.add(JsonDescription.simple("around you, however far away they are.\n\n"));
                jsonElements.add(JsonDescription.simple("Granted by Tenos at god level 5. Its cooldown cannot be reduced."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("radius");
                current.add("ultimateCooldown");
            }, next -> {
                next.add("radius");
                next.add("ultimateCooldown");
            });

            builder.addDescription("Bullet_Time", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Infuse yourself with the power of "));
                jsonElements.add(JsonDescription.simple("Wendarr", "$wendarr"));
                jsonElements.add(JsonDescription.simple(", stretching every tick of the "));
                jsonElements.add(JsonDescription.simple("vault timer ", "$duration"));
                jsonElements.add(JsonDescription.simple("so the run lasts longer, while you gain "));
                jsonElements.add(JsonDescription.simple("attack speed", "$speed"));
                jsonElements.add(JsonDescription.simple(", "));
                jsonElements.add(JsonDescription.simple("movement speed ", "$speed"));
                jsonElements.add(JsonDescription.simple("and reflexes sharp enough to "));
                jsonElements.add(JsonDescription.simple("dodge ", "$chance"));
                jsonElements.add(JsonDescription.simple("almost anything.\n\n"));
                jsonElements.add(JsonDescription.simple("Granted by Wendarr at god level 5. Its cooldown cannot be reduced."));
                jsonElements.add(castAbility());
            }, current -> {
                current.add("timerStretch");
                current.add("dodgeChance");
                current.add("bonusAttackSpeed");
                current.add("bonusMovementSpeed");
                current.add("duration");
                current.add("ultimateCooldown");
            }, next -> {
                next.add("timerStretch");
                next.add("dodgeChance");
                next.add("bonusAttackSpeed");
                next.add("bonusMovementSpeed");
                next.add("duration");
                next.add("ultimateCooldown");
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
