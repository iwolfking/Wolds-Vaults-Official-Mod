package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sack of Mobs (r113): damage scales as {@code log1000(1000 + kills)}, counting only kills scored
 * this vault while a loot sack was held.
 *
 * <p>No such counter exists anywhere in either mod - the base mod's mob stat is keyed per mob type
 * and knows nothing about held items - so it is kept here, per player and per vault. It lives in
 * the shared {@link GodNodeState} player scratch rather than in a map of this class's own, which
 * is what resets it when the player leaves the vault and again on logout without this class owning
 * either listener.
 *
 * <p>The kill count only moves on a kill, so counting stays on the Forge death event; the
 * multiplier derived from it is pushed into the shared global damage registry once a second by
 * {@link TenosVaultHandlers.SackOfMobsHandler} rather than being recomputed on every hit, and that
 * handler's deactivation pass is what takes the factor back off.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosSackOfMobs {
    private TenosSackOfMobs() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void countKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getOffhandItem().getItem() instanceof VaultLootSackItem)) {
            return;
        }
        if (!TenosNodes.isActive(player, TenosNodes.SACK_OF_MOBS)) {
            return;
        }
        if (TenosVaultUtil.vaultOf(player) == null) {
            return;
        }
        kills(player).incrementAndGet();
    }

    /** Republishes the player's damage factor from their current kill count. */
    public static void updateMultiplier(ServerPlayer player, float logBase) {
        GlobalDamageMultiplierRegistry.register(player, TenosNodes.key(TenosNodes.SACK_OF_MOBS),
                factor(logBase, kills(player).get()));
    }

    private static float factor(float logBase, int kills) {
        return (float) (Math.log(logBase + kills) / Math.log(logBase));
    }

    /** The gods tab preview of Sack of Mobs: the multiplier the player's sack kills this vault give. */
    static GodNodePreviews.Preview preview(ServerPlayer player) {
        float logBase = TenosNodeHandlers.params(TenosNodes.SACK_OF_MOBS,
                TenosNodeHandlers.SackOfMobsParams.class).log_base();
        int kills = GodNodeState.<AtomicInteger>peek(player.getUUID(), TenosNodes.SACK_OF_MOBS)
                .map(AtomicInteger::get).orElse(0);
        float factor = factor(logBase, kills);
        String baseText = GodNodePreviews.number(logBase);
        return new GodNodePreviews.Working(VaultGod.TENOS)
                .formula("Damage multiplier", "log" + baseText + "(" + baseText + " + kills)")
                .input("kills", "kills this vault while holding a vault sack", String.valueOf(kills))
                .result("log" + baseText + "(" + GodNodePreviews.number(logBase + kills) + ")", factor)
                .inactive(!TenosNodes.isActive(player, TenosNodes.SACK_OF_MOBS))
                .build(factor);
    }

    private static AtomicInteger kills(ServerPlayer player) {
        return GodNodeState.get(player.getUUID(), TenosNodes.SACK_OF_MOBS, AtomicInteger::new);
    }
}
