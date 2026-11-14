package xyz.iwolfking.woldsvaults.spellcraft.play;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.spellcraft.roll.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;

/**
 * The spellcraft work done at the end of each server player tick, in this order: sync equipped sources, recast held
 * slots, mirror stats, roll missing glyph lines and refresh the spell bar.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class PlayerTicks {
    private static final int REFRESH_TICKS = 4;
    private static final int ROLL_SCAN_TICKS = 40;
    private static final int STAT_SYNC_TICKS = 20;

    private PlayerTicks() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % EquippedSources.SYNC_TICKS == 0) {
            EquippedSources.sync(player);
        }
        HeldSlots.recast(player);
        if (player.tickCount % STAT_SYNC_TICKS == 0) {
            StatMirror.sync(player);
        }
        if (player.tickCount % REFRESH_TICKS != 0) {
            return;
        }
        if (player.tickCount % ROLL_SCAN_TICKS == 0) {
            SpellcraftRoller.rollMissingSpellcraft(player);
        }
        SpellBars.refresh(player);
    }
}
