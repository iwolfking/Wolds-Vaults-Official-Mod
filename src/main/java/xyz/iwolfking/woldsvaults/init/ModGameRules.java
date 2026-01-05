package xyz.iwolfking.woldsvaults.init;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.mixins.GameRulesBooleanValueAccessor;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class ModGameRules {

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_FLIGHT_IN_VAULTS;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_BREAKING_SPAWNERS_IN_VAULT;
    public static GameRules.Key<GameRules.BooleanValue> NORMALIZED_ENABLED;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_OLD_AFFINITY_HANDLING;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_PLACING_VAULT_DOLLS;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_VAULT_DOLLS;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_VAULTS;
    public static GameRules.Key<GameRules.BooleanValue> UNLIMITED_ALCHEMY_OVERSTACKING;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_SKILL_ALTARS;
    public static GameRules.Key<GameRules.BooleanValue> ENABLE_MODIFIER_WORKBENCH;

    public static void initialize() {
        ALLOW_FLIGHT_IN_VAULTS = GameRules.register("enableFlightInVaults", GameRules.Category.PLAYER, booleanRule(false));
        ALLOW_BREAKING_SPAWNERS_IN_VAULT = GameRules.register("enableSpawnerBreakingInVaults", GameRules.Category.PLAYER, booleanRule(false));
        NORMALIZED_ENABLED = GameRules.register("enableDifficultyLockModifiers", GameRules.Category.PLAYER, booleanRule(true));
        ENABLE_OLD_AFFINITY_HANDLING = GameRules.register("enableLegacyGodAffinityHandling", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_PLACING_VAULT_DOLLS = GameRules.register("enablePlacingVaultDolls", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_VAULT_DOLLS = GameRules.register("enableVaultDolls", GameRules.Category.PLAYER, booleanRule(true));
        ENABLE_VAULTS = GameRules.register("enableVaults", GameRules.Category.PLAYER, booleanRule(true));
        UNLIMITED_ALCHEMY_OVERSTACKING = GameRules.register("unlimitedAlchemyOverflow", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_SKILL_ALTARS = GameRules.register("enableSkillAltars", GameRules.Category.PLAYER, booleanRule(false));
        ENABLE_MODIFIER_WORKBENCH = GameRules.register("enableModifierWorkbench", GameRules.Category.PLAYER, booleanRule(false));
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
