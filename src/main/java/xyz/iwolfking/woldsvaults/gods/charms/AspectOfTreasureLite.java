package xyz.iwolfking.woldsvaults.gods.charms;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.MythicVaultCharmItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect of Treasure Lite - the mythic charm's weaker copy of the Aspect of Treasure prestige
 * power. Opening chests builds stacks; each stack adds the charm's rolled percentage to item
 * quantity AND item rarity; taking OR dealing damage resets the stacks, matching the prestige
 * power's combat reset; the stack cap is one per fifty piety, read from the charm's stored
 * scale units. Because it contributes through the same additive player-stat events the prestige
 * power uses, the two stack together additively by construction.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AspectOfTreasureLite {
    private static final Map<UUID, Integer> STACKS = new ConcurrentHashMap<>();
    private static final int UNITS_PER_STACK = 5;

    private AspectOfTreasureLite() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AspectOfTreasureLite::register);
    }

    private static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AspectOfTreasureLite::onDamageTaken);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AspectOfTreasureLite::onLogout);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AspectOfTreasureLite::onServerStopping);
        CommonEvents.PLAYER_STAT.of(PlayerStat.ITEM_QUANTITY).register(AspectOfTreasureLite.class, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() + bonus(player));
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.ITEM_RARITY).register(AspectOfTreasureLite.class, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                data.setValue(data.getValue() + bonus(player));
            }
        });
        CommonEvents.CHEST_LOOT_GENERATION.post().register(AspectOfTreasureLite.class, event -> {
            ServerPlayer player = event.getPlayer();
            if (player != null && perStack(player) > 0.0F) {
                STACKS.merge(player.getUUID(), 1, Integer::sum);
            }
        });
    }

    private static float bonus(ServerPlayer player) {
        float perStack = perStack(player);
        if (perStack <= 0.0F) {
            return 0.0F;
        }
        int cap = stackCap(player);
        if (cap <= 0) {
            return 0.0F;
        }
        int stacks = Math.min(STACKS.getOrDefault(player.getUUID(), 0), cap);
        return perStack * stacks;
    }

    private static float perStack(ServerPlayer player) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player)
                .getAttributeValue(ModGearAttributes.ASPECT_OF_TREASURE_LITE,
                        iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger.floatSum());
    }

    private static int stackCap(ServerPlayer player) {
        ItemStack charm = VaultCharmItem.getCharm(player).orElse(ItemStack.EMPTY);
        if (!MythicVaultCharmItem.isMythic(charm)) {
            return 0;
        }
        return MythicCharmRolls.storedUnits(charm) / UNITS_PER_STACK;
    }

    public static void onDamageTaken(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STACKS.remove(player.getUUID());
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            STACKS.remove(attacker.getUUID());
        }
    }

    /**
     * Drops a leaving player's stacks. Nothing else removed them: they are keyed by player id and
     * only ever reset by combat, so a player who logged out mid-stack left an entry behind for the
     * life of the process.
     */
    private static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        STACKS.remove(event.getPlayer().getUUID());
    }

    /** Drops every player's stacks when the server stops, so they cannot cross a world switch. */
    private static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        STACKS.clear();
    }
}
