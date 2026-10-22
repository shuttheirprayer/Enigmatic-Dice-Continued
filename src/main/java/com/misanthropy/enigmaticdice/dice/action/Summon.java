package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceCodecs;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.misanthropy.enigmaticdice.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Summon(List<Group> spawns, Optional<Component> title, List<String> onSpawn) implements DiceAction {
    private static final int ON_SPAWN_DELAY = 5;
    public static final MapCodec<Summon> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Group.CODEC.listOf().fieldOf("spawns").forGetter(Summon::spawns),
            ExtraCodecs.COMPONENT.optionalFieldOf("title").forGetter(Summon::title),
            Codec.STRING.listOf().optionalFieldOf("on_spawn", List.of()).forGetter(Summon::onSpawn)
    ).apply(instance, Summon::new));

    @Override
    public boolean canRun(DiceContext context) {
        return spawns.stream().allMatch(group -> group.placement().hasSpace(context.player()));
    }

    @Override
    public Result run(DiceContext context) {
        ServerPlayer player = context.player();
        for (Group group : spawns) {
            Vec3 origin = player.position();
            Vec3 look = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            for (int i = 0; i < group.count(); i++) {
                int index = i;
                Vec3 position = group.placement().position(context.level(), origin, look, index, group.count(), context.random());
                Runnable spawn = () -> {
                    Entity created = group.spawn(context, position);
                    if (created != null && !onSpawn.isEmpty()) Scheduler.schedule(ON_SPAWN_DELAY, () -> runOnSpawn(created));
                };
                int delay = group.delay() + group.interval() * index;
                if (delay <= 0) spawn.run();
                else Scheduler.schedule(delay, spawn);
            }
        }
        title.ifPresent(text -> {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 20, 0));
            player.connection.send(new ClientboundSetTitleTextPacket(text));
        });
        return Result.DONE;
    }

    private void runOnSpawn(Entity created) {
        if (!created.isAlive()) return;
        CommandSourceStack source = created.createCommandSourceStack().withPermission(Commands.LEVEL_GAMEMASTERS).withSuppressedOutput();
        for (String command : onSpawn) {
            RunCommand.execute(source, command);
        }
    }

    public record Group(EntityType<?> entity, int count, Optional<CompoundTag> nbt, List<CompoundTag> nbtVariants, Placement placement,
                        int delay, int interval, boolean tame, boolean targetRoller, boolean persistent,
                        Map<Attribute, Double> attributes, boolean fullHealth, List<Effect> effects, int randomEffects, List<String> names,
                        Optional<SoundEvent> sound) {
        static final Codec<Group> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(Group::entity),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(Group::count),
                DiceCodecs.SNBT.optionalFieldOf("nbt").forGetter(Group::nbt),
                DiceCodecs.SNBT.listOf().optionalFieldOf("nbt_variants", List.of()).forGetter(Group::nbtVariants),
                Placement.CODEC.optionalFieldOf("placement", Placement.FRONT).forGetter(Group::placement),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("delay", 0).forGetter(Group::delay),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("interval", 0).forGetter(Group::interval),
                Codec.BOOL.optionalFieldOf("tame", false).forGetter(Group::tame),
                Codec.BOOL.optionalFieldOf("target_roller", false).forGetter(Group::targetRoller),
                Codec.BOOL.optionalFieldOf("persistent", false).forGetter(Group::persistent),
                Codec.unboundedMap(ForgeRegistries.ATTRIBUTES.getCodec(), Codec.DOUBLE).optionalFieldOf("attributes", Map.of()).forGetter(Group::attributes),
                Codec.BOOL.optionalFieldOf("full_health", false).forGetter(Group::fullHealth),
                Effect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(Group::effects),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("random_effects", 0).forGetter(Group::randomEffects),
                Codec.STRING.listOf().optionalFieldOf("names", List.of()).forGetter(Group::names),
                ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("sound").forGetter(Group::sound)
        ).apply(instance, Group::new));

        @Nullable
        Entity spawn(DiceContext context, Vec3 position) {
            ServerLevel level = context.level();
            RandomSource random = level.random;
            CompoundTag tag = nbt.map(CompoundTag::copy).orElseGet(CompoundTag::new);
            if (!nbtVariants.isEmpty()) tag.merge(nbtVariants.get(random.nextInt(nbtVariants.size())));
            tag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entity).toString());
            Entity created = EntityType.loadEntityRecursive(tag, level, loaded -> {
                loaded.moveTo(position.x, position.y, position.z, loaded.getYRot(), loaded.getXRot());
                return loaded;
            });
            if (created == null) return null;
            ServerPlayer owner = context.onlinePlayer();
            if (created instanceof LivingEntity living) {
                attributes.forEach((attribute, value) -> {
                    AttributeInstance instance = living.getAttribute(attribute);
                    if (instance != null) instance.setBaseValue(value);
                });
                if (fullHealth) living.setHealth(living.getMaxHealth());
                for (Effect effect : effects) {
                    living.addEffect(effect.create());
                }
                List<MobEffect> pool = randomEffects > 0 ? randomEffectPool() : List.of();
                for (int i = 0; i < randomEffects && !pool.isEmpty(); i++) {
                    living.addEffect(new MobEffectInstance(pool.get(random.nextInt(pool.size())), MobEffectInstance.INFINITE_DURATION, 0, false, false));
                }
            }
            if (!names.isEmpty()) created.setCustomName(Component.literal(names.get(random.nextInt(names.size()))));
            if (created instanceof Mob mob) {
                if (persistent) mob.setPersistenceRequired();
                if (targetRoller && owner != null) mob.setTarget(owner);
            }
            if (tame && owner != null) {
                if (created instanceof TamableAnimal tamable) tamable.tame(owner);
                else if (created instanceof AbstractHorse horse) horse.tameWithName(owner);
            }
            if (!level.tryAddFreshEntityWithPassengers(created)) return null;
            sound.ifPresent(event -> level.playSound(null, BlockPos.containing(position), event, SoundSource.HOSTILE, 1.2F, 1.0F));
            return created;
        }

        private static List<MobEffect> randomEffectPool() {
            List<MobEffect> pool = new ArrayList<>();
            for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
                if (effect != MobEffects.INVISIBILITY && !effect.isInstantenous()) pool.add(effect);
            }
            return pool;
        }
    }

    public record Effect(MobEffect effect, int amplifier, int duration, boolean ambient, boolean visible) {
        static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(Effect::effect),
                Codec.intRange(0, 127).optionalFieldOf("amplifier", 0).forGetter(Effect::amplifier),
                Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("duration", -1).forGetter(Effect::duration),
                Codec.BOOL.optionalFieldOf("ambient", false).forGetter(Effect::ambient),
                Codec.BOOL.optionalFieldOf("visible", false).forGetter(Effect::visible)
        ).apply(instance, Effect::new));

        MobEffectInstance create() {
            return new MobEffectInstance(effect, duration == -1 ? MobEffectInstance.INFINITE_DURATION : duration, amplifier, ambient, visible);
        }
    }

    public enum Shape implements StringRepresentable {
        FRONT("front"),
        RING("ring"),
        SCATTER("scatter");

        static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

        private final String name;

        Shape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Placement(Shape type, double distance, double height, boolean requireSpace, double minRadius, double maxRadius, Heightmap.Types heightmap) {
        static final Placement FRONT = new Placement(Shape.FRONT, 3, 1, true, 0, 0, Heightmap.Types.WORLD_SURFACE);
        static final Codec<Placement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Shape.CODEC.fieldOf("type").forGetter(Placement::type),
                Codec.DOUBLE.optionalFieldOf("distance", 3.0).forGetter(Placement::distance),
                Codec.DOUBLE.optionalFieldOf("height", 0.0).forGetter(Placement::height),
                Codec.BOOL.optionalFieldOf("require_space", false).forGetter(Placement::requireSpace),
                Codec.DOUBLE.optionalFieldOf("min_radius", 0.0).forGetter(Placement::minRadius),
                Codec.DOUBLE.optionalFieldOf("max_radius", 0.0).forGetter(Placement::maxRadius),
                Heightmap.Types.CODEC.optionalFieldOf("heightmap", Heightmap.Types.WORLD_SURFACE).forGetter(Placement::heightmap)
        ).apply(instance, Placement::new));

        boolean hasSpace(ServerPlayer player) {
            if (!requireSpace || type != Shape.FRONT) return true;
            Vec3 look = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
            BlockPos pos = BlockPos.containing(player.position().add(look.scale(distance)).add(0, height, 0));
            return player.level().getBlockState(pos).getCollisionShape(player.level(), pos).isEmpty();
        }

        Vec3 position(ServerLevel level, Vec3 origin, Vec3 look, int index, int count, RandomSource random) {
            return switch (type) {
                case RING -> {
                    double angle = Math.toRadians(index * 360.0 / count);
                    yield origin.add(distance * Math.cos(angle), height, distance * Math.sin(angle));
                }
                case SCATTER -> {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = minRadius + (maxRadius - minRadius) * random.nextDouble();
                    int x = (int) (origin.x + Math.cos(angle) * radius);
                    int z = (int) (origin.z + Math.sin(angle) * radius);
                    yield new Vec3(x + 0.5, level.getHeight(heightmap, x, z) + height, z + 0.5);
                }
                case FRONT -> origin.add(look.scale(distance)).add(0, height, 0);
            };
        }
    }
}
