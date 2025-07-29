package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.mixins.GameRulesBooleanValueAccessor;

import java.util.function.BiConsumer;

public class ModGameRules {

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_FLIGHT_IN_VAULTS;
    public static GameRules.Key<GameRules.BooleanValue> NORMALIZED_ENABLED;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_OLD_AFFINITY_HANDLING;

    public static void initialize() {
        ALLOW_FLIGHT_IN_VAULTS = GameRules.register("enableFlightInVaults", GameRules.Category.PLAYER, booleanRule(false));
        NORMALIZED_ENABLED = GameRules.register("enableDifficultyLockModifiers", GameRules.Category.PLAYER, booleanRule(true));
        ENABLE_OLD_AFFINITY_HANDLING = GameRules.register("enableLegacyGodAffinityHandling", GameRules.Category.PLAYER, booleanRule(false));
    }


    public static GameRules.Type<GameRules.BooleanValue> booleanRule(boolean defaultValue) {
        return GameRulesBooleanValueAccessor.create(defaultValue, ((minecraftServer, booleanValue) -> {}));
    }

    public static boolean isEnabled(GameRules.Key<GameRules.BooleanValue> gamerule, Level level) {
        return level.getGameRules().getBoolean(gamerule);
    }
   
}
