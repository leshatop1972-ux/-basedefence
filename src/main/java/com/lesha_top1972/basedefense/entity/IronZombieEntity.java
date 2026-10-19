package com.lesha_top1972.basedefense.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * Железный зомби: поведение как у обычного зомби,
 * но больше здоровья, сильнее удар и чуть медленнее.
 */
public class IronZombieEntity extends Zombie {

    public IronZombieEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createIronZombieAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)      // у зомби 20 (+5)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)    // у зомби 3 (+2 = +1 сердце)
                .add(Attributes.MOVEMENT_SPEED, 0.20D); // у зомби 0.23
    }
}
