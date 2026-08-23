package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEffects;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.items.gear.VaultBattleStaffItem;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

import java.util.regex.Pattern;

/** Shared target and weapon classification for the Idona nodes. */
public final class IdonaTargeting {
    private static final ResourceLocation GREED_ASSASSIN_GROUP = VaultMod.id("greed_assassin");

    /** Item ids Better Combat treats as two-handed, mirroring its fallback compatibility config. */
    private static final Pattern TWO_HANDED_ITEMS =
            Pattern.compile("claymore|great_?sword|scythe|halberd|spear|lance|battle_?staff");

    private IdonaTargeting() {
    }

    /** Hostile, non-allied living entities: excludes players, eternals and pets. */
    public static boolean countsAsEnemy(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        return !(living instanceof Player) && !(living instanceof EternalEntity) && !(living instanceof PetEntity);
    }

    public static boolean isGreedAssassin(Entity entity) {
        return ModConfigs.ENTITY_GROUPS != null && ModConfigs.ENTITY_GROUPS.isInGroup(GREED_ASSASSIN_GROUP, entity);
    }

    public static boolean isGreedChampion(Entity entity) {
        return VaultChampion.isChampion(entity);
    }

    /** Counts harmful effects on a target, excluding those in {@link #isMechanicalEffect}. */
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

    /** True when both hands hold a mainhand-intended vault gear item. */
    public static boolean isDualWielding(ServerPlayer player) {
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        return main.getItem() instanceof VaultGearItem mainGear
                && off.getItem() instanceof VaultGearItem offGear
                && mainGear.isIntendedForSlot(main, EquipmentSlot.MAINHAND)
                && offGear.isIntendedForSlot(off, EquipmentSlot.MAINHAND);
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
