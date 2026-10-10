package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.gear.attribute.ability.special.base.ConfiguredModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.template.config.FloatRangeConfig;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import iskallia.vault.skill.ability.effect.FireballAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.FireballModification;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Mixin(value = FireballAbility.class, remap = false)
public class MixinFireballAbility implements IWoldAbilityEnhancements {

    @Unique
    private int woldsVaults$tier = -1;

    @Unique
    private List<ElementalType> woldsVaults$elementalTypes = new ArrayList<>();

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected Ability.ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            VaultFireball fireball = new VaultFireball(player.level, player);
            if (fireball instanceof IWoldAbilityEnhancements storedAbilityTier) {
                storedAbilityTier.setTierLevel(getTierLevel());
                storedAbilityTier.setElementTypes(woldsVaults$elementalTypes);
            }
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 0.0F);
            fireball.pickup = AbstractArrow.Pickup.DISALLOWED;
            fireball.setType(VaultFireball.FireballType.BASE);
            player.level.addFreshEntity(fireball);
            for (ConfiguredModification<FireballModification, FloatRangeConfig, FloatValue> mod : SpecialAbilityModification.getModifications(player, FireballModification.class)) {
                if (player.level.random.nextFloat() <= mod.value().getValue()) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            VaultFireball fireball2 = new VaultFireball(player.level, player);
                            if (fireball2 instanceof IWoldAbilityEnhancements storedAbilityTier) {
                                storedAbilityTier.setTierLevel(getTierLevel());
                            }
                            fireball2.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 0.0F);
                            fireball2.pickup = AbstractArrow.Pickup.DISALLOWED;
                            fireball2.setType(VaultFireball.FireballType.BASE);
                            player.level.addFreshEntity(fireball2);
                            player.level.playSound(null, player, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                            timer.cancel();
                        }
                    }, 2000, 10000);

                }
            }
            player.level.playSound(null, player, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            return Ability.ActionResult.successCooldownImmediate();
        }).orElse(Ability.ActionResult.fail());
    }

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
        this.woldsVaults$elementalTypes = elementalTypes;
    }

    @Override
    public List<ElementalType> getElementTypes() {
        return woldsVaults$elementalTypes;
    }
}
