package xyz.iwolfking.woldsvaults.augments;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.cast.AugmentCaster;
import xyz.iwolfking.woldsvaults.augments.cast.SpellCharge;
import xyz.iwolfking.woldsvaults.augments.effect.AugmentEvents;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBars;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Spellcraft: focuses and wands roll arcana, glyph lines and depth; their owner builds augmented spells from the glyphs
 * and puts them on the item's spell bar, which overlays the default bar while the item is in the off hand. This class
 * holds the entry points the bar and the focus screen call.
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
     * The focus or wand whose bar applies: the off-hand item, if it carries spellcraft, is identified, not broken or on
     * cooldown, and within the player's level, the same rules VH uses to count gear in the attribute snapshot.
     */
    public static ItemStack sourceItem(ServerPlayer player) {
        ItemStack stack = player.getOffhandItem();
        if (stack.isEmpty() || !SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)
                || player.getCooldowns().isOnCooldown(stack.getItem())) {
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
     * A key on a slot holding an augmented spell. Spells with Charge wind up while the key is held and cast on release;
     * others cast on release. A spell whose item breaks a rule, or that no longer validates, refuses with the reason.
     */
    public static void onSpellKey(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot, boolean down) {
        FocusAugmentsConfig config = config();
        SpellModel model = slot.model();
        if (config == null || model == null) {
            return;
        }
        if (!worksHere(config, player)) {
            if (!down) {
                AugmentCaster.showMessage(player, "Spells don't work here");
            }
            return;
        }
        if (!bar.itemErrors().isEmpty() || !model.isValid()) {
            if (!down) {
                String reason = !bar.itemErrors().isEmpty() ? bar.itemErrors().get(0) : model.errors().get(0);
                AugmentCaster.showMessage(player, "Can't cast " + slot.spell().name() + ": " + reason);
            }
            return;
        }
        if (!model.hasCharge()) {
            if (!down) {
                AugmentCaster.castSlot(player, slot, -1.0F);
            }
            return;
        }
        if (down) {
            AbilityTree tree = SpellCooldowns.tree(player);
            iskallia.vault.skill.ability.effect.spi.core.Ability trigger = SpellCooldowns.ability(tree, slot.group());
            if (trigger == null) {
                AugmentCaster.showMessage(player, slot.group().replace('_', ' ') + " is not learned");
                return;
            }
            if (trigger.isTreeOnCooldown()) {
                AugmentCaster.showMessage(player, slot.spell().name() + " is on cooldown");
                return;
            }
            Float haste = SpellHaste.factor(player, model.hasteMultiplier());
            int fullTicks = Math.round(config.tuning.chargeSeconds * 20.0F / (haste == null ? 1.0F : haste));
            SpellCharge.start(player, slot.index(), fullTicks, slot.spell().name(), config.tuning.chargeMoveMultiplier);
        } else {
            float share = SpellCharge.release(player, slot.index());
            if (share >= 0.0F) {
                AugmentCaster.castSlot(player, slot, share);
            }
        }
    }

    /**
     * Creates (id 0), changes or deletes a spell on the focus or wand in the player's inventory slot. The whole item is
     * checked against the table first; a change that breaks any rule is refused with the reason.
     */
    public static void editSpell(ServerPlayer player, int inventorySlot, int spellId, String name, List<String> tokens, boolean delete) {
        FocusAugmentsConfig config = config();
        if (config == null || inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            AugmentCaster.showMessage(player, "That item has no spellcraft");
            return;
        }
        if (delete) {
            SpellcraftItemData.removeSpell(stack, spellId);
            SpellBars.refresh(player);
            return;
        }
        String cleanName = name.strip();
        if (cleanName.isEmpty()) {
            cleanName = "Spell";
        }
        if (cleanName.length() > config.rules.maxNameLength) {
            cleanName = cleanName.substring(0, config.rules.maxNameLength);
        }
        int checkId = spellId > 0 ? spellId : Integer.MAX_VALUE;
        SpellcraftItemData.Spell edited = new SpellcraftItemData.Spell(checkId, cleanName, List.copyOf(tokens));
        SpellcraftRules.ItemCheck check = SpellcraftRules.check(config, stack, SpellcraftRules.withEdit(stack, checkId, edited),
                SpellcraftRules.specOf(SpellCooldowns.tree(player)), true);
        if (!check.isValid()) {
            AugmentCaster.showMessage(player, "Spell not saved: " + check.errors().get(0));
            return;
        }
        SpellcraftItemData.putSpell(stack, new SpellcraftItemData.Spell(spellId > 0 ? spellId : 0, cleanName, List.copyOf(tokens)));
        SpellBars.refresh(player);
    }
}
