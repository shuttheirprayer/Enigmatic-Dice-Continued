package com.misanthropy.enigmaticdice.client;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID, value = Dist.CLIENT)
public final class ClientLook {
    private static final float TURN_SPEED = 720F;

    private static UUID target;
    private static int remaining;

    private ClientLook() {
    }

    public static void start(UUID newTarget, int duration) {
        if (remaining > 0 && newTarget.equals(target)) {
            remaining = Math.max(remaining, duration);
        } else {
            target = newTarget;
            remaining = Math.max(1, duration);
        }
    }

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remaining > 0) remaining--;
    }

    @SubscribeEvent
    static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (remaining <= 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        Player focus = minecraft.level.getPlayerByUUID(target);
        if (focus == null || !focus.isAlive()) return;
        float partialTick = (float) event.getPartialTick();
        Vec3 direction = focus.getEyePosition(partialTick).subtract(player.getEyePosition(partialTick));
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F);
        float pitch = Mth.clamp((float) -(Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG), -90.0F, 90.0F);
        float step = TURN_SPEED * minecraft.getDeltaFrameTime() / 20F;
        float newYaw = Mth.approachDegrees(player.getYRot(), yaw, step);
        player.setYRot(newYaw);
        player.setXRot(Mth.approachDegrees(player.getXRot(), pitch, step));
        player.setYHeadRot(newYaw);
        player.yBodyRot = newYaw;
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        remaining = 0;
    }
}
