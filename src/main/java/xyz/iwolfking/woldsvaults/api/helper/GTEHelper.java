package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.util.data.WeightedList;
import shadows.gateways.Gateways;
import shadows.gateways.entity.GatewayEntity;
import shadows.gateways.gate.Gateway;
import shadows.gateways.gate.GatewayManager;

import java.util.Random;

public class GTEHelper {
    private static final WeightedList<Gateway> WEIGHTED_GATEWAY_LIST = new WeightedList<>();
    private static final int BASE_WEIGHT = 0;
    private static final int SMALL_WAVE_WEIGHT = 2;
    private static final int MEDIUM_WAVE_WEIGHT = 4;
    private static final int LARGE_WAVE_WEIGHT = 8;
    private static final Random rand = new Random();

    public static Gateway getRandomGate() {
        if(WEIGHTED_GATEWAY_LIST.isEmpty()) {
            for(Gateway gate : GatewayManager.INSTANCE.getValues()) {
                int weight = BASE_WEIGHT;
                if(gate.getSize().equals(GatewayEntity.GatewaySize.SMALL)) {
                    weight += SMALL_WAVE_WEIGHT;
                }

                if(gate.getSize().equals(GatewayEntity.GatewaySize.MEDIUM)) {
                    weight += MEDIUM_WAVE_WEIGHT;
                }

                if(gate.getSize().equals(GatewayEntity.GatewaySize.LARGE)) {
                    weight += LARGE_WAVE_WEIGHT;
                }
                WEIGHTED_GATEWAY_LIST.add(gate, weight);
            }
        }

        return WEIGHTED_GATEWAY_LIST.getRandom(rand);
    }
}
