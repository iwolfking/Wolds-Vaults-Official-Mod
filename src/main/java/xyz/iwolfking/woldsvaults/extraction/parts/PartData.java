package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Style;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One rolled part or attachment: what it is, where it came from and every number it contributes.
 * Stat values are already quality-scaled; percentages are stored as whole percents.
 */
public class PartData {
    public static final String TAG = "PartData";

    public ExtractorPartType type = ExtractorPartType.CHASSIS;
    public String series = "";
    public String drillClass = "";
    public String attachmentId = "";
    public VaultGearRarity rarity = VaultGearRarity.SCRAPPY;
    public float quality;
    public String name = "";
    public final Map<ExtractorStat, Float> stats = new LinkedHashMap<>();
    public final Map<ExtractorStat, Float> multipliers = new LinkedHashMap<>();
    public final Map<ExtractorStat, Float> percents = new LinkedHashMap<>();
    public final List<Affix> affixes = new ArrayList<>();

    /** The part's name without its affix words: what the series or drill class calls it. */
    public String baseName = "";

    /**
     * An attachment's own wear pool. It is entirely separate from the extractor's durability: an
     * attachment never adds to it, drains only while bolted to a drilling extractor, and simply
     * stops contributing its stats once empty. Zero on every structural part and drill.
     */
    public int durability;
    public int maxDurability;

    public static class Affix {
        public String id;
        public ExtractorStat stat;
        public float value;
        public boolean percent;
        public boolean prefix;

        public Affix(String id, ExtractorStat stat, float value, boolean percent, boolean prefix) {
            this.id = id;
            this.stat = stat;
            this.value = value;
            this.percent = percent;
            this.prefix = prefix;
        }
    }

    public boolean isAttachment() {
        return this.type == ExtractorPartType.ATTACHMENT;
    }

    /** True once an attachment has worn through its own pool; it stays installed but contributes nothing. */
    public boolean isDepleted() {
        return this.maxDurability > 0 && this.durability <= 0;
    }

    /** How much of the attachment's own pool is left, 0..1; 1 for anything with no pool of its own. */
    public float durabilityFraction() {
        if (this.maxDurability <= 0) {
            return 1.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (float) this.durability / this.maxDurability));
    }

    public void addStat(ExtractorStat stat, float value) {
        this.stats.merge(stat, value, Float::sum);
    }

    public void addPercent(ExtractorStat stat, float value) {
        this.percents.merge(stat, value, Float::sum);
    }

    public void multiply(ExtractorStat stat, float value) {
        this.multipliers.merge(stat, value, (a, b) -> a * b);
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getId());
        tag.putString("Series", this.series);
        tag.putString("DrillClass", this.drillClass);
        tag.putString("Attachment", this.attachmentId);
        tag.putString("Rarity", ExtractorPartsConfig.rarityKey(this.rarity));
        tag.putFloat("Quality", this.quality);
        tag.putString("Name", this.name);
        tag.putString("BaseName", this.baseName);
        tag.putInt("Durability", this.durability);
        tag.putInt("MaxDurability", this.maxDurability);
        tag.put("Stats", writeMap(this.stats));
        tag.put("Multipliers", writeMap(this.multipliers));
        tag.put("Percents", writeMap(this.percents));
        ListTag affixList = new ListTag();
        for (Affix affix : this.affixes) {
            CompoundTag a = new CompoundTag();
            a.putString("Id", affix.id);
            a.putString("Stat", affix.stat.getId());
            a.putFloat("Value", affix.value);
            a.putBoolean("Percent", affix.percent);
            a.putBoolean("Prefix", affix.prefix);
            affixList.add(a);
        }
        tag.put("Affixes", affixList);
        return tag;
    }

    /**
     * Attachments rolled before they had a pool of their own stored their durability as an ordinary
     * stat, which fed the extractor's pool. Those roll into the attachment's own pool here.
     */
    @Nullable
    public static PartData read(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        PartData data = new PartData();
        ExtractorPartType type = ExtractorPartType.byId(tag.getString("Type"));
        if (type == null) {
            return null;
        }
        data.type = type;
        data.series = tag.getString("Series");
        data.drillClass = tag.getString("DrillClass");
        data.attachmentId = tag.getString("Attachment");
        VaultGearRarity rarity = ExtractorPartsConfig.rarityOf(tag.getString("Rarity"));
        data.rarity = rarity == null ? VaultGearRarity.SCRAPPY : rarity;
        data.quality = tag.getFloat("Quality");
        data.name = tag.getString("Name");
        data.baseName = tag.contains("BaseName") ? tag.getString("BaseName") : data.name;
        data.maxDurability = tag.getInt("MaxDurability");
        data.durability = tag.contains("Durability") ? tag.getInt("Durability") : data.maxDurability;
        readMap(tag.getCompound("Stats"), data.stats);
        readMap(tag.getCompound("Multipliers"), data.multipliers);
        readMap(tag.getCompound("Percents"), data.percents);
        if (data.type == ExtractorPartType.ATTACHMENT && data.stats.containsKey(ExtractorStat.DURABILITY)) {
            float legacy = data.stats.remove(ExtractorStat.DURABILITY);
            if (data.maxDurability <= 0) {
                data.maxDurability = Math.max(0, Math.round(legacy));
                data.durability = data.maxDurability;
            }
        }
        ListTag affixList = tag.getList("Affixes", Tag.TAG_COMPOUND);
        for (int i = 0; i < affixList.size(); i++) {
            CompoundTag a = affixList.getCompound(i);
            ExtractorStat stat = ExtractorStat.byId(a.getString("Stat"));
            if (stat != null) {
                boolean prefix = a.contains("Prefix") ? a.getBoolean("Prefix") : !a.getString("Id").startsWith("of_");
                data.affixes.add(new Affix(a.getString("Id"), stat, a.getFloat("Value"), a.getBoolean("Percent"), prefix));
            }
        }
        return data;
    }

    private static CompoundTag writeMap(Map<ExtractorStat, Float> map) {
        CompoundTag tag = new CompoundTag();
        map.forEach((stat, value) -> tag.putFloat(stat.getId(), value));
        return tag;
    }

    private static void readMap(CompoundTag tag, Map<ExtractorStat, Float> into) {
        for (String key : tag.getAllKeys()) {
            ExtractorStat stat = ExtractorStat.byId(key);
            if (stat != null) {
                into.put(stat, tag.getFloat(key));
            }
        }
    }

    public static Style style(VaultGearRarity rarity) {
        return Style.EMPTY.withColor(rarity.getColor());
    }

    public String describeOrigin() {
        String origin = this.type == ExtractorPartType.DRILL ? this.drillClass : this.series;
        return origin + "/" + ExtractorPartsConfig.rarityKey(this.rarity) + String.format("/Q%.2f", this.quality);
    }
}
