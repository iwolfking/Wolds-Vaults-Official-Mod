package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.entities.barnyard.HostileChickenEntity;
import xyz.iwolfking.woldsvaults.entities.barnyard.HostilePigEntity;
import xyz.iwolfking.woldsvaults.entities.barnyard.HostileSheepEntity;
import xyz.iwolfking.woldsvaults.entities.ghosts.*;
import xyz.iwolfking.woldsvaults.entities.thanksgiving.CranberrySlimeEntity;
import xyz.iwolfking.woldsvaults.entities.thanksgiving.HostileTurkeyEntity;
import xyz.iwolfking.woldsvaults.items.gear.rang.VaultRangEntity;
import xyz.iwolfking.woldsvaults.objectives.data.bosses.WoldBoss;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModEntities {
    private static final Map<EntityType<? extends LivingEntity>, Supplier<AttributeSupplier.Builder>> ATTRIBUTE_BUILDERS = new HashMap<>();
    public static EntityType<WoldBoss> WOLD;
    public static EntityType<GreenGhost> GREEN_GHOST;
    public static EntityType<BlueGhost> BLUE_GHOST;
    public static EntityType<BlackGhost> BLACK_GHOST;
    public static EntityType<BrownGhost> BROWN_GHOST;
    public static EntityType<DarkBlueGhost> DARK_BLUE_GHOST;
    public static EntityType<DarkRedGhost> DARK_RED_GHOST;
    public static EntityType<PurpleGhost> PURPLE_GHOST;
    public static EntityType<RedGhost> RED_GHOST;
    public static EntityType<YellowGhost> YELLOW_GHOST;
    public static EntityType<DarkGrayGhost> DARK_GRAY_GHOST;
    public static EntityType<VaultRangEntity> VAULT_RANG;
    public static EntityType<HostileTurkeyEntity> HOSTILE_TURKEY;
    public static EntityType<CranberrySlimeEntity> CRANBERRY_SLIME;
    public static EntityType<HostileChickenEntity> HOSTILE_CHICKEN;
    public static EntityType<HostileSheepEntity> HOSTILE_SHEEP;
    public static EntityType<HostilePigEntity> HOSTILE_PIG;

    public static void register(RegistryEvent.Register<EntityType<?>> event) {
        WOLD = registerLiving("wold", EntityType.Builder.of(WoldBoss::new, MobCategory.MONSTER)
        .sized(1.2F, 3.9F), Zombie::createAttributes, event);
        GREEN_GHOST = registerLivingWV("green_ghost", EntityType.Builder.of(GreenGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new GreenGhost(GREEN_GHOST, level))), GreenGhost::createAttributes, event);
        BLUE_GHOST = registerLivingWV("blue_ghost", EntityType.Builder.of(BlueGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new BlueGhost(BLUE_GHOST, level))), BlueGhost::createAttributes, event);
        BLACK_GHOST = registerLivingWV("black_ghost", EntityType.Builder.of(BlackGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new BlackGhost(BLACK_GHOST, level))), BlackGhost::createAttributes, event);
        BROWN_GHOST = registerLivingWV("brown_ghost", EntityType.Builder.of(BrownGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new BrownGhost(BROWN_GHOST, level))), BrownGhost::createAttributes, event);
        DARK_BLUE_GHOST = registerLivingWV("dark_blue_ghost", EntityType.Builder.of(DarkBlueGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new DarkBlueGhost(DARK_BLUE_GHOST, level))), DarkBlueGhost::createAttributes, event);
        DARK_RED_GHOST = registerLivingWV("dark_red_ghost", EntityType.Builder.of(DarkRedGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new DarkRedGhost(DARK_RED_GHOST, level))), DarkRedGhost::createAttributes, event);
        PURPLE_GHOST = registerLivingWV("purple_ghost", EntityType.Builder.of(PurpleGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new PurpleGhost(PURPLE_GHOST, level))), PurpleGhost::createAttributes, event);
        RED_GHOST = registerLivingWV("red_ghost", EntityType.Builder.of(RedGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new RedGhost(RED_GHOST, level))), RedGhost::createAttributes, event);
        YELLOW_GHOST = registerLivingWV("yellow_ghost", EntityType.Builder.of(YellowGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new YellowGhost(YELLOW_GHOST, level))), YellowGhost::createAttributes, event);
        DARK_GRAY_GHOST = registerLivingWV("dark_gray_ghost", EntityType.Builder.of(DarkGrayGhost::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new DarkGrayGhost(DARK_GRAY_GHOST, level))), DarkGrayGhost::createAttributes, event);
        HOSTILE_TURKEY = registerLivingWV("hostile_turkey", EntityType.Builder.of(HostileTurkeyEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new HostileTurkeyEntity(HOSTILE_TURKEY, level))), HostileTurkeyEntity::createAttributes, event);
        CRANBERRY_SLIME = registerLivingWV("cranberry_slime", EntityType.Builder.of(CranberrySlimeEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new CranberrySlimeEntity(CRANBERRY_SLIME, level))), CranberrySlimeEntity::createAttributes, event);
        HOSTILE_CHICKEN = registerLivingWV("hostile_chicken", EntityType.Builder.of(HostileChickenEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new HostileChickenEntity(HOSTILE_CHICKEN, level))), HostileChickenEntity::createAttributes, event);
        HOSTILE_SHEEP = registerLivingWV("hostile_sheep", EntityType.Builder.of(HostileSheepEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new HostileSheepEntity(HOSTILE_SHEEP, level))), HostileSheepEntity::createAttributes, event);
        HOSTILE_PIG = registerLivingWV("hostile_pig", EntityType.Builder.of(HostilePigEntity::new, MobCategory.MONSTER).sized(1.0F, 1.0F).clientTrackingRange(8).fireImmune().setCustomClientFactory(((spawnEntity, level) -> new HostilePigEntity(HOSTILE_PIG, level))), HostilePigEntity::createAttributes, event);
        VAULT_RANG = registerWV("rang", EntityType.Builder.<VaultRangEntity>of(VaultRangEntity::new, MobCategory.MISC)
                .sized(0.4F, 0.4F)
                .clientTrackingRange(4)
                .updateInterval(10) // update interval
                .setCustomClientFactory((spawnEntity, world) -> new VaultRangEntity(VAULT_RANG, world)), event);
    }


    private static <T extends LivingEntity> EntityType<T> registerLiving(String name, EntityType.Builder<T> builder, Supplier<AttributeSupplier.Builder> attributes, RegistryEvent.Register<EntityType<?>> event) {
        EntityType<T> entityType = register(name, builder, event);
        if (attributes != null) {
            ATTRIBUTE_BUILDERS.put(entityType, attributes);
        }

        return entityType;
    }

    private static <T extends LivingEntity> EntityType<T> registerLivingWV(String name, EntityType.Builder<T> builder, Supplier<AttributeSupplier.Builder> attributes, RegistryEvent.Register<EntityType<?>> event) {
        EntityType<T> entityType = registerWV(name, builder, event);
        if (attributes != null) {
            ATTRIBUTE_BUILDERS.put(entityType, attributes);
        }

        return entityType;
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder, RegistryEvent.Register<EntityType<?>> event) {
        EntityType<T> entityType = builder.build(VaultMod.sId(name));
        event.getRegistry().register(entityType.setRegistryName(VaultMod.id(name)));
        return entityType;
    }

    private static <T extends Entity> EntityType<T> registerWV(String name, EntityType.Builder<T> builder, RegistryEvent.Register<EntityType<?>> event) {
        EntityType<T> entityType = builder.build(WoldsVaults.sId(name));
        event.getRegistry().register(entityType.setRegistryName(WoldsVaults.id(name)));
        return entityType;
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        ATTRIBUTE_BUILDERS.forEach((e, b) -> event.put(e, b.get().build()));
        ATTRIBUTE_BUILDERS.clear();
    }
}
