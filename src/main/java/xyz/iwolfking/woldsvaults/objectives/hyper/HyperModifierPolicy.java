package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.Map;
import java.util.Set;

/**
 * The single home of hyper's modifier policy: which modifiers may never roll into a hyper
 * vault, and how many stacks the allowed ones may reach.
 *
 * <p>BANNED drives datagen: {@link xyz.iwolfking.woldsvaults.datagen.ModVaultModifierPoolsProvider}
 * derives the three hyper pools (hyper_mixed / hyper_all_bad / hyper_bad_timer_events) from the
 * concealed-chaos and negative-event source lists minus this set, so an upstream pool change
 * propagates automatically at the next runData instead of drifting silently. The caps are
 * enforced at runtime by every hyper add path (chaos dumps, ambient events, brutal kills and
 * the enchanted-event redirect). Ban here + runData, never by hand-editing pool JSON.
 */
public final class HyperModifierPolicy {
    /**
     * Modifiers that must never enter a hyper vault, with the playtest reason each earned its
     * place. A ban only affects the derived hyper pools — the source pools are untouched.
     * <ul>
     * <li>ethereal_mobs, ethereal: damage-immune mobs (two distinct ids grant the Ethereal
     * effect)</li>
     * <li>inert, nullifying, rending, weakened_powers, tenos_challenge, weak_heart: player
     * cooldown-reduction drains (Inert is instead dripped deterministically every 2nd kill by
     * the escalation manager)</li>
     * <li>fading, injured: permanent max-health destruction (Wounded stays, capped at 4
     * stacks)</li>
     * <li>no_heal: total heal elimination at -500% healing (Grievous Wounds/Enervated stay)</li>
     * <li>super_unextension, companion_shortened, no_companion, mob_levitate: flat vault-time
     * and progression losses with no counterplay</li>
     * <li>mildly_enchanted, bingo_infernal, curse: random-event backdoors that smuggle
     * unfiltered pools back in</li>
     * <li>chemical_bath, bubbling_trouble: on-hit effect storms (instakill stacking in deep
     * cycles)</li>
     * <li>woldsvaults:phantasmal_mobs: invisible mobs (the def displays as "Resistant Mobs";
     * the real woldsvaults:resistant_mobs stays)</li>
     * <li>no_ores, no_souls, soulless, true_noxp, no_champ_drops, lost_quantity, rotten,
     * locked: loot/progression negations that break the mini-objectives or the loop's
     * point</li>
     * <li>overpower, no_crit_mobs, catastrophic_brew: balance breakers — doubled player
     * damage, one roll deleting a whole mob axis, unbounded player-damage compounding (Frenzy
     * stays, capped)</li>
     * </ul>
     */
    public static final Set<String> BANNED = Set.of(
            "the_vault:ethereal_mobs",
            "the_vault:ethereal",
            "the_vault:inert",
            "the_vault:nullifying",
            "the_vault:rending",
            "the_vault:weakened_powers",
            "the_vault:tenos_challenge",
            "the_vault:weak_heart",
            "the_vault:fading",
            "the_vault:injured",
            "the_vault:no_heal",
            "the_vault:super_unextension",
            "the_vault:companion_shortened",
            "the_vault:no_companion",
            "the_vault:mob_levitate",
            "the_vault:mildly_enchanted",
            "the_vault:bingo_infernal",
            "the_vault:curse",
            "the_vault:chemical_bath",
            "the_vault:bubbling_trouble",
            "woldsvaults:phantasmal_mobs",
            "the_vault:no_ores",
            "the_vault:no_souls",
            "the_vault:soulless",
            "the_vault:true_noxp",
            "the_vault:no_champ_drops",
            "the_vault:lost_quantity",
            "the_vault:rotten",
            "the_vault:locked",
            "the_vault:overpower",
            "the_vault:no_crit_mobs",
            "the_vault:catastrophic_brew");

    /**
     * Fixed per-modifier stack caps, counting crystal-applied stacks too. Electric mob spam is
     * annoying enough that one stack is plenty; Wounded (-5 hearts) is the one player
     * max-health drain left in the pools and must never zero a health pool; Explosive is a TNT
     * spawner per stack (grouped carriers are banned/unpooled, so the id-keyed cap cannot be
     * bypassed); lava/void pools and the spawner trios get playability caps so deep runs don't
     * carpet every room.
     */
    private static final Map<ResourceLocation, Integer> STACK_CAPS = Map.of(
            ResourceLocation.parse("the_vault:electric"), 1,
            ResourceLocation.parse("the_vault:wounded"), 4,
            ResourceLocation.parse("the_vault:explosive"), 1,
            ResourceLocation.parse("the_vault:volcanic"), 2,
            ResourceLocation.parse("the_vault:void_pools"), 2,
            ResourceLocation.parse("the_vault:safari"), 5,
            ResourceLocation.parse("the_vault:winter"), 5,
            ResourceLocation.parse("the_vault:fungal"), 5);
    private static final ResourceLocation MANA_LEAK = ResourceLocation.parse("the_vault:mana_leak");
    private static final ResourceLocation FRENZY = ResourceLocation.parse("the_vault:frenzy");

    private HyperModifierPolicy() {
    }

    /** Datagen-time filter for the derived hyper pools. */
    public static boolean isBanned(String modifierId) {
        return BANNED.contains(modifierId);
    }

    /**
     * The cycle-aware stack cap for a modifier id (MAX_VALUE = uncapped). Mana Leak (-2000%
     * mana regen per stack — one is punishment enough early) allows one stack plus one per 3
     * cycles. Frenzy (+200% to ALL player damage per stack under the MixinMobFrenzyModifier
     * rework — uncapped it outgrows the boss's own escalation and ruins the balance) allows
     * one stack through cycle 4, then one more every 4 cycles.
     */
    public static int stackCap(Vault vault, ResourceLocation id) {
        if (MANA_LEAK.equals(id)) {
            return 1 + HyperVaultObjective.getCycleCount(vault) / 3;
        }
        if (FRENZY.equals(id)) {
            return 1 + Math.max(0, HyperVaultObjective.getCycleCount(vault) - 1) / 4;
        }
        return STACK_CAPS.getOrDefault(id, Integer.MAX_VALUE);
    }

    /** True when adding this modifier would exceed its hyper stack cap; logs the skip. */
    public static boolean isStackCapped(Vault vault, VaultModifier<?> modifier) {
        int cap = stackCap(vault, modifier.getId());
        if (cap == Integer.MAX_VALUE
                || VaultModifierUtils.getCountOfModifiers(vault, modifier.getId()) < cap) {
            return false;
        }
        WoldsVaults.LOGGER.info("Skipped rolling another {} — capped at {} stack(s) in Hyper vaults.", modifier.getId(), cap);
        return true;
    }
}
