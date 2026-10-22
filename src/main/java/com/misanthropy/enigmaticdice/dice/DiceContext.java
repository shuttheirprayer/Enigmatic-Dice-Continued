package com.misanthropy.enigmaticdice.dice;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public final class DiceContext {
    private final ServerPlayer player;
    private final DiceEvent event;
    private final Set<ResourceLocation> tried;

    DiceContext(ServerPlayer player, DiceEvent event, Set<ResourceLocation> tried) {
        this.player = player;
        this.event = event;
        this.tried = tried;
    }

    public ServerPlayer player() {
        return player;
    }

    public ServerLevel level() {
        return player.serverLevel();
    }

    public RandomSource random() {
        return player.getRandom();
    }

    public MinecraftServer server() {
        return player.server;
    }

    public UUID playerId() {
        return player.getUUID();
    }

    @Nullable
    public ServerPlayer onlinePlayer() {
        ServerPlayer online = server().getPlayerList().getPlayer(player.getUUID());
        return online != null && online.isAlive() ? online : null;
    }

    public void announce() {
        ServerPlayer target = onlinePlayer();
        if (target == null) return;
        DiceSettings settings = event.settings();
        settings.message().ifPresent(key -> {
            String full = settings.messageVariants() > 0 ? key + "." + target.getRandom().nextInt(settings.messageVariants()) : key;
            target.displayClientMessage(Component.translatable(full), false);
        });
    }

    public void fallback() {
        ServerPlayer target = onlinePlayer();
        if (target != null) DiceRoller.roll(target, tried);
    }
}
