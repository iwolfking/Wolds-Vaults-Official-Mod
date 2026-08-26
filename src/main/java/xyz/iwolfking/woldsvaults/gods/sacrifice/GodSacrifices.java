package xyz.iwolfking.woldsvaults.gods.sacrifice;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The god sacrifice gate tables: eleven gates per god, Initiation then Level 1 through Level 10, each
 * listing what the Greed Cauldron must be fed. Completing gate N opens god level N, so the Initiation opens
 * no level of its own and instead marks the player for the god's mapped-vault experience; past the defined
 * gates, levels flow on experience alone.
 */
public final class GodSacrifices {
    public static final int GATE_COUNT = 11;

    public record Entry(ResourceLocation item, int count) {
    }

    public record Gate(int index, List<Entry> entries) {
        public String label() {
            return gateLabel(this.index);
        }
    }

    private static final Map<VaultGod, List<Gate>> GATES = buildGates();

    private GodSacrifices() {
    }

    public static String gateLabel(int index) {
        return index == 0 ? "Initiation" : "Level " + index + " Sacrifice";
    }

    /** Logs every gate entry whose item id is not in the item registry, which makes that gate unfinishable. */
    public static void validateItems() {
        for (Map.Entry<VaultGod, List<Gate>> godGates : GATES.entrySet()) {
            for (Gate gate : godGates.getValue()) {
                for (Entry entry : gate.entries()) {
                    if (!ForgeRegistries.ITEMS.containsKey(entry.item())) {
                        WoldsVaults.LOGGER.error("God sacrifice gate '{}' for {} asks for {} x{}, which is not a registered item; that gate cannot be completed.",
                                gate.label(), godGates.getKey().getName(), entry.item(), entry.count());
                    }
                }
            }
        }
    }

    @Nullable
    public static Gate gate(VaultGod god, int sacrificesCompleted) {
        List<Gate> gates = GATES.get(god);
        if (gates == null || sacrificesCompleted < 0 || sacrificesCompleted >= gates.size()) {
            return null;
        }
        return gates.get(sacrificesCompleted);
    }

    private static Entry e(String id, int count) {
        return new Entry(ResourceLocation.parse(id), count);
    }

    private static Gate gate(int index, Entry... entries) {
        return new Gate(index, List.of(entries));
    }

    private static Map<VaultGod, List<Gate>> buildGates() {
        Map<VaultGod, List<Gate>> gates = new EnumMap<>(VaultGod.class);
        gates.put(VaultGod.WENDARR, List.of(
                gate(0, e("the_vault:bitter_lemon", 25), e("the_vault:sour_orange", 10), e("the_vault:silver_scrap", 500), e("the_vault:gemstone", 40), e("the_vault:carbon", 400), e("the_vault:vault_gold", 100)),
                gate(1, e("the_vault:bitter_lemon", 25), e("the_vault:sour_orange", 15), e("the_vault:silver_scrap", 1000), e("the_vault:gemstone", 75), e("the_vault:carbon", 750), e("the_vault:vault_gold", 200)),
                gate(2, e("the_vault:bitter_lemon", 25), e("the_vault:sour_orange", 15), e("the_vault:grapes", 100), e("the_vault:silver_scrap", 2000), e("the_vault:gemstone", 125), e("the_vault:carbon", 1200), e("the_vault:vault_gold", 400)),
                gate(3, e("the_vault:bitter_lemon", 25), e("the_vault:sour_orange", 20), e("the_vault:grapes", 150), e("the_vault:silver_scrap", 5000), e("the_vault:gemstone", 200), e("the_vault:carbon", 2000), e("the_vault:vault_gold", 600), e("woldsvaults:altar_recatalyzer", 5), e("the_vault:vault_catalyst_fragment", 50)),
                gate(4, e("the_vault:bitter_lemon", 25), e("the_vault:sour_orange", 25), e("the_vault:grapes", 200), e("the_vault:silver_scrap", 7500), e("the_vault:gemstone", 250), e("the_vault:carbon", 2400), e("the_vault:vault_gold", 1000), e("woldsvaults:altar_recatalyzer", 8), e("the_vault:vault_catalyst_fragment", 100)),
                gate(5, e("the_vault:bitter_lemon", 50), e("the_vault:sour_orange", 30), e("the_vault:grapes", 250), e("the_vault:silver_scrap", 12500), e("the_vault:gemstone", 300), e("the_vault:carbon", 3000), e("the_vault:vault_gold", 2000), e("woldsvaults:altar_recatalyzer", 12), e("the_vault:vault_catalyst_fragment", 200)),
                gate(6, e("the_vault:bitter_lemon", 50), e("the_vault:sour_orange", 35), e("the_vault:grapes", 400), e("the_vault:silver_scrap", 50000), e("the_vault:gemstone", 1000), e("the_vault:carbon", 10000), e("the_vault:vault_gold", 5000), e("woldsvaults:altar_recatalyzer", 16), e("the_vault:vault_catalyst_fragment", 500), e("the_vault:phoenix_dust", 4)),
                gate(7, e("the_vault:bitter_lemon", 50), e("the_vault:sour_orange", 40), e("the_vault:grapes", 600), e("the_vault:silver_scrap", 75000), e("the_vault:gemstone", 1500), e("the_vault:carbon", 15000), e("the_vault:vault_gold", 7500), e("woldsvaults:altar_recatalyzer", 20), e("the_vault:vault_catalyst_fragment", 1000), e("the_vault:phoenix_dust", 4), e("the_vault:mystic_pear", 1)),
                gate(8, e("the_vault:bitter_lemon", 50), e("the_vault:sour_orange", 45), e("the_vault:grapes", 800), e("the_vault:silver_scrap", 125000), e("the_vault:gemstone", 2500), e("the_vault:carbon", 20000), e("the_vault:vault_gold", 10000), e("woldsvaults:altar_recatalyzer", 24), e("the_vault:vault_catalyst_fragment", 2000), e("the_vault:phoenix_dust", 4), e("the_vault:mystic_pear", 1)),
                gate(9, e("the_vault:bitter_lemon", 50), e("the_vault:sour_orange", 45), e("the_vault:grapes", 1000), e("the_vault:silver_scrap", 300000), e("the_vault:gemstone", 10000), e("the_vault:carbon", 50000), e("the_vault:vault_gold", 40000), e("woldsvaults:altar_recatalyzer", 32), e("the_vault:vault_catalyst_fragment", 10000), e("the_vault:phoenix_dust", 4), e("the_vault:mystic_pear", 2), e("woldsvaults:chunk_of_power", 5)),
                gate(10, e("the_vault:bitter_lemon", 100), e("the_vault:sour_orange", 50), e("the_vault:grapes", 1500), e("the_vault:silver_scrap", 500000), e("the_vault:gemstone", 15000), e("the_vault:carbon", 60000), e("the_vault:vault_gold", 75000), e("woldsvaults:altar_recatalyzer", 32), e("the_vault:vault_catalyst_fragment", 15000), e("the_vault:phoenix_dust", 4), e("the_vault:mystic_pear", 5), e("woldsvaults:chunk_of_power", 10), e("woldsvaults:core_of_the_vault_gods", 1))
        ));
        gates.put(VaultGod.IDONA, List.of(
                gate(0, e("the_vault:carbon", 100), e("the_vault:black_chromatic_steel_ingot", 2), e("the_vault:wild_focus", 40), e("the_vault:amplifying_focus", 20), e("the_vault:nullifying_focus", 20), e("the_vault:vault_gold", 100), e("the_vault:vault_scrap", 100)),
                gate(1, e("the_vault:carbon", 200), e("the_vault:black_chromatic_steel_ingot", 10), e("the_vault:wild_focus", 100), e("the_vault:amplifying_focus", 64), e("the_vault:nullifying_focus", 64), e("the_vault:vault_gold", 200), e("the_vault:vault_scrap", 500)),
                gate(2, e("the_vault:carbon", 400), e("the_vault:black_chromatic_steel_ingot", 25), e("the_vault:wild_focus", 300), e("the_vault:amplifying_focus", 100), e("the_vault:nullifying_focus", 200), e("the_vault:vault_gold", 400), e("the_vault:vault_scrap", 2000), e("the_vault:fundamental_focus", 10)),
                gate(3, e("the_vault:carbon", 800), e("the_vault:black_chromatic_steel_ingot", 40), e("the_vault:wild_focus", 600), e("the_vault:amplifying_focus", 400), e("the_vault:nullifying_focus", 200), e("the_vault:vault_gold", 600), e("the_vault:vault_scrap", 4000), e("the_vault:fundamental_focus", 15)),
                gate(4, e("the_vault:carbon", 1200), e("the_vault:black_chromatic_steel_ingot", 60), e("the_vault:wild_focus", 1000), e("the_vault:amplifying_focus", 400), e("the_vault:nullifying_focus", 500), e("the_vault:vault_gold", 1000), e("the_vault:vault_scrap", 10000), e("the_vault:fundamental_focus", 15), e("the_vault:adaptive_focus", 2)),
                gate(5, e("the_vault:carbon", 1500), e("the_vault:black_chromatic_steel_ingot", 80), e("the_vault:wild_focus", 2000), e("the_vault:amplifying_focus", 750), e("the_vault:nullifying_focus", 600), e("the_vault:vault_gold", 2000), e("the_vault:vault_scrap", 15000), e("the_vault:fundamental_focus", 15), e("the_vault:adaptive_focus", 4), e("the_vault:cryonic_focus", 2), e("the_vault:empowered_chaotic_focus", 1)),
                gate(6, e("the_vault:carbon", 2000), e("the_vault:black_chromatic_steel_ingot", 125), e("the_vault:wild_focus", 5000), e("the_vault:amplifying_focus", 1000), e("the_vault:nullifying_focus", 1500), e("the_vault:vault_gold", 5000), e("the_vault:vault_scrap", 40000), e("the_vault:fundamental_focus", 50), e("the_vault:adaptive_focus", 6), e("the_vault:cryonic_focus", 3), e("the_vault:empowered_chaotic_focus", 2)),
                gate(7, e("the_vault:carbon", 2500), e("the_vault:black_chromatic_steel_ingot", 200), e("the_vault:wild_focus", 7500), e("the_vault:amplifying_focus", 2000), e("the_vault:nullifying_focus", 1500), e("the_vault:vault_gold", 7500), e("the_vault:vault_scrap", 60000), e("the_vault:fundamental_focus", 100), e("the_vault:adaptive_focus", 10), e("the_vault:cryonic_focus", 3), e("the_vault:empowered_chaotic_focus", 2)),
                gate(8, e("the_vault:carbon", 3000), e("the_vault:black_chromatic_steel_ingot", 300), e("the_vault:wild_focus", 15000), e("the_vault:amplifying_focus", 4000), e("the_vault:nullifying_focus", 3000), e("the_vault:vault_gold", 10000), e("the_vault:vault_scrap", 100000), e("the_vault:fundamental_focus", 200), e("the_vault:adaptive_focus", 10), e("the_vault:cryonic_focus", 3), e("the_vault:empowered_chaotic_focus", 5), e("woldsvaults:suspension_focus", 1)),
                gate(9, e("the_vault:carbon", 4000), e("the_vault:black_chromatic_steel_ingot", 500), e("the_vault:wild_focus", 30000), e("the_vault:amplifying_focus", 7000), e("the_vault:nullifying_focus", 7000), e("the_vault:vault_gold", 40000), e("the_vault:vault_scrap", 150000), e("the_vault:fundamental_focus", 500), e("the_vault:adaptive_focus", 15), e("the_vault:cryonic_focus", 3), e("the_vault:empowered_chaotic_focus", 10), e("woldsvaults:suspension_focus", 1), e("woldsvaults:chunk_of_power", 5)),
                gate(10, e("the_vault:carbon", 4000), e("the_vault:black_chromatic_steel_ingot", 500), e("the_vault:wild_focus", 60000), e("the_vault:amplifying_focus", 15000), e("the_vault:nullifying_focus", 15000), e("the_vault:vault_gold", 75000), e("the_vault:vault_scrap", 250000), e("the_vault:fundamental_focus", 2000), e("the_vault:adaptive_focus", 15), e("the_vault:cryonic_focus", 5), e("the_vault:empowered_chaotic_focus", 50), e("woldsvaults:suspension_focus", 1), e("woldsvaults:chunk_of_power", 10), e("woldsvaults:core_of_the_vault_gods", 1))
        ));
        gates.put(VaultGod.VELARA, List.of(
                gate(0, e("the_vault:knowledge_star", 5), e("the_vault:vault_meat", 100), e("the_vault:vault_essence", 100), e("the_vault:vault_gold", 100), e("the_vault:vault_plating", 100), e("the_vault:gem_larimar", 100)),
                gate(1, e("the_vault:knowledge_star", 10), e("the_vault:vault_meat", 250), e("the_vault:vault_essence", 500), e("the_vault:vault_gold", 200), e("the_vault:vault_plating", 150), e("the_vault:gem_larimar", 400)),
                gate(2, e("the_vault:knowledge_star", 20), e("the_vault:vault_meat", 500), e("the_vault:vault_essence", 1000), e("the_vault:vault_gold", 400), e("the_vault:vault_plating", 200), e("the_vault:gem_larimar", 500)),
                gate(3, e("the_vault:knowledge_star", 50), e("the_vault:vault_meat", 1000), e("the_vault:vault_essence", 2000), e("the_vault:vault_gold", 600), e("the_vault:vault_plating", 250), e("the_vault:gem_larimar", 2000), e("woldsvaults:soul_ichor", 50)),
                gate(4, e("the_vault:knowledge_star", 100), e("the_vault:vault_meat", 1500), e("the_vault:vault_essence", 3000), e("the_vault:vault_gold", 1000), e("the_vault:vault_plating", 250), e("the_vault:gem_larimar", 5000), e("woldsvaults:soul_ichor", 100)),
                gate(5, e("the_vault:knowledge_star", 250), e("the_vault:vault_meat", 3000), e("the_vault:vault_essence", 5000), e("the_vault:vault_gold", 2000), e("the_vault:vault_plating", 400), e("the_vault:gem_larimar", 10000), e("woldsvaults:soul_ichor", 200), e("the_vault:repair_core", 5)),
                gate(6, e("the_vault:knowledge_star", 600), e("the_vault:vault_meat", 7000), e("the_vault:vault_essence", 15000), e("the_vault:vault_gold", 5000), e("the_vault:vault_plating", 400), e("the_vault:gem_larimar", 25000), e("woldsvaults:soul_ichor", 400), e("the_vault:repair_core", 10), e("the_vault:recharge_core", 5), e("the_vault:companion_relic_fragment", 100)),
                gate(7, e("the_vault:knowledge_star", 1500), e("the_vault:vault_meat", 12000), e("the_vault:vault_essence", 15000), e("the_vault:vault_gold", 7500), e("the_vault:vault_plating", 600), e("the_vault:gem_larimar", 50000), e("woldsvaults:soul_ichor", 750), e("the_vault:repair_core", 10), e("the_vault:recharge_core", 5), e("the_vault:companion_relic_fragment", 250)),
                gate(8, e("the_vault:knowledge_star", 4000), e("the_vault:vault_meat", 25000), e("the_vault:vault_essence", 25000), e("the_vault:vault_gold", 10000), e("the_vault:vault_plating", 600), e("the_vault:gem_larimar", 100000), e("woldsvaults:soul_ichor", 3000), e("the_vault:repair_core", 15), e("the_vault:recharge_core", 10), e("the_vault:companion_relic_fragment", 750)),
                gate(9, e("the_vault:knowledge_star", 10000), e("the_vault:vault_meat", 100000), e("the_vault:vault_essence", 100000), e("the_vault:vault_gold", 40000), e("the_vault:vault_plating", 5000), e("the_vault:gem_larimar", 250000), e("woldsvaults:soul_ichor", 10000), e("the_vault:repair_core", 25), e("the_vault:recharge_core", 10), e("the_vault:companion_relic_fragment", 2000), e("woldsvaults:chunk_of_power", 5)),
                gate(10, e("the_vault:knowledge_star", 25000), e("the_vault:vault_meat", 250000), e("the_vault:vault_essence", 250000), e("the_vault:vault_gold", 75000), e("the_vault:vault_plating", 15000), e("the_vault:gem_larimar", 500000), e("woldsvaults:soul_ichor", 30000), e("the_vault:repair_core", 40), e("the_vault:recharge_core", 20), e("the_vault:companion_relic_fragment", 5000), e("woldsvaults:chunk_of_power", 10), e("woldsvaults:wold_star_chunk", 4), e("woldsvaults:core_of_the_vault_gods", 1))
        ));
        gates.put(VaultGod.TENOS, List.of(
                gate(0, e("the_vault:vault_diamond", 50), e("the_vault:vault_essence", 200), e("the_vault:gem_pog", 10), e("the_vault:gem_wutodie", 50), e("the_vault:vault_gold", 100), e("the_vault:magic_silk", 100)),
                gate(1, e("the_vault:vault_diamond", 200), e("the_vault:vault_essence", 1000), e("the_vault:gem_pog", 30), e("the_vault:gem_wutodie", 100), e("the_vault:vault_gold", 200), e("the_vault:magic_silk", 250)),
                gate(2, e("the_vault:vault_diamond", 500), e("the_vault:vault_essence", 2500), e("the_vault:gem_pog", 50), e("the_vault:gem_wutodie", 250), e("the_vault:vault_gold", 400), e("the_vault:magic_silk", 700), e("the_vault:faceted_focus", 10)),
                gate(3, e("the_vault:vault_diamond", 1000), e("the_vault:vault_essence", 4000), e("the_vault:gem_pog", 100), e("the_vault:gem_wutodie", 500), e("the_vault:vault_gold", 600), e("the_vault:magic_silk", 2000), e("the_vault:faceted_focus", 25), e("woldsvaults:arcane_essence", 15)),
                gate(4, e("the_vault:vault_diamond", 2000), e("the_vault:vault_essence", 10000), e("the_vault:gem_pog", 200), e("the_vault:gem_wutodie", 1000), e("the_vault:vault_gold", 1000), e("the_vault:magic_silk", 4000), e("the_vault:faceted_focus", 30), e("woldsvaults:arcane_essence", 40)),
                gate(5, e("the_vault:vault_diamond", 5000), e("the_vault:vault_essence", 25000), e("the_vault:gem_pog", 500), e("the_vault:gem_wutodie", 2000), e("the_vault:vault_gold", 2000), e("the_vault:magic_silk", 8000), e("the_vault:faceted_focus", 40), e("woldsvaults:arcane_essence", 75), e("the_vault:card_juice", 20)),
                gate(6, e("the_vault:vault_diamond", 10000), e("the_vault:vault_essence", 50000), e("the_vault:gem_pog", 2000), e("the_vault:gem_wutodie", 5000), e("the_vault:vault_gold", 5000), e("the_vault:magic_silk", 16000), e("the_vault:faceted_focus", 75), e("woldsvaults:arcane_essence", 125), e("the_vault:card_juice", 40)),
                gate(7, e("the_vault:vault_diamond", 25000), e("the_vault:vault_essence", 100000), e("the_vault:gem_pog", 5000), e("the_vault:gem_wutodie", 10000), e("the_vault:vault_gold", 7500), e("the_vault:magic_silk", 40000), e("the_vault:faceted_focus", 75), e("woldsvaults:arcane_essence", 200), e("the_vault:card_juice", 80), e("woldsvaults:nullite_fragment", 10)),
                gate(8, e("the_vault:vault_diamond", 50000), e("the_vault:vault_essence", 200000), e("the_vault:gem_pog", 10000), e("the_vault:gem_wutodie", 25000), e("the_vault:vault_gold", 10000), e("the_vault:magic_silk", 75000), e("the_vault:faceted_focus", 100), e("woldsvaults:arcane_essence", 300), e("the_vault:card_juice", 125), e("woldsvaults:nullite_fragment", 15)),
                gate(9, e("the_vault:vault_diamond", 125000), e("the_vault:vault_essence", 500000), e("the_vault:gem_pog", 25000), e("the_vault:gem_wutodie", 75000), e("the_vault:vault_gold", 40000), e("the_vault:magic_silk", 150000), e("the_vault:faceted_focus", 200), e("woldsvaults:arcane_essence", 400), e("the_vault:card_juice", 300), e("woldsvaults:nullite_fragment", 25), e("woldsvaults:chunk_of_power", 5)),
                gate(10, e("the_vault:vault_diamond", 400000), e("the_vault:vault_essence", 1500000), e("the_vault:gem_pog", 40000), e("the_vault:gem_wutodie", 125000), e("the_vault:vault_gold", 75000), e("the_vault:magic_silk", 300000), e("the_vault:faceted_focus", 400), e("woldsvaults:arcane_essence", 750), e("the_vault:card_juice", 600), e("woldsvaults:nullite_fragment", 30), e("woldsvaults:chunk_of_power", 10), e("woldsvaults:core_of_the_vault_gods", 1))
        ));
        return gates;
    }
}
