package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

/**
 * The attack-damage half of Unstoppable Greed (r114): 10% of the player's item quantity plus item
 * rarity is added to attack damage. (The ability-power half is a {@code PLAYER_STAT} listener in
 * {@link TenosLootStats}, because ability power has a stat entry and attack damage does not.)
 *
 * <p>It rides the global damage registry rather than a dynamic gear attribute on purpose: emitting
 * {@code damage_increase} from the attribute provider would mean reading the player's own item
 * quantity while their snapshot is still being built. Item quantity and rarity only move on gear
 * changes, so a one second refresh is indistinguishable from live.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosUnstoppableGreed {
    private static final ResourceLocation DAMAGE_KEY = WoldsVaults.id("tenos_unstoppable_greed");
    private static final int REFRESH_INTERVAL_TICKS = 20;

    private static int tickCounter;

    private TenosUnstoppableGreed() {
    }

    @SubscribeEvent
    public static void refresh(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tickCounter < REFRESH_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!TenosNodes.hasMinor(player, TenosNodes.UNSTOPPABLE_GREED)) {
                GlobalDamageMultiplierRegistry.remove(player, DAMAGE_KEY);
                continue;
            }
            float factor = 1.0F + TenosLootStats.unstoppableGreedRatio() * TenosLootStats.lootStatSum(player);
            GlobalDamageMultiplierRegistry.register(player, DAMAGE_KEY, Math.max(1.0F, factor));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GlobalDamageMultiplierRegistry.remove(event.getPlayer(), DAMAGE_KEY);
    }
}
