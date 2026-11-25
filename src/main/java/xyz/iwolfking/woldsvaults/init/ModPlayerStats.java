package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.util.calc.PlayerStat;
import xyz.iwolfking.vhapi.api.extensions.general.PlayerStatFactory;

public class ModPlayerStats {
    public static final PlayerStat THORNS_SCALING_DAMAGE =
            PlayerStatFactory.INSTANCE.create("thorns_scaling_damage");

    public static final PlayerStat DISMANTLE_CHANCE =
            PlayerStatFactory.INSTANCE.create("dismantle_chance");

    public static void init() {
    }
}