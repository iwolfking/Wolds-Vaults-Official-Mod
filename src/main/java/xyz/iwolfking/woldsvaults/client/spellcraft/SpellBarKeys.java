package xyz.iwolfking.woldsvaults.client.spellcraft;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellSlotKeyMessage;

/**
 * The 16 spell slot keys (unbound by default, category "Spell Bar"). Each sends a press when it goes down and a
 * release when it comes up; instants cast on release so Charge can use the hold. Opening a screen releases any held
 * slot so holds and charges end cleanly.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public final class SpellBarKeys {
    public static final KeyMapping[] KEYS = new KeyMapping[SpellcraftItemData.SLOTS];
    private static final boolean[] DOWN = new boolean[SpellcraftItemData.SLOTS];

    private SpellBarKeys() {
    }

    /** Registers the slot keys; called from client setup. */
    public static void register() {
        for (int i = 0; i < KEYS.length; i++) {
            KEYS[i] = new KeyMapping("key." + WoldsVaults.MOD_ID + ".spell_slot_" + (i + 1), KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.category." + WoldsVaults.MOD_ID + ".spell_bar");
            ClientRegistry.registerKeyBinding(KEYS[i]);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || KEYS[0] == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = minecraft.player != null && minecraft.screen == null && minecraft.getConnection() != null;
        for (int i = 0; i < KEYS.length; i++) {
            boolean down = active && KEYS[i].isDown();
            if (down != DOWN[i]) {
                DOWN[i] = down;
                if (minecraft.getConnection() != null) {
                    ModNetwork.sendToServer(new SpellSlotKeyMessage(i, down));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        java.util.Arrays.fill(DOWN, false);
        ClientSpellBar.reset();
    }
}
