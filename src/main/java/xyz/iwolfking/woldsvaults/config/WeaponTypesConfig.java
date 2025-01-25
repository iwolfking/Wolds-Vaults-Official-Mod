package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponTypesConfig extends Config {

    @Expose
    public Map<String, Entry> WEAPON_TYPES_MAP = new HashMap<>();

    @Override
    public String getName() {
        return "weapon_types";
    }

    @Override
    protected void reset() {

    }

    public class Entry {
        @Expose
        public String NBT;

        @Expose
        public int COLOR;

        @Expose
        public List<String> ALLOWED_TYPES;

        public String displayAllowedTypes() {
            return "Allowed On: " + String.join(",", ALLOWED_TYPES);
        }


    }
}
