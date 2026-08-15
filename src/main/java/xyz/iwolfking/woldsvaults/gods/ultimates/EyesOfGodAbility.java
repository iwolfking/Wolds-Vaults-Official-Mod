package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.event.common.DiscoverVaultRoomEvent;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.stat.DetailedRoomStat;
import iskallia.vault.core.vault.stat.DiscoveredRoomStat;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Optional;

/**
 * Tenos' ultimate. Reveals every room in a square of vault cells centred on the caster, as if they
 * had walked each one.
 *
 * <p>It writes through {@code DiscoveredRoomStat.recordDiscovery} with the {@code SYSTEM} context,
 * the same path the Vault Globe uses, which means the minimap and the map overlay update with no
 * packet work of our own: {@code ROOMS_DISCOVERED} is already a client-synced stat pushed by the
 * per-tick vault sync. Rooms the caster already knows are skipped by that method's own dedupe.
 *
 * <p>The reveal is the caster's alone — party members read their own discovery stat, and the sheet
 * describes a radius around the caster, not a shared broadcast.
 */
public class EyesOfGodAbility extends GodUltimateAbility {
    public EyesOfGodAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                            float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public EyesOfGodAbility() {
    }

    @Override
    public VaultGod getGod() {
        return VaultGod.TENOS;
    }

    @Override
    protected ActionResult doUltimate(ServerPlayer player, int level) {
        Optional<Vault> maybeVault = ServerVaults.get(player.level);
        if (maybeVault.isEmpty()) {
            player.displayClientMessage(new TranslatableComponent("message.woldsvaults.eyes_of_god_outside_vault")
                    .withStyle(ChatFormatting.GRAY), true);
            return ActionResult.fail();
        }
        Vault vault = maybeVault.get();
        WorldManager world = vault.get(Vault.WORLD);
        if (world == null || !(world.get(WorldManager.GENERATOR) instanceof GridGenerator generator)
                || !(generator.get(GridGenerator.LAYOUT) instanceof VaultGridLayout layout)) {
            WoldsVaults.LOGGER.error("Eyes of God found no grid layout in the vault {}; nothing was revealed.",
                    vault.get(Vault.ID));
            return ActionResult.fail();
        }
        StatsCollector stats = vault.get(Vault.STATS);
        StatCollector collector = stats == null ? null : stats.get(player.getUUID());
        DiscoveredRoomStat discovered = collector == null ? null : collector.get(StatCollector.ROOMS_DISCOVERED);
        if (discovered == null) {
            WoldsVaults.LOGGER.error("Eyes of God found no room-discovery stat for {}; nothing was revealed.",
                    player.getGameProfile().getName());
            return ActionResult.fail();
        }
        DetailedRoomStat detailed = collector.getDetailedRoomsDiscovered();
        int cellX = generator.get(GridGenerator.CELL_X);
        int cellZ = generator.get(GridGenerator.CELL_Z);
        RegionPos centre = RegionPos.ofBlockPos(player.blockPosition(), cellX, cellZ);
        int radius = UltimateLevels.eyesRadius(level);
        int revealed = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                RegionPos region = centre.add(x, z);
                Optional<DiscoveredRoomStat.RoomDiscovery> discovery =
                        DiscoveredRoomStat.resolveRoom(vault, generator, layout, region);
                if (discovery.isEmpty()) {
                    continue;
                }
                if (DiscoveredRoomStat.recordDiscovery(player, vault, discovered, detailed, discovery.get(),
                        DiscoverVaultRoomEvent.Context.SYSTEM)) {
                    revealed++;
                }
            }
        }
        player.displayClientMessage(new TranslatableComponent("message.woldsvaults.eyes_of_god_revealed", revealed)
                .withStyle(ChatFormatting.AQUA), true);
        return ActionResult.successCooldownImmediate();
    }
}
