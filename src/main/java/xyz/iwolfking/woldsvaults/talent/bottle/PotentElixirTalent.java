package xyz.iwolfking.woldsvaults.talent.bottle;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.item.bottle.*;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.events.vault.UsedVaultBottleEvent;
import xyz.iwolfking.woldsvaults.events.vault.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.*;

import java.util.Optional;

public class PotentElixirTalent extends LearnableSkill {
    private float additionalAbsorptionHearts;
    private float additionalCooldownReduction;
    private int additionalAbilityLevels;
    private int additionalFlatMana;
    private float additionalPercentMana;
    private int additionalDuration;
    private int additionalAmplifier;

    public PotentElixirTalent(int unlockLevel, int learnPointCost, int regretPointCost, float additionalAbsorptionHearts, float additionalCooldownReduction, int additionalAbilityLevels, int additionalFlatMana, float additionalPercentMana, int additionalDuration, int additionalAmplifier) {
        super(unlockLevel, learnPointCost, regretPointCost);
        this.additionalAbsorptionHearts = additionalAbsorptionHearts;
        this.additionalCooldownReduction = additionalCooldownReduction;
        this.additionalAbilityLevels = additionalAbilityLevels;
        this.additionalFlatMana = additionalFlatMana;
        this.additionalPercentMana = additionalPercentMana;
        this.additionalDuration = additionalDuration;
        this.additionalAmplifier = additionalAmplifier;
    }

    public PotentElixirTalent() {
    }

    public float getAdditionalAbsorptionHearts() {
        return this.additionalAbsorptionHearts;
    }

    public float getAdditionalCooldownReduction() {
        return this.additionalCooldownReduction;
    }

    public int getAdditionalAbilityLevels() {
        return this.additionalAbilityLevels;
    }

    public int getAdditionalFlatMana() {
        return this.additionalFlatMana;
    }

    public float getAdditionalPercentMana() {
        return this.additionalPercentMana;
    }

    public int getAdditionalDuration() {
        return this.additionalDuration;
    }

    public int getAdditionalAmplifier() {
        return this.additionalAmplifier;
    }

    private static void applyBonus(UsedVaultBottleEvent.Data data) {
        if(data.isCancelled()) {
            return;
        }

        ServerPlayer player = data.getDrinker();
        if (player.getServer() != null) {
            TalentTree tree = PlayerTalentsData.get(player.getServer()).getTalents(player);

            for (PotentElixirTalent talent : tree.getAll(PotentElixirTalent.class, Skill::isUnlocked)) {
                if (data.getEffect().isPresent()) {
                    BottleEffect effect = data.getEffect().get();
                    if (effect instanceof AbsorptionBottleEffect absorptionBottleEffect) {
                        data.setEffect(new AbsorptionBottleEffect(absorptionBottleEffect.getEffectId(), ((AbsorptionBottleEffectAccessor) absorptionBottleEffect).getAmount() + talent.getAdditionalAbsorptionHearts()));
                    } else if (effect instanceof CastAbilityBottleEffect castAbilityBottleEffect) {
                        String abilityId = ((CastAbilityBottleEffectAccessor) castAbilityBottleEffect).getAbilityId();
                        data.setEffect(new CastAbilityBottleEffect(castAbilityBottleEffect.getEffectId(), incrementAbilityId(abilityId, talent.getAdditionalAbilityLevels())));
                    } else if (effect instanceof CooldownReductionBottleEffect cooldownReductionBottleEffect) {
                        data.setEffect(new CooldownReductionBottleEffect(cooldownReductionBottleEffect.getEffectId(), ((CooldownReductionBottleEffectAccessor) cooldownReductionBottleEffect).getAmount() + talent.getAdditionalCooldownReduction()));
                    } else if (effect instanceof ManaFlatBottleEffect manaFlatBottleEffect) {
                        data.setEffect(new ManaFlatBottleEffect(manaFlatBottleEffect.getEffectId(), ((ManaFlatBottleEffectAccessor) manaFlatBottleEffect).getAmount() + talent.getAdditionalFlatMana()));
                    } else if (effect instanceof ManaPercentBottleEffect manaPercentBottleEffect) {
                        data.setEffect(new ManaPercentBottleEffect(manaPercentBottleEffect.getEffectId(), ((ManaPercentBottleEffectAccessor) manaPercentBottleEffect).getAmount() + talent.getAdditionalPercentMana()));
                    } else if (effect instanceof PotionBottleEffect potionBottleEffect) {
                        data.setEffect(new PotionBottleEffect(potionBottleEffect.getEffectId(), ((PotionBottleEffectAccessor) potionBottleEffect).getPotion(), ((PotionBottleEffectAccessor) potionBottleEffect).getDuration() + talent.getAdditionalDuration(), ((PotionBottleEffectAccessor) potionBottleEffect).getAmplifier() + talent.getAdditionalAmplifier()));
                    }
                }
            }
        }
    }

    static {
        WoldCommonEvents.VAULT_BOTTLE_DRINK.register(PotentElixirTalent.class, PotentElixirTalent::applyBonus);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.additionalAbsorptionHearts, buffer);
        Adapters.FLOAT.writeBits(this.additionalCooldownReduction, buffer);
        Adapters.INT.writeBits(this.additionalAbilityLevels, buffer);
        Adapters.INT.writeBits(this.additionalFlatMana, buffer);
        Adapters.FLOAT.writeBits(this.additionalPercentMana, buffer);
        Adapters.INT.writeBits(this.additionalDuration, buffer);
        Adapters.INT.writeBits(this.additionalAmplifier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.additionalAbsorptionHearts = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.additionalCooldownReduction = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.additionalAbilityLevels = Adapters.INT.readBits(buffer).orElse(0);
        this.additionalFlatMana = Adapters.INT.readBits(buffer).orElse(0);
        this.additionalPercentMana = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.additionalDuration = Adapters.INT.readBits(buffer).orElse(0);
        this.additionalAmplifier = Adapters.INT.readBits(buffer).orElse(0);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.additionalAbsorptionHearts).ifPresent(tag -> nbt.put("additionalAbsorptionHearts", tag));
            Adapters.FLOAT.writeNbt(this.additionalCooldownReduction).ifPresent(tag -> nbt.put("additionalCooldownReduction", tag));
            Adapters.INT.writeNbt(this.additionalAbilityLevels).ifPresent(tag -> nbt.put("additionalAbilityLevels", tag));
            Adapters.INT.writeNbt(this.additionalFlatMana).ifPresent(tag -> nbt.put("additionalFlatMana", tag));
            Adapters.FLOAT.writeNbt(this.additionalPercentMana).ifPresent(tag -> nbt.put("additionalPercentMana", tag));
            Adapters.INT.writeNbt(this.additionalDuration).ifPresent(tag -> nbt.put("additionalDuration", tag));
            Adapters.INT.writeNbt(this.additionalAmplifier).ifPresent(tag -> nbt.put("additionalAmplifier", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.additionalAbsorptionHearts = Adapters.FLOAT.readNbt(nbt.get("additionalAbsorptionHearts")).orElse(0.0F);
        this.additionalCooldownReduction = Adapters.FLOAT.readNbt(nbt.get("additionalCooldownReduction")).orElse(0.0F);
        this.additionalAbilityLevels = Adapters.INT.readNbt(nbt.get("additionalAbilityLevels")).orElse(0);
        this.additionalFlatMana = Adapters.INT.readNbt(nbt.get("additionalFlatMana")).orElse(0);
        this.additionalPercentMana = Adapters.FLOAT.readNbt(nbt.get("additionalPercentMana")).orElse(0.0F);
        this.additionalDuration = Adapters.INT.readNbt(nbt.get("additionalDuration")).orElse(0);
        this.additionalAmplifier = Adapters.INT.readNbt(nbt.get("additionalAmplifier")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.additionalAbsorptionHearts).ifPresent(element -> json.add("additionalAbsorptionHearts", element));
            Adapters.FLOAT.writeJson(this.additionalCooldownReduction).ifPresent(element -> json.add("additionalCooldownReduction", element));
            Adapters.INT.writeJson(this.additionalAbilityLevels).ifPresent(element -> json.add("additionalAbilityLevels", element));
            Adapters.INT.writeJson(this.additionalFlatMana).ifPresent(element -> json.add("additionalFlatMana", element));
            Adapters.FLOAT.writeJson(this.additionalPercentMana).ifPresent(element -> json.add("additionalPercentMana", element));
            Adapters.INT.writeJson(this.additionalDuration).ifPresent(element -> json.add("additionalDuration", element));
            Adapters.INT.writeJson(this.additionalAmplifier).ifPresent(element -> json.add("additionalAmplifier", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.additionalAbsorptionHearts = Adapters.FLOAT.readJson(json.get("additionalAbsorptionHearts")).orElse(0.0F);
        this.additionalCooldownReduction = Adapters.FLOAT.readJson(json.get("additionalCooldownReduction")).orElse(0.0F);
        this.additionalAbilityLevels = Adapters.INT.readJson(json.get("additionalAbilityLevels")).orElse(0);
        this.additionalFlatMana = Adapters.INT.readJson(json.get("additionalFlatMana")).orElse(0);
        this.additionalPercentMana = Adapters.FLOAT.readJson(json.get("additionalPercentMana")).orElse(0.0F);
        this.additionalDuration = Adapters.INT.readJson(json.get("additionalDuration")).orElse(0);
        this.additionalAmplifier = Adapters.INT.readJson(json.get("additionalAmplifier")).orElse(0);
    }

    public static String incrementAbilityId(String abilityId, int n) {
        if (abilityId == null || !abilityId.contains("_")) {
            return abilityId;
        }

        try {
            int lastUnderscoreIndex = abilityId.lastIndexOf('_');
            String prefix = abilityId.substring(0, lastUnderscoreIndex + 1);
            int currentNum = Integer.parseInt(abilityId.substring(lastUnderscoreIndex + 1));
            int newNum = Math.min(30, currentNum + n);
            return prefix + newNum;
        } catch (NumberFormatException e) {
            return abilityId;
        }
    }
}