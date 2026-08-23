package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service seam between the god core and god tree node content. Wave 1 ships the no-op default;
 * wave 2 registers the real implementation, which reads
 * {@link GodAlignmentData#getSpentLedger(java.util.UUID, VaultGod)} to decide which nodes are
 * unlocked and returns the vault gear attributes they contribute.
 *
 * <p>The carryover fold ({@link GodCarryover}) is the only caller: it asks for the active tree at
 * {@link Scope#ALL}, each foreign tree at {@link Scope#BASIC} (whose values it then scales to 25%),
 * and the minors carried by every non-active god's transfer slots at full value.
 */
public interface GodNodeAttributeSource {
    GodNodeAttributeSource NOOP = new GodNodeAttributeSource() {
        @Override
        public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, VaultGod god, Scope scope) {
            return Collections.emptyList();
        }

        @Override
        public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
            return Collections.emptyList();
        }
    };

    /** Which nodes of a tree a request covers. */
    enum Scope {
        /** Every unlocked node of the tree. */
        ALL,
        /** Only basic (plain stat) nodes — the subset eligible for foreign-tree carryover. */
        BASIC
    }

    List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, VaultGod god, Scope scope);

    /**
     * Resolves the given minor node ids, wherever they live, into their gear attribute
     * contributions. Ids come from the transfer slots of every god that is not the active one.
     */
    List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds);

    static GodNodeAttributeSource get() {
        return Holder.current;
    }

    /**
     * Installs the node content implementation. Called once during wave-2 setup; replacing an
     * already-installed source is logged as a mistake rather than silently accepted.
     */
    static void register(GodNodeAttributeSource source) {
        Holder.install(source);
    }

    final class Holder {
        private static GodNodeAttributeSource current = NOOP;

        private Holder() {
        }

        private static void install(GodNodeAttributeSource source) {
            if (current != NOOP) {
                xyz.iwolfking.woldsvaults.WoldsVaults.LOGGER.error(
                        "GodNodeAttributeSource was already registered as {}; replacing it with {}.",
                        current.getClass().getName(), source.getClass().getName());
            }
            current = source;
        }
    }
}
