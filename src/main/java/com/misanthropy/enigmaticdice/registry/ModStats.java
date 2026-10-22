package com.misanthropy.enigmaticdice.registry;

import com.misanthropy.enigmaticdice.EnigmaticDice;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = EnigmaticDice.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModStats {
    public static final ResourceLocation DICE_FROM_BLOCK = EnigmaticDice.id("obtained_dice_from_block");
    public static final ResourceLocation DICE_FROM_MOB = EnigmaticDice.id("obtained_dice_from_mob");

    private ModStats() {
    }

    @SubscribeEvent
    static void register(RegisterEvent event) {
        event.register(Registries.CUSTOM_STAT, helper -> {
            helper.register(new ResourceLocation(DICE_FROM_BLOCK.getPath()), DICE_FROM_BLOCK);
            helper.register(new ResourceLocation(DICE_FROM_MOB.getPath()), DICE_FROM_MOB);
        });
    }

    public static void init() {
        Stats.CUSTOM.get(DICE_FROM_BLOCK, StatFormatter.DEFAULT);
        Stats.CUSTOM.get(DICE_FROM_MOB, StatFormatter.DEFAULT);
    }
}
