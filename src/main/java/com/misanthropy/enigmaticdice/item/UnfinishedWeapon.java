package com.misanthropy.enigmaticdice.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class UnfinishedWeapon extends SwordItem {
    private static final String DISCORD = "https://discord.gg/ZMKnAB92GZ";

    public UnfinishedWeapon(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(stack);
        Component link = Component.literal("Discord").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DISCORD)));
        player.displayClientMessage(Component.translatable("tooltip.enigmaticdice.unfinished_item_discord").append(" ").append(link), false);
        return InteractionResultHolder.success(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.unfinished_item_1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.unfinished_item_2"));
            lines.add(Component.translatable("tooltip.enigmaticdice.unfinished_item_3"));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.unfinished_item_4"));
            lines.add(Component.translatable("tooltip.enigmaticdice.unfinished_item_5"));
        });
    }
}
