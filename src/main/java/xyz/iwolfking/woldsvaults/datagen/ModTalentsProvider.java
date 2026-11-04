package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.TalentsConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.SkillOverlay;
import xyz.iwolfking.woldsvaults.talent.lucky_hit.ExecutionDamageLuckyHitTalent;

/**
 * The greed rework's overlay on the base mod's {@code talents.json}. VHAPI's talent loader swaps the tiers and max
 * learnable tier of each talent in the "replace" file into the base talent of the same id.
 */
public class ModTalentsProvider extends AbstractTalentProvider {
    public static final String REPLACE_FILE = "greed_talents_replace";

    protected ModTalentsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add(REPLACE_FILE, ModTalentsProvider::replacements);
    }

    /** Talents whose tiers the greed rework replaces: Executing Strike adds 4% execute threshold, then 2% per tier. */
    public static void replacements(Builder builder) {
        builder.addTalent("Execution_Strike", "Executing Strike", 1, 0, 2, 29,
                i -> new ExecutionDamageLuckyHitTalent(i == 0 ? 0 : i == 1 ? 50 : 75, i == 0 ? 2 : 1, 1, (4 + 2 * i) / 100.0F));
    }

    /** The talent config as VHAPI leaves it after applying this provider's files to {@code base}. */
    public static TalentsConfig withOverlay(TalentsConfig base) {
        Builder builder = new Builder();
        replacements(builder);
        SkillOverlay.replaceTiers(base.tree.skills, builder.build().tree.skills, true);
        return base;
    }
}
