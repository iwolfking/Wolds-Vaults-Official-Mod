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
                current.add("duration");
            }, next -> {
                next.add("additionalResistance");
                next.add("size");
                next.add("cooldown");
                next.add("manaCost");
                next.add("duration");
            });

            builder.addDescription("Sneaky_Getaway", jsonElements -> {
                jsonElements.add(JsonDescription.simple("Makes you considerably smaller and increases your "));
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
                current.add("duration");
            }, next -> {
                next.add("additionalResistance");
                next.add("size");
                next.add("cooldown");
                next.add("manaCost");
                next.add("duration");
            });

        });
    }

    public JsonObject castAbility() {
        return JsonDescription.simple("\n\n✴ Cast Ability", "$castType");
    }
}
