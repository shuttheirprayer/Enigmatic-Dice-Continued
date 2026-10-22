package com.misanthropy.enigmaticdice.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.Tags;
import top.theillusivec4.curios.api.CuriosApi;

public final class Combat {
    private Combat() {
    }

    public static boolean canHarm(LivingEntity attacker, LivingEntity target) {
        if (isAllied(attacker, target)) return false;
        if (target instanceof OwnableEntity ownable) {
            LivingEntity owner = ownable.getOwner();
            if (owner != null && (owner == attacker || isAllied(attacker, owner))) return false;
        }
        return true;
    }

    private static boolean isAllied(LivingEntity attacker, LivingEntity other) {
        Team team = attacker.getTeam();
        return team != null && team == other.getTeam() && !team.isAllowFriendlyFire();
    }

    public static boolean isBoss(Entity entity) {
        return entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    public static boolean isWearing(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> handler.isEquipped(item)).orElse(false);
    }

    public static void hurtWithoutKnockback(LivingEntity target, DamageSource source, float amount) {
        Vec3 motion = target.getDeltaMovement();
        target.hurt(source, amount);
        target.setDeltaMovement(motion);
    }
}
