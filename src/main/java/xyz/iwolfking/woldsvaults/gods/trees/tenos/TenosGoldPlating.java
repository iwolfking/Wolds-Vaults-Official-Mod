package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gold Plating (r116): 75% of incoming damage is blocked, paid for with
 * {@code 2 * log10(10 + resisted)} gold per hit.
 *
 * <p>The charge is accumulated and settled at most once a second, never per hit.
 * {@code CoinDefinition.extractCurrency} walks the player's whole inventory including coin pouches
 * and keyrings, so billing on every hit would be twenty full inventory scans a second per player.
 * Batching keeps the total cost exactly as specified while collapsing the scans into one.
 *
 * <p>A player who cannot pay keeps the debt: the resistance stays on and the outstanding gold is
 * taken as soon as they have it again, rather than the node flickering off mid-fight.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosGoldPlating {
    public static final float DAMAGE_MULTIPLIER = 0.25F;
    public static final float COST_COEFFICIENT = 2.0F;
    public static final float COST_OFFSET = 10.0F;

    private static final ResourceLocation STAGE_ID = WoldsVaults.id("tenos_gold_plating");
    private static final int SETTLE_INTERVAL_TICKS = 20;

    private static final Map<UUID, Double> DEBT = new ConcurrentHashMap<>();
    private static int tickCounter;

    private TenosGoldPlating() {
    }

    static void register() {
        FinalDamageStage.register(STAGE_ID, FinalDamageStage.ORDER_REDUCTION, (event, amount) -> {
            if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
                return amount;
            }
            if (!TenosNodes.hasMinor(player, TenosNodes.GOLD_PLATING)) {
                return amount;
            }
            float reduced = amount * DAMAGE_MULTIPLIER;
            float resisted = amount - reduced;
            if (resisted > 0.0F) {
                DEBT.merge(player.getUUID(), (double) (COST_COEFFICIENT * (float) Math.log10(COST_OFFSET + resisted)), Double::sum);
            }
            return reduced;
        });
    }

    @SubscribeEvent
    public static void settleDebts(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tickCounter < SETTLE_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || DEBT.isEmpty()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            settle(player);
        }
    }

    private static void settle(ServerPlayer player) {
        Double owed = DEBT.get(player.getUUID());
        if (owed == null || owed < 1.0) {
            return;
        }
        int gold = (int) Math.floor(owed);
        List<InventoryUtil.ItemAccess> items = InventoryUtil.findAllItems(player);
        ItemStack price = new ItemStack(ModBlocks.VAULT_GOLD, gold);
        if (!CoinDefinition.hasEnoughCurrency(items, price)) {
            WoldsVaults.LOGGER.debug("Gold Plating could not charge {} {} gold; the debt is carried over.",
                    player.getGameProfile().getName(), gold);
            return;
        }
        CoinDefinition.extractCurrency(player, items, price);
        DEBT.merge(player.getUUID(), -(double) gold, Double::sum);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DEBT.remove(event.getPlayer().getUUID());
    }
}
