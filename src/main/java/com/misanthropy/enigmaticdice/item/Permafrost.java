package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import com.misanthropy.enigmaticdice.util.Claims;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Permafrost extends ConfiguredSword {
    private static final int AURA_PERIOD = 5;
    private static final int AURA_REFRESH_MARGIN = 20;

    public Permafrost(Properties properties) {
        super(Config.PERMAFROST_ATTACK_DAMAGE::get, Config.PERMAFROST_ATTACK_SPEED::get, properties);
    }

    public static boolean isFrozenSolid(LivingEntity entity) {
        MobEffectInstance frost = entity.getEffect(ModEffects.FROST_HIT.get());
        return frost != null && frost.getAmplifier() + 1 >= Config.PERMAFROST_ATTACK_MAX_STACKS.get();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player && player.getAttackStrengthScale(0.5F) > 0.9F && target.isAlive()) {
            int maxStacks = Config.PERMAFROST_ATTACK_MAX_STACKS.get();
            MobEffectInstance current = target.getEffect(ModEffects.FROST_HIT.get());
            int stacks = Math.min(maxStacks, current == null ? 1 : current.getAmplifier() + 2);
            target.addEffect(uncurable(ModEffects.FROST_HIT.get(), Config.PERMAFROST_ATTACK_DEBUFF_DURATION.get(), stacks - 1), player);
            if (stacks == maxStacks) {
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.PERMAFROST_FROZEN.get(), SoundSource.PLAYERS, 1F, 1.0F);
                if (target instanceof Player frozen) frozen.stopFallFlying();
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || level.getGameTime() % AURA_PERIOD != 0 || !(entity instanceof ServerPlayer player) || player.getMainHandItem() != stack) return;
        int radius = Config.PERMAFROST_AURA_RADIUS.get();
        int duration = Config.PERMAFROST_AURA_DEBUFF_DURATION.get();
        MobEffect aura = ModEffects.FROST_AURA.get();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive() && e.isPickable())) {
            if (!Combat.canHarm(player, victim)) continue;
            MobEffectInstance current = victim.getEffect(aura);
            if (current == null || current.getDuration() <= duration - AURA_REFRESH_MARGIN) {
                victim.addEffect(uncurable(aura, duration, 0), player);
            }
        }
        freeze((ServerLevel) level, player, radius);
    }

    private static void freeze(ServerLevel level, ServerPlayer player, int radius) {
        BlockPos center = player.blockPosition();
        Map<Long, Boolean> editable = new HashMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (!pos.closerToCenterThan(center.getCenter(), radius)) continue;
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            BlockState frozen;
            if (fluid.is(Fluids.WATER)) {
                if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) continue;
                frozen = Blocks.ICE.defaultBlockState();
            } else if (fluid.is(Fluids.LAVA)) {
                frozen = Blocks.OBSIDIAN.defaultBlockState();
            } else if (fluid.is(Fluids.FLOWING_LAVA)) {
                frozen = Blocks.COBBLESTONE.defaultBlockState();
            } else {
                continue;
            }
            if (!editable.computeIfAbsent(ChunkPos.asLong(pos), key -> Claims.canEdit(player, pos))) continue;
            level.setBlockAndUpdate(pos, frozen);
        }
    }

    private static MobEffectInstance uncurable(MobEffect effect, int duration, int amplifier) {
        MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier);
        instance.setCurativeItems(new ArrayList<>());
        return instance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_2", Config.PERMAFROST_ATTACK_REDUCTION_PER_STACK.get() * 100,
                    Config.PERMAFROST_ATTACK_DEBUFF_DURATION.get() / 20).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_3", Config.PERMAFROST_ATTACK_MAX_STACKS.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_4"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_5", Config.PERMAFROST_AURA_RADIUS.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_6", Config.PERMAFROST_AURA_REDUCTION_FACTOR.get() * 100).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.permafrost_7"));
        });
    }
}
