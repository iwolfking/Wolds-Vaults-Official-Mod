package xyz.iwolfking.woldsvaults.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.SpawnMobTask;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

public class ModVaultEvents {


    public static void init() {
        VaultEventSystem.register(WoldsVaults.id("tombstone_0"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .displayType(VaultEvent.EventDisplayType.ACTION_BAR)
                .message((TextComponent) new TextComponent("Spirits react to your grave-robbing!").withStyle(ChatFormatting.DARK_GRAY))
                .task(new SpawnMobTask.Builder()
                        .entity(ModEntities.GREEN_GHOST, 1)
                        .entity(ModEntities.BROWN_GHOST, 1)
                        .entity(ModEntities.RED_GHOST, 1)
                        .entity(ModEntities.BLUE_GHOST, 1)
                        .entity(ModEntities.YELLOW_GHOST, 1)
                        .amount(3, 1)
                        .amount(5, 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 1, 200), 1)
                        .build()
                )
                .build("Tombstone Spirit Spawn", new TextComponent("Spawns multiple ghostly wraiths to impede your grave robbing.")));
    }
}
