package xyz.iwolfking.woldsvaults.talent.special;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.PlayerActiveFlags;
import iskallia.vault.event.ShockedMobTracker;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ShockedParticleMessage;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.EntityHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.api.util.WoldEventHelper;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaStackTuning;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.Optional;
import java.util.stream.Stream;

public class MomentumEngineTalent extends GearAttributeTalent {
    private MobEffect effect;
    private int maxStacks;
    private int stacks;
    private int chargeIntervalTicks;
    private int moveTickTracker;
    private int stationaryGraceTicks;
    private int stationaryTracker;
    private double shockwaveRadius;
    private double knockbackStrength;

    public MomentumEngineTalent(
        int unlockLevel,
        int learnPointCost,
        int regretPointCost,
        VaultGearAttribute<?> attribute,
        double valuePerStack,
        MobEffect effect,
        int maxStacks,
        int chargeIntervalTicks,
        int stationaryGraceTicks,
        double shockwaveRadius,
        double knockbackStrength
    ) {
        super(unlockLevel, learnPointCost, regretPointCost, attribute, valuePerStack);
        this.effect = effect;
        this.maxStacks = maxStacks;
        this.chargeIntervalTicks = chargeIntervalTicks;
        this.stationaryGraceTicks = stationaryGraceTicks;
        this.shockwaveRadius = shockwaveRadius;
        this.knockbackStrength = knockbackStrength;
    }

    public MomentumEngineTalent() {
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getStackCount() {
        return this.stacks;
    }

    @Override
    public double getValue() {
        return super.getValue() * this.stacks;
    }

    public int getMaxStacks(ServerPlayer player) {
        int maxStacksBonus = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ADDITIONAL_STACKING_STACKS, VaultGearAttributeTypeMerger.intSum());
        return IdonaStackTuning.maxStacks(player, maxStacks + maxStacksBonus);
    }

    @Override
    public boolean canApply(SkillContext context) {
        return super.canApply(context) && this.stacks > 0;
    }

    @Override
    public void onTick(SkillContext context) {
        super.onTick(context);

        if (!this.isUnlocked()) return;

        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            boolean isMoving = player.walkDist != player.walkDistO;
            if (isMoving) {
                this.stationaryTracker = 0;
                this.moveTickTracker++;

                if (this.moveTickTracker >= this.chargeIntervalTicks) {
                    this.moveTickTracker = 0;
                    if (this.stacks < (this.getMaxStacks(player))) {
                        this.stacks++;
                        this.refreshSnapshot(player);
                    }
                }
            } else {
                this.stationaryTracker++;

                if (this.stationaryTracker >= this.stationaryGraceTicks) {
                    this.stationaryTracker = 0;
                    this.moveTickTracker = 0;
                    if (this.stacks > 0) {
                        this.stacks--;
                        this.refreshSnapshot(player);
                    }
                }
            }

            if (this.effect != null) {
                if (this.stacks > 0) {
                    player.removeEffect(this.effect);
                    player.addEffect(new MobEffectInstance(this.effect, 40, this.stacks - 1, true, false, true));
                } else {
                    player.removeEffect(this.effect);
                }
            }
        });
    }

    public void onMeleeHit(ServerPlayer player, LivingEntity target) {
        if (this.stacks <= 0) return;

        if (this.stacks >= (this.getMaxStacks(player))) {
            ActiveFlags.IS_AOE_ATTACKING
                    .runIfNotSet(
                            () -> {
                                EntityHelper.knockbackIgnoreResist(target, player, (float)knockbackStrength);
                                ModNetwork.sendTrackingAndSelf(
                                        new ShockedParticleMessage(
                                                new Vec3(target.position().x, target.position().y + target.getBbHeight() / 2.0F, target.position().z),
                                                new Vec3(target.getBbWidth() / 2.0F, target.getBbHeight() / 2.0F, target.getBbWidth() / 2.0F),
                                                target.getId()
                                        ),
                                        target
                                );
                                if (target instanceof Mob attackedMob) {
                                    float attackDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                                    float adjustedRadius = AreaOfEffectHelper.adjustAreaOfEffect(player, null, (float)shockwaveRadius);
                                    ShockedMobTracker.register(attackedMob, player, attackDamage, adjustedRadius);
                                }

                                PlayerActiveFlags.set(player, PlayerActiveFlags.Flag.ATTACK_AOE, 2);

                            }
                    );
        }

        this.stacks = 0;
        this.moveTickTracker = 0;
        this.stationaryTracker = 0;
        if (this.effect != null) {
            player.removeEffect(this.effect);
        }
        this.refreshSnapshot(player);
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

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.EFFECT.writeBits(this.effect, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(this.maxStacks, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(this.stacks, buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.chargeIntervalTicks, buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.stationaryGraceTicks, buffer);
        Adapters.DOUBLE.writeBits(this.shockwaveRadius, buffer);
        Adapters.DOUBLE.writeBits(this.knockbackStrength, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.effect = Adapters.EFFECT.readBits(buffer).orElse(null);
        this.maxStacks = Adapters.INT_SEGMENTED_3.readBits(buffer).orElse(10);
        this.stacks = Adapters.INT_SEGMENTED_3.readBits(buffer).orElse(0);
        this.chargeIntervalTicks = Adapters.INT_SEGMENTED_7.readBits(buffer).orElse(10);
        this.stationaryGraceTicks = Adapters.INT_SEGMENTED_7.readBits(buffer).orElse(30);
        this.shockwaveRadius = Adapters.DOUBLE.readBits(buffer).orElse(3.5D);
        this.knockbackStrength = Adapters.DOUBLE.readBits(buffer).orElse(1.5D);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            if (this.effect != null) Adapters.EFFECT.writeNbt(this.effect).ifPresent(tag -> nbt.put("effect", tag));
            Adapters.INT.writeNbt(this.maxStacks).ifPresent(tag -> nbt.put("maxStacks", tag));
            Adapters.INT.writeNbt(this.stacks).ifPresent(tag -> nbt.put("stacks", tag));
            Adapters.INT.writeNbt(this.chargeIntervalTicks).ifPresent(tag -> nbt.put("chargeIntervalTicks", tag));
            Adapters.INT.writeNbt(this.stationaryGraceTicks).ifPresent(tag -> nbt.put("stationaryGraceTicks", tag));
            Adapters.DOUBLE.writeNbt(this.shockwaveRadius).ifPresent(tag -> nbt.put("shockwaveRadius", tag));
            Adapters.DOUBLE.writeNbt(this.knockbackStrength).ifPresent(tag -> nbt.put("knockbackStrength", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.effect = Adapters.EFFECT.readNbt(nbt.get("effect")).orElse(null);
        this.maxStacks = Adapters.INT.readNbt(nbt.get("maxStacks")).orElse(10);
        this.stacks = Adapters.INT.readNbt(nbt.get("stacks")).orElse(0);
        this.chargeIntervalTicks = Adapters.INT.readNbt(nbt.get("chargeIntervalTicks")).orElse(10);
        this.stationaryGraceTicks = Adapters.INT.readNbt(nbt.get("stationaryGraceTicks")).orElse(30);
        this.shockwaveRadius = Adapters.DOUBLE.readNbt(nbt.get("shockwaveRadius")).orElse(3.5D);
        this.knockbackStrength = Adapters.DOUBLE.readNbt(nbt.get("knockbackStrength")).orElse(1.5D);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            if (this.effect != null) Adapters.EFFECT.writeJson(this.effect).ifPresent(element -> json.add("effect", element));
            Adapters.INT.writeJson(this.maxStacks).ifPresent(element -> json.add("maxStacks", element));
            Adapters.INT.writeJson(this.stacks).ifPresent(element -> json.add("stacks", element));
            Adapters.INT.writeJson(this.chargeIntervalTicks).ifPresent(element -> json.add("chargeIntervalTicks", element));
            Adapters.INT.writeJson(this.stationaryGraceTicks).ifPresent(element -> json.add("stationaryGraceTicks", element));
            Adapters.DOUBLE.writeJson(this.shockwaveRadius).ifPresent(element -> json.add("shockwaveRadius", element));
            Adapters.DOUBLE.writeJson(this.knockbackStrength).ifPresent(element -> json.add("knockbackStrength", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.effect = Adapters.EFFECT.readJson(json.get("effect")).orElse(null);
        this.maxStacks = Adapters.INT.readJson(json.get("maxStacks")).orElse(10);
        this.stacks = Adapters.INT.readJson(json.get("stacks")).orElse(0);
        this.chargeIntervalTicks = Adapters.INT.readJson(json.get("chargeIntervalTicks")).orElse(10);
        this.stationaryGraceTicks = Adapters.INT.readJson(json.get("stationaryGraceTicks")).orElse(30);
        this.shockwaveRadius = Adapters.DOUBLE.readJson(json.get("shockwaveRadius")).orElse(3.5D);
        this.knockbackStrength = Adapters.DOUBLE.readJson(json.get("knockbackStrength")).orElse(1.5D);
    }


    static {
        CommonEvents.ENTITY_DAMAGE.register(MomentumEngineTalent.class, EventPriority.HIGHEST, event -> {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer player && !attacker.getLevel().isClientSide() && WoldEventHelper.isNormalAttack()) {
                if (event.getEntity() instanceof LivingEntity target && target.isAlive()) {
                    if (player.getServer() != null) {
                        ServerPlayer existing = player.getServer().getPlayerList().getPlayer(player.getUUID());
                        if (existing == player) {
                            TalentTree tree = PlayerTalentsData.get(attacker.getServer()).getTalents(player);
                            tree.getAll(MomentumEngineTalent.class, Skill::isUnlocked)
                                .forEach(talent -> talent.onMeleeHit(player, target));
                        }
                    }
                }
            }
        });
    }
}