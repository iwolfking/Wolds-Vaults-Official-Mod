package xyz.iwolfking.woldsvaults.augments;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.cast.AugmentCaster;
import xyz.iwolfking.woldsvaults.augments.cast.SpellPlan;
import xyz.iwolfking.woldsvaults.augments.effect.AugmentEvents;
import xyz.iwolfking.woldsvaults.augments.item.AugmentItemData;
import xyz.iwolfking.woldsvaults.augments.item.AugmentRoller;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Focus augments: wands and focuses roll augment tokens, the owner arranges them into a recipe per ability, and
 * pressing that ability's key runs the recipe instead of the plain cast. The recipe's first step is always the same
 * native key press, so mana, cooldown, curses and cast failure stay VH's own.
 */
public final class FocusAugments {
    private FocusAugments() {
    }

    /** Registers the VH event listeners; called once from common setup. */
    public static void init() {
        AbilityCastTracker.register();
        AugmentEvents.register();
    }

    @Nullable
    public static FocusAugmentsConfig config() {
        return ModConfigs.FOCUS_AUGMENTS;
    }

    /**
     * The wand or focus whose recipes apply: the off-hand item, if it is identified, not broken or on cooldown, and
     * within the player's level, the same rules VH uses to count gear in the attribute snapshot.
     */
    public static ItemStack sourceItem(ServerPlayer player) {
        ItemStack stack = player.getOffhandItem();
        if (stack.isEmpty() || !AugmentRoller.canCarry(stack) || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return ItemStack.EMPTY;
        }
        if (stack.getItem() instanceof VaultGearItem gear && (!gear.isIntendedForSlot(stack, EquipmentSlot.OFFHAND) || gear.isBroken(stack))) {
            return ItemStack.EMPTY;
        }
        VaultGearData data = VaultGearData.read(stack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return ItemStack.EMPTY;
        }
        int playerLevel = PlayerVaultStatsData.get(player.getLevel()).getVaultStats(player).getVaultLevel();
        return data.getItemLevel() > playerLevel ? ItemStack.EMPTY : stack;
    }

    public static boolean worksHere(FocusAugmentsConfig config, ServerPlayer player) {
        return config.worksOutsideVaults || player.getLevel().dimension().location().getNamespace().equals("the_vault");
    }

    /**
     * Called when the ability key is released for the selected ability. Runs the source item's recipe for that ability
     * and returns true, so the native key handling is skipped; returns false to let VH handle the press as usual. A
     * recipe that no longer validates (table changed, item edited) logs an error and falls back to the plain cast.
     */
    public static boolean tryCastRecipe(ServerPlayer player, AbilityTree tree) {
        FocusAugmentsConfig config = config();
        SpecializedSkill selected = tree.getSelected();
        if (config == null || selected == null || selected.getSpecialization() == null) {
            return false;
        }
        String abilityId = selected.getSpecialization().getId();
        ItemStack source = sourceItem(player);
        if (source.isEmpty()) {
            return false;
        }
        Map<String, List<String>> recipes = AugmentItemData.recipes(source);
        List<String> glyphs = recipes.get(abilityId);
        if (glyphs == null || !worksHere(config, player)) {
            return false;
        }
        String budget = AugmentItemData.checkBudget(config, source, recipes);
        if (budget != null) {
            WoldsVaults.LOGGER.error("Focus augments: {}'s recipe on {} can't run ({}); casting it without augments",
                    player.getScoreboardName(), abilityId, budget);
            AugmentCaster.showMessage(player, "Augments off: " + budget);
            return false;
        }
        SpellPlan plan = SpellPlan.parse(config, abilityId, glyphs);
        AugmentCaster.cast(player, plan);
        return true;
    }
}
