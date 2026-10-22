package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PermanentEffects(List<Entry> effects) implements DiceAction {
    public static final MapCodec<PermanentEffects> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("effects").forGetter(PermanentEffects::effects)
    ).apply(instance, PermanentEffects::new));

    @Override
    public boolean canRun(DiceContext context) {
        return effects.stream().anyMatch(entry -> entry.canUpgrade(context.player()));
    }

    @Override
    public Result run(DiceContext context) {
        ServerPlayer player = context.player();
        List<Entry> upgradable = new ArrayList<>(effects.stream().filter(entry -> entry.canUpgrade(player)).toList());
        Entry entry = upgradable.get(context.random().nextInt(upgradable.size()));
        MobEffectInstance existing = player.getEffect(entry.effect());
        int amplifier = existing == null ? 0 : existing.getAmplifier() + 1;
        MobEffectInstance instance = new MobEffectInstance(entry.effect(), MobEffectInstance.INFINITE_DURATION, amplifier, false, false);
        instance.setCurativeItems(new ArrayList<>());
        player.addEffect(instance);
        entry.message().ifPresent(key -> player.displayClientMessage(Component.translatable(key), false));
        return Result.DONE;
    }

    public record Entry(MobEffect effect, int maxLevel, Optional<String> message) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(Entry::effect),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_level", 1).forGetter(Entry::maxLevel),
                Codec.STRING.optionalFieldOf("message").forGetter(Entry::message)
        ).apply(instance, Entry::new));

        boolean canUpgrade(ServerPlayer player) {
            MobEffectInstance existing = player.getEffect(effect);
            return existing == null || existing.getAmplifier() < maxLevel - 1;
        }
    }
}
