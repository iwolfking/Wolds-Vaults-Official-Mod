package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** The gear attributes a player's unlocked nodes contribute, per god; {@link #NOOP} until registered. */
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

    enum Scope {
        ALL,
        /** Only basic (plain stat) nodes - the subset eligible for foreign-tree carryover. */
        BASIC
    }

    List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, VaultGod god, Scope scope);

    /** Resolves the given minor node ids, whichever god they belong to, into gear attributes. */
    List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds);

    static GodNodeAttributeSource get() {
        return Holder.current;
    }

    /** Installs the node content implementation; replacing an already-installed source is logged. */
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
