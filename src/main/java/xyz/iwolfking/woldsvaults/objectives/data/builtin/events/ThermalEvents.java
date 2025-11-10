package xyz.iwolfking.woldsvaults.objectives.data.builtin.events;

import cofh.thermal.core.entity.explosive.DetonateUtils;
import cofh.thermal.core.init.TCoreEntities;
import iskallia.vault.init.ModEntities;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.RepeatTask;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.SpawnMobTask;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

public class ThermalEvents {
    public static void init() {

        EnchantedEventsRegistry.register(WoldsVaults.id("bombs_away"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .color(TextColor.parseColor("#7D3963"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                        .task(new RepeatTask(new SpawnMobTask.Builder()
                                .entity(DetonateUtils.GRENADES.get(0).get(), 8.0)
                                .entity(DetonateUtils.GRENADES.get(1).get(), 8.0)
                                .entity(DetonateUtils.GRENADES.get(2).get(), 8.0)
                                .entity(DetonateUtils.GRENADES.get(3).get(), 8.0)
                                .entity(DetonateUtils.GRENADES.get(4).get(), 4.0)
                                .entity(DetonateUtils.GRENADES.get(5).get(), 1.0)
                                .entity(DetonateUtils.GRENADES.get(6).get(), 4.0)
                                .entity(DetonateUtils.GRENADES.get(7).get(), 4.0)
                                .entity(DetonateUtils.GRENADES.get(8).get(), 8.0)
                                .amount(7, 3.0)
                                .amount(10, 1.0)
                                .spawnRanges(4.0, 16.0)
                                .build(), 120, 10))
                .build("Thermal Catastrophe", new TextComponent("The climate is changing o.o")), 5.0);

        EnchantedEventsRegistry.register(WoldsVaults.id("thermal_attack"), new VaultEvent.Builder()
                .tag(EventTag.NEGATIVE)
                .tag(EventTag.SPAWN_MOB)
                .color(TextColor.parseColor("#8ab97d"))
                .displayType(VaultEvent.EventDisplayType.LEGACY)
                .task(new SpawnMobTask.Builder()
                        .entity(TCoreEntities.BASALZ.get(), 1.0)
                        .entity(TCoreEntities.BLIZZ.get(), 1.0)
                        .entity(TCoreEntities.BLITZ.get(), 1.0)
                        .entity(EntityType.BLAZE, 1.0)
                        .amount(4,  15)
                        .amount(6, 10)
                        .amount(8, 8)
                        .effect(new Effect(MobEffects.DAMAGE_RESISTANCE, 2, 400), 1)
                        .effect(new Effect(MobEffects.DAMAGE_BOOST, 2, 400), 1)
                        .effect(new Effect(MobEffects.MOVEMENT_SPEED, 2, 400), 1)
                        .build()
                )
                .build("Thermal Expansion", new TextComponent("Thermally expand these mobs into your face.")), 10.0);
    }
}

