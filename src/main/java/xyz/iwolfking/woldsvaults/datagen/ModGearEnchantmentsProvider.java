
package xyz.iwolfking.woldsvaults.datagen;

import cofh.core.init.CoreEnchantments;
import cofh.ensorcellation.init.EnsorcEnchantments;
import com.cursedcauldron.wildbackport.common.registry.WBEnchantments;
import com.github.alexthe666.alexsmobs.enchantment.AMEnchantmentRegistry;
import com.hollingsworth.arsnouveau.common.enchantment.EnchantmentRegistry;
import com.hollingsworth.arsnouveau.setup.ItemsRegistry;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.Create;
import de.castcrafter.travel_anchors.ModEnchantments;
import iskallia.vault.init.ModItems;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.vhapi.api.datagen.AbstractGearEnchantmentProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

public class ModGearEnchantmentsProvider extends AbstractGearEnchantmentProvider {
    protected ModGearEnchantmentsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("ensorcellation", builder -> {
            builder.addEnchantment(EnsorcEnchantments.ANGLER.get(), 5, 2);
            builder.addEnchantment(EnsorcEnchantments.VOLLEY.get(), 45, 1);
            builder.addEnchantment(EnsorcEnchantments.TRUESHOT.get(), 45, 2);
            builder.addEnchantment(EnsorcEnchantments.HUNTER.get(), 5, 2);
            builder.addEnchantment(EnsorcEnchantments.QUICK_DRAW.get(), 5, 3);
            builder.addEnchantment(EnsorcEnchantments.VITALITY.get(), List.of(new ItemStack(Items.GOLDEN_APPLE, 64)), 3);
            builder.addEnchantment(EnsorcEnchantments.AIR_AFFINITY.get(), List.of(new ItemStack(ModItems.PHOENIX_FEATHER)), 1);
            builder.addEnchantment(EnsorcEnchantments.XP_BOOST.get(), List.of(new ItemStack(ModItems.WUTODIC_MASS, 8)), 3);
            builder.addEnchantment(EnsorcEnchantments.DAMAGE_ENDER.get(), List.of(new ItemStack(Items.ENDER_EYE, 32)), 1);
            builder.addEnchantment(EnsorcEnchantments.GOURMAND.get(), List.of(new ItemStack(Items.PUMPKIN_PIE, 64)), 2);
            builder.addEnchantment(EnsorcEnchantments.SOULBOUND.get(), List.of(new ItemStack(ModItems.ECHO_POG, 1)), 1);
        });

        add("wild_backport", builder -> {
            builder.addEnchantment(WBEnchantments.SWIFT_SNEAK.get(), 20, 1);
        });

        add("alexsmobs", builder -> {
            builder.addEnchantment(AMEnchantmentRegistry.STRADDLE_BOARDRETURN, 5, 1);
            builder.addEnchantment(AMEnchantmentRegistry.STRADDLE_JUMP, 5, 1);
            builder.addEnchantment(AMEnchantmentRegistry.STRADDLE_LAVAWAX, 5, 1);
            builder.addEnchantment(AMEnchantmentRegistry.STRADDLE_SERPENTFRIEND, 5, 1);
        });

        add("supplementaries", builder -> {
            builder.addEnchantment(ModRegistry.STASIS_ENCHANTMENT.get(), 5, 1);
        });

        add("cofh_core", builder -> {
            builder.addEnchantment(CoreEnchantments.HOLDING.get(), 5, 1);
        });

        add("create", builder -> {
            builder.addEnchantment(ResourceLocation.fromNamespaceAndPath(Create.ID, "potato_recovery"), 5, 1);
            builder.addEnchantment(ResourceLocation.fromNamespaceAndPath(Create.ID, "capacity"), 5, 1);
        });

        add("ars_nouveau", builder -> {
            builder.addEnchantment(EnchantmentRegistry.MANA_BOOST_ENCHANTMENT, List.of(new ItemStack(ItemsRegistry.SOURCE_GEM, 16)), 1);
            builder.addEnchantment(EnchantmentRegistry.MANA_REGEN_ENCHANTMENT, List.of(new ItemStack(ItemsRegistry.SOURCE_GEM, 16)), 3);
        });

        add("travel_anchors", builder -> {
            builder.addEnchantment(ModEnchantments.range, 5, 1);
        });
    }
}
