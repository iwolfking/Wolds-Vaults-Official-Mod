package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.init.ModSounds;
import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.skill.ability.effect.ManaShieldAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbilityEffect;
import iskallia.vault.skill.ability.effect.spi.core.ToggleManaAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.Optional;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class UltimateShieldAbility extends ToggleManaAbility {
    private float manaPerDamageScalar;
    private float percentageDamageAbsorbed;

    private float baseManaDrainPerTick = 0.5F;
    private float manaDrainRampPerSecond = 0.25F;
    private float maxManaDrainPerTick = 10.0F;
    private int activeTicks = 0;

    public float getPercentageDamageAbsorbed() {
        return this.percentageDamageAbsorbed;
    }

    public float getManaPerDamageScalar() {
        return manaPerDamageScalar;
    }

    private ToggleAbilityEffect getEffect() {
        return ModEffects.ULTIMATE_SHIELD;
    }

    @Override
    protected Ability.ActionResult doToggle(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            if (this.isActive()) {
                this.activeTicks = 0;
                this.getEffect().addTo(player, 0);
                return Ability.ActionResult.successCooldownDeferred();
            } else {
                this.activeTicks = 0;
                player.removeEffect(this.getEffect());
                return Ability.ActionResult.successCooldownImmediate();
            }
        }).orElse(Ability.ActionResult.fail());
    }

    @Override
    protected void doToggleSound(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            if(!this.isActive()) return;
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MANA_SHIELD, SoundSource.PLAYERS, 0.5F, 0.2F);
        });
    }

    @Override
    public Ability.TickResult doActiveTick(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            this.activeTicks++;

            float elapsedSeconds = this.activeTicks / 20.0F;
            float currentTickCost = Math.min(
                    this.baseManaDrainPerTick + (elapsedSeconds * this.manaDrainRampPerSecond),
                    this.maxManaDrainPerTick
            );

            if (Mana.get(player) < currentTickCost) {
                player.removeEffect(this.getEffect());
                this.activeTicks = 0;
                return Ability.TickResult.PASS;
            }

            Mana.decrease(player, ManaAction.PLAYER_ACTION, currentTickCost);
            return Ability.TickResult.PASS;
        }).orElse(Ability.TickResult.PASS);
    }

    @Override
    public Ability.TickResult doInactiveTick(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            this.activeTicks = 0;
            if (player.hasEffect(this.getEffect())) {
                player.removeEffect(this.getEffect());
            }

            return Ability.TickResult.PASS;
        }).orElse(Ability.TickResult.PASS);
    }

    @Override
    public void onRemove(SkillContext context) {
        this.activeTicks = 0;
        context.getSource().as(ServerPlayer.class).ifPresent((entity) -> entity.removeEffect(this.getEffect()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void on(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AbilityTree abilities = PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);
            for (UltimateShieldAbility ability : abilities.getAll(UltimateShieldAbility.class, Skill::isUnlocked)) {
                if (player.hasEffect(ability.getEffect())) {
                    float percentageDamageAbsorbed = Mth.clamp(ability.getPercentageDamageAbsorbed(), 0.0F, 1.0F);

                    float damageToManaEfficiencyMultiplier = 0.5F; 
                    float manaCostPerDamage = ability.getManaPerDamageScalar() * damageToManaEfficiencyMultiplier;

                    manaCostPerDamage = Math.max(manaCostPerDamage, 1.0E-5F);
                    float manaUsed = Math.min(event.getAmount() * percentageDamageAbsorbed * manaCostPerDamage, Mana.get(player));
                    float damageAbsorbed = manaUsed / manaCostPerDamage;
                    
                    if (Mth.equal(damageAbsorbed, 0.0F)) {
                        return;
                    }
                    if (Mth.equal(damageAbsorbed, event.getAmount())) {
                        event.setCanceled(true);
                    } else {
                        event.setAmount(event.getAmount() - damageAbsorbed);
                    }

                    float mana = Mana.decrease(player, ManaAction.PLAYER_ACTION, manaUsed);
                    ability.onDamageAbsorbed(player, damageAbsorbed);
                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MANA_SHIELD_HIT, SoundSource.PLAYERS, 0.1F, 1.25F + -0.5F * (mana / Mana.getMax(player)));
                }
            }
        }
    }

    protected void onDamageAbsorbed(ServerPlayer player, float amount) {
    }

    protected void onEffectRemoved(ServerPlayer player) {
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.percentageDamageAbsorbed, buffer);
        Adapters.FLOAT.writeBits(this.manaPerDamageScalar, buffer);
        Adapters.FLOAT.writeBits(this.baseManaDrainPerTick, buffer);
        Adapters.FLOAT.writeBits(this.manaDrainRampPerSecond, buffer);
        Adapters.FLOAT.writeBits(this.maxManaDrainPerTick, buffer);
        Adapters.INT.writeBits(this.activeTicks, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.percentageDamageAbsorbed = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.manaPerDamageScalar = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.baseManaDrainPerTick = Adapters.FLOAT.readBits(buffer).orElse(0.5F);
        this.manaDrainRampPerSecond = Adapters.FLOAT.readBits(buffer).orElse(0.25F);
        this.maxManaDrainPerTick = Adapters.FLOAT.readBits(buffer).orElse(10.0F);
        this.activeTicks = Adapters.INT.readBits(buffer).orElse(0);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.percentageDamageAbsorbed).ifPresent(tag -> nbt.put("percentageDamageAbsorbed", tag));
            Adapters.FLOAT.writeNbt(this.manaPerDamageScalar).ifPresent(tag -> nbt.put("manaPerDamageScalar", tag));
            Adapters.FLOAT.writeNbt(this.baseManaDrainPerTick).ifPresent(tag -> nbt.put("baseManaDrainPerTick", tag));
            Adapters.FLOAT.writeNbt(this.manaDrainRampPerSecond).ifPresent(tag -> nbt.put("manaDrainRampPerSecond", tag));
            Adapters.FLOAT.writeNbt(this.maxManaDrainPerTick).ifPresent(tag -> nbt.put("maxManaDrainPerTick", tag));
            Adapters.INT.writeNbt(this.activeTicks).ifPresent(tag -> nbt.put("activeTicks", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.percentageDamageAbsorbed = Adapters.FLOAT.readNbt(nbt.get("percentageDamageAbsorbed")).orElse(0.0F);
        this.manaPerDamageScalar = Adapters.FLOAT.readNbt(nbt.get("manaPerDamageScalar")).orElse(0.0F);
        this.baseManaDrainPerTick = Adapters.FLOAT.readNbt(nbt.get("baseManaDrainPerTick")).orElse(0.5F);
        this.manaDrainRampPerSecond = Adapters.FLOAT.readNbt(nbt.get("manaDrainRampPerSecond")).orElse(0.25F);
        this.maxManaDrainPerTick = Adapters.FLOAT.readNbt(nbt.get("maxManaDrainPerTick")).orElse(10.0F);
        this.activeTicks = Adapters.INT.readNbt(nbt.get("activeTicks")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(Float.valueOf(this.percentageDamageAbsorbed)).ifPresent(element -> json.add("percentageDamageAbsorbed", element));
            Adapters.FLOAT.writeJson(Float.valueOf(this.manaPerDamageScalar)).ifPresent(element -> json.add("manaPerDamageScalar", element));
            Adapters.FLOAT.writeJson(Float.valueOf(this.baseManaDrainPerTick)).ifPresent(element -> json.add("baseManaDrainPerTick", element));
            Adapters.FLOAT.writeJson(Float.valueOf(this.manaDrainRampPerSecond)).ifPresent(element -> json.add("manaDrainRampPerSecond", element));
            Adapters.FLOAT.writeJson(Float.valueOf(this.maxManaDrainPerTick)).ifPresent(element -> json.add("maxManaDrainPerTick", element));
            Adapters.INT.writeJson(Integer.valueOf(this.activeTicks)).ifPresent(element -> json.add("activeTicks", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.percentageDamageAbsorbed = Adapters.FLOAT.readJson(json.get("percentageDamageAbsorbed")).orElse(0.0F);
        this.manaPerDamageScalar = Adapters.FLOAT.readJson(json.get("manaPerDamageScalar")).orElse(0.0F);
        this.baseManaDrainPerTick = Adapters.FLOAT.readJson(json.get("baseManaDrainPerTick")).orElse(0.5F);
        this.manaDrainRampPerSecond = Adapters.FLOAT.readJson(json.get("manaDrainRampPerSecond")).orElse(0.25F);
        this.maxManaDrainPerTick = Adapters.FLOAT.readJson(json.get("maxManaDrainPerTick")).orElse(10.0F);
        this.activeTicks = Adapters.INT.readJson(json.get("activeTicks")).orElse(0);
    }

    public static class UltimateShieldEffect extends ToggleAbilityEffect {
        public UltimateShieldEffect(int color, ResourceLocation resourceLocation) {
            super(ManaShieldAbility.class, color, resourceLocation);
        }

        @Override
        protected void removeAttributeModifiers(ServerPlayer player, AttributeMap attributeMap, int amplifier) {
            super.removeAttributeModifiers(player, attributeMap, amplifier);
            if (!player.hasEffect(this)) {
                AbilityTree abilities = PlayerAbilitiesData.get(player.getLevel()).getAbilities(player);

                for (UltimateShieldAbility ability : abilities.getAll(UltimateShieldAbility.class, Skill::isUnlocked)) {
                    ability.onEffectRemoved(player);
                }
            }
        }
    }
}