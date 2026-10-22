package com.misanthropy.enigmaticdice.client;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID, value = Dist.CLIENT)
public final class ClientTime {
    private static int extraPerTick;
    private static int remaining;

    private ClientTime() {
    }

    public static void start(int multiplier, int duration) {
        extraPerTick = multiplier - 1;
        remaining = duration;
    }

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || remaining <= 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;
        if (minecraft.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            minecraft.level.setDayTime(minecraft.level.getDayTime() + extraPerTick);
        }
        remaining--;
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        remaining = 0;
    }
}
