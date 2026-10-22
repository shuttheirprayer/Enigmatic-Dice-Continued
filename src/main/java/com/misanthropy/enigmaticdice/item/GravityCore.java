package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class GravityCore extends Item implements ICurioItem {
    private static final UUID GRAVITY_ID = UUID.fromString("fa944f2a-d00f-48bb-a6c9-b2d0115ba628");
    private static final int VELOCITY_SAMPLES = 5;
    private static final double MIN_SLAM_SPEED = 1.8;

    private static final Map<Player, Deque<Double>> FALL_SPEEDS = new WeakHashMap<>();
    private static final Set<Player> HEAVY = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<Player> AIR_JUMP_USED = Collections.newSetFromMap(new WeakHashMap<>());

    public GravityCore(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity().level().isClientSide || !(context.entity() instanceof Player player)) return;
        AttributeInstance gravity = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity != null) gravity.removeModifier(GRAVITY_ID);
        HEAVY.remove(player);
        FALL_SPEEDS.remove(player);
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level().isClientSide || !(context.entity() instanceof Player player)) return;
        if (player.onGround()) AIR_JUMP_USED.remove(player);
        if (player.isCreative() || player.isSpectator()) return;
        AttributeInstance gravity = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity == null) return;
        boolean heavy = HEAVY.contains(player);
        if (!heavy && player.isShiftKeyDown() && !player.onGround()) {
            HEAVY.add(player);
            gravity.removeModifier(GRAVITY_ID);
            gravity.addTransientModifier(new AttributeModifier(GRAVITY_ID, "Gravity Core", Config.GRAVITY_CORE_GRAVITY_MULTIPLIER.get() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (!heavy) return;
        Deque<Double> speeds = FALL_SPEEDS.computeIfAbsent(player, key -> new ArrayDeque<>());
        speeds.addLast(player.getDeltaMovement().y);
        if (speeds.size() > VELOCITY_SAMPLES) speeds.removeFirst();
        if (!player.onGround()) return;
        HEAVY.remove(player);
        gravity.removeModifier(GRAVITY_ID);
        double fastest = speeds.stream().min(Double::compare).orElse(0.0);
        speeds.clear();
        if (fastest > -MIN_SLAM_SPEED) return;
        slam(player, Math.abs(fastest));
    }

    private static void slam(Player player, double speed) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 1F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * Config.GRAVITY_CORE_IMPACT_DAMAGE_COEFFICIENT.get() * speed);
        float radius = (float) (speed * Config.GRAVITY_CORE_IMPACT_RADIUS_COEFFICIENT.get());
        AABB area = new AABB(player.getX() - radius, player.getY() - 1.1, player.getZ() - radius, player.getX() + radius, player.getY() + 1.1, player.getZ() + radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && e.isPickable())) {
            if (!Combat.canHarm(player, victim)) continue;
            victim.hurt(player.damageSources().playerAttack(player), damage);
            victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.0, 2.0, 0.0));
        }
        if (level instanceof ServerLevel serverLevel) {
            BlockState ground = level.getBlockState(player.blockPosition().below());
            int particles = (int) (radius * 20);
            for (int i = 0; i < particles; i++) {
                float angle = (float) (i / (double) particles * 360.0);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                        player.getX() + radius * Mth.sin(angle), player.getY() + 0.15, player.getZ() + radius * Mth.cos(angle), 1, 0, 0, 0, 0);
            }
        }
    }

    public static void airJump(ServerPlayer player) {
        if (!player.isAlive() || player.onGround() || player.isCreative() || player.isSpectator()) return;
        if (!Combat.isWearing(player, ModItems.GRAVITY_CORE.get()) || player.getCooldowns().isOnCooldown(ModItems.GRAVITY_CORE.get())) return;
        if (!AIR_JUMP_USED.add(player)) return;
        double strength = Config.GRAVITY_CORE_JUMP_STRENGTH.get();
        MobEffectInstance jumpBoost = player.getEffect(MobEffects.JUMP);
        double boost = jumpBoost != null ? 0.1 * (jumpBoost.getAmplifier() + 1) : 0;
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, (0.42 + boost) * strength, motion.z);
        if (player.isSprinting()) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            player.setDeltaMovement(player.getDeltaMovement().add(-Mth.sin(yaw) * 0.2 * strength, 0, Mth.cos(yaw) * 0.2 * strength));
        }
        player.hasImpulse = true;
        player.awardStat(Stats.JUMP);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        int cooldown = Config.GRAVITY_CORE_COOLDOWN.get();
        if (cooldown > 0) player.getCooldowns().addCooldown(ModItems.GRAVITY_CORE.get(), cooldown);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.gravity_core_1"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.gravity_core_2", (int) (double) Config.GRAVITY_CORE_GRAVITY_MULTIPLIER.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.gravity_core_3"));
            lines.add(Component.translatable("tooltip.enigmaticdice.gravity_core_4"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.gravity_core_5"));
        });
    }
}
