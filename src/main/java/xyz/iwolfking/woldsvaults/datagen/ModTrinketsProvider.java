package xyz.iwolfking.woldsvaults.datagen;

import com.alrex.parcool.api.Effects;
import com.alrex.parcool.common.potion.effects.InexhaustibleEffect;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
import iskallia.vault.gear.trinket.effects.AttributeTrinket;
import iskallia.vault.gear.trinket.effects.PotionEffectTrinket;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.vhapi.api.datagen.AbstractTrinketProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.trinket.MultiAttributeTrinket;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;

public class ModTrinketsProvider extends AbstractTrinketProvider {
    protected ModTrinketsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("wolds_trinkets", builder -> {
            builder.addTrinket(VaultMod.id("vibrating_stone"), AbstractTrinketProvider.Builder.createTrinket(10, "Vibrating Stone", "Gives your attacks a 10% chance to echo, your echoed attacks do 25% more damage", 20, 30, 12, 12, new MultiAttributeTrinket.Config<Float>(List.of(ModGearAttributes.ECHOING_CHANCE, ModGearAttributes.ECHOING_DAMAGE), List.of(0.1F, 0.25F)), AbstractTrinketProvider.Builder.TrinketSlot.RED_TRINKET))
                    .addTrinket(VaultMod.id("swift_amulet"), AbstractTrinketProvider.Builder.createTrinket(10, "Swift Amulet", "Gives you a 15% chance to dodge attacks", 20, 30, 12, 12, new AttributeTrinket.Config<Float>(ModGearAttributes.DODGE_PERCENT, 0.15F), AbstractTrinketProvider.Builder.TrinketSlot.BLUE_TRINKET))
                    .addTrinket(VaultMod.id("running_shoes"), AbstractTrinketProvider.Builder.createTrinket(0, "Running Shoes", "Parkour uses no energy", 20, 30, 12, 12, new PotionEffectTrinket.Config(Effects.INEXHAUSTIBLE.getId(), 1), AbstractTrinketProvider.Builder.TrinketSlot.BLUE_TRINKET))
                    .addTrinket(VaultMod.id("chromatic_diffuser"), AbstractTrinketProvider.Builder.createTrinket(10, "Chromatic Diffuser", "Adds an additional 10% chance to trigger Effect Clouds", 20, 30, 12, 12, new AttributeTrinket.Config<Float>(ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, 0.1F), AbstractTrinketProvider.Builder.TrinketSlot.RED_TRINKET))
                    .addTrinket(VaultMod.id("miners_headlamp"), AbstractTrinketProvider.Builder.createTrinket(10, "Miner's Headlamp", "Grants night vision, +25% Copiously", 20, 30, 12, 12, new PotionEffectTrinket.Config(MobEffects.NIGHT_VISION.getRegistryName(), 1), AbstractTrinketProvider.Builder.TrinketSlot.GREEN_TRINKET))
                    .build();
        });

    }
}
