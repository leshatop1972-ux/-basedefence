package com.lesha_top1972.basedefense;

import com.lesha_top1972.basedefense.entity.Brute1Entity;
import com.lesha_top1972.basedefense.entity.Brute2Entity;
import com.lesha_top1972.basedefense.entity.IronZombieEntity;
import com.lesha_top1972.basedefense.entity.LeaperEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<IronZombieEntity> IRON_ZOMBIE =
            registerMob("iron_zombie", IronZombieEntity::new, 0.6F, 1.95F, 1.74F);
    // громилы: базовые размеры как у зомби, увеличиваются атрибутом scale
    public static final EntityType<Brute1Entity> BRUTE_1 =
            registerMob("brute_1", Brute1Entity::new, 0.6F, 1.95F, 1.74F);
    public static final EntityType<Brute2Entity> BRUTE_2 =
            registerMob("brute_2", Brute2Entity::new, 0.6F, 1.95F, 1.74F);
    // прыгун: базовые 0.6 x 1.5, атрибут scale 1.2 даёт 0.72 x 1.8 (рост игрока)
    public static final EntityType<LeaperEntity> LEAPER =
            registerMob("leaper", LeaperEntity::new, 0.6F, 1.5F, 1.0F);

    private static <T extends Entity> EntityType<T> registerMob(
            String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(BaseDefenseInit.MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(factory, MobCategory.MONSTER)
                        .sized(width, height)
                        .eyeHeight(eyeHeight)
                        .passengerAttachments(height + 0.075F)
                        .ridingOffset(-0.7F)
                        .clientTrackingRange(10)
                        .build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(IRON_ZOMBIE, IronZombieEntity.createIronZombieAttributes());
        FabricDefaultAttributeRegistry.register(BRUTE_1, Brute1Entity.createBrute1Attributes());
        FabricDefaultAttributeRegistry.register(BRUTE_2, Brute2Entity.createBrute2Attributes());
        FabricDefaultAttributeRegistry.register(LEAPER, LeaperEntity.createLeaperAttributes());
    }
}
