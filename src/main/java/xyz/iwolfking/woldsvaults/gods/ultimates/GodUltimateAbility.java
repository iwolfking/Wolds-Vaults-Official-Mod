package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;

/**
 * Shared shell for the four god ultimates, each one specialization of the single {@code Stirrings of
 * Power} ability. The cooldown is absolute, and casting requires alignment to this god at the unlock level.
 */
public abstract class GodUltimateAbility extends InstantManaAbility {
    protected GodUltimateAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                                 float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    protected GodUltimateAbility() {
    }

    /** The god this ultimate belongs to; {@code null} for the dormant placeholder. */
    public abstract VaultGod getGod();

    protected abstract ActionResult doUltimate(ServerPlayer player, int level);

    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            VaultGod god = this.getGod();
            if (!isAvailable(player, god)) {
                player.displayClientMessage(new TranslatableComponent("message.woldsvaults.ultimate_locked",
                        god == null ? "Your god" : god.getName(), GodLevels.ultimateUnlockLevel())
                        .withStyle(ChatFormatting.GRAY), true);
                return ActionResult.fail();
            }
            return this.doUltimate(player, UltimateLevels.resolveLevel(player, god));
        }).orElse(ActionResult.fail());
    }

    /** Sets the configured cooldown directly; cooldown-reduction attributes and the cooldown-skip roll do not apply. */
    @Override
    public void putOnCooldown(int cooldownDelayTicks, SkillContext context) {
        this.setCooldown(this.getCooldownTicks(), cooldownDelayTicks);
    }

    /** Refuses every in-flight cooldown reduction: etchings, cooldown bottles and the Arcane Cascade talent. */
    @Override
    public void reduceCooldownBy(int reduceBy) {
    }

    protected static boolean isAvailable(ServerPlayer player, VaultGod god) {
        if (god == null || player.getServer() == null) {
            return false;
        }
        if (!ActiveGodResolver.isActive(player, god)) {
            return false;
        }
        int level = GodAlignmentData.get(player.getServer()).getLevel(player.getUUID(), god);
        return GodLevels.hasUltimate(level);
    }
}
