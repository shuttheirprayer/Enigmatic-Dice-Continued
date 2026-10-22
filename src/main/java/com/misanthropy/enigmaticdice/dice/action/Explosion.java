package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record Explosion(float power, boolean fire, boolean breakBlocks, int fuse) implements DiceAction {
    public static final MapCodec<Explosion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0, 128).optionalFieldOf("power", 4F).forGetter(Explosion::power),
            Codec.BOOL.optionalFieldOf("fire", false).forGetter(Explosion::fire),
            Codec.BOOL.optionalFieldOf("break_blocks", false).forGetter(Explosion::breakBlocks),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("fuse", 75).forGetter(Explosion::fuse)
    ).apply(instance, Explosion::new));

    @Override
    public Result run(DiceContext context) {
        ServerLevel level = context.level();
        Vec3 origin = Vec3.atBottomCenterOf(context.player().blockPosition());
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.2F, 1.0F);
        PrimedTnt tnt = new PrimedTnt(level, origin.x, origin.y, origin.z, null);
        tnt.setFuse(fuse + 20);
        level.addFreshEntity(tnt);
        Scheduler.schedule(fuse, () -> {
            Vec3 at = tnt.isRemoved() ? origin : tnt.position();
            tnt.discard();
            level.explode(null, at.x, at.y, at.z, power, fire, breakBlocks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        });
        return Result.DONE;
    }
}
