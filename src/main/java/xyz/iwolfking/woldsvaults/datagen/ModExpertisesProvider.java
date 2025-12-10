package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.skill.expertise.type.*;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractExpertiseProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.InfuserExpertiseAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.MysticExpertiseAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.TrinketerExpertiseAccessor;

public class ModExpertisesProvider extends AbstractExpertiseProvider {
    protected ModExpertisesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_expertises", builder -> {
            builder.addExpertise("Grave_Insurance", "Grave Insurance", 20, 1, 2, value -> {
               return new GraveInsurance(20, 1, 0, 0.25F + (0.25F * value));
            });

            builder.addExpertise("Augmentation_Luck", "Augmentation Luck", 20, 1, 1, value -> {
                return new EclecticGearExpertise(20, 1, 0, 0.01F);
            });

            builder.addExpertise("Blessed", "Blessed", 40, 1, 1, value -> {
                return new BlessedExpertise(40, 1, 0, 0.1F + (0.1F * value));
            });

            builder.addExpertise("ShopReroll", "Negotiator", 0, 1, 3, value -> {
                return new ShopRerollExpertise(0, 1, 0, value == 0 ? 900 : value == 1 ? 300 : 100);
            });

            builder.addExpertise("Pylon_Pilferer", "Pylon Pilferer", 0, 1, 3, value -> {
                return new PylonPilfererExpertise(0, 1, 0, 0.02F + (value * 0.015F));
            });

            builder.addExpertise("Surprise_Favors", "Surprise Favors", 0, 1, 3, value -> {
                return new SurpriseModifiersExpertise(0, 1, 0, 2 + (value * 2));
            });

            builder.addExpertise("Craftsman", "Craftsman", 0, 1, 3, value -> {
                return new CraftsmanExpertise(0, 1, 0, value + 1);
            });
        });

        add("replace/additional_vanilla_tiers", builder -> {
            builder.addExpertise("Companion_Loyalty", "Companion's Loyalty", 50, 1, 5, value -> {
                return new CompanionCooldownExpertise(50, 1, 0, 0.1F + (0.1F * value));
            });

            builder.addExpertise("Lucky_Altar", "Lucky Altar", 0, 1, 5, value -> {
                return new LuckyAltarExpertise(0, 1, 0, 0.1F + (0.1F * value));
            });

            builder.addExpertise("Trinketer", "Trinketer", 0, 1, 5, value -> {
                TrinketerExpertise expertise = new TrinketerExpertise();
                ((TrinketerExpertiseAccessor)expertise).setDamageAvoidanceChance(0.1F + (value * 0.1F));
                return expertise;
            });

            builder.addExpertise("Mystic", "Crystalmancer", 0, 1, 5, value -> {
                MysticExpertise expertise = new MysticExpertise();
                ((MysticExpertiseAccessor)expertise).setAdditionalVolume(10 + (value * 10));
                return expertise;
            });

            builder.addExpertise("Infuser", "Infuser", 0, 1, 5, value -> {
                InfuserExpertise expertise = new InfuserExpertise();
                ((InfuserExpertiseAccessor)expertise).setChance(0.15F + (value * 0.15F));
                return expertise;
            });

            builder.addExpertise("Experienced", "Experienced", 0, 1, 5, value -> {
                return new ExperiencedExpertise(0, 1, 0, 0.15F + (0.15F * value));
            });
        });

        add("replace/jeweler", builder -> {
            builder.addExpertise("Jeweler", "Jeweler", 0 ,1, 1, value -> {
               return new JewelExpertise();
            });
        });

        add("replace/marketer", builder -> {
            builder.addExpertise("Marketer", "Marketer", 0 ,1, 3, value -> {
                return new BlackMarketExpertise(0, 1, 0, 0, value + 1);
            });
        });
    }
}
