package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.EternalAttributeConfig;
import iskallia.vault.config.entry.FloatRangeEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class EternalAttributesConfig extends EternalAttributeConfig {
    @Expose
    private final Map<String, FloatRangeEntry> initialAttributes = new HashMap<>();

    @Expose
    private FloatRangeEntry healthPerLevel;

    @Expose
    private FloatRangeEntry damagePerLevel;

    @Expose
    private FloatRangeEntry moveSpeedPerLevel;

    @Override
    public Map<Attribute, Float> createAttributes() {
        Map<Attribute, Float> selectedAttributes = new HashMap<>();
        this.initialAttributes.forEach((attrKey, valueRange) -> {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(attrKey));
            if(attribute != null) {
                selectedAttributes.put(attribute, valueRange.getRandom());
            }
        });
        return selectedAttributes;
    }

    @Override
    public FloatRangeEntry getHealthRollRange() {
        return this.healthPerLevel;
    }

    @Override
    public FloatRangeEntry getDamageRollRange() {
        return this.damagePerLevel;
    }

    @Override
    public FloatRangeEntry getMoveSpeedRollRange() {
        return this.moveSpeedPerLevel;
    }
}
