package com.misanthropy.enigmaticdice.client;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import com.misanthropy.enigmaticdice.network.DoubleJumpPacket;
import com.misanthropy.enigmaticdice.network.Network;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final int DOUBLE_TAP_TICKS = 6;

    private static long lastJumpPress = -100;
    private static boolean jumpHeld;
    private static boolean airJumpSpent;

    private ClientEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof LocalPlayer player)) return;
        if (player.onGround()) airJumpSpent = false;
        boolean jumping = player.input.jumping;
        boolean pressed = jumping && !jumpHeld;
        jumpHeld = jumping;
        if (!pressed || airJumpSpent || player.isCreative() || player.isSpectator()) return;
        if (!Combat.isWearing(player, ModItems.GRAVITY_CORE.get())) return;
        long now = player.level().getGameTime();
        if (now - lastJumpPress < DOUBLE_TAP_TICKS) {
            Network.CHANNEL.sendToServer(new DoubleJumpPacket());
            airJumpSpent = true;
            lastJumpPress = -100;
        } else {
            lastJumpPress = now;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (item != ModItems.GIANTS_RING.get() && item != ModItems.RING_OF_AGILITY.get() && item != ModItems.DIVINE_SHIELD.get()) return;
        event.getToolTip().removeIf(ClientEvents::isCuriosSlotLine);
    }

    private static boolean isCuriosSlotLine(Component line) {
        return line.getContents() instanceof TranslatableContents contents && contents.getKey().equals("curios.tooltip.slot");
    }
}
