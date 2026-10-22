package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulEater extends ConfiguredSword {
    public static final UUID HEALTH_BOOST_ID = UUID.fromString("1c378b22-ac27-406d-a123-9fa54753f35b");
    private static final String SPENT_HEALTH = "soul_eater_spent_health_sum";
    private static final String CHARGED_AT = "enigmaticdice_soul_eater_charged_at";
    private static final int USE_DURATION = 100;
    private static final int SACRIFICE_INTERVAL = 10;
    private static final int COOLDOWN = 40;

    public SoulEater(Properties properties) {
        super(Config.SOUL_EATER_ATTACK_DAMAGE::get, Config.SOUL_EATER_ATTACK_SPEED::get, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            releaseCharge(player, target);
            if (target.getHealth() <= 0F) onKill(player, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private static void releaseCharge(Player player, LivingEntity target) {
        CompoundTag data = player.getPersistentData();
        float spent = data.getFloat(SPENT_HEALTH);
        long elapsed = player.level().getGameTime() - data.getLong(CHARGED_AT);
        if (spent <= 0F || elapsed >= 20L * Config.SOUL_EATER_CHARGE_DURATION.get()) return;
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().indirectMagic(player, player), spent * Config.SOUL_EATER_CHARGED_ATTACK_DAMAGE_PER_HP.get().floatValue());
        data.putFloat(SPENT_HEALTH, 0F);
        player.removeEffect(ModEffects.SOUL_EATER_CHARGED_HIT.get());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SOUL_EATER_CHARGED_HIT.get(), SoundSource.PLAYERS, 1F, 1.0F);
        if (target.getHealth() <= 0F) player.heal(spent / 2);
    }

    private static void onKill(Player player, LivingEntity target) {
        player.heal(target.getMaxHealth() * Config.SOUL_EATER_HEAL_PERCENT_ON_KILL.get().floatValue());
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        AttributeModifier existing = maxHealth.getModifier(HEALTH_BOOST_ID);
        double previous = existing != null ? existing.getAmount() : 0;
        if (existing != null) maxHealth.removeModifier(HEALTH_BOOST_ID);
        double limit = maxHealth.getValue() * Config.SOUL_EATER_MAX_HEALTH_MULTIPLIER_LIMIT.get();
        double bonus = Math.min(previous + target.getMaxHealth() * Config.SOUL_EATER_MAX_HEALTH_STEAL_PERCENT.get(), limit);
        maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_BOOST_ID, "SoulEaterBonusHealth", bonus, AttributeModifier.Operation.ADDITION));
        MobEffectInstance indicator = new MobEffectInstance(ModEffects.SOUL_EATER_HEALTH_BOOST.get(), 20 * Config.SOUL_EATER_MAX_HEALTH_BUFF_DURATION.get(), 0);
        indicator.setCurativeItems(new ArrayList<>());
        player.addEffect(indicator);
    }

    public static void expireHealthBoost(Player player) {
        if (player.hasEffect(ModEffects.SOUL_EATER_HEALTH_BOOST.get())) return;
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null || maxHealth.getModifier(HEALTH_BOOST_ID) == null) return;
        maxHealth.removeModifier(HEALTH_BOOST_ID);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.getPersistentData().putFloat(SPENT_HEALTH, 0F);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level.isClientSide || remaining % SACRIFICE_INTERVAL != 0) return;
        float newHealth = Math.max(user.getHealth() - user.getMaxHealth() * 0.2F, 1.0F);
        float spentNow = user.getHealth() - newHealth;
        user.setHealth(newHealth);
        CompoundTag data = user.getPersistentData();
        float spent = data.getFloat(SPENT_HEALTH) + spentNow;
        data.putFloat(SPENT_HEALTH, spent);
        data.putLong(CHARGED_AT, level.getGameTime());
        user.removeEffect(ModEffects.SOUL_EATER_CHARGED_HIT.get());
        if (spent > 0F) {
            MobEffectInstance charge = new MobEffectInstance(ModEffects.SOUL_EATER_CHARGED_HIT.get(), 20 * Config.SOUL_EATER_CHARGE_DURATION.get(), Math.min((int) (spent - 1), 127));
            charge.setCurativeItems(new ArrayList<>());
            user.addEffect(charge);
        }
        if (newHealth <= 1.0F) user.stopUsingItem();
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof Player player) player.getCooldowns().addCooldown(this, COOLDOWN);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_2"));
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_3", Config.SOUL_EATER_CHARGED_ATTACK_DAMAGE_PER_HP.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_4"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_5"));
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_6", Tooltips.percent(Config.SOUL_EATER_HEAL_PERCENT_ON_KILL.get())).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.soul_eater_7", Tooltips.percent(Config.SOUL_EATER_MAX_HEALTH_STEAL_PERCENT.get()),
                    Config.SOUL_EATER_MAX_HEALTH_BUFF_DURATION.get() / 60D, Tooltips.percent(Config.SOUL_EATER_MAX_HEALTH_MULTIPLIER_LIMIT.get())).withStyle(ChatFormatting.GOLD));
        }, Component.translatable("tooltip.enigmaticdice.soul_eater_0"));
    }
}
