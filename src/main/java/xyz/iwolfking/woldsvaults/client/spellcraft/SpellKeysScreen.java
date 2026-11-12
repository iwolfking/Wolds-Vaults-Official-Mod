package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ServerboundOpenAbilitiesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;

import javax.annotation.Nullable;

/**
 * Vanilla Controls, opened from a spell bar's Keys button. From the skill menu it has no parent screen (the skill menu
 * is a server container, closed before this opens) and asks the server to reopen the Abilities tab once it closes
 * to the game (not when it opens its own sub-screens); from the focus screen it returns to that screen.
 */
public class SpellKeysScreen extends ControlsScreen {
    private final boolean reopenAbilities;

    public SpellKeysScreen(@Nullable Screen parent, boolean reopenAbilities) {
        super(parent, Minecraft.getInstance().options);
        this.reopenAbilities = reopenAbilities;
    }

    /** Opens the key binds from the skill menu: closes its container first so client and server stay in step. */
    public static void openFromSkillMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        minecraft.setScreen(new SpellKeysScreen(null, true));
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft minecraft = Minecraft.getInstance();
        if (!this.reopenAbilities) {
            return;
        }
        minecraft.tell(() -> {
            if (minecraft.screen == null && minecraft.player != null && minecraft.getConnection() != null) {
                ModNetwork.CHANNEL.sendToServer(ServerboundOpenAbilitiesMessage.INSTANCE);
            }
        });
    }
}
