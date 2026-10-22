package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class DivineShield extends Item implements ICurioItem {
    public DivineShield(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    @Override
    public void onEquip(SlotContext context, ItemStack previous, ItemStack stack) {
        if (!context.entity().level().isClientSide) context.entity().removeEffect(ModEffects.DIVINE_SHIELD_INVULNERABILITY.get());
    }

    @Override
    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (!context.entity().level().isClientSide) context.entity().removeEffect(ModEffects.DIVINE_SHIELD_INVULNERABILITY.get());
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity().level().isClientSide || !(context.entity() instanceof Player player) || !Config.DIVINE_SHIELD_GIVE_EFFECT.get()) return;
        boolean ready = !player.getCooldowns().isOnCooldown(this);
        boolean shown = player.hasEffect(ModEffects.DIVINE_SHIELD_INVULNERABILITY.get());
        if (ready && !shown) {
            player.addEffect(new MobEffectInstance(ModEffects.DIVINE_SHIELD_INVULNERABILITY.get(), MobEffectInstance.INFINITE_DURATION, 0, true, true));
        } else if (!ready && shown) {
            player.removeEffect(ModEffects.DIVINE_SHIELD_INVULNERABILITY.get());
        }
    }

    public boolean tryBlock(Player player) {
        if (player.getCooldowns().isOnCooldown(this)) return false;
        player.getCooldowns().addCooldown(this, Config.DIVINE_SHIELD_COOLDOWN_TICKS.get());
        player.invulnerableTime = 20;
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> lines.add(Component.translatable("tooltip.enigmaticdice.divine_shield_1",
                        String.format("%.1f", Config.DIVINE_SHIELD_COOLDOWN_TICKS.get() / 20F)).withStyle(ChatFormatting.GOLD)),
                Component.translatable("tooltip.enigmaticdice.divine_shield_0"));
    }
}
