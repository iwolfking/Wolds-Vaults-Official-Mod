package xyz.iwolfking.woldsvaults.integration.vaultfilters.extractor;

import xyz.iwolfking.woldsvaults.extraction.parts.PartData;

public class ExtractorPartSuffixAttribute extends ExtractorPartAffixAttribute {
    public ExtractorPartSuffixAttribute(String value) {
        super(value);
    }

    @Override
    protected boolean shouldList(PartData.Affix affix) {
        return !affix.prefix;
    }

    @Override
    public String getTranslationKey() {
        return "extractor_part_suffix";
    }
}
