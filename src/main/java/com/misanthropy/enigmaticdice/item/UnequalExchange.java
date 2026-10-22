package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModDamageTypes;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UnequalExchange extends ConfiguredSword {
    public UnequalExchange(Properties properties) {
        super(Config.UNEQUAL_EXCHANGE_ATTACK_DAMAGE::get, Config.UNEQUAL_EXCHANGE_ATTACK_SPEED::get, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player && player.getAttackStrengthScale(0.5F) > 0.9F) {
            double share = Combat.isBoss(target) ? Config.UNEQUAL_EXCHANGE_BOSS_HEALTH_REDUCTION.get() : Config.UNEQUAL_EXCHANGE_TARGET_HEALTH_REDUCTION.get();
            Combat.hurtWithoutKnockback(target, ModDamageTypes.source(player.level(), ModDamageTypes.UNEQUAL_EXCHANGE, player), (float) (target.getMaxHealth() * share));
            player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.UNEQUAL_EXCHANGE_SELF, null),
                    (float) (player.getMaxHealth() * Config.UNEQUAL_EXCHANGE_PLAYER_HEALTH_REDUCTION.get()));
            if (player.isAlive()) {
                MobEffectInstance current = player.getEffect(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get());
                int amplifier = current == null ? 0 : Math.min(current.getAmplifier() + 1, 127);
                MobEffectInstance debuff = new MobEffectInstance(ModEffects.UNEQUAL_EXCHANGE_DEBUFFS.get(), Config.UNEQUAL_EXCHANGE_DEBUFF_DURATION.get() * 20, amplifier);
                debuff.setCurativeItems(new ArrayList<>());
                player.addEffect(debuff);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.UNEQUAL_EXCHANGE_HIT.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            String debuff = Tooltips.percent(Config.UNEQUAL_EXCHANGE_STAT_DEBUFF.get());
            lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_2", Tooltips.percent(Config.UNEQUAL_EXCHANGE_TARGET_HEALTH_REDUCTION.get())).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_boss", Tooltips.percent(Config.UNEQUAL_EXCHANGE_BOSS_HEALTH_REDUCTION.get())).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_3", Tooltips.percent(Config.UNEQUAL_EXCHANGE_PLAYER_HEALTH_REDUCTION.get())).withStyle(ChatFormatting.GOLD));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_4", Config.UNEQUAL_EXCHANGE_DEBUFF_DURATION.get()).withStyle(ChatFormatting.GOLD));
            for (int i = 5; i <= 9; i++) {
                lines.add(Component.translatable("tooltip.enigmaticdice.unequal_exchange_" + i, debuff).withStyle(ChatFormatting.GOLD));
            }
        }, Component.translatable("tooltip.enigmaticdice.unequal_exchange_0"));
    }
}
