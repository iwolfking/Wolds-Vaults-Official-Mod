package xyz.iwolfking.woldsvaults.spellcraft;

import net.minecraftforge.common.MinecraftForge;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityCastTracker;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.buff.LingeringBuffs;
import xyz.iwolfking.woldsvaults.spellcraft.buff.ToggleDiscounts;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellScheduler;
import xyz.iwolfking.woldsvaults.spellcraft.gear.ConvertedAffixes;
import xyz.iwolfking.woldsvaults.spellcraft.gear.ConvertedEtchings;
import xyz.iwolfking.woldsvaults.spellcraft.hit.GlyphArea;
import xyz.iwolfking.woldsvaults.spellcraft.hit.GlyphDamage;
import xyz.iwolfking.woldsvaults.spellcraft.hit.ShotgunHits;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellBars;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellCharge;
import xyz.iwolfking.woldsvaults.spellcraft.play.ToggleGuard;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectilePhysics;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileSizing;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileSteering;
import xyz.iwolfking.woldsvaults.spellcraft.roll.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.spellcraft.roll.UniqueSetRoller;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;

/**
 * Spellcraft setup: the Forge handlers that must run in a fixed order and the Vault Hunters hooks installed at common
 * setup.
 */
public final class Spellcraft {
    private Spellcraft() {
    }

    /**
     * Registers these handlers at setup, in this order, instead of through annotated subscribers, so they run after
     * every annotated handler of the same priority: elemental procs read a hit before glyph damage scales it.
     */
    public static void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(EquippedSources.class);
        MinecraftForge.EVENT_BUS.register(ToggleGuard.class);
        MinecraftForge.EVENT_BUS.register(SpellBars.class);
        MinecraftForge.EVENT_BUS.register(ConvertedEtchings.class);
        MinecraftForge.EVENT_BUS.register(ConvertedAffixes.class);
        MinecraftForge.EVENT_BUS.register(LingeringBuffs.class);
        MinecraftForge.EVENT_BUS.register(ToggleDiscounts.class);
        MinecraftForge.EVENT_BUS.register(ProjectileSteering.class);
        MinecraftForge.EVENT_BUS.register(ProjectileSizing.class);
        MinecraftForge.EVENT_BUS.register(ProjectilePhysics.class);
        MinecraftForge.EVENT_BUS.register(DeferredReach.class);
        MinecraftForge.EVENT_BUS.register(BuffGlyphs.class);
        MinecraftForge.EVENT_BUS.register(ProjectileGlyphs.class);
        MinecraftForge.EVENT_BUS.register(GlyphDamage.class);
        MinecraftForge.EVENT_BUS.register(SpellScheduler.class);
        MinecraftForge.EVENT_BUS.register(SpellCharge.class);
        MinecraftForge.EVENT_BUS.register(ShotgunHits.class);
    }

    public static void init() {
        try {
            AbilityCastTracker.register();
            GlyphArea.register();
            ConvertedAffixes.init();
            ConvertedEtchings.init();
            LingeringBuffs.init();
            SpellcraftRoller.setUniqueSets(new UniqueSetRoller());
        } catch (RuntimeException e) {
            WoldLogger.error("Spellcraft setup failed; any of its VH hooks after the failing one may be missing (cast tracking, glyph areas, "
                    + "converted affixes, converted etchings, lingering buffs, unique set rolls).", e);
            throw e;
        }
    }
}
