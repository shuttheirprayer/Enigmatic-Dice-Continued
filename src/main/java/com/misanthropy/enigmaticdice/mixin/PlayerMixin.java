package com.misanthropy.enigmaticdice.mixin;

import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.item.Permafrost;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.util.SizeScaled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Unique
    private static final String enigmaticdice$LEGACY_STACKS = "PermafrostStacksFlySpeedReduction";
    @Unique
    private static final String enigmaticdice$LEGACY_AURA = "PermafrostAuraFlySpeedReduction";

    @Shadow
    @Final
    private Abilities abilities;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void enigmaticdice$scaleDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        float scale = ((SizeScaled) this).enigmaticdice$getScale();
        if (scale != 1F) cir.setReturnValue(cir.getReturnValue().scale(scale));
    }

    @Inject(method = "getStandingEyeHeight", at = @At("RETURN"), cancellable = true)
    private void enigmaticdice$scaleEyes(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        float scale = ((SizeScaled) this).enigmaticdice$getScale();
        if (scale != 1F && scale > 0F) cir.setReturnValue(cir.getReturnValue() * scale);
    }

    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void enigmaticdice$frostFlight(CallbackInfoReturnable<Float> cir) {
        if (!abilities.flying) return;
        float factor = 1F;
        MobEffectInstance hit = getEffect(ModEffects.FROST_HIT.get());
        if (hit != null) factor *= (float) Math.max(0, 1 - Config.PERMAFROST_ATTACK_REDUCTION_PER_STACK.get() * (hit.getAmplifier() + 1));
        if (hasEffect(ModEffects.FROST_AURA.get())) factor *= (float) Math.max(0, 1 - Config.PERMAFROST_AURA_REDUCTION_FACTOR.get());
        if (factor != 1F) cir.setReturnValue(cir.getReturnValue() * factor);
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void enigmaticdice$frozenWings(CallbackInfoReturnable<Boolean> cir) {
        if (Permafrost.isFrozenSolid(this)) cir.setReturnValue(false);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void enigmaticdice$restoreLegacyFlightSpeed(CompoundTag tag, CallbackInfo ci) {
        CompoundTag data = getPersistentData();
        float lost = data.getFloat(enigmaticdice$LEGACY_STACKS) + data.getFloat(enigmaticdice$LEGACY_AURA);
        if (!data.contains(enigmaticdice$LEGACY_STACKS) && !data.contains(enigmaticdice$LEGACY_AURA)) return;
        data.remove(enigmaticdice$LEGACY_STACKS);
        data.remove(enigmaticdice$LEGACY_AURA);
        if (lost != 0) abilities.setFlyingSpeed(abilities.getFlyingSpeed() + lost);
    }
}
