package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.VaultMod;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.RaidCrystalObjective;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PoolCrystalThemeAccessor;

import java.util.List;

@Mixin(value = CrystalData.class, remap = false)
public abstract class MixinClientCrystalData {
    @Shadow public abstract CrystalTheme getTheme();

    @WrapOperation(method = "addText", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/objective/CrystalObjective;addText(Ljava/util/List;ILnet/minecraft/world/item/TooltipFlag;FI)V"))
    private void improveRaidTooltip(CrystalObjective instance, List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level, Operation<Void> original){
        CrystalTheme theme = this.getTheme();
        if (instance instanceof RaidCrystalObjective && theme instanceof PoolCrystalTheme themePool) {
            ResourceLocation id = ((PoolCrystalThemeAccessor)themePool).getId();
            if (id != null) {
                if (id.equals(VaultMod.id("infinite_raid"))) {
                    tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("∞ Raid")).withStyle(Style.EMPTY.withColor(instance.getColor(time).orElseThrow()))));
                    return;
                }
                if (id.equals(VaultMod.id("infinite_raid_hard"))) {
                    tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("∞ Brutal Raid")).withStyle(Style.EMPTY.withColor(0x660000))));
                    return;
                }
            }
        }
        original.call(instance, tooltip, minIndex, flag, time, level);
    }
}
