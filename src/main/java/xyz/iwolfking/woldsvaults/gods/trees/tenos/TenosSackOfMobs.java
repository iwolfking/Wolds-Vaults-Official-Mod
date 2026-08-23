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

/** Sack of Mobs: {@code log_base(base + kills)} over this vault's kills with a loot sack held. */
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

    public static void updateMultiplier(ServerPlayer player, float logBase) {
        GlobalDamageMultiplierRegistry.register(player, TenosNodes.key(TenosNodes.SACK_OF_MOBS),
                factor(logBase, kills(player).get()));
    }

    private static float factor(float logBase, int kills) {
        return (float) (Math.log(logBase + kills) / Math.log(logBase));
    }

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
