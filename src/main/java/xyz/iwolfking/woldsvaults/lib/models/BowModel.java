package xyz.iwolfking.woldsvaults.lib.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.init.ModDynamicModels;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

public class BowModel extends DynamicModel<BowModel> {
    public BowModel(ResourceLocation id, String displayName) {
        super(id, displayName);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockModel generateItemModel(Map<String, ResourceLocation> textures) {
        String jsonPattern = """
            {
              "parent": "item/generated",
              "textures": {
                "layer0": "minecraft:item/bow"
              },
              "display": {
                "thirdperson_righthand": {
                  "rotation": [
                    -80,
                    260,
                    -40
                  ],
                  "translation": [
                    -1,
                    -2,
                    2.5
                  ],
                  "scale": [
                    0.9,
                    0.9,
                    0.9
                  ]
                },
                "thirdperson_lefthand": {
                  "rotation": [
                    -80,
                    -280,
                    40
                  ],
                  "translation": [
                    -1,
                    -2,
                    2.5
                  ],
                  "scale": [
                    0.9,
                    0.9,
                    0.9
                  ]
                },
                "firstperson_righthand": {
                  "rotation": [
                    0,
                    -90,
                    25
                  ],
                  "translation": [
                    1.13,
                    3.2,
                    1.13
                  ],
                  "scale": [
                    0.68,
                    0.68,
                    0.68
                  ]
                },
                "firstperson_lefthand": {
                  "rotation": [
                    0,
                    90,
                    -25
                  ],
                  "translation": [
                    1.13,
                    3.2,
                    1.13
                  ],
                  "scale": [
                    0.68,
                    0.68,
                    0.68
                  ]
                }
              },
              "overrides": [
                {
                  "predicate": {
                    "pulling": 1
                  },
                  "model": "minecraft:item/bow_pulling_0"
                },
                {
                  "predicate": {
                    "pulling": 1,
                    "pull": 0.65
                  },
                  "model": "minecraft:item/bow_pulling_1"
                },
                {
                  "predicate": {
                    "pulling": 1,
                    "pull": 0.9
                  },
                  "model": "minecraft:item/bow_pulling_2"
                },
            {
                        "predicate": {
                            "custom_model_data": 1
                        },
                        "model": "item/custom_bow"
                    },
                    {
                        "predicate": {
                            "custom_model_data": 1,
                            "pulling": 1
                        },
                        "model": "item/custom_bow_pulling_0"
                    },
                    {
                        "predicate": {
                            "custom_model_data": 1,
                            "pulling": 1,
                            "pull": 0.65
                        },
                        "model": "item/custom_bow_pulling_1"
                    },
                    {
                        "predicate": {
                            "custom_model_data": 1,
                            "pulling": 1,
                            "pull": 0.9
                        },
                        "model": "item/custom_bow_pulling_2"
                    }\
              ]
            }""";
        return this.createUnbakedModel(jsonPattern, textures);
    }

    @Override
    protected BlockModel createUnbakedModel(String jsonPattern, Map<String, ResourceLocation> textures) {
//        String texturesJson = (String)textures.entrySet().stream().map((entry) -> {
//            if(entry.getKey().equals("bow_pulling_0") || entry.getKey().equals("bow_pulling_1") || entry.getKey().equals("bow_pulling_2")) {
//                return null;
//            }
//            String var10000 = (String)entry.getKey();
//            return "\"" + var10000 + "\": \"" + entry.getValue() + "\"";
//        }).collect(Collectors.joining(", ", "{", "}"));
//        String modelJson = jsonPattern.replace("{{textures}}", texturesJson);
//        modelJson = modelJson.replace("{{bow_pulling_0}}", textures.get("bow_pulling_model_0").toString());
//        modelJson = modelJson.replace("{{bow_pulling_1}}", textures.get("bow_pulling_model_1").toString());
//        modelJson = modelJson.replace("{{bow_pulling_2}}", textures.get("bow_pulling_model_2").toString());
//        System.out.println(modelJson);
//        WoldsVaults.LOGGER.info(modelJson);
        return BlockModel.fromString(jsonPattern);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Map<String, ResourceLocation> resolveTextures(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        HashMap<String, ResourceLocation> textures = new HashMap<>();
        textures.put("layer0", ModDynamicModels.textureExists(resourceManager, resourceLocation) ? resourceLocation : ModDynamicModels.EMPTY_TEXTURE);
        if (ModDynamicModels.textureExists(resourceManager, appendToId(resourceLocation, "_overlay"))) {
            textures.put("layer1", appendToId(resourceLocation, "_overlay"));
        }

        for(int i = 0; i < 10; ++i) {
            if (ModDynamicModels.textureExists(resourceManager, appendToId(resourceLocation, "_layer" + i))) {
                textures.put("layer" + i, appendToId(resourceLocation, "_layer" + i));
            }
        }

        String modelName = resourceLocation.toString().substring(resourceLocation.toString().lastIndexOf('/') + 1);
        if(ModDynamicModels.jsonModelExists(resourceManager, appendToId(resourceLocation, "_pulling_0"))) {
            textures.put("bow_pulling_model_0", VaultMod.id("models/item/gear/bow/" + modelName + "_pulling_0"));
        }

        if(ModDynamicModels.jsonModelExists(resourceManager, appendToId(resourceLocation, "_pulling_1"))) {
            textures.put("bow_pulling_model_1", VaultMod.id("models/item/gear/bow/" + modelName + "_pulling_1"));
        }

        if(ModDynamicModels.jsonModelExists(resourceManager, appendToId(resourceLocation, "_pulling_2"))) {
            textures.put("bow_pulling_model_2", VaultMod.id("models/item/gear/bow/" + modelName + "_pulling_2"));
        }

        return textures;
    }
}
