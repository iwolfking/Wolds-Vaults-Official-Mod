package xyz.iwolfking.woldsvaults.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.*;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

public class ModVaultEvents {


    public static void init() {
        VaultEventSystem.register(WoldsVaults.id("tombstone_event"), new VaultEvent.Builder()
                .tag(EventTag.TOMBSTONE)
                .displayType(VaultEvent.EventDisplayType.NONE)
                        .task(new WeightedTask.Builder()
                                .task(new SpawnMobTask.Builder()
                                        .entity(iskallia.vault.init.ModEntities.ELITE_ZOMBIE, 2.0)
                                        .entity(iskallia.vault.init.ModEntities.ELITE_SKELETON, 7.0)
                                        .entity(iskallia.vault.init.ModEntities.ELITE_WITHER_SKELETON, 1.0)
                                        .build(), 1.0)
                                .task(new PlaySoundTask(SoundEvents.AMBIENT_CAVE, 1.0F, 0.4F, SoundSource.AMBIENT), 1.0)
                                .task(new TaskGroup.Builder()
                                        .task(new PlayerMobEffectTask.Builder()
                                                .effect(MobEffects.WITHER, 1, 200)
                                                .build()
                                        )
                                        .task(new PlaySoundTask(SoundEvents.SOUL_ESCAPE))
                                        .task(new MessageTask(VaultEvent.EventDisplayType.ACTION_BAR, (TextComponent) new TextComponent("You feel your body start to decay!").withStyle(ChatFormatting.DARK_RED)))
                                        .build(),1.0)
                                .task(new TaskGroup.Builder()
                                        .task(new SpawnMobTask.Builder()
                                                .entity(ModEntities.GREEN_GHOST, 1)
                                                .entity(ModEntities.BROWN_GHOST, 1)
                                                .entity(ModEntities.RED_GHOST, 1)
                                                .entity(ModEntities.BLUE_GHOST, 1)
                                                .entity(ModEntities.YELLOW_GHOST, 1)
                                                .amount(3, 1)
                                                .amount(5, 1)
                                                .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 200), 1)
                                                .build()
                                        )
                                        .task(new PlaySoundTask(SoundEvents.SOUL_ESCAPE))
                                        .task(new MessageTask(VaultEvent.EventDisplayType.ACTION_BAR, (TextComponent) new TextComponent("Spirits awaken around you!").withStyle(ChatFormatting.AQUA)))
                                        .build(),1.0)
                                .task(new TaskGroup.Builder()
                                        .task(new SpawnMobTask.Builder()
                                                .entity(iskallia.vault.init.ModEntities.T3_ZOMBIE, 1)
                                                .amount(3, 1)
                                                .amount(5, 1)
                                                .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 200), 1)
                                                .build()
                                        )
                                        .task(new PlaySoundTask(SoundEvents.SOUL_ESCAPE))
                                        .task(new MessageTask(VaultEvent.EventDisplayType.ACTION_BAR, (TextComponent) new TextComponent("Spirits awaken around you!").withStyle(ChatFormatting.AQUA)))
                                        .build(),1.0)
                                .build()
                        )
                        .task(new DelayTask(20))
                        .task(new BlockConversionTask.Builder()
                                .replacementBlock(Blocks.DIRT.defaultBlockState(), 4)
                                .replacementBlock(Blocks.ROOTED_DIRT.defaultBlockState(), 2)
                                .replacementBlock(Blocks.STONE.defaultBlockState(), 1)
                                .replacementBlock(Blocks.COARSE_DIRT.defaultBlockState(), 2)
                                .replacementBlock(Blocks.ANDESITE.defaultBlockState(), 1)
                                .replacementBlock(Blocks.PODZOL.defaultBlockState(), 2)
                                .build(block -> block.equals(Blocks.BEDROCK)))
                        .task(new DelayTask(10))
                        .task(new BlockConversionTask.Builder().build(block -> block.equals(Blocks.BARRIER)))

                .build("Tombstone", new TextComponent("Possible events that can occur when breaking a Tombstone.")));
    }
}
