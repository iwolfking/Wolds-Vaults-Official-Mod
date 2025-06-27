package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.enchantment.DecorShieldEnchantment;

public class ModEnchantments {
    public static final Enchantment DECOR_SHIELD = new DecorShieldEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND);

    public static void register(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(DECOR_SHIELD);
    }
}
