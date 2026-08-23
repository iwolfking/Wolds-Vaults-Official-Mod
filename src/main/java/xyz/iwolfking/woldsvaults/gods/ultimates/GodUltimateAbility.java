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
 * Shared shell for the four god ultimates. Each is one specialization of the single
 * {@code Stirrings of Power} ability, so they all cast through the ordinary ability system and
 * differ only in what {@link #doUltimate(ServerPlayer, int)} does.
 *
 * <p>Two design rules live here rather than in each ultimate. First, the five-minute cooldown is
 * absolute: {@link #putOnCooldown(int, SkillContext)} sets the configured value directly instead of
 * routing it through {@code CooldownHelper.adjustCooldown}, so cooldown-reduction attributes, the
 * per-ability cooldown attributes and the cooldown-skip roll cannot shorten it, and
 * {@link #reduceCooldownBy(int)} is refused outright so that the other half of the cooldown
 * surface - etchings, cooldown bottles and the Arcane Cascade lucky-hit talent, which all mutate a
 * running cooldown rather than the value it started at - cannot either. Second, casting is gated on
 * the caster actually being aligned to this god and having reached the ultimate unlock level, which
 * is what keeps the morphing ability honest if a specialization is ever selected out of step with
 * the player's charm.
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

    /**
     * The ultimate's cooldown is a flat five minutes that no attribute may shorten, so this
     * deliberately skips the adjustment and skip-chance path every other ability uses.
     */
    @Override
    public void putOnCooldown(int cooldownDelayTicks, SkillContext context) {
        this.setCooldown(this.getCooldownTicks(), cooldownDelayTicks);
    }

    /**
     * Refuses every in-flight cooldown reduction. {@code Ability#reduceCooldownBy} is the shared
     * entry point for vault etchings, cooldown-reduction bottles and the Arcane Cascade lucky-hit
     * talent; a god ultimate is immune to all three.
     */
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
