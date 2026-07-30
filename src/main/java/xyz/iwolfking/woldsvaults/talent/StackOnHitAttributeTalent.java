package xyz.iwolfking.woldsvaults.talent;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.util.calc.EffectDurationHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;

import java.util.Optional;
import java.util.stream.Stream;

public class StackOnHitAttributeTalent extends GearAttributeTalent {
    private MobEffect effect;
    private int durationTicks;
    private int maxStacks;
    private int timeLeft;
    private int stacks;

    private boolean decayAllAtOnce = true;

    public StackOnHitAttributeTalent(
        int unlockLevel,
        int learnPointCost,
        int regretPointCost,
        VaultGearAttribute<?> attribute,
        double value,
        MobEffect effect,
        int durationTicks,
        int maxStacks,
        boolean decayAllAtOnce
    ) {
        super(unlockLevel, learnPointCost, regretPointCost, attribute, value);
        this.effect = effect;
        this.durationTicks = durationTicks;
        this.maxStacks = maxStacks;
        this.decayAllAtOnce = decayAllAtOnce;
    }

    public StackOnHitAttributeTalent() {
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getUnmodifiedDurationTicks() {
        return this.durationTicks;
    }

    public int getDurationTicks(LivingEntity entity) {
        int duration = this.getUnmodifiedDurationTicks();
        return EffectDurationHelper.adjustEffectDurationFloor(entity, duration);
    }

    @Override
    public double getValue() {
        return super.getValue() * this.stacks;
    }

    public int getStackCount() {
        return this.stacks;
    }

    @Override
    public boolean canApply(SkillContext context) {
        return super.canApply(context) && this.timeLeft > 0 && this.stacks > 0;
    }

    @Override
    public void onTick(SkillContext context) {
        if (--this.timeLeft < 0 && this.isUnlocked() && this.stacks > 0) {
            context.getSource().as(ServerPlayer.class).ifPresent(player -> {
                if (this.decayAllAtOnce) {
                    this.stacks = 0;
                    this.timeLeft = 0;
                } else {
                    this.stacks--;
                    if (this.stacks > 0) {
                        this.timeLeft = this.getDurationTicks(player);
                    } else {
                        this.timeLeft = 0;
                    }
                }
                this.refreshSnapshot(player);
            });
        }

        super.onTick(context);

        if (this.isUnlocked() && this.effect != null && this.timeLeft > 0 && this.stacks > 0) {
            context.getSource().as(ServerPlayer.class).ifPresent(player -> {
                player.removeEffect(this.effect);
                player.addEffect(new MobEffectInstance(this.effect, this.timeLeft, this.stacks - 1, true, false, true));
            });
        }
    }

    @Override
    public void onRemoveModifiers(SkillContext context) {
        context.getSource().as(LivingEntity.class).ifPresent(entity -> {
            Stream<VaultGearAttributeInstance<?>> attributesToRemove = Stream.of(VaultGearAttributeInstance.cast(this.getAttribute(), super.getValue()));
            VaultGearHelper.getModifiers(this.getUuid(), attributesToRemove).forEach((attribute, modifier) -> {
                AttributeInstance present = entity.getAttribute(attribute);
                if (present != null) {
                    present.removeModifier(modifier.getId());
                }
            });
        });
    }

    public void onStack(ServerPlayer player) {
        if (++this.stacks > this.maxStacks) {
            this.stacks = this.maxStacks;
        }

        this.timeLeft = this.getDurationTicks(player);
        this.refreshSnapshot(player);
    }

    public void removeStack(ServerPlayer player) {
        if (this.stacks > 0) {
            this.stacks--;
            if (this.stacks <= 0) {
                this.timeLeft = 0;
                if (this.effect != null) {
                    player.removeEffect(this.effect);
                }
            }
            this.refreshSnapshot(player);
        }
    }


    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.EFFECT.writeBits(this.effect, buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.durationTicks, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(this.maxStacks, buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.timeLeft, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(this.stacks, buffer);
        Adapters.BOOLEAN.writeBits(this.decayAllAtOnce, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.effect = Adapters.EFFECT.readBits(buffer).orElseThrow();
        this.durationTicks = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
        this.maxStacks = Adapters.INT_SEGMENTED_3.readBits(buffer).orElseThrow();
        this.timeLeft = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
        this.stacks = Adapters.INT_SEGMENTED_3.readBits(buffer).orElseThrow();
        this.decayAllAtOnce = Adapters.BOOLEAN.readBits(buffer).orElse(true);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.EFFECT.writeNbt(this.effect).ifPresent(tag -> nbt.put("effect", tag));
            Adapters.INT.writeNbt(this.durationTicks).ifPresent(tag -> nbt.put("durationTicks", tag));
            Adapters.INT.writeNbt(this.maxStacks).ifPresent(tag -> nbt.put("maxStacks", tag));
            Adapters.INT.writeNbt(this.timeLeft).ifPresent(tag -> nbt.put("timeLeft", tag));
            Adapters.INT.writeNbt(this.stacks).ifPresent(tag -> nbt.put("stacks", tag));
            Adapters.BOOLEAN.writeNbt(this.decayAllAtOnce).ifPresent(tag -> nbt.put("decayAllAtOnce", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.effect = Adapters.EFFECT.readNbt(nbt.get("effect")).orElseThrow(() -> new IllegalStateException("Unknown effect in " + nbt));
        this.durationTicks = Adapters.INT.readNbt(nbt.get("durationTicks")).orElse(0);
        this.maxStacks = Adapters.INT.readNbt(nbt.get("maxStacks")).orElse(0);
        this.timeLeft = Adapters.INT.readNbt(nbt.get("timeLeft")).orElse(0);
        this.stacks = Adapters.INT.readNbt(nbt.get("stacks")).orElse(0);
        this.decayAllAtOnce = Adapters.BOOLEAN.readNbt(nbt.get("decayAllAtOnce")).orElse(true);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.EFFECT.writeJson(this.effect).ifPresent(element -> json.add("effect", element));
            Adapters.INT.writeJson(this.durationTicks).ifPresent(element -> json.add("durationTicks", element));
            Adapters.INT.writeJson(this.maxStacks).ifPresent(element -> json.add("maxStacks", element));
            Adapters.INT.writeJson(this.timeLeft).ifPresent(element -> json.add("timeLeft", element));
            Adapters.INT.writeJson(this.stacks).ifPresent(element -> json.add("stacks", element));
            Adapters.BOOLEAN.writeJson(this.decayAllAtOnce).ifPresent(element -> json.add("decayAllAtOnce", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.effect = Adapters.EFFECT.readJson(json.get("effect")).orElseThrow(() -> new IllegalStateException("Unknown effect in " + json));
        this.durationTicks = Adapters.INT.readJson(json.get("durationTicks")).orElse(0);
        this.maxStacks = Adapters.INT.readJson(json.get("maxStacks")).orElse(0);
        this.timeLeft = Adapters.INT.readJson(json.get("timeLeft")).orElse(0);
        this.stacks = Adapters.INT.readJson(json.get("stacks")).orElse(0);
        this.decayAllAtOnce = Adapters.BOOLEAN.readJson(json.get("decayAllAtOnce")).orElse(true);
    }


    static {
        CommonEvents.ENTITY_DAMAGE.register(StackOnHitAttributeTalent.class, EventPriority.HIGHEST, event -> {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer player && !attacker.getLevel().isClientSide() && WoldEventHelper.isNormalAttack()) {
                if (event.getEntity() instanceof LivingEntity target && target.isAlive()) {
                    if (player.getServer() != null) {
                        ServerPlayer existing = player.getServer().getPlayerList().getPlayer(player.getUUID());
                        if (existing == player) {
                            TalentTree tree = PlayerTalentsData.get(attacker.getServer()).getTalents(player);
                            tree.getAll(StackOnHitAttributeTalent.class, Skill::isUnlocked)
                                .forEach(talent -> talent.onStack(player));
                        }
                    }
                }
            }
        });
    }

    public static int getStacks(ServerPlayer player, String talentId) {
        if (!player.getLevel().isClientSide()) {
            if (player.getServer() == null) return 0;

            TalentTree tree = PlayerTalentsData.get(player.getServer()).getTalents(player);
            Skill talent = tree.getForId(talentId).orElse(null);
            if(talent instanceof StackOnHitAttributeTalent stackingWeaponAttributeTalent) {
                return stackingWeaponAttributeTalent.getStackCount();
            }
        }

        return 0;
    }
}