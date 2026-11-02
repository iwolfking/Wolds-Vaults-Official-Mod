package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import iskallia.vault.skill.ability.effect.NecromancyAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = NecromancyAbility.class, remap = false)
public class MixinNecromancyAbility implements IWoldAbilityEnhancements {

    @Unique
    private int woldsVaults$tier = -1;

    @Unique
    private List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    @Override
    public void setTierLevel(int level) {
        this.woldsVaults$tier = level;
    }

    @Override
    public int getTierLevel() {
        return woldsVaults$tier;
    }

    @Override
    public void setElementTypes(List<ElementalType> elementalTypes) {
        this.woldsVaults$elementalTypes = elementalTypes == null ? new ArrayList<>() : elementalTypes;
    }

    @Override
    public List<ElementalType> getElementTypes() {
        return woldsVaults$elementalTypes;
    }
}