package xyz.iwolfking.woldsvaults.models.lib;

import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

public class BowHandheldModel extends DynamicModel<HandHeldModel> {

    public BowHandheldModel(ResourceLocation id, String displayName) {
        super(id, displayName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockModel generateItemModel(Map<String, ResourceLocation> textures) {
        String jsonPattern = """
                {
                    "parent": "item/generated",
                    "textures": {{textures}},
                    "display": {
                        "thirdperson_righthand": {
                            "rotation": [ -80, 260, -40 ],
                            "translation": [ -1, -2, 2.5 ],
                            "scale": [ 0.9, 0.9, 0.9 ]
                        },
                        "thirdperson_lefthand": {
                            "rotation": [ -80, -280, 40 ],
                            "translation": [ -1, -2, 2.5 ],
                            "scale": [ 0.9, 0.9, 0.9 ]
                        },
                        "firstperson_righthand": {
                            "rotation": [ 0, -90, 25 ],
                            "translation": [ 1.13, 3.2, 1.13],
                            "scale": [ 0.68, 0.68, 0.68 ]
                        },
                        "firstperson_lefthand": {
                            "rotation": [ 0, 90, -25 ],
                            "translation": [ 1.13, 3.2, 1.13],
                            "scale": [ 0.68, 0.68, 0.68 ]
                        }
                    },
                    "overrides": [
                        {
                            "predicate": {
                                "pulling": 1
                            },
                            "model": "item/bow_pulling_0"
                        },
                        {
                            "predicate": {
                                "pulling": 1,
                                "pull": 0.65
                            },
                            "model": "item/bow_pulling_1"
                        },
                        {
                            "predicate": {
                                "pulling": 1,
                                "pull": 0.9
                            },
                            "model": "item/bow_pulling_2"
                        }
                    ]
                }
                """;
        return this.createUnbakedModel(jsonPattern, textures);
    }
}
