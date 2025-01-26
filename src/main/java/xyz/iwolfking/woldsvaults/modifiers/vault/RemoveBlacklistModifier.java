package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RemoveBlacklistModifier extends VaultModifier<RemoveBlacklistModifier.Properties> {

    public RemoveBlacklistModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public static class Properties {
        @Expose
        private final List<String> whitelist;

        @Expose
        private final boolean useAsBlacklist;

        @Expose
        private final boolean appliesToItems;

        @Expose
        private final boolean appliesToBlocks;

        public Properties(List<String> whitelist, boolean useAsBlacklist, boolean appliesToItems, boolean appliesToBlocks) {
            this.whitelist = whitelist;
            this.useAsBlacklist = useAsBlacklist;
            this.appliesToItems = appliesToItems;
            this.appliesToBlocks = appliesToBlocks;
        }



        public List<String> getWhitelist() {
            return this.whitelist;
        }

        public boolean shouldUseAsBlacklist() {
            return this.useAsBlacklist;
        }

        public boolean shouldApplyToItems() {
            return appliesToBlocks;
        }

        public boolean shouldApplyToBlocks() {
            return appliesToItems;
        }

    }
}
