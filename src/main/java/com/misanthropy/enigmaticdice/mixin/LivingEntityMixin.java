package com.misanthropy.enigmaticdice.mixin;

import com.misanthropy.enigmaticdice.registry.ModAttributes;
import com.misanthropy.enigmaticdice.registry.ModItems;
import com.misanthropy.enigmaticdice.util.Combat;
import com.misanthropy.enigmaticdice.util.SizeScaled;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements SizeScaled {
    @Unique
    private float enigmaticdice$scale = 1F;

    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public float enigmaticdice$getScale() {
        return enigmaticdice$scale;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void enigmaticdice$updateScale(CallbackInfo ci) {
        AttributeInstance size = ((LivingEntity) (Object) this).getAttribute(ModAttributes.SIZE_SCALE.get());
        float scale = size == null ? 1F : (float) size.getValue();
        if (scale != enigmaticdice$scale) {
            enigmaticdice$scale = scale;
            refreshDimensions();
        }
    }

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void enigmaticdice$scaleDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (enigmaticdice$scale != 1F) cir.setReturnValue(cir.getReturnValue().scale(enigmaticdice$scale));
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void enigmaticdice$moaiCharm(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Combat.isWearing(player, ModItems.MOAI_CHARM.get())) cir.setReturnValue(false);
    }
}
