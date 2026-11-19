package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SkillMenuSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.MirroredStats;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellSlotKeyMessage;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import java.util.Arrays;

/**
 * Polls the spell slot keys each client tick in slot mode, sending a press when one goes down and a release when it
 * comes up; opening a screen or switching to classic mode releases any held slot. The same tick restores quick-fire
 * binds once and counts cooldowns down, and {@link #reset()} clears the client spellcraft caches on logout.
 */
public final class SpellBarKeys {
    private static final boolean[] DOWN = new boolean[SpellcraftItemData.SLOTS];

    private SpellBarKeys() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ModKeybinds.spellSlots[0] == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        QuickfireBindMigration.restoreIfReady(minecraft);
        if (minecraft.player != null && !minecraft.isPaused()) {
            ClientCooldownTicker.tick();
        }
        SkillMenuSpellBar.forgetClosed();
        boolean active = minecraft.player != null && minecraft.screen == null && minecraft.getConnection() != null
                && ClientSpellBar.mode() == BarMode.SLOT;
        for (int i = 0; i < ModKeybinds.spellSlots.length; i++) {
            boolean down = active && ModKeybinds.spellSlots[i].isDown();
            if (down != DOWN[i]) {
                DOWN[i] = down;
                if (minecraft.getConnection() != null) {
                    ModNetwork.sendToServer(new SpellSlotKeyMessage(i, down));
                }
            }
        }
    }

    public static void reset() {
        MirroredStats.reset();
        Arrays.fill(DOWN, false);
        ClientSpellBar.reset();
        ClientGlyphViews.reset();
        SkillMenuSpellBar.reset();
    }
}
