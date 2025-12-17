package xyz.iwolfking.woldsvaults.api.data.enchantments;

import cofh.ensorcellation.init.EnsorcEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.Set;

public class AllowedEnchantmentsData {

    public static final Set<Enchantment> allowedUtilityEnchantments = Set.of(EnsorcEnchantments.SOULBOUND.get());

    public static boolean isAllowedUtilityEnchantment(Enchantment enchantment) {
        return allowedUtilityEnchantments.contains(enchantment);
    }
}
