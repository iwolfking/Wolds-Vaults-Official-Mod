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
    private static boolean quickfireHidden;

    private SpellBarKeys() {
    }

    /** Registers the slot keys; called from client setup. */
    public static void register() {
        for (int i = 0; i < KEYS.length; i++) {
            KEYS[i] = new SlotKeyMapping(i, "key." + WoldsVaults.MOD_ID + ".spell_slot_" + (i + 1),
                    "key.category." + WoldsVaults.MOD_ID + ".spell_bar");
            ClientRegistry.registerKeyBinding(KEYS[i]);
        }
    }

    /** A slot key that sorts by slot number in the Controls list instead of by name (1, 2 ... 16, not 1, 10, 11). */
    private static final class SlotKeyMapping extends KeyMapping {
        private final int slot;

        SlotKeyMapping(int slot, String name, String category) {
            super(name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category);
            this.slot = slot;
        }

        @Override
        public int compareTo(KeyMapping other) {
            if (other instanceof SlotKeyMapping slotKey) {
                return Integer.compare(this.slot, slotKey.slot);
            }
            return super.compareTo(other);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || KEYS[0] == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!quickfireHidden && minecraft.options != null) {
            hideQuickfireKeys(minecraft);
        }
        if (minecraft.player != null && !minecraft.isPaused()) {
            tickClientCooldowns();
        }
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

    /**
     * Counts the client's copy of ability cooldowns down every tick. VH only syncs them on casts and every half second
     * for the selected ability, so without this the HUD sat on stale values (a 1 s cooldown stuck at "1s", or coming
     * back after it had already ended); every server sync still overwrites the local count.
     */
    private static void tickClientCooldowns() {
        iskallia.vault.skill.tree.AbilityTree tree = iskallia.vault.client.data.ClientAbilityData.getTree();
        if (tree == null) {
            return;
        }
        tree.iterate(iskallia.vault.skill.ability.effect.spi.core.Ability.class, ability -> ability.getCooldown().ifPresent(cooldown -> {
            cooldown.decrement();
            if (cooldown.getRemainingTicks() <= 0 && cooldown.getRemainingDelayTicks() <= 0) {
                ability.setCooldownFromSync(0, 0, 0);
            }
        }));
    }

    /**
     * Retires VH's per-ability quick-fire keys: they are unbound and taken out of the Controls list, since abilities
     * are cast from spell slots now (the server ignores their packet too).
     */
    private static void hideQuickfireKeys(Minecraft minecraft) {
        quickfireHidden = true;
        java.util.List<KeyMapping> kept = new java.util.ArrayList<>();
        int hidden = 0;
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            if (iskallia.vault.init.ModKeybinds.QUICKFIRE_CATEGORY.equals(mapping.getCategory())) {
                mapping.setKey(InputConstants.UNKNOWN);
                hidden++;
            } else {
                kept.add(mapping);
            }
        }
        if (hidden > 0) {
            KeyMapping.resetMapping();
            ((xyz.iwolfking.woldsvaults.mixins.accessors.OptionsKeyMappingsAccessor) minecraft.options)
                    .woldsvaults$setKeyMappings(kept.toArray(new KeyMapping[0]));
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        xyz.iwolfking.woldsvaults.spellcraft.haste.StatMirror.clearClient();
        java.util.Arrays.fill(DOWN, false);
        ClientSpellBar.reset();
        ClientGlyphViews.reset();
    }
}
