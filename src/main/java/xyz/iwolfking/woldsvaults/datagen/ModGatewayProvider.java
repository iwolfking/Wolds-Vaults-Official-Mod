package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractGatewayProvider;

public class ModGatewayProvider extends AbstractGatewayProvider {


    protected ModGatewayProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerGateways(GatewayRegistrar registrar) {
        registrar.add("greed_assassin_gateway", gatewayBuilder -> {
            gatewayBuilder
                    .size(GatewaySize.MEDIUM)
                    .color("#A2961D")
                    .spawnRange(16)
                    .completionXp(1000)
                    .reward("woldsvaults:gateway_rare", 1, "Rare Rewards")
                    .wave(waveBuilder -> {
                        waveBuilder
                                .entity(VaultMod.id("acid_pursuer"), 4)
                                .entity(VaultMod.id("acid_enforcer"), 4)
                                .entity(VaultMod.id("acid_fiend"), 2)
                                .entity(VaultMod.id("acid_priest"), 2)
                                .modifier("generic.max_health", AttributeOperation.MULTIPLY_TOTAL, 1.25)
                                .modifier("generic.knockback_resistance", AttributeOperation.ADDITION, 0.5)
                                .modifier("generic.movement_speed", AttributeOperation.MULTIPLY_TOTAL, 0.05)
                                .modifier("generic.armor", AttributeOperation.ADDITION, 20)
                                .reward("woldsvaults:gateway_greed_coins", 1, "Greed Coins")
                                .setupTime(40)
                                .maxWaveTime(900);
                    })
                    .wave(waveBuilder -> {
                        waveBuilder
                                .entity(VaultMod.id("death_guard"), 4)
                                .entity(VaultMod.id("death_dancer"), 4)
                                .entity(VaultMod.id("death_knight"), 2)
                                .entity(VaultMod.id("death_inquisitor"), 2)
                                .modifier("generic.max_health", AttributeOperation.MULTIPLY_TOTAL, 1.25)
                                .modifier("generic.knockback_resistance", AttributeOperation.ADDITION, 0.5)
                                .modifier("generic.armor", AttributeOperation.ADDITION, 20)
                                .reward("woldsvaults:gateway_greed_coins", 1, "Greed Coins")
                                .setupTime(40)
                                .maxWaveTime(900);
                    })
                    .wave(waveBuilder -> {
                        waveBuilder
                                .entity(VaultMod.id("death_guard"), 2)
                                .entity(VaultMod.id("death_dancer"), 2)
                                .entity(VaultMod.id("death_knight"), 1)
                                .entity(VaultMod.id("death_inquisitor"), 1)
                                .entity(VaultMod.id("acid_pursuer"), 2)
                                .entity(VaultMod.id("acid_enforcer"), 2)
                                .entity(VaultMod.id("acid_fiend"), 1)
                                .entity(VaultMod.id("acid_priest"), 1)
                                .modifier("generic.max_health", AttributeOperation.MULTIPLY_TOTAL, 1.25)
                                .modifier("generic.knockback_resistance", AttributeOperation.ADDITION, 0.5)
                                .modifier("generic.armor", AttributeOperation.ADDITION, 40)
                                .reward("woldsvaults:gateway_greed_coins", 1, "Greed Coins")
                                .setupTime(80)
                                .maxWaveTime(1200);
                    });
        });
    }
}
