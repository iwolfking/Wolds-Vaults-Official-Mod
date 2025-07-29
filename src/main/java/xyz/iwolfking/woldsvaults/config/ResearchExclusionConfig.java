package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.util.GlobUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchExclusionConfig extends Config {

    @Expose
    public Map<String, List<String>> RESEARCH_EXCLUSIONS = new HashMap<>();
    public Map<String, Map<String, Boolean>> researchExclusionCache = new HashMap<>();

    @Override
    public String getName() {
        return "research_exclusions";
    }

    @Override
    protected void reset() {

    }

    public boolean isExcludedFromResearch(String researchName, ResourceLocation itemId) {
        if(!RESEARCH_EXCLUSIONS.containsKey(researchName)) {
            return false;
        }

        if(!researchExclusionCache.containsKey(researchName)) {
            researchExclusionCache.put(researchName, new HashMap<>());
        }

        if(itemId == null) {
            return false;
        }

        Map<String, Boolean> researchCache = researchExclusionCache.get(researchName);

        return researchCache.computeIfAbsent(itemId.toString(), id -> {
            for(String pattern : RESEARCH_EXCLUSIONS.get(researchName)) {
                if(pattern.startsWith("!")) {
                    if(GlobUtils.matches(pattern.substring(1), id)) {
                        return false;
                    }
                }
                else if(GlobUtils.matches(pattern, id)) {
                    return true;
                }
            }
            return false;
        });
    }
}
