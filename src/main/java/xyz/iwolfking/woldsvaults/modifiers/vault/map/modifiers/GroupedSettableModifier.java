package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GroupedSettableModifier extends SettableValueVaultModifier<GroupedSettableModifier.Properties> {
    public GroupedSettableModifier(ResourceLocation id, GroupedSettableModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }


    public Stream<Modifiers.Entry> flatten(boolean display, RandomSource random) {
        return Stream.concat(((GroupedSettableModifier.Properties)this.properties).getChildren().stream().map((modifier) -> {
            return new Modifiers.Entry(modifier, false);
        }), Stream.of(new Modifiers.Entry(this, true)));
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private Map<String, Integer> children;

        public Properties(Map<String, Integer> children) {
            this.children = children;
        }

        public List<VaultModifier<?>> getChildren() {
            List<VaultModifier<?>> result = new ArrayList();
            Iterator var2 = this.children.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)var2.next();
                System.out.println(entry.getKey());
                VaultModifierRegistry.getOpt(new ResourceLocation((String)entry.getKey())).ifPresent((modifier) -> {
                    for(int i = 0; i < (Integer)entry.getValue(); ++i) {
                        if(modifier instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
                            settableValueVaultModifier.properties().setValue(getValue());
                            result.add(settableValueVaultModifier);
                        }
                        else {
                            result.add(modifier);
                        }
                    }

                });
            }

            return result;
        }
    }
}
