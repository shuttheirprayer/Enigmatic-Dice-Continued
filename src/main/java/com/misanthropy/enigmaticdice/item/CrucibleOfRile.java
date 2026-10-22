package com.misanthropy.enigmaticdice.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.enigmaticdice.Config;
import com.misanthropy.enigmaticdice.network.LookAtTargetPacket;
import com.misanthropy.enigmaticdice.network.Network;
import com.misanthropy.enigmaticdice.registry.ModDamageTypes;
import com.misanthropy.enigmaticdice.registry.ModEffects;
import com.misanthropy.enigmaticdice.registry.ModSounds;
import com.misanthropy.enigmaticdice.util.Combat;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CrucibleOfRile extends ConfiguredSword {
    private static final String HITS = "enigmaticdice_crucible_of_rile_hits";
    private static final String UNIQUE_KILLS = "enigmaticdice_crucible_of_rile_unique_kills";
    private static final UUID ARMOR_ID = UUID.fromString("5f4837ed-f638-4e29-8e26-bc26d2c41e2d");
    private static final UUID TOUGHNESS_ID = UUID.fromString("4430c313-c502-4db4-9b9a-ae50d3f446aa");

    public CrucibleOfRile(Properties properties) {
        super(Config.CRUCIBLE_OF_RILE_ATTACK_DAMAGE::get, Config.CRUCIBLE_OF_RILE_ATTACK_SPEED::get, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
        int kills = uniqueKills(stack).size();
        if (kills == 0) return super.getAttributeModifiers(slot, stack);
        double bonus = Config.CRUCIBLE_OF_RILE_PERM_ARMOR_PER_UNIQUE.get() * kills;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = baseModifiers();
        builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_ID, "Weapon modifier", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(TOUGHNESS_ID, "Weapon modifier", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return builder.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.CRUCIBLE_OF_RILE_ATTACK.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            if (target.isAlive() && !Combat.isBoss(target) && target.getHealth() <= Config.CRUCIBLE_OF_RILE_EXECUTE_THRESHOLD.get()) {
                Combat.hurtWithoutKnockback(target, ModDamageTypes.source(player.level(), ModDamageTypes.EXECUTE, player),
                        Math.max(target.getMaxHealth(), target.getHealth() + target.getAbsorptionAmount()) * 10F);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer caster)) return InteractionResultHolder.success(stack);
        taunt(caster.serverLevel(), caster);
        MobEffectInstance boost = new MobEffectInstance(ModEffects.CRUCIBLE_OF_RILE_ARMOR_BOOST.get(), Config.CRUCIBLE_OF_RILE_ARMOR_BUFF_DURATION.get(), 0);
        boost.setCurativeItems(new ArrayList<>());
        caster.addEffect(boost);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.CRUCIBLE_OF_RILE_USE.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        caster.getCooldowns().addCooldown(this, Config.CRUCIBLE_OF_RILE_COOLDOWN.get());
        return InteractionResultHolder.consume(stack);
    }

    private static void taunt(ServerLevel level, ServerPlayer caster) {
        double radius = Config.CRUCIBLE_OF_RILE_AGGRO_RADIUS.get();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(radius), mob -> mob.isAlive() && Combat.canHarm(caster, mob))) {
            mob.setTarget(caster);
        }
        if (!Config.CRUCIBLE_OF_RILE_AGGRO_PLAYERS.get()) return;
        LookAtTargetPacket packet = new LookAtTargetPacket(caster.getUUID(), Config.CRUCIBLE_OF_RILE_AGGRO_DURATION.get());
        for (ServerPlayer victim : level.getEntitiesOfClass(ServerPlayer.class, caster.getBoundingBox().inflate(radius), other -> other != caster && other.isAlive() && Combat.canHarm(caster, other))) {
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> victim), packet);
        }
    }

    public static void onHolderAttacked(LivingEntity holder) {
        CompoundTag data = holder.getPersistentData();
        int hits = data.getInt(HITS) + 1;
        if (hits < Config.CRUCIBLE_OF_RILE_HITS_FOR_COUNTERATTACK.get()) {
            data.putInt(HITS, hits);
            return;
        }
        data.putInt(HITS, 0);
        holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(), ModSounds.CRUCIBLE_OF_RILE_HELIX.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        float damage = (float) holder.getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource source = holder instanceof Player player ? holder.damageSources().playerAttack(player) : holder.damageSources().mobAttack(holder);
        for (LivingEntity target : holder.level().getEntitiesOfClass(LivingEntity.class, holder.getBoundingBox().inflate(Config.CRUCIBLE_OF_RILE_COUNTERATTACK_RADIUS.get()),
                e -> e.isAlive() && e != holder && Combat.canHarm(holder, e))) {
            target.hurt(source, damage);
        }
    }

    public static void recordKill(ItemStack stack, String typeId, LivingEntity victim) {
        Set<String> kills = uniqueKills(stack);
        if (!kills.add(typeId)) return;
        ListTag list = new ListTag();
        kills.forEach(id -> list.add(StringTag.valueOf(id)));
        stack.getOrCreateTag().put(UNIQUE_KILLS, list);
        victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.CRUCIBLE_OF_RILE_UNIQUE_KILL.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private static Set<String> uniqueKills(ItemStack stack) {
        Set<String> kills = new HashSet<>();
        CompoundTag tag = stack.getTag();
        if (tag != null) tag.getList(UNIQUE_KILLS, Tag.TAG_STRING).forEach(entry -> kills.add(entry.getAsString()));
        return kills;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Tooltips.withDetails(tooltip, lines -> {
            lines.add(Component.translatable("tooltip.enigmaticdice.on_use"));
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_1", Config.CRUCIBLE_OF_RILE_AGGRO_RADIUS.get().intValue()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_2", Config.CRUCIBLE_OF_RILE_ARMOR_BUFF_VALUE.get() * 100,
                    Config.CRUCIBLE_OF_RILE_ARMOR_BUFF_DURATION.get() / 20).withStyle(ChatFormatting.GOLD));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_3", Config.CRUCIBLE_OF_RILE_HITS_FOR_COUNTERATTACK.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_4", Config.CRUCIBLE_OF_RILE_COUNTERATTACK_RADIUS.get()).withStyle(ChatFormatting.GOLD));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.on_hit"));
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_5", Config.CRUCIBLE_OF_RILE_EXECUTE_THRESHOLD.get().intValue()).withStyle(ChatFormatting.GOLD));
            lines.add(Tooltips.blank());
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_6"));
            lines.add(Component.translatable("tooltip.enigmaticdice.crucible_of_rile_7", Config.CRUCIBLE_OF_RILE_PERM_ARMOR_PER_UNIQUE.get() * 100).withStyle(ChatFormatting.GOLD));
        });
    }
}
