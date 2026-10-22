package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.SafeTeleport;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public record TeleportToBiome(List<ResourceLocation> biomes, int radius, int horizontalStep, int verticalStep) implements DiceAction {
    public static final MapCodec<TeleportToBiome> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("biomes").forGetter(TeleportToBiome::biomes),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("radius", 4480).forGetter(TeleportToBiome::radius),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("horizontal_step", 64).forGetter(TeleportToBiome::horizontalStep),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("vertical_step", 128).forGetter(TeleportToBiome::verticalStep)
    ).apply(instance, TeleportToBiome::new));

    @Override
    public boolean canRun(DiceContext context) {
        return !candidates(context.level()).isEmpty();
    }

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        List<ResourceKey<Biome>> candidates = candidates(level);
        ResourceKey<Biome> biome = candidates.get(context.random().nextInt(candidates.size()));
        BlockPos origin = context.player().blockPosition();
        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        CompletableFuture.supplyAsync(() -> source.findClosestBiome3d(origin, radius, horizontalStep, verticalStep, holder -> holder.is(biome), sampler, level), Util.backgroundExecutor())
                .whenCompleteAsync((found, error) -> {
                    if (error != null) EnigmaticDice.LOGGER.error("Biome search for {} failed", biome.location(), error);
                    ServerPlayer player = context.onlinePlayer();
                    if (player == null) return;
                    if (found == null || player.serverLevel() != level) {
                        context.fallback();
                        return;
                    }
                    BlockPos pos = found.getFirst();
                    SafeTeleport.toSurface(player, level, pos.getX(), pos.getZ(), player.getYRot(), success -> {
                        if (success) context.announce();
                        else context.fallback();
                    });
                }, context.server());
        return Result.PENDING;
    }

    private List<ResourceKey<Biome>> candidates(ServerLevel level) {
        Set<Holder<Biome>> possible = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
        return biomes.stream()
                .map(id -> ResourceKey.create(Registries.BIOME, id))
                .filter(key -> possible.stream().anyMatch(holder -> holder.is(key)))
                .toList();
    }
}
