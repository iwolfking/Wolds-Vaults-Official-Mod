package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The 25% carryover fold. Folds god tree gear attributes into a player's attribute snapshot:
 * the active tree at full value, every foreign tree's basic nodes at a quarter, nothing from any
 * tree while no charm is equipped, and the minor stars carried by the transfer slots of every god
 * that is not the active one at full value. The scale per tree comes from
 * {@link GodNodeCache#treeScale}, the same rule the node gate applies, so this fold and the
 * gate-driven effects can never disagree about what a tree is worth.
 *
 * <p>Folding into the snapshot rather than tracking vanilla attribute modifiers is deliberate —
 * the snapshot is rebuilt from scratch on every refresh, so a charm swap cannot leak stats, and
 * the client HUD stays correct through the base mod's existing snapshot sync.
 */
public final class GodCarryover {
    public static final float FOREIGN_TREE_SCALE = 0.25F;

    private static final Set<String> WARNED_UNSCALABLE = ConcurrentHashMap.newKeySet();

    private GodCarryover() {
    }

    /**
     * Adds every god tree contribution for {@code player} to {@code snapshot}. Called from the
     * tail of the base mod's snapshot computation; never cancels or replaces existing values.
     * The applied post-scale instances are also handed to {@link GodVanillaAttributes}, which
     * mirrors the few gear attributes the base mod only consumes from equipped gear onto real
     * vanilla attribute modifiers on the same rebuild cadence.
     */
    public static void addGodInformationToSnapshot(ServerPlayer player, AttributeSnapshot snapshot) {
        GodNodeAttributeSource source = GodNodeAttributeSource.get();
        if (source == GodNodeAttributeSource.NOOP) {
            return;
        }
        List<VaultGearAttributeInstance<?>> applied = new ArrayList<>();
        Optional<VaultGod> active = ActiveGodResolver.getActiveGod(player);
        for (VaultGod god : VaultGod.values()) {
            float scale = GodNodeCache.treeScale(player, god);
            if (scale >= 1.0F) {
                contribute(snapshot, source.getGearAttributes(player, god, GodNodeAttributeSource.Scope.ALL), 1.0F, applied);
            } else if (scale > 0.0F) {
                contribute(snapshot, source.getGearAttributes(player, god, GodNodeAttributeSource.Scope.BASIC), scale, applied);
            }
        }
        List<String> carried = new ArrayList<>();
        for (VaultGod god : VaultGod.values()) {
            if (active.isEmpty() || active.get() != god) {
                carried.addAll(minorTransfersOf(player, god));
            }
        }
        if (!carried.isEmpty()) {
            contribute(snapshot, source.getMinorTransferAttributes(player, carried), 1.0F, applied);
        }
        GodVanillaAttributes.reconcile(player, applied);
    }

    private static List<String> minorTransfersOf(ServerPlayer player, VaultGod god) {
        if (player.getServer() == null) {
            return Collections.emptyList();
        }
        return GodAlignmentData.get(player.getServer()).getMinorTransfers(player.getUUID(), god);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void contribute(AttributeSnapshot snapshot, List<VaultGearAttributeInstance<?>> values, float scale,
                                   List<VaultGearAttributeInstance<?>> applied) {
        for (VaultGearAttributeInstance<?> instance : values) {
            VaultGearAttribute attribute = instance.getAttribute();
            Object value = instance.getValue();
            if (scale != 1.0F) {
                if (!(value instanceof Number)) {
                    warnUnscalable(attribute);
                    continue;
                }
                value = attribute.scaleValue(VaultGearModifier.cast(attribute, value), scale).getValue();
            }
            snapshot.addAttributeValue(attribute, value);
            applied.add(new VaultGearAttributeInstance<>(attribute, value));
        }
    }

    private static void warnUnscalable(VaultGearAttribute<?> attribute) {
        String name = String.valueOf(attribute.getRegistryName());
        if (WARNED_UNSCALABLE.add(name)) {
            WoldsVaults.LOGGER.warn("Skipping god carryover for attribute {}: its value is not numeric, so scaling it "
                    + "to {}% would silently apply it in full.", name, (int) (FOREIGN_TREE_SCALE * 100));
        }
    }
}
