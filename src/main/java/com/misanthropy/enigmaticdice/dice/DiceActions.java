package com.misanthropy.enigmaticdice.dice;

import com.misanthropy.enigmaticdice.dice.action.AccelerateTime;
import com.misanthropy.enigmaticdice.dice.action.ApplyEffect;
import com.misanthropy.enigmaticdice.dice.action.BlockCage;
import com.misanthropy.enigmaticdice.dice.action.EnchantArmor;
import com.misanthropy.enigmaticdice.dice.action.Explosion;
import com.misanthropy.enigmaticdice.dice.action.GiveEnchantedBook;
import com.misanthropy.enigmaticdice.dice.action.GiveItem;
import com.misanthropy.enigmaticdice.dice.action.LightningStorm;
import com.misanthropy.enigmaticdice.dice.action.PermanentEffects;
import com.misanthropy.enigmaticdice.dice.action.RandomPotion;
import com.misanthropy.enigmaticdice.dice.action.RunCommand;
import com.misanthropy.enigmaticdice.dice.action.Skyfall;
import com.misanthropy.enigmaticdice.dice.action.Summon;
import com.misanthropy.enigmaticdice.dice.action.TeleportMonsters;
import com.misanthropy.enigmaticdice.dice.action.TeleportToBiome;
import com.misanthropy.enigmaticdice.dice.action.TeleportToStructure;
import com.misanthropy.enigmaticdice.dice.action.TeleportToWorldEdge;
import com.mojang.serialization.MapCodec;

import javax.annotation.Nullable;
import java.util.Map;

public final class DiceActions {
    private static final Map<String, MapCodec<? extends DiceAction>> TYPES = Map.ofEntries(
            Map.entry("give_item", GiveItem.CODEC),
            Map.entry("give_enchanted_book", GiveEnchantedBook.CODEC),
            Map.entry("random_potion", RandomPotion.CODEC),
            Map.entry("enchant_armor", EnchantArmor.CODEC),
            Map.entry("explosion", Explosion.CODEC),
            Map.entry("lightning_storm", LightningStorm.CODEC),
            Map.entry("permanent_effects", PermanentEffects.CODEC),
            Map.entry("block_cage", BlockCage.CODEC),
            Map.entry("teleport_to_biome", TeleportToBiome.CODEC),
            Map.entry("teleport_to_structure", TeleportToStructure.CODEC),
            Map.entry("teleport_to_world_edge", TeleportToWorldEdge.CODEC),
            Map.entry("teleport_monsters", TeleportMonsters.CODEC),
            Map.entry("accelerate_time", AccelerateTime.CODEC),
            Map.entry("apply_effect", ApplyEffect.CODEC),
            Map.entry("skyfall", Skyfall.CODEC),
            Map.entry("summon", Summon.CODEC),
            Map.entry("command", RunCommand.CODEC));

    private DiceActions() {
    }

    @Nullable
    public static MapCodec<? extends DiceAction> get(String type) {
        return TYPES.get(type.startsWith("enigmaticdice:") ? type.substring("enigmaticdice:".length()) : type);
    }
}
