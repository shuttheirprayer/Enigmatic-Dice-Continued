package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.SafeTeleport;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

public record TeleportToStructure(List<ResourceLocation> structures, int radius, int offsetX, int offsetZ, float yaw) implements DiceAction {
    public static final MapCodec<TeleportToStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("structures").forGetter(TeleportToStructure::structures),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("radius", 100).forGetter(TeleportToStructure::radius),
            Codec.INT.optionalFieldOf("offset_x", 100).forGetter(TeleportToStructure::offsetX),
            Codec.INT.optionalFieldOf("offset_z", 0).forGetter(TeleportToStructure::offsetZ),
            Codec.FLOAT.optionalFieldOf("yaw", 90F).forGetter(TeleportToStructure::yaw)
    ).apply(instance, TeleportToStructure::new));

    @Override
    public boolean canRun(DiceContext context) {
        return !candidates(context.level()).isEmpty();
    }

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        List<Holder<Structure>> candidates = candidates(level);
        Holder<Structure> structure = candidates.get(context.random().nextInt(candidates.size()));
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(structure), context.player().blockPosition(), radius, false);
        if (found == null) return Result.FAILED;
        BlockPos target = found.getFirst().offset(offsetX, 0, offsetZ);
        SafeTeleport.toSurface(context.player(), level, target.getX(), target.getZ(), yaw, success -> {
            if (success) context.announce();
            else context.fallback();
        });
        return Result.PENDING;
    }

    private List<Holder<Structure>> candidates(ServerLevel level) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return structures.stream()
                .map(id -> registry.getHolder(ResourceKey.create(Registries.STRUCTURE, id)))
                .flatMap(Optional::stream)
                .<Holder<Structure>>map(holder -> holder)
                .toList();
    }
}
