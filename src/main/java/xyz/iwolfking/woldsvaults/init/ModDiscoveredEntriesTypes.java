package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage.Type;
import xyz.iwolfking.vhapi.api.extensions.general.DiscoveredEntriesTypeFactory;

public class ModDiscoveredEntriesTypes {
    public static final Type THEME = DiscoveredEntriesTypeFactory.INSTANCE.create("THEME");
    public static final Type WOLD_RECIPE = DiscoveredEntriesTypeFactory.INSTANCE.create("WOLD_RECIPE");
    public static final Type PLAYER_GREED = DiscoveredEntriesTypeFactory.INSTANCE.create("PLAYER_GREED");

    public static void init() {
    }
}