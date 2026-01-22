/*
* Lets you cast the specified ability around any entity
* currently implemented:
*   Nova
*   Smite
*   Implode
* */

package xyz.iwolfking.woldsvaults.abilities.flexible;

import iskallia.vault.skill.ability.effect.ImplodeAbility;
import iskallia.vault.skill.ability.effect.SmiteAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractNovaAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jline.utils.Log;

import java.util.Optional;

public class FlexibleAbility {

    public float cast(String ability, Player player, Entity target) {
        Optional<Ability> abilityToCast = getAbilityData(ability, PlayerAbilitiesData.get((ServerLevel)player.level).getAbilities(player));
        return abilityToCast.map(value -> castAbility(ability, value, player, target)).orElse(0F);
    }

    protected float castAbility(String ability, Ability abilityToCast, Player player, Entity target) {
        float durabilityDamage = 0;
        //Log.info("Ability: " + abilityToCast);
        switch (ability) {
            case "Nova":
                FlexibleNova novaAbility = new FlexibleNova();
                novaAbility.cast(player, target, (AbstractNovaAbility) abilityToCast);
                durabilityDamage = ((AbstractNovaAbility) abilityToCast).getManaCost();
                break;
            case "Implode":
                FlexibleImplode implodeAbility = new FlexibleImplode();
                if (target instanceof LivingEntity) {
                    durabilityDamage = implodeAbility.cast(player, (LivingEntity) target, (ImplodeAbility) abilityToCast);
                }
                break;
            case "Smite":
                FlexibleSmite smiteAbility = new FlexibleSmite();
                durabilityDamage = smiteAbility.cast(player, target, (SmiteAbility) abilityToCast);
                break;
            default:
        }
        return durabilityDamage;
    }

    public Optional<Ability> getAbilityData(String ability, AbilityTree tree) {
        return tree.getForId(ability).map((skill) -> {
            Object activeSkill;
            if (skill instanceof SpecializedSkill specialized) {
                activeSkill = specialized.getSpecialization();
            } else {
                activeSkill = skill;
            }

            Skill var2 = (Skill) activeSkill;
            if (var2 instanceof TieredSkill tiered) {
                activeSkill = tiered.getChild();
            } else {
                activeSkill = var2;
            }

            var2 = (Skill) activeSkill;
            return (Ability) (var2 instanceof Ability ? var2 : null);
        });
    }
}
