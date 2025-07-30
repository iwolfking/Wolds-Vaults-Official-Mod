package xyz.iwolfking.woldsvaults.init;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.mixins.GameRulesBooleanValueAccessor;
import xyz.iwolfking.woldsvaults.network.message.ClientboundSyncGamerulesMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class ModGameRules {

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_FLIGHT_IN_VAULTS;
    public static GameRules.Key<GameRules.BooleanValue> NORMALIZED_ENABLED;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_OLD_AFFINITY_HANDLING;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_PLACING_VAULT_DOLLS;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_VAULT_DOLLS;

    public static void initialize() {
        ALLOW_FLIGHT_IN_VAULTS = GameRules.register("enableFlightInVaults", GameRules.Category.PLAYER, booleanRule(false));
        NORMALIZED_ENABLED = GameRules.register("enableDifficultyLockModifiers", GameRules.Category.PLAYER, booleanRule(true));
        ENABLE_OLD_AFFINITY_HANDLING = GameRules.register("enableLegacyGodAffinityHandling", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_PLACING_VAULT_DOLLS = GameRules.register("enablePlacingVaultDolls", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_VAULT_DOLLS = GameRules.register("enableVaultDolls", GameRules.Category.PLAYER, booleanRule(true));
    }


    public static GameRules.Type<GameRules.BooleanValue> booleanRule(boolean defaultValue) {
        return GameRulesBooleanValueAccessor.create(defaultValue, ((minecraftServer, booleanValue) -> {}));
    }

    @SubscribeEvent
    public static void syncGameRules(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if(player == null) {
            GameruleHelper.syncGameRules(event.getPlayerList().getPlayers());
        }
        else {
            GameruleHelper.syncGameRules(player);
        }
    }
   
}
