package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.client.gui.helper.SkillFrame;
import iskallia.vault.config.PrestigePowersConfig;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrestigePowerStyleProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModPrestigePowerStylesProvider extends AbstractPrestigePowerStyleProvider {
    protected ModPrestigePowerStylesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.PRESTIGE_POWERS = new PrestigePowersConfig().readConfig();
        add("wolds_prestiges", builder -> {
                builder.addStyle("BarrierOfResilience", new SkillStyle(0, 0, VaultMod.id("gui/prestige/barrier_of_resilience"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/barrier_of_resilience_inactive")));
                builder.addStyle("ShieldOfLastingGuard", new SkillStyle(0, 0, VaultMod.id("gui/prestige/shield_of_lasting_guard"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/shield_of_lasting_guard_inactive")));
                builder.addStyle("WeaverOfTime", new SkillStyle(0, 0, VaultMod.id("gui/prestige/weaver_of_time"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/weaver_of_time_inactive")));
                builder.addStyle("PrismaticPouch", new SkillStyle(0, 0, VaultMod.id("gui/prestige/prism_possessor"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/prism_possessor_inactive")));
                builder.addStyle("Artificer", new SkillStyle(0, 0, VaultMod.id("gui/prestige/artificer"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/artificer_inactive")));
                builder.addStyle("HealthIncrease", new SkillStyle(0, 0, VaultMod.id("gui/prestige/dryad"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/dryad_inactive")));
                builder.addStyle("SoulFetcher", new SkillStyle(0, 0, VaultMod.id("gui/prestige/soul_fetcher"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/soul_fetcher_inactive")));
                builder.addStyle("Toolsmith", new SkillStyle(0, 0, VaultMod.id("gui/prestige/toolsmith"), SkillFrame.RECTANGULAR, VaultMod.id("gui/prestige/toolsmith_inactive")));
        });
    }

}
