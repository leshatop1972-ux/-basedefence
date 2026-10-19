package com.lesha_top1972.basedefense.effect;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;

/** Пока висит эффект «Обезоружен», игрок не может ударить и не может стрелять/бросать. */
public class DisarmHandler {

    public static void register() {
        // любой удар по сущности (меч, топор, кулак, булава...)
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (player.hasEffect(ModEffects.DISARMED)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // луки, арбалеты, трезубец и всё, что бросается
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player.hasEffect(ModEffects.DISARMED) && isRangedWeapon(player.getItemInHand(hand))) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ProjectileWeaponItem
                || item instanceof TridentItem
                || item instanceof ThrowablePotionItem
                || item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof EnderpearlItem
                || item instanceof WindChargeItem;
    }
}
