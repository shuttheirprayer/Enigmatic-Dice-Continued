package com.misanthropy.enigmaticdice.dice.action;

import com.misanthropy.enigmaticdice.dice.DiceAction;
import com.misanthropy.enigmaticdice.dice.DiceContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public record RandomPotion(boolean vanillaOnly, String name) implements DiceAction {
    public static final MapCodec<RandomPotion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("vanilla_only", true).forGetter(RandomPotion::vanillaOnly),
            Codec.STRING.optionalFieldOf("name", "item.enigmaticdice.vanilla_random_splash_potion").forGetter(RandomPotion::name)
    ).apply(instance, RandomPotion::new));

    @Override
    public Result run(DiceContext context) {
        RandomSource random = context.random();
        List<MobEffect> available = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (!vanillaOnly || key != null && "minecraft".equals(key.getNamespace())) available.add(effect);
        }
        int count = Math.min(3 + random.nextInt(4), available.size());
        List<MobEffectInstance> selected = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MobEffect effect = available.remove(random.nextInt(available.size()));
            if (effect.isInstantenous()) {
                selected.add(new MobEffectInstance(effect, 1, random.nextInt(5)));
            } else {
                selected.add(new MobEffectInstance(effect, (10 + random.nextInt(15 * 60 - 10)) * 20, random.nextInt(3)));
            }
        }
        ItemStack potion = new ItemStack(Items.SPLASH_POTION);
        PotionUtils.setCustomEffects(potion, selected);
        ListTag glint = new ListTag();
        CompoundTag fake = new CompoundTag();
        fake.putString("id", "enigmaticdice:glint");
        fake.putShort("lvl", (short) 0);
        glint.add(fake);
        potion.getOrCreateTag().put("Enchantments", glint);
        potion.setHoverName(Component.translatable(name).withStyle(style -> style.withItalic(false)));
        GiveItem.drop(context.player(), potion, 1);
        return Result.DONE;
    }
}
