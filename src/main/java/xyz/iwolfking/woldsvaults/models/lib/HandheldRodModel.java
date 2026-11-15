package xyz.iwolfking.woldsvaults.models.lib;

import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

public class HandheldRodModel extends DynamicModel<HandheldRodModel> {
    public HandheldRodModel(ResourceLocation id, String displayName) {
        super(id, displayName);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockModel generateItemModel(Map<String, ResourceLocation> textures) {
        String jsonPattern = "{" +
                "    \"parent\": \"item/handheld\"," +
                "    \"textures\": {{textures}}," +
                "    \"display\": {" +
                "        \"thirdperson_righthand\": {" +
                "            \"rotation\": [ 0, 90, 55 ]," +
                "            \"translation\": [ 0, 4.0, 2.5 ]," +
                "            \"scale\": [ 0.85, 0.85, 0.85 ]" +
                "        }," +
                "        \"thirdperson_lefthand\": {" +
                "            \"rotation\": [ 0, -90, -55 ]," +
                "            \"translation\": [ 0, 4.0, 2.5 ]," +
                "            \"scale\": [ 0.85, 0.85, 0.85 ]" +
                "        }," +
                "        \"firstperson_righthand\": {" +
                "            \"rotation\": [ 0, 90, 25 ]," +
                "            \"translation\": [ 0, 1.6, 0.8 ]," +
                "            \"scale\": [ 0.68, 0.68, 0.68 ]" +
                "        }," +
                "        \"firstperson_lefthand\": {" +
                "            \"rotation\": [ 0, -90, -25 ]," +
                "            \"translation\": [ 0, 1.6, 0.8 ]," +
                "            \"scale\": [ 0.68, 0.68, 0.68 ]" +
                "        }" +
                "    }" +
                "}";
        return this.createUnbakedModel(jsonPattern, textures);
    }
}