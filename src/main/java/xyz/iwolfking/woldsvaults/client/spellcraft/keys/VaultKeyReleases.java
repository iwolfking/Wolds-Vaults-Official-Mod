package xyz.iwolfking.woldsvaults.client.spellcraft.keys;

import iskallia.vault.init.ModKeybinds;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.AbilityQuickselectMessage;
import iskallia.vault.network.message.ServerboundAbilityKeyMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Polls Vault Hunters' cast key and quick-fire keys each client tick. When a screen opens while one is held, or was
 * pressed in the same tick, it sends Vault Hunters' own release for that key, which Vault Hunters skips while a screen
 * is open.
 */
final class VaultKeyReleases {
    private static final Set<String> QUICKFIRE_HELD = new HashSet<>();
    private static boolean castHeld;

    private VaultKeyReleases() {
    }

    static void tick(Minecraft minecraft) {
        KeyMapping cast = ModKeybinds.abilityKey;
        Map<String, KeyMapping> quickfire = ModKeybinds.abilityQuickfireKey;
        if (cast == null || quickfire == null) {
            return;
        }
        if (minecraft.player == null || minecraft.getConnection() == null) {
            reset();
            return;
        }
        boolean screen = minecraft.screen != null;
        castHeld = track(cast, castHeld, screen, () -> ServerboundAbilityKeyMessage.send(ServerboundAbilityKeyMessage.Opcode.KeyUp));
        for (Map.Entry<String, KeyMapping> entry : quickfire.entrySet()) {
            String group = entry.getKey();
            boolean held = track(entry.getValue(), QUICKFIRE_HELD.contains(group), screen,
                    () -> ModNetwork.CHANNEL.sendToServer(new AbilityQuickselectMessage(group, AbilityQuickselectMessage.RELEASED)));
            if (held) {
                QUICKFIRE_HELD.add(group);
            } else {
                QUICKFIRE_HELD.remove(group);
            }
        }
    }

    /** Whether {@code key} counts as held after this tick; runs {@code release} when a screen covers a held key. */
    private static boolean track(KeyMapping key, boolean held, boolean screen, Runnable release) {
        boolean pressed = false;
        while (key.consumeClick()) {
            pressed = true;
        }
        if (!screen) {
            return key.isDown();
        }
        if (held || pressed) {
            release.run();
        }
        return false;
    }

    static void reset() {
        castHeld = false;
        QUICKFIRE_HELD.clear();
    }
}
