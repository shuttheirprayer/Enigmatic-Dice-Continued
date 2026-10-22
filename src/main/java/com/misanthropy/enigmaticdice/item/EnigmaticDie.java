package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.dice.DiceRoller;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EnigmaticDie extends Item {
    private static final int COOLDOWN = 20;

    public EnigmaticDie(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);
        if (!DiceRoller.roll(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("enigmaticdice.die.nothing_happens"), true);
            return InteractionResultHolder.fail(stack);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        serverPlayer.getCooldowns().addCooldown(this, COOLDOWN);
        if (!serverPlayer.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.enigmatic_die_2"));
            lines.add(Component.translatable("tooltip.enigmaticdice.enigmatic_die_3"));
        }, Component.translatable("tooltip.enigmaticdice.enigmatic_die"));
    }
}
