package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultStashProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class ModVaultStashProvider extends AbstractVaultStashProvider {
    protected ModVaultStashProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_items", builder -> {
            builder.addCategory("gems", "Gems", ModItems.BENITOITE_GEM, itemBasicListBuilder -> {
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.POG_PRISM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.EXTRAORDINARY_POG_PRISM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.SMASHED_VAULT_GEM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_BOX);
                itemBasicListBuilder.add(ModItems.WUTODIE_GEM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.VAULT_DIAMOND_NUGGET);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GODS_MASTERY);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.EXPERTISE_ORB_ITEM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.SKILL_ORB_ITEM);
            }, resourceLocationBasicListBuilder -> {
                resourceLocationBasicListBuilder.add(VaultMod.id("keys"));
            });

            builder.addCategory("metals", "Metals", ModItems.CHROMATIC_STEEL_INGOT, itemBasicListBuilder -> {
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.CHROMATIC_GOLD_NUGGET);
                itemBasicListBuilder.add(ModItems.VAULT_INGOT);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.POGOMINIUM_INGOT);
            }, resourceLocationBasicListBuilder -> {

            });

            builder.addCategory("foci", "Foci", ModItems.WILD_FOCUS, itemBasicListBuilder -> {
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.ECCENTRIC_FOCUS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WEAPON_TYPE_FOCUS);
                //itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WEAPON_TYPE_SETTER);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.BLAZING_FOCUS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.SUSPENSION_FOCUS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.INSCRIBING_FOCUS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.STYLISH_FOCUS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.MERCY_ORB);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.REPAIR_AUGMENTER);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.CRYSTAL_REINFORCEMENT);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WANING_AUGMENTER);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WANING_AUGMENTER);
            }, resourceLocationBasicListBuilder -> {

            });

            builder.addCategory("crafting", "Crafting", ModItems.MAGIC_SILK, itemBasicListBuilder -> {
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.ALTAR_DECATALYZER);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.SOUL_ICHOR);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WOLD_STAR_CHUNK);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WOLD_STAR);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.ARCANE_ESSENCE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.HEART_OF_CHAOS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_ASHIUM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_BOMIGNITE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_GORGINITE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_UPALINE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_ISKALLIUM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_PETEZANITE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_XENIUM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_SPARKLETINE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GEM_REAGENT_TUBIUM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.BLUE_VAULT_ESSENCE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.YELLOW_VAULT_ESSENCE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.GREEN_VAULT_ESSENCE);
                itemBasicListBuilder.add(ModItems.RED_VAULT_ESSENCE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.TOME_OF_TENOS);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.VELARA_APPLE);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.IDONA_DAGGER);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.WENDARR_GEM);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.COMMUNITY_TOKEN);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.INFUSED_DRIFTWOOD);
                itemBasicListBuilder.add(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER);
            }, resourceLocationBasicListBuilder -> {
                resourceLocationBasicListBuilder.add(VaultMod.id("burgers"));
                resourceLocationBasicListBuilder.add(VaultMod.id("burger_parts"));
                resourceLocationBasicListBuilder.add(VaultMod.id("chest_scrolls"));

            });

            builder.addCategory("crystal_modifiers", "Vault Crystal Modifiers", ModItems.CRYSTAL_SEAL_SAGE, itemBasicListBuilder -> {
            }, resourceLocationBasicListBuilder -> {
                resourceLocationBasicListBuilder.add(VaultMod.id("seals"));
                resourceLocationBasicListBuilder.add(VaultMod.id("capstones"));
                resourceLocationBasicListBuilder.add(VaultMod.id("vault_rocks"));
            });

            builder.addCategory("fruit", "Vault Powerups", ModItems.BITTER_LEMON, itemBasicListBuilder -> {
            }, resourceLocationBasicListBuilder -> {
                resourceLocationBasicListBuilder.add(VaultMod.id("fruits"));
            });

            builder.addLadder(forms -> {
                forms.add(builder.ladderForm(ModItems.WUTODIE_GEM, 1));
                forms.add(builder.ladderForm(ModItems.PERFECT_WUTODIE, 4));
                forms.add(builder.ladderForm(ModItems.EXTRAORDINARY_WUTODIE, 16));
            });

            builder.addLadder(forms -> {
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.SMASHED_VAULT_GEM, 1));
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.SMASHED_VAULT_GEM_CLUSTER, 4));
            });

            builder.addLadder(forms -> {
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.VAULT_DIAMOND_NUGGET, 1));
                forms.add(builder.ladderForm(ModItems.VAULT_DIAMOND, 9));
                forms.add(builder.ladderForm(iskallia.vault.init.ModBlocks.VAULT_DIAMOND_BLOCK.asItem(), 81));
            });

            builder.addLadder(forms -> {
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.CHROMATIC_GOLD_NUGGET, 1));
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.CHROMATIC_GOLD_INGOT, 9));
                forms.add(builder.ladderForm(ModBlocks.CHROMATIC_GOLD_BLOCK.asItem(), 81));
            });

            builder.addLadder(forms -> {
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.ARCANE_ESSENCE, 1));
                forms.add(builder.ladderForm(xyz.iwolfking.woldsvaults.init.ModItems.ARCANE_SHARD, 9));
            });

        });
    }
}
