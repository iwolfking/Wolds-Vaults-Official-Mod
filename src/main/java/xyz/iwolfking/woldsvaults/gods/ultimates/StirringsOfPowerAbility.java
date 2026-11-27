package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodLevels;

/**
 * The dormant specialization of Stirrings of Power, held below the ultimate unlock level; it costs nothing and
 * starts no cooldown.
 */
public class StirringsOfPowerAbility extends GodUltimateAbility {
    public StirringsOfPowerAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                                   float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public StirringsOfPowerAbility() {
    }

    @Override
    public VaultGod getGod() {
        return null;
    }

    @Override
    protected ActionResult doUltimate(ServerPlayer player, int level) {
        return ActionResult.fail();
    }

    @Override
    protected ActionResult doAction(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(player ->
                player.displayClientMessage(new TranslatableComponent("message.woldsvaults.stirrings_dormant",
                        GodLevels.ultimateUnlockLevel()).withStyle(ChatFormatting.GRAY), true));
        return ActionResult.fail();
    }
}
