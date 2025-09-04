package xyz.iwolfking.woldsvaults.items.fruits.smoothie;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;

public abstract class SmoothieModifierConfig {

    @Expose
    private ResourceLocation id;

    public SmoothieModifierConfig(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getModifierId() {
        return id;
    }
}
