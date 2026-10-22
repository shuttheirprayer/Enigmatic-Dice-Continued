package com.misanthropy.enigmaticdice.mixin;

import com.misanthropy.enigmaticdice.util.SizeScaled;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private float enigmaticdice$baseShadowRadius;
    @Unique
    private boolean enigmaticdice$scaled;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void enigmaticdice$rememberShadow(EntityRendererProvider.Context context, M model, float shadowRadius, CallbackInfo ci) {
        enigmaticdice$baseShadowRadius = shadowRadius;
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void enigmaticdice$pushScale(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        float scale = ((SizeScaled) entity).enigmaticdice$getScale();
        enigmaticdice$scaled = scale != 1F;
        if (enigmaticdice$scaled) {
            pose.pushPose();
            pose.scale(scale, scale, scale);
        }
        ((EntityRendererAccessor) this).enigmaticdice$setShadowRadius(enigmaticdice$baseShadowRadius * scale);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void enigmaticdice$popScale(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (enigmaticdice$scaled) {
            pose.popPose();
            enigmaticdice$scaled = false;
        }
    }
}
