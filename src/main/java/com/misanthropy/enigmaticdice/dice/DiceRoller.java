package com.misanthropy.enigmaticdice.dice;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.util.Claims;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DiceRoller {
    private DiceRoller() {
    }

    public static boolean roll(ServerPlayer player) {
        return roll(player, new HashSet<>());
    }

    static boolean roll(ServerPlayer player, Set<ResourceLocation> tried) {
        float luck = player.getLuck();
        List<DiceEvent> pool = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double total = 0;
        for (DiceEvent event : DiceEvents.all()) {
            if (tried.contains(event.id())) continue;
            double weight = event.settings().chance(luck);
            if (weight <= 0) continue;
            pool.add(event);
            weights.add(weight);
            total += weight;
        }
        while (!pool.isEmpty()) {
            double target = player.getRandom().nextDouble() * total;
            int index = 0;
            while (index < pool.size() - 1 && (target -= weights.get(index)) >= 0) index++;
            DiceEvent event = pool.remove(index);
            total -= weights.remove(index);
            tried.add(event.id());
            if (attempt(player, event, tried)) return true;
        }
        return false;
    }

    public static boolean trigger(ServerPlayer player, DiceEvent event) {
        Set<ResourceLocation> tried = new HashSet<>();
        tried.add(event.id());
        return attempt(player, event, tried);
    }

    private static boolean attempt(ServerPlayer player, DiceEvent event, Set<ResourceLocation> tried) {
        DiceContext context = new DiceContext(player, event, tried);
        try {
            int claimRadius = event.settings().claimRadius();
            if (claimRadius > 0 && Claims.isAnyClaimed(player.serverLevel(), player.blockPosition(), claimRadius)) return false;
            if (!event.action().canRun(context)) return false;
            DiceAction.Result result = event.action().run(context);
            if (result == DiceAction.Result.DONE) context.announce();
            return result != DiceAction.Result.FAILED;
        } catch (RuntimeException exception) {
            EnigmaticDice.LOGGER.error("Dice event {} failed", event.id(), exception);
            return false;
        }
    }
}
