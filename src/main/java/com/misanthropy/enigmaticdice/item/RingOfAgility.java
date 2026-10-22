package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

public class RingOfAgility extends Item implements ICurioItem {
    public RingOfAgility(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return true;
    }

    public static double dodgeChance(LivingEntity entity) {
        double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double chance = 1.0 - Math.pow(0.99, speed * 100 * Config.RING_OF_AGILITY_CHANCE_SCALE.get());
        return Math.max(0, Math.min(chance, Config.RING_OF_AGILITY_MAX_DODGE_CHANCE.get()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            double chance = Minecraft.getInstance().player != null ? dodgeChance(Minecraft.getInstance().player) : 0.1;
            lines.add(Component.translatable("tooltip.enigmaticdice.ring_of_agility_1", String.format("%.1f", chance * 100)).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.ring_of_agility_2"));
        }, Component.translatable("tooltip.enigmaticdice.ring_of_agility_0"));
    }
}
