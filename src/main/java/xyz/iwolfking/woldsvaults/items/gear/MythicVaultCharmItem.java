package xyz.iwolfking.woldsvaults.items.gear;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.client.data.ClientPrestigePowersData;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.NoVaultUsesPrestigePower;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import xyz.iwolfking.woldsvaults.gods.GodPiety;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The mythic god charm: everything a regular vault god charm is - curio charm slot, uses, active
 * god selection, piety scaling - plus implicits and suffixes on top of the prefixes, wider piety
 * roll tables, base-system legendary potential and a toggleable temporal blessing driven by the
 * charm keybind. Unlike regular charms, mythics fully re-materialize from their stored draws
 * whenever the owner's piety changes (see {@link MythicCharmRolls}), so this item replaces the
 * base inventory-tick ratio rescale with its own and keeps rescaling while worn.
 *
 * <p>The tooltip keeps the base charm's info lines (uses, piety, god) but renders the affix body
 * through the standard {@link VaultGearTooltipItem} groups, so implicits, prefixes and suffixes
 * look exactly as they do on gear. The multiplier implicits that map to vanilla attributes
 * (attack damage, armor, max health, movement speed) are delivered through the curio
 * attribute-modifier pathway as multiply-total modifiers, which applies them on both sides and
 * shows them in every attribute readout.
 */
public class MythicVaultCharmItem extends VaultCharmItem implements VaultGearTooltipItem {
    private static final ResourceLocation BASE_CHARM_ID = new ResourceLocation("the_vault", "vault_god_charm");

    public MythicVaultCharmItem(ResourceLocation id) {
        super(id);
    }

    public static boolean isMythic(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MythicVaultCharmItem;
    }

    public static int getTemporalRemaining(ItemStack stack) {
        return stack.getOrCreateTag().getInt(MythicCharmRolls.TEMPORAL_REMAINING_TAG);
    }

    public static void setTemporalRemaining(ItemStack stack, int ticks) {
        stack.getOrCreateTag().putInt(MythicCharmRolls.TEMPORAL_REMAINING_TAG, Math.max(0, ticks));
    }

    @Override
    public void tickFinishRoll(ItemStack stack, @Nullable Player player, boolean identify) {
        VaultGearData data = VaultGearData.read(stack);
        data.setState(VaultGearState.IDENTIFIED);
        data.write(stack);
        MythicCharmRolls.initialize(stack, player);
    }

    /**
     * Replaces the base charm's inventory tick: the base implementation ratio-rescales prefixes
     * only, which would corrupt a mythic's implicits and suffixes, so this runs the mythic
     * re-materialization instead.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof ServerPlayer serverPlayer) {
            this.vaultGearTick(stack, serverPlayer);
            if (level.getGameTime() % 10L == 0L) {
                MythicCharmRolls.rescale(stack, serverPlayer);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);
        if (slotContext.entity() instanceof ServerPlayer serverPlayer
                && serverPlayer.getLevel().getGameTime() % 10L == 0L) {
            MythicCharmRolls.rescale(stack, serverPlayer);
            iskallia.vault.world.data.ServerVaults.get(serverPlayer.getLevel())
                    .filter(vault -> vault.has(iskallia.vault.core.vault.Vault.ID))
                    .ifPresent(vault -> MythicCharmRolls.resetTemporalForVault(stack,
                            vault.get(iskallia.vault.core.vault.Vault.ID)));
        }
    }

    /**
     * The base check compares the no-uses prestige power's configured item id against this
     * stack's own id, which would exclude mythic charms from Immortal Charms. A power configured
     * for the base god charm counts for mythics too.
     */
    @Override
    public boolean isUsable(ItemStack charmStack, Player player) {
        VaultGearData data = VaultGearData.read(charmStack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return false;
        }
        if (iskallia.vault.skill.prestige.helper.PrestigeHelper.getPrestige(player)
                .getAll(NoVaultUsesPrestigePower.class, Skill::isUnlocked).stream()
                .anyMatch(power -> BASE_CHARM_ID.equals(power.getItemId()))) {
            return true;
        }
        return super.isUsable(charmStack, player);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> base = super.getAttributeModifiers(context, uuid, stack);
        if (!"charm".equals(context.identifier()) || !VaultUsesHelper.hasUsesLeft(stack)) {
            return base;
        }
        if (context.entity() instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem())) {
            return base;
        }
        VaultGearData data = VaultGearData.read(stack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return base;
        }
        Multimap<Attribute, AttributeModifier> combined = HashMultimap.create(base);
        addMultiplier(combined, data, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ATTACK_DAMAGE_MULTIPLIER,
                Attributes.ATTACK_DAMAGE, uuid, "Mythic charm attack damage multiplier");
        addMultiplier(combined, data, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ARMOR_MULTIPLIER,
                Attributes.ARMOR, uuid, "Mythic charm armor multiplier");
        addMultiplier(combined, data, xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEALTH_MULTIPLIER,
                Attributes.MAX_HEALTH, uuid, "Mythic charm health multiplier");
        addMultiplier(combined, data, xyz.iwolfking.woldsvaults.init.ModGearAttributes.MOVEMENT_SPEED_MULTIPLIER,
                Attributes.MOVEMENT_SPEED, uuid, "Mythic charm movement speed multiplier");
        return combined;
    }

    private static void addMultiplier(Multimap<Attribute, AttributeModifier> modifiers, VaultGearData data,
                                      VaultGearAttribute<Float> gearAttribute, Attribute vanillaAttribute,
                                      UUID uuid, String name) {
        float value = data.get(gearAttribute, VaultGearAttributeTypeMerger.floatSum());
        if (value > 0.0F) {
            modifiers.put(vanillaAttribute,
                    new AttributeModifier(uuid, name, value, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    /**
     * Full tooltip replacement: the base charm's info lines, then the standard gear affix layout
     * (implicit group, prefix group, suffix group through {@link VaultGearTooltipItem}) so the
     * affixes read exactly like they do on gear, then the blessing clock and dormant draws.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        VaultGearData data = VaultGearData.read(stack);
        VaultGearState state = data.getState();
        boolean shiftDown = Screen.hasShiftDown();
        if (state == VaultGearState.IDENTIFIED && ClientPrestigePowersData.getTree()
                .getAll(NoVaultUsesPrestigePower.class, Skill::isUnlocked).stream()
                .noneMatch(power -> power.getItemId().equals(stack.getItem().getRegistryName())
                        || BASE_CHARM_ID.equals(power.getItemId()))) {
            int totalUses = VaultUsesHelper.getUses(stack);
            int remaining = Math.max(totalUses - VaultUsesHelper.getUsedVaults(stack).size(), 0);
            MutableComponent usesTxt = new TextComponent("Uses: ").withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(String.valueOf(remaining)).withStyle(ChatFormatting.WHITE));
            if (shiftDown) {
                usesTxt.append(new TextComponent(" / ").withStyle(ChatFormatting.GRAY)
                        .append(new TextComponent(String.valueOf(totalUses)).withStyle(ChatFormatting.WHITE)));
            }
            tooltip.add(usesTxt);
        }
        int piety = MythicCharmRolls.storedUnits(stack) * GodPiety.PIETY_PER_UNIT;
        tooltip.add(new TextComponent("Piety: ").withStyle(ChatFormatting.DARK_PURPLE)
                .append(new TextComponent(String.valueOf(piety)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        VaultCharmItem.getGod(stack).ifPresent(god -> tooltip.add(new TextComponent("Vault God: ")
                .append(new TextComponent(god.getName()).withStyle(Style.EMPTY.withColor(god.getChatColor())))));
        if (shiftDown || state == VaultGearState.UNIDENTIFIED) {
            this.addTooltipRarity(data, stack, tooltip, state);
        }
        if (state != VaultGearState.IDENTIFIED) {
            return;
        }
        boolean detail = GearTooltip.itemTooltip().displayModifierDetail();
        List<VaultGearModifier<?>> implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
        if (!implicits.isEmpty()) {
            this.addHintedAffixGroup(data, VaultGearModifier.AffixType.IMPLICIT, stack, tooltip, detail, shiftDown);
            int total = stack.getOrCreateTag().getInt(MythicCharmRolls.TEMPORAL_TOTAL_TAG);
            if (total > 0) {
                tooltip.add(new TextComponent("Blessing time: ").withStyle(ChatFormatting.GRAY)
                        .append(new TextComponent((getTemporalRemaining(stack) / 20) + "s / " + (total / 20) + "s")
                                .withStyle(ChatFormatting.WHITE))
                        .append(new TextComponent("  (toggle with the charm blessing key)")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
            tooltip.add(TextComponent.EMPTY);
        }
        int maxPrefixes = data.getFirstValue(iskallia.vault.init.ModGearAttributes.PREFIXES).orElse(0);
        List<VaultGearModifier<?>> prefixes = data.getModifiers(VaultGearModifier.AffixType.PREFIX);
        int maxSuffixes = data.getFirstValue(iskallia.vault.init.ModGearAttributes.SUFFIXES).orElse(0);
        List<VaultGearModifier<?>> suffixes = data.getModifiers(VaultGearModifier.AffixType.SUFFIX);
        if (maxPrefixes > 0 || !prefixes.isEmpty()) {
            this.addHintedAffixGroup(data, VaultGearModifier.AffixType.PREFIX, stack, tooltip, detail, shiftDown);
            if (detail && (maxSuffixes > 0 || !suffixes.isEmpty())) {
                tooltip.add(TextComponent.EMPTY);
            }
        }
        if (maxSuffixes > 0 || !suffixes.isEmpty()) {
            this.addHintedAffixGroup(data, VaultGearModifier.AffixType.SUFFIX, stack, tooltip, detail, shiftDown);
        }
        List<String> dormant = MythicCharmRolls.dormantRollNames(stack, MythicCharmRolls.storedUnits(stack));
        for (String name : dormant) {
            tooltip.add(new TextComponent("Dormant: " + name + " (awakens with more Piety)")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * The standard gear affix group with per-piety roll hints on shift: each materialized
     * modifier line gains its roll's per-10-piety gain (or per-level piety cost for threshold
     * rolls), implicits included - the hint list from {@link MythicCharmRolls#rollHints} is
     * aligned index-for-index with the group's modifiers.
     */
    private void addHintedAffixGroup(VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack,
                                     List<Component> tooltip, boolean displayDetails, boolean shiftDown) {
        List<VaultGearModifier<?>> affixes = data.getModifiers(type);
        if (displayDetails) {
            tooltip.add(new net.minecraft.network.chat.TranslatableComponent("tooltip.the_vault.affix_group",
                    type.getPlural()).withStyle(ChatFormatting.GRAY));
        }
        List<String> hints = shiftDown ? MythicCharmRolls.rollHints(stack, type) : List.of();
        for (int i = 0; i < affixes.size(); i++) {
            VaultGearModifier<?> modifier = affixes.get(i);
            int index = i;
            modifier.getDisplay(data, type, stack, displayDetails).ifPresent(display -> {
                MutableComponent line = display;
                if (shiftDown && index < hints.size() && !hints.get(index).isEmpty()) {
                    line = display.copy().append(new TextComponent(hints.get(index))
                            .withStyle(ChatFormatting.GRAY));
                    line.setStyle(display.getStyle());
                }
                tooltip.add(this.applyGearTooltipColour(line));
            });
        }
        if (displayDetails && type != VaultGearModifier.AffixType.IMPLICIT) {
            int emptyAffixes = data.getFirstValue(type == VaultGearModifier.AffixType.PREFIX
                    ? iskallia.vault.init.ModGearAttributes.PREFIXES
                    : iskallia.vault.init.ModGearAttributes.SUFFIXES).orElse(0);
            for (int i = 0; i < emptyAffixes - affixes.size(); i++) {
                tooltip.add(this.addTooltipEmptyAffixes(type));
            }
        }
    }

    @Override
    public Component addTooltipEmptyAffixes(VaultGearModifier.AffixType type) {
        return new TextComponent("■ empty %s".formatted(type.name().toLowerCase(Locale.ROOT)))
                .withStyle(ChatFormatting.GRAY);
    }
}
