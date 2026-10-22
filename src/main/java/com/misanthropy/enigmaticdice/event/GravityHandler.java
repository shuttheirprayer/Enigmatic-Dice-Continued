package com.misanthropy.enigmaticdice.event;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

final class GravityHandler {
    private static final UUID MOON_ID = UUID.fromString("1fa5c3f2-4e37-44e2-a618-d747870fbd05");
    private static final UUID SHARD_ID = UUID.fromString("205dfaf9-1a96-4005-9a30-5d736a87c9b4");
    private static final Set<Player> CLIENT_SUPPRESSED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<Player> SERVER_SUPPRESSED = Collections.newSetFromMap(new WeakHashMap<>());

    private GravityHandler() {
    }

    static void tick(Player player) {
        AttributeInstance gravity = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity == null) return;
        boolean sneaking = player.isShiftKeyDown();
        boolean moonHeld = isHeld(player, ModItems.MOON.get());
        boolean shard = !moonHeld && hasInHotbar(player, ModItems.MOON_SHARD.get());
        boolean shardActive = false;
        if (shard && Combat.isWearing(player, ModItems.GRAVITY_CORE.get())) {
            Set<Player> suppressed = player.level().isClientSide ? CLIENT_SUPPRESSED : SERVER_SUPPRESSED;
            if (player.onGround()) suppressed.remove(player);
            else if (sneaking) suppressed.add(player);
            shardActive = !suppressed.contains(player);
        } else if (shard) {
            shardActive = !sneaking;
        }
        apply(gravity, MOON_ID, "Moon gravity reduction", moonHeld && !sneaking, Config.MOON_GRAVITY_REDUCTION.get());
        apply(gravity, SHARD_ID, "MoonShard gravity reduction", shardActive, Config.MOON_SHARD_GRAVITY_REDUCTION.get());
    }

    static boolean negatesFall(Player player) {
        return hasInHotbar(player, ModItems.MOON_SHARD.get()) || hasInHotbar(player, ModItems.MOON.get());
    }

    private static boolean isHeld(Player player, Item item) {
        return player.getMainHandItem().is(item) || player.getOffhandItem().is(item);
    }

    private static boolean hasInHotbar(Player player, Item item) {
        if (player.getOffhandItem().is(item)) return true;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(item)) return true;
        }
        return false;
    }

    private static void apply(AttributeInstance gravity, UUID id, String name, boolean active, double amount) {
        AttributeModifier existing = gravity.getModifier(id);
        if (!active) {
            if (existing != null) gravity.removeModifier(id);
            return;
        }
        if (existing != null && existing.getAmount() == amount) return;
        if (existing != null) gravity.removeModifier(id);
        gravity.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }
}
