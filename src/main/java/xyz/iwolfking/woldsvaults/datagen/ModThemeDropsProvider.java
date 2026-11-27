package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonElement;
import iskallia.vault.VaultMod;
import iskallia.vault.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.config.ThemeDropsConfig;
import xyz.iwolfking.woldsvaults.config.ThemeDropsConfig.Rule;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The theme drops: theme groups and the rules that graft a payload table into tiers of the chests they name. Tidebreaker
 * pieces drop from ornate chests and strongboxes in underwater vaults from Scavenger 1, taking a share of the omega
 * and mythic draws. Refuses a theme id the pack's theme index does not list, a payload this mod does not
 * generate, and a tier past mythic.
 */
public class ModThemeDropsProvider extends AbstractWoldsTableProvider {
    private static final int OMEGA = 3;
    private static final int MYTHIC = 4;

    public ModThemeDropsProvider(DataGenerator generator) {
        super(generator, "loot/theme_drops");
    }

    @Override
    public void registerConfigs() {
        ThemeDropsConfig table = new ThemeDropsConfig();
        table.themeGroups = new LinkedHashMap<>();
        table.rules = new LinkedHashMap<>();

        group(table, "underwater", "classic_vault_undersea", "classic_vault_sea_floor", "classic_vault_flooded",
                "classic_vault_shipwreck", "vault_lost_depths");

        Rule tidebreaker = rule("underwater", MilestoneRankLadder.SCAVENGER_1, ModThemeDropPayloadsProvider.TIDEBREAKER,
                ModBlocks.ORNATE_CHEST, ModBlocks.ORNATE_STRONGBOX);
        tidebreaker.shares.put(OMEGA, 0.001D);
        tidebreaker.shares.put(MYTHIC, 0.01D);
        table.rules.put("tidebreaker", tidebreaker);

        requireKnown(table);
        this.table("theme_drops", table);
    }

    private static void group(ThemeDropsConfig table, String name, String... themes) {
        table.themeGroups.put(name, Arrays.stream(themes).map(theme -> VaultMod.id(theme).toString()).toList());
    }

    private static Rule rule(String themeGroup, int minRank, ResourceLocation payload, Block... chests) {
        Rule rule = new Rule();
        rule.themeGroup = themeGroup;
        rule.minRank = minRank;
        rule.payload = payload.toString();
        rule.chests = Arrays.stream(chests).map(chest -> chest.getRegistryName().toString()).toList();
        rule.shares = new LinkedHashMap<>();
        return rule;
    }

    private static void requireKnown(ThemeDropsConfig table) {
        Set<String> themes = seededThemeIds();
        for (Map.Entry<String, List<String>> group : table.themeGroups.entrySet()) {
            for (String theme : group.getValue()) {
                if (!themes.contains(theme)) {
                    throw new IllegalStateException("Theme group " + group.getKey() + " names " + theme + ", which the pack's theme index does not list");
                }
            }
        }
        for (Map.Entry<String, Rule> rule : table.rules.entrySet()) {
            if (!ModThemeDropPayloadsProvider.ids().contains(ResourceLocation.parse(rule.getValue().payload))) {
                throw new IllegalStateException("Theme drop " + rule.getKey() + " names payload " + rule.getValue().payload + ", which is not generated");
            }
            for (int tier : rule.getValue().shares.keySet()) {
                if (tier > MYTHIC) {
                    throw new IllegalStateException("Theme drop " + rule.getKey() + " has a share for tier " + tier + ", past mythic");
                }
            }
        }
    }

    private static Set<String> seededThemeIds() {
        Set<String> ids = new HashSet<>();
        for (JsonElement key : AbstractConfigPatchProvider.seeded("gen/themes.json").getAsJsonObject().getAsJsonArray("keys")) {
            ids.add(key.getAsJsonObject().get("id").getAsString());
        }
        return ids;
    }
}
