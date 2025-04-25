package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.TemplateGenerationEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.DecoratorAddModifier;
import iskallia.vault.core.vault.modifier.reputation.ScalarReputationProperty;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.EmptyTemplate;
import iskallia.vault.core.world.template.JigsawTemplate;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.Template;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class DecoratorAddModifierSettable extends SettableValueVaultModifier<DecoratorAddModifierSettable.Properties>{
    public DecoratorAddModifierSettable(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.TEMPLATE_GENERATION.in(world).at(TemplateGenerationEvent.Phase.POST).register(context.getUUID(), (data) -> {
            if (!(data.getTemplate().getParent() instanceof EmptyTemplate)) {
                ProcessorContext processorContext = new ProcessorContext(vault, data.getRandom());

                for(int i = 0; i < ((DecoratorAddModifierSettable.Properties)this.properties).getAttemptsPerChunk(context); ++i) {
                    int x = data.getRandom().nextInt(16) + data.getChunkPos().x * 16;
                    int z = data.getRandom().nextInt(16) + data.getChunkPos().z * 16;
                    int y = data.getRandom().nextInt(64);
                    BlockPos pos = new BlockPos(x, y, z);
                    ServerLevelAccessor serverLevelAccessor = data.getWorld();
                    BlockState state = serverLevelAccessor.getBlockState(pos);
                    if (state.getBlock() == Blocks.AIR && (!((DecoratorAddModifierSettable.Properties)this.properties).isRequiresConditions() || serverLevelAccessor.getBlockState(pos.above()).isAir() && serverLevelAccessor.getBlockState(pos.below()).isFaceSturdy(serverLevelAccessor, pos, Direction.UP))) {
                        IZonedWorld.runWithBypass(world, true, () -> {
                            PartialTile tile = ((DecoratorAddModifierSettable.Properties)this.properties).output.copy().setPos(pos);
                            PlacementSettings settings = data.getTemplate().getSettings().copy();
                            Template patt3358$temp = data.getTemplate().getParent();
                            if (patt3358$temp instanceof JigsawTemplate jigsaw) {
                                jigsaw.getConfigurator().accept(settings);
                            }

                            for(TileProcessor processor : settings.getTileProcessors()) {
                                tile = (PartialTile)processor.process(tile, processorContext);
                                if (tile == null) {
                                    break;
                                }
                            }

                            if (tile != null) {
                                tile.place(serverLevelAccessor, pos, 3);
                            }
                        });
                    }
                }

            }
        });
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final PartialTile output;

        @Expose
        private final boolean requiresConditions;

        public Properties(PartialTile output) {
            this(output, true);
        }

        public Properties(PartialTile output, boolean requiresConditions) {
            this.output = output;
            this.requiresConditions = requiresConditions;
        }

        public PartialTile getOutput() {
            return this.output;
        }

        public int getAttemptsPerChunk(ModifierContext context) {
            return (int)this.getValue();
        }

        public boolean isRequiresConditions() {
            return requiresConditions;
        }
    }
}
