package xyz.iwolfking.woldsvaults.objectives.data;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EntityType;

import java.util.Random;

public class BrutalBossesRegistry {

    public static final WeightedList<EntityType<?>> BOSS_LIST = new WeightedList<>();

    public static final WeightedList<Component> BOSS_NAME_LIST = new WeightedList<>();
    public static final WeightedList<String> BOSS_MODS_LIST = new WeightedList<>();


    public static void init() {
        addBossNames();
        addBosses();
        addBossModifiers();
    }

    public static void addBosses() {
        register(ModEntities.AGGRESSIVE_COW_BOSS, 10.0);
        register(ModEntities.BOOGIEMAN, 10.0);
        register(ModEntities.BLUE_BLAZE, 10.0);
        register(ModEntities.ROBOT, 10.0);
        register(ModEntities.MONSTER_EYE, 10.0);
        register(xyz.iwolfking.woldsvaults.init.ModEntities.WOLD, 10.0);
        register(ModEntities.ELITE_DROWNED, 20.0);
        register(ModEntities.ELITE_ZOMBIE, 20.0);
        register(ModEntities.ELITE_WITCH, 20.0);
        register(ModEntities.ELITE_HUSK, 20.0);
        register(ModEntities.ELITE_SKELETON, 20.0);
        register(ModEntities.ELITE_SPIDER, 20.0);
        register(ModEntities.ELITE_WITHER_SKELETON, 20.0);
        register(ModEntities.ELITE_STRAY, 20.0);
        register(ModEntities.VAULT_DOOD, 20.0);
    }

    public static void addBossNames() {
        register(new TextComponent("Ivan").withStyle(ChatFormatting.RED), 2.0);
        register(new TextComponent("Wold's Henchman").withStyle(ChatFormatting.LIGHT_PURPLE), 2.0);
        register(new TextComponent("Steve Irwin").withStyle(ChatFormatting.GREEN), 2.0);
        register(new TextComponent("Joseph").withStyle(ChatFormatting.GREEN), 2.0);
        register(new TextComponent("Nausicaa").withStyle(ChatFormatting.LIGHT_PURPLE), 2.0);
        register(new TextComponent("Player Hunter").withStyle(ChatFormatting.RED), 2.0);
        register(new TextComponent("Anime Hater").withStyle(ChatFormatting.RED), 2.0);
        register(new TextComponent("Steve").withStyle(ChatFormatting.YELLOW), 2.0);
        register(new TextComponent("Billy Joe").withStyle(ChatFormatting.YELLOW), 2.0);
        register(new TextComponent("Giraffe Hater").withStyle(ChatFormatting.YELLOW), 2.0);
        register(new TextComponent("Vault Filters Hater").withStyle(ChatFormatting.DARK_RED), 2.0);
        register(new TextComponent("Karen").withStyle(ChatFormatting.AQUA), 2.0);
        register(new TextComponent("Arnold").withStyle(ChatFormatting.WHITE), 2.0);
        register(new TextComponent("The Beast").withStyle(ChatFormatting.GRAY), 2.0);
        register(new TextComponent("Slayer").withStyle(ChatFormatting.GRAY), 2.0);
        register(new TextComponent("Brutalizer").withStyle(ChatFormatting.GRAY), 2.0);
        register(new TextComponent("Wold's Elite").withStyle(ChatFormatting.GRAY), 2.0);
        register(new TextComponent("Wold's Mercenary").withStyle(ChatFormatting.GRAY), 2.0);
        register(new TextComponent("Skill Issue").withStyle(ChatFormatting.GREEN), 2.0);
        register(new TextComponent("Murderous Intentions").withStyle(ChatFormatting.RED), 2.0);
        register(new TextComponent("Powerful Entity").withStyle(ChatFormatting.DARK_PURPLE), 2.0);
        register(new TextComponent("The Rock").withStyle(ChatFormatting.BLACK), 2.0);
        register(new TextComponent("Spire Slayer").withStyle(ChatFormatting.GOLD), 2.0);
        register(new TextComponent("Meance").withStyle(ChatFormatting.BLUE), 2.0);
        register(new TextComponent("Wold's Chunker").withStyle(ChatFormatting.BLUE), 2.0);
        register(new TextComponent("Chonker").withStyle(ChatFormatting.BLUE), 2.0);
        register(new TextComponent("Big Boy").withStyle(ChatFormatting.AQUA), 2.0);
        register(new TextComponent("Big Bertha").withStyle(ChatFormatting.AQUA), 2.0);
        register(new TextComponent("Large Searge").withStyle(ChatFormatting.DARK_GREEN), 2.0);
        register(new TextComponent("King").withStyle(ChatFormatting.GOLD), 2.0);
        register(new TextComponent("Ohmwrecker").withStyle(ChatFormatting.GOLD), 2.0);
        register(new TextComponent("Rager").withStyle(ChatFormatting.RED), 2.0);
        register(new TextComponent("Elite").withStyle(ChatFormatting.DARK_GRAY), 2.0);
        register(new TextComponent("Professional Vault Hunter Slayer").withStyle(ChatFormatting.DARK_GRAY), 2.0);
    }

    public static void addBossModifiers() {
        register("Bulwark", 10.0);
        register("1UP", 10.0);
        register("Alchemist", 10.0);
        register("Berserk", 10.0);
        register("Blastoff", 10.0);
        register("Choke", 10.0);
        register("Cloaking", 10.0);
        register("Darkness", 10.0);
        register("Exhaust", 10.0);
        register("Fiery", 10.0);
        register("Ghastly", 10.0);
        register("Gravity", 10.0);
        register("Poisonous", 10.0);
        register("Quicksand", 10.0);
        register("Regen", 10.0);
        register("Sapper", 10.0);
        register("Sprint", 10.0);
        register("Storm", 10.0);
        register("Vengeance", 10.0);
        register("Weakness", 10.0);
        register("Webber", 10.0);
        register("Wither", 10.0);
    }


    public static void register(EntityType<?> bossEntity, Double weight) {
        BOSS_LIST.add(bossEntity, weight);
    }
    public static void register(Component bossName, Double weight) {
        BOSS_NAME_LIST.add(bossName, weight);
    }
    public static void register(String modifierString, Double weight) {
        BOSS_MODS_LIST.add(modifierString, weight);
    }

    public static String getRandomMobModifiers() {
        return getRandomMobModifiers(6, true);
    }

    public static String getRandomMobModifiers(int count, boolean randomlyFail) {
        StringBuilder modifierList = new StringBuilder();
        Random random = new Random();
        int failCount = 0;
        int maxFails = 4;

        for(int i = 0; i < count; i++) {
            if(randomlyFail && random.nextBoolean() && failCount < maxFails) {
                failCount++;
                continue;
            }
            modifierList.append(BOSS_MODS_LIST.getRandom().get()).append(" ");
        }

        if(modifierList.isEmpty()) {
            modifierList.append("Bulwark");
        }

        return modifierList.toString().trim();
    }
}
