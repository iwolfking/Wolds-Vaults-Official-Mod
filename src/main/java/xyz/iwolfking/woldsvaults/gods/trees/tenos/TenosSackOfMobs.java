package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * Sack of Mobs (r113): damage scales as {@code log1000(1000 + kills)}, counting only kills scored
 * this vault while a loot sack was held.
 *
 * <p>No such counter exists anywhere in either mod - the base mod's mob stat is keyed per mob type
 * and knows nothing about held items - so it is kept here, per player and per vault, and reset
 * when the player leaves. The resulting multiplier is pushed into the shared global damage
 * registry once a second rather than being recomputed on every hit.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosSackOfMobs {
    public static float logBase() {
        return GodNodeValues.number(TenosNodes.SACK_OF_MOBS, "log_base");
    }

    private static final ResourceLocation DAMAGE_KEY = WoldsVaults.id("tenos_sack_of_mobs");
    private static final int RECONCILE_INTERVAL_TICKS = 20;

    private static final Map<UUID, Integer> KILLS = new ConcurrentHashMap<>();
    private static final Object OWNER = new Object();

    private TenosSackOfMobs() {
    }

    static void register() {
        CommonEvents.LISTENER_TICK.register(OWNER, data -> {
            Vault vault = data.getVault();
            TickClock clock = vault.get(Vault.CLOCK);
            if (clock == null || clock.get(TickClock.GLOBAL_TIME) % RECONCILE_INTERVAL_TICKS != 0) {
                return;
            }
            for (ServerPlayer player : TenosVaultUtil.runners(vault)) {
                updateMultiplier(player);
            }
        });
        CommonEvents.LISTENER_LEAVE.register(OWNER, data -> data.getListener().getPlayer().ifPresent(player -> {
            KILLS.remove(player.getUUID());
            GlobalDamageMultiplierRegistry.remove(player, DAMAGE_KEY);
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void countKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getOffhandItem().getItem() instanceof VaultLootSackItem)) {
            return;
        }
        if (!TenosNodes.hasMinor(player, TenosNodes.SACK_OF_MOBS)) {
            return;
        }
        if (TenosVaultUtil.vaultOf(player) == null) {
            return;
        }
        KILLS.merge(player.getUUID(), 1, Integer::sum);
    }

    private static void updateMultiplier(ServerPlayer player) {
        if (!TenosNodes.hasMinor(player, TenosNodes.SACK_OF_MOBS)) {
            GlobalDamageMultiplierRegistry.remove(player, DAMAGE_KEY);
            return;
        }
        int kills = KILLS.getOrDefault(player.getUUID(), 0);
        float factor = (float) (Math.log(logBase() + kills) / Math.log(logBase()));
        GlobalDamageMultiplierRegistry.register(player, DAMAGE_KEY, factor);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        KILLS.remove(event.getPlayer().getUUID());
    }
}
