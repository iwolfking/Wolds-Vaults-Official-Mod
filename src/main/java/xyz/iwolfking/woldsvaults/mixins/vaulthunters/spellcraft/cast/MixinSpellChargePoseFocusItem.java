package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.cast;

import iskallia.vault.item.gear.FocusItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FocusItem.class)
public abstract class MixinSpellChargePoseFocusItem extends Item {
    private MixinSpellChargePoseFocusItem(Properties properties) {
        super(properties);
    }

    /** Charging a spell uses the spell's item, drawn like a bow (only Charge starts using it). */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    /** As long as a bow, so a held charge never runs out on its own. */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
}
