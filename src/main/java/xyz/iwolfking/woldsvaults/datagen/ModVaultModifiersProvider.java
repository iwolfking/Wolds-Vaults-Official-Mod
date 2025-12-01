package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifiersProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.modifiers.vault.ChampionDropsVaultModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifiersProvider extends AbstractVaultModifiersProvider {
    public ModVaultModifiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("test", builder -> {
            builder.addModifiersToGroup(VaultMod.id("modifier_type/test"), map -> {
                map.put(VaultMod.id("test"), grouped(VaultMod.id("test"), "Test", TextColor.fromLegacyFormat(ChatFormatting.AQUA), "Test", "Test", VaultMod.id("test"), stringIntegerMap -> {
                    stringIntegerMap.put("the_vault:baren", 1);
                }));
            });
        });
    }

    public static VaultModifier<?> grouped(ResourceLocation id, String name, TextColor color, String description, String descriptionFormatted, ResourceLocation icon, Consumer<Map<String, Integer>> modifiersConsumer) {
        Map<String, Integer> modifiers = new HashMap<>();
        modifiersConsumer.accept(modifiers);

        return new GroupedModifier(id, new GroupedModifier.Properties(modifiers), display(name, color, description, descriptionFormatted, icon));
    }

    public static VaultModifier.Display display(String name, TextColor color, String description, String descriptionFormatted, ResourceLocation icon) {
        return new VaultModifier.Display(name, color, description, descriptionFormatted, icon);
    }
}
