package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEffects;
import iskallia.vault.VaultMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.items.gear.VaultBattleStaffItem;

import java.util.regex.Pattern;

/**
 * Shared target and weapon classification for the Idona nodes. Kept in one place because four
 * nodes ask overlapping questions about the same entity, and the answers must not drift apart.
 */
public final class IdonaTargeting {
    private static final ResourceLocation GREED_ASSASSIN_GROUP = VaultMod.id("greed_assassin");

    /**
     * Item ids Better Combat treats as two-handed in this pack. It mirrors the regexes in
     * {@code configureddefaults/config/bettercombat/fallback_compatibility.json} rather than
     * calling {@code WeaponRegistry}, because the addon's own registry mixin dereferences the
     * client {@code Minecraft} instance and is therefore not safe to invoke from server logic.
     */
    private static final Pattern TWO_HANDED_ITEMS =
            Pattern.compile("claymore|great_?sword|scythe|halberd|spear|lance|battle_?staff");

    private IdonaTargeting() {
    }

    /** Hostile, non-allied mobs -  the set Surrounded counts and the set node damage bonuses target. */
    public static boolean countsAsEnemy(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        return !(living instanceof Player) && !(living instanceof EternalEntity) && !(living instanceof PetEntity);
    }

    public static boolean isGreedAssassin(Entity entity) {
        return ModConfigs.ENTITY_GROUPS != null && ModConfigs.ENTITY_GROUPS.isInGroup(GREED_ASSASSIN_GROUP, entity);
    }

    /**
     * The greed champion half of Greedbane. The entity is introduced by the Medallions half of the
     * greed rework and does not exist in any config, entity group or class today, so this predicate
     * is deliberately constant-false; wiring it up is a one-line change once the entity lands.
     */
    public static boolean isGreedChampion(Entity entity) {
        return false;
    }

    /**
     * Counts distinct harmful effects on a target for Sneaky Advantage. The five effects the
     * addon's own Concentrate ability filters out are excluded here too: they are tagged harmful
     * but are mechanical bookkeeping rather than debuffs, and two of them (Reaving, Echoing) are
     * applied by the attacking player, which would let the node feed itself.
     */
    public static int countNegativeEffects(LivingEntity target) {
        int count = 0;
        for (MobEffectInstance instance : target.getActiveEffects()) {
            MobEffect effect = instance.getEffect();
            if (effect.getCategory() != MobEffectCategory.HARMFUL || isMechanicalEffect(effect)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static boolean isMechanicalEffect(MobEffect effect) {
        return effect.equals(xyz.iwolfking.woldsvaults.init.ModEffects.REAVING)
                || effect.equals(xyz.iwolfking.woldsvaults.init.ModEffects.ECHOING)
                || effect.equals(ModEffects.NOVA_DOT)
                || effect.equals(ModEffects.NO_AI)
                || effect.equals(ModEffects.GLACIAL_SHATTER)
                || effect.equals(ModEffects.BLOODTHIRST)
                || effect.equals(ModEffects.MANA_STEAL);
    }

    public static boolean isBattlestaff(ItemStack stack) {
        return stack.getItem() instanceof VaultBattleStaffItem;
    }

    public static boolean isTwoHanded(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isBattlestaff(stack)) {
            return true;
        }
        ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && TWO_HANDED_ITEMS.matcher(id.getPath()).find();
    }
}
