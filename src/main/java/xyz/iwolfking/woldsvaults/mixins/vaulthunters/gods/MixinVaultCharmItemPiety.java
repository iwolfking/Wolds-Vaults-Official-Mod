package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.GodPiety;

import java.util.List;
import java.util.UUID;

@Mixin(value = VaultCharmItem.class, remap = false)
public abstract class MixinVaultCharmItemPiety {
    /**
     * @author PoorMansPhysicist
     * @reason god charms scale off piety instead of god reputation, at ten piety per scale unit
     */
    @Redirect(method = {"shouldUpdateItem", "updateCharm"},
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/world/data/PlayerReputationData;getReputation(Ljava/util/UUID;Liskallia/vault/core/vault/influence/VaultGod;)I"))
    private static int woldsVaults$readPietyUnits(UUID playerId, VaultGod god, ItemStack stack, Player player) {
        return GodPiety.scaleUnits(player, god);
    }

    /**
     * @author PoorMansPhysicist
     * @reason the charm shows its scaling currency as piety, which is the stored scale units times ten
     */
    @ModifyVariable(method = "appendHoverText", at = @At("STORE"), name = "reputation", remap = true)
    private int woldsVaults$displayPiety(int storedUnits) {
        return storedUnits * GodPiety.PIETY_PER_UNIT;
    }

    @ModifyConstant(method = "appendHoverText", constant = @Constant(stringValue = "Reputation: "), remap = true)
    private String woldsVaults$pietyLabel(String label) {
        return "Piety: ";
    }

    /**
     * @author PoorMansPhysicist
     * @reason the piety line displays in purple; the base line is assembled inline, so the finished
     * tooltip entry is restyled at the end
     */
    @Inject(method = "appendHoverText", at = @At("TAIL"), remap = true)
    private void woldsVaults$purplePiety(ItemStack stack, Level worldIn, List<Component> tooltip,
                                         TooltipFlag flagIn, CallbackInfo ci) {
        for (int i = 0; i < tooltip.size(); i++) {
            String text = tooltip.get(i).getString();
            if (text.startsWith("Piety: ")) {
                tooltip.set(i, new TextComponent("Piety: ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(new TextComponent(text.substring("Piety: ".length()))
                                .withStyle(ChatFormatting.LIGHT_PURPLE)));
                return;
            }
        }
    }

    @ModifyConstant(method = "addTooltipAffixGroupWithBaseValue",
            constant = @Constant(stringValue = " (+%.1f%% Per Reputation)"))
    private String woldsVaults$pietyPercentSuffix(String suffix) {
        return " (+%.1f%% Per 10 Piety)";
    }

    @ModifyConstant(method = "addTooltipAffixGroupWithBaseValue",
            constant = @Constant(stringValue = " (+%d Per Reputation)"))
    private String woldsVaults$pietyFlatSuffix(String suffix) {
        return " (+%d Per 10 Piety)";
    }

    @ModifyConstant(method = "addTooltipAffixGroupWithBaseValue",
            constant = @Constant(stringValue = " (+%s seconds per reputation)"))
    private String woldsVaults$pietyTemporalSuffix(String suffix) {
        return " (+%s seconds per 10 piety)";
    }
}
