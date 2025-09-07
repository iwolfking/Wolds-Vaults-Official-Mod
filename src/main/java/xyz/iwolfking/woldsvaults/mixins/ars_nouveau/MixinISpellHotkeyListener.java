package xyz.iwolfking.woldsvaults.mixins.ars_nouveau;

import com.hollingsworth.arsnouveau.api.item.ISpellHotkeyListener;
import com.hollingsworth.arsnouveau.api.spell.ISpellCaster;
import com.hollingsworth.arsnouveau.api.util.CasterUtil;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ars_nouveau")
    }
)
@Mixin(value = ISpellHotkeyListener.class, remap = false)
public interface MixinISpellHotkeyListener {
    /**
     * @author iwolfking
     * @reason Disable spell cast in vaults
     */
    @Overwrite
    default void onQuickCast(ItemStack stack, ServerPlayer player, InteractionHand hand, int slot) {
        if(player.getLevel().dimension().location().getNamespace().equals("the_vault")) {
            return;
        }

        ISpellCaster iSpellCaster = CasterUtil.getCaster(stack);
        iSpellCaster.castSpell(player.level, player, hand, (TranslatableComponent) null, iSpellCaster.getSpell(slot));
    }
}
