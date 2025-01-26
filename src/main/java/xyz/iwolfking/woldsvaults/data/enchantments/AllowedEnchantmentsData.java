package xyz.iwolfking.woldsvaults.data.enchantments;

import cofh.ensorcellation.init.EnsorcEnchantments;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashSet;
import java.util.Set;

public class AllowedEnchantmentsData {

    public static final Set<Enchantment> allowedUtilityEnchantments = Set.of(ModEnchantments.MONEY_MENDING.get(), EnsorcEnchantments.SOULBOUND.get());

    public static boolean isAllowedUtilityEnchantment(Enchantment enchantment) {
        return allowedUtilityEnchantments.contains(enchantment);
    }
}
