package xyz.iwolfking.woldsvaults.api.core.theme;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.config.ThemeAugmentLoreConfig;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.ArchitectVaultLayout;
import iskallia.vault.core.world.generator.layout.ClassicVaultLayout;
import iskallia.vault.core.world.generator.layout.DIYVaultLayout;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.generator.theme.ClassicVaultTheme;
import iskallia.vault.core.world.generator.theme.DIYVaultTheme;
import iskallia.vault.core.world.generator.theme.Theme;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.api.util.WoldTexFX;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;

import java.util.List;
import java.util.Optional;

public class InfusedCrystalTheme extends CrystalTheme {
    private ResourceLocation id;

    public InfusedCrystalTheme(ResourceLocation id) {
        this.id = id;
    }

    public InfusedCrystalTheme() {
    }

    @Override
    public void configure(Vault vault, RandomSource random, String sigil) {
        ThemeKey key = VaultRegistry.THEME.getKey(this.id);
        if (key == null) {
            VaultMod.LOGGER.error("Theme with key [" + this.id + "] does not exist!");
        } else {
            Theme theme = key.get(vault.get(Vault.VERSION));
            if (theme != null) {
                vault.ifPresent(Vault.WORLD, (world) -> {
                    world.ifPresent(WorldManager.GENERATOR, (generator) -> {
                        if (generator instanceof GridGenerator grid) {
                            this.configureLayout(grid.get(GridGenerator.LAYOUT), theme);
                        }
                    });
                    vault.ifPresent(Vault.MODIFIERS, (modifiers -> {
                        int vaultLevel = vault.getOr(Vault.LEVEL, new VaultLevel()).get();
                        Optional<ResourceLocation> modifierPool = ThemeModifiersConfig.getModifierPoolForInfusedTheme(id, vaultLevel);
                        modifierPool.ifPresent(resourceLocation -> {
                            List<VaultModifier<?>> modifiersFromPool = VaultModifierUtils.getModifiersFromPool(resourceLocation, vaultLevel);
                            modifiersFromPool.forEach(vaultModifier -> {
                                modifiers.addModifier(vaultModifier, 1, true, ChunkRandom.ofNanoTime());
                            });
                        });
                    }));
                    world.setTheme(key, vault.get(Vault.VERSION));
                });
            }
        }
    }

    private void configureLayout(GridLayout layout, Theme theme) {
        if (layout instanceof ClassicVaultLayout classic) {
            if (theme instanceof ClassicVaultTheme classicTheme) {
                classic.set(ClassicVaultLayout.START_POOL, classicTheme.getStarts()).set(ClassicVaultLayout.ROOM_POOL, classicTheme.getRooms()).set(ClassicVaultLayout.TUNNEL_POOL, classicTheme.getTunnels());
            } else if (theme instanceof DIYVaultTheme diyTheme) {
                classic.set(ClassicVaultLayout.START_POOL, diyTheme.getStarts()).set(ClassicVaultLayout.ROOM_POOL, diyTheme.getCommonRooms()).set(ClassicVaultLayout.TUNNEL_POOL, diyTheme.getTunnels());
            }
        } else if (layout instanceof DIYVaultLayout diy) {
            if (theme instanceof ClassicVaultTheme classicTheme) {
                diy.set(DIYVaultLayout.START_POOL, classicTheme.getStarts()).set(DIYVaultLayout.COMMON_ROOM_POOL, classicTheme.getRooms()).set(DIYVaultLayout.CHALLENGE_ROOM_POOL, classicTheme.getRooms()).set(DIYVaultLayout.OMEGA_ROOM_POOL, classicTheme.getRooms()).set(DIYVaultLayout.TUNNEL_POOL, classicTheme.getTunnels());
            } else if (theme instanceof DIYVaultTheme diyTheme) {
               diy.set(DIYVaultLayout.START_POOL, diyTheme.getStarts()).set(DIYVaultLayout.COMMON_ROOM_POOL, diyTheme.getCommonRooms()).set(DIYVaultLayout.CHALLENGE_ROOM_POOL, diyTheme.getChallengeRooms()).set(DIYVaultLayout.OMEGA_ROOM_POOL, diyTheme.getOmegaRooms()).set(DIYVaultLayout.TUNNEL_POOL, diyTheme.getTunnels());
            }
        } else if (layout instanceof ArchitectVaultLayout architect) {
            if (theme instanceof ClassicVaultTheme classicTheme) {
                architect.set(ArchitectVaultLayout.START_POOL, classicTheme.getStarts()).set(ArchitectVaultLayout.COMMON_ROOM_POOL, classicTheme.getRooms()).set(ArchitectVaultLayout.CHALLENGE_ROOM_POOL, classicTheme.getRooms()).set(ArchitectVaultLayout.OMEGA_ROOM_POOL, classicTheme.getRooms()).set(ArchitectVaultLayout.TUNNEL_POOL, classicTheme.getTunnels());
            } else if (theme instanceof DIYVaultTheme diyTheme) {
                 architect.set(ArchitectVaultLayout.START_POOL, diyTheme.getStarts()).set(ArchitectVaultLayout.COMMON_ROOM_POOL, diyTheme.getCommonRooms()).set(ArchitectVaultLayout.CHALLENGE_ROOM_POOL, diyTheme.getChallengeRooms()).set(ArchitectVaultLayout.OMEGA_ROOM_POOL, diyTheme.getOmegaRooms()).set(ArchitectVaultLayout.TUNNEL_POOL, diyTheme.getTunnels());
            }
        }

    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
        ThemeKey theme = VaultRegistry.THEME.getKey(this.id);
        if (theme == null) {
            tooltip.add((new TextComponent("Theme: ")).append(new TextComponent("Unknown").withStyle(ChatFormatting.RED)));
        } else {
            Optional<ThemeAugmentLoreConfig.AugmentLore> augmentLore = ModConfigs.THEME_AUGMENT_LORE.getAugmentLore(theme.getId());
            if (augmentLore.isPresent()) {
                TextComponent var10001 = new TextComponent("Theme: ");
                String var10004 = augmentLore.get().displayName;
                tooltip.add(var10001.append((WoldTexFX.rainbowEffect(new TextComponent(var10004 + ": " + theme.getName())).withStyle(ChatFormatting.GRAY))));
            } else {
                tooltip.add((new TextComponent("Theme: ")).append((WoldTexFX.rainbowEffect(new TextComponent(theme.getName())).withStyle(ChatFormatting.GRAY))));
            }
        }
    }

    public Optional<Integer> getColor() {
        ThemeKey theme = VaultRegistry.THEME.getKey(this.id);
        return theme == null ? Optional.empty() : Optional.of(theme.getColor());
    }

    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.IDENTIFIER.writeNbt(this.id).ifPresent((id) -> nbt.put("id", id));
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag compound) {
        this.id = Adapters.IDENTIFIER.readNbt(compound.get("id")).orElse(null);
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.IDENTIFIER.writeJson(this.id).ifPresent((id) -> json.add("id", id));
        return Optional.of(json);
    }

    public void readJson(JsonObject object) {
        this.id = Adapters.IDENTIFIER.readJson(object.get("id")).orElse(null);
    }
}
