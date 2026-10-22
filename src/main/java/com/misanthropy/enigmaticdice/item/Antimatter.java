package com.misanthropy.enigmaticdice.item;

import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.util.Claims;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Antimatter extends Item {
    private static final float POWER = 10F;

    public Antimatter(Properties properties) {
        super(properties);
    }

    public static void registerDispenserBehavior() {
        DispenserBlock.registerBehavior(ModItems.ANTIMATTER.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                ServerLevel level = source.getLevel();
                BlockPos target = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                if (canAnnihilate(level, target) && !Claims.isClaimed(level, target)) {
                    annihilate(level, target);
                    stack.shrink(1);
                }
                return stack;
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!canAnnihilate(level, pos)) return InteractionResult.FAIL;
        if (level instanceof ServerLevel serverLevel && context.getPlayer() instanceof ServerPlayer player) {
            if (!player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || Claims.isBreakDenied(serverLevel, pos, player)) {
                return InteractionResult.FAIL;
            }
            annihilate(serverLevel, pos);
            if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean canAnnihilate(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && (state.getDestroySpeed(level, pos) >= 0 || state.is(Blocks.BEDROCK));
    }

    private static void annihilate(ServerLevel level, BlockPos pos) {
        level.destroyBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, POWER, false, Level.ExplosionInteraction.NONE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.antimatter.1"));
            lines.add(Component.translatable("tooltip.enigmaticdice.antimatter.2"));
            lines.add(Component.translatable("tooltip.enigmaticdice.antimatter.3"));
        });
    }
}
