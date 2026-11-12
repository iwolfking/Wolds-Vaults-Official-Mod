package xyz.iwolfking.woldsvaults.mixins.client;

import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientSpellBar;

import javax.annotation.Nullable;

@Mixin(targets = "iskallia.vault.client.render.hud.module.AbilitiesModule$AbilityData", remap = false)
public abstract class MixinAbilitiesModuleSpellBar {
    @Shadow
    private Ability previousAbilityNode;
    @Shadow
    private Ability nextAbilityNode;

    /** The HUD tray's previous and next icons follow the spell bar's order of filled slots, not VH's ability order. */
    @Inject(method = "update", at = @At("TAIL"))
    private void woldsvaults$barNeighbours(CallbackInfo ci) {
        if (!ClientSpellBar.isSynced()) {
            return;
        }
        int selected = ClientSpellBar.selected();
        Ability previous = abilityAt(ClientSpellBar.neighbour(selected, -1));
        Ability next = abilityAt(ClientSpellBar.neighbour(selected, 1));
        if (previous != null) {
            this.previousAbilityNode = previous;
        }
        if (next != null) {
            this.nextAbilityNode = next;
        }
    }

    @Nullable
    private static Ability abilityAt(int slot) {
        if (slot < 0) {
            return null;
        }
        String group = ClientSpellBar.entry(slot).group();
        if (group.isEmpty() || ClientAbilityData.getTree() == null) {
            return null;
        }
        return ClientAbilityData.getTree().getForId(group)
                .filter(SpecializedSkill.class::isInstance).map(SpecializedSkill.class::cast)
                .map(SpecializedSkill::getSpecialization)
                .filter(TieredSkill.class::isInstance).map(TieredSkill.class::cast)
                .filter(TieredSkill::isUnlocked)
                .map(TieredSkill::getChild)
                .filter(Ability.class::isInstance).map(Ability.class::cast)
                .orElse(null);
    }
}
