package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

public class ExtractorPartPrefixAttribute extends ExtractorPartAffixAttribute {
    public ExtractorPartPrefixAttribute(String value) {
        super(value);
    }

    @Override
    protected boolean shouldList(PartData.Affix affix) {
        return affix.prefix;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_prefix";
    }
}
