package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.discoverylogic.goal.base.DiscoveryGoal;
import iskallia.vault.init.ModModelDiscoveryGoals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredThemesData;
import xyz.iwolfking.woldsvaults.discovery.recipes.LootersPouchDiscoveryGoal;
import xyz.iwolfking.woldsvaults.discovery.recipes.TreasuredJewelDiscoveryGoal;
import xyz.iwolfking.woldsvaults.discovery.recipes.WizardPouchDiscoveryGoal;
import xyz.iwolfking.woldsvaults.discovery.themes.GodThemesDiscoveryGoal;

@Mixin(value = ModModelDiscoveryGoals.class, remap = false)
public abstract class MixinModModelDiscoveryGoals {

    @Unique
    private static GodThemesDiscoveryGoal IDONA_UNLOCK;
    private static GodThemesDiscoveryGoal TENOS_UNLOCK;
    private static GodThemesDiscoveryGoal VELARA_UNLOCK;
    private static GodThemesDiscoveryGoal WENDARR_UNLOCK;
    private static WizardPouchDiscoveryGoal WIZARD_POUCH_UNLOCK;
    private static LootersPouchDiscoveryGoal LOOTERS_POUCH_UNLOCK;
    private static TreasuredJewelDiscoveryGoal TREASURED_JEWEL;

    static {
        IDONA_UNLOCK = registerGoal(WoldsVaults.id("complete_idona_altar"), (new GodThemesDiscoveryGoal(1.0F, VaultGod.IDONA)).setReward((player, goal) -> {
            DiscoveredThemesData discoversData = DiscoveredThemesData.get(player.getLevel());
            ResourceLocation themeId = new ResourceLocation(VaultMod.MOD_ID, "classic_vault_idona_normal");
            if (!discoversData.getDiscoveredThemes(player.getUUID()).contains(themeId)) {
                MutableComponent info = (new TextComponent("Idona shares insight on entering their domain with you.")).withStyle(ChatFormatting.RED);
                if(VaultRegistry.THEME.getKey(themeId) != null) {
                    player.displayClientMessage(info, false);
                    discoversData.discoverThemeAndBroadcast(VaultRegistry.THEME.getKey(themeId), player);
                }
            }
        }));

        TENOS_UNLOCK = registerGoal(WoldsVaults.id("complete_tenos_altar"), (new GodThemesDiscoveryGoal(1.0F, VaultGod.TENOS)).setReward((player, goal) -> {
            DiscoveredThemesData discoversData = DiscoveredThemesData.get(player.getLevel());
            ResourceLocation themeId = new ResourceLocation(VaultMod.MOD_ID, "classic_vault_tenos_normal");
            if (!discoversData.getDiscoveredThemes(player.getUUID()).contains(themeId)) {
                MutableComponent info = (new TextComponent("Tenos shares insight on entering their domain with you.")).withStyle(ChatFormatting.AQUA);
                if(VaultRegistry.THEME.getKey(themeId) != null) {
                    player.displayClientMessage(info, false);
                    discoversData.discoverThemeAndBroadcast(VaultRegistry.THEME.getKey(themeId), player);
                }

            }
        }));

        VELARA_UNLOCK = registerGoal(WoldsVaults.id("complete_velara_altar"), (new GodThemesDiscoveryGoal(1.0F, VaultGod.VELARA)).setReward((player, goal) -> {
            DiscoveredThemesData discoversData = DiscoveredThemesData.get(player.getLevel());
            ResourceLocation themeId = new ResourceLocation(VaultMod.MOD_ID, "classic_vault_velara_normal");
            if (!discoversData.getDiscoveredThemes(player.getUUID()).contains(themeId)) {
                MutableComponent info = (new TextComponent("Velara shares insight on entering their domain with you.")).withStyle(ChatFormatting.GREEN);
                if(VaultRegistry.THEME.getKey(themeId) != null) {
                    player.displayClientMessage(info, false);
                    discoversData.discoverThemeAndBroadcast(VaultRegistry.THEME.getKey(themeId), player);
                }
            }
        }));

        WENDARR_UNLOCK = registerGoal(WoldsVaults.id("complete_wendarr_altar"), (new GodThemesDiscoveryGoal(1.0F, VaultGod.WENDARR)).setReward((player, goal) -> {
            DiscoveredThemesData discoversData = DiscoveredThemesData.get(player.getLevel());
            ResourceLocation themeId = new ResourceLocation(VaultMod.MOD_ID, "classic_vault_wendarr_normal");
            if (!discoversData.getDiscoveredThemes(player.getUUID()).contains(themeId)) {
                MutableComponent info = (new TextComponent("Wendarr shares insight on entering their domain with you.")).withStyle(ChatFormatting.GOLD);
                if(VaultRegistry.THEME.getKey(themeId) != null) {
                    player.displayClientMessage(info, false);
                    discoversData.discoverThemeAndBroadcast(VaultRegistry.THEME.getKey(themeId), player);
                }
            }
        }));

        WIZARD_POUCH_UNLOCK = registerGoal(WoldsVaults.id("cast_250_abilities"), (new WizardPouchDiscoveryGoal(250.0F)).setReward((player, goal) -> {
            DiscoveredRecipesData data = DiscoveredRecipesData.get(player.getLevel());
            ResourceLocation unlock = WoldsVaults.id("wizard_trinket_pouch");
            if(!data.hasDiscovered(player, unlock)) {
                MutableComponent info = (new TextComponent("Your arcane energy has enlightened you!")).withStyle(ChatFormatting.BLUE);
                player.displayClientMessage(info, false);
                data.discoverRecipeAndBroadcast(unlock, player);
            }
        }));

        LOOTERS_POUCH_UNLOCK = registerGoal(WoldsVaults.id("open_15_treasure_rooms"), (new LootersPouchDiscoveryGoal(15.0F)).setReward((player, goal) -> {
            DiscoveredRecipesData data = DiscoveredRecipesData.get(player.getLevel());
            ResourceLocation unlock = WoldsVaults.id("looters_trinket_pouch");
            if(!data.hasDiscovered(player, unlock)) {
                MutableComponent info = (new TextComponent("You are enlightened with a beautiful dream of boundless loot!")).withStyle(ChatFormatting.GOLD);
                player.displayClientMessage(info, false);
                data.discoverRecipeAndBroadcast(unlock, player);
            }
        }));

        TREASURED_JEWEL = registerGoal(WoldsVaults.id("open_10_treasure_rooms"), (new TreasuredJewelDiscoveryGoal(05.0F)).setReward((player, goal) -> {
            DiscoveredRecipesData data = DiscoveredRecipesData.get(player.getLevel());
            ResourceLocation unlock = WoldsVaults.id("treasure_jewel");
            if(!data.hasDiscovered(player, unlock)) {
                MutableComponent info = (new TextComponent("You envision a better way of opening Treasure Chests!")).withStyle(ChatFormatting.GOLD);
                player.displayClientMessage(info, false);
                data.discoverRecipeAndBroadcast(unlock, player);
            }
        }));



    }

    @Shadow
    private static <G extends DiscoveryGoal<G>> G registerGoal(ResourceLocation id, G goal) {
        return null;
    }
}
