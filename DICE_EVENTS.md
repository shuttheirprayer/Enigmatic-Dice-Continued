# Dice event format

Every event is one JSON file in a data pack at `data/<namespace>/dice_events/<name>.json`.
The file path is the event id (`enigmaticdice:minecraft_explosion`). `/reload` applies changes.
To change or disable a built-in event, put a file with the same path in your own data pack.

## Common fields

| Field | Default | Meaning |
|---|---|---|
| `type` | required | One of the types below. |
| `rarity` | required | Base chance is `1 / rarity` before luck. |
| `category` | `positive` | `positive`, `neutral` or `negative`. Luck only helps positive events, bad luck only helps negative ones. |
| `enabled` | `true` | `false` removes the event. |
| `message` | none | Translation key shown in chat when the event happens. |
| `message_variants` | `0` | If above 0, a random `<message>.0` … `<message>.N-1` is used. |
| `claim_radius` | `0` | If any claimed chunk is within this many blocks, the event cannot be rolled there. |
| `conditions` | none | Standard Forge conditions, e.g. `[{"type": "forge:mod_loaded", "modid": "alexscaves"}]`. |

An event that cannot happen where the player is (no armor to curse, no room to spawn, biome not in this dimension) is skipped and the roll picks again, so the odds of everything else scale up.

## Types

**`give_item`**: `item`, `count` (1), `nbt` (SNBT string).

**`give_enchanted_book`**: `item`, `level` (1). Stores one random enchantment from the whole registry on the item.

**`random_potion`**: `vanilla_only` (true), `name` (translation key). A splash potion with 3 to 6 random effects.

**`enchant_armor`**: `enchantment`, `level` (1). Adds it to every worn armor piece that lacks it.

**`explosion`**: `power` (4), `fire` (false), `break_blocks` (false), `fuse` in ticks (75).

**`lightning_storm`**: `bolts` (15), `radius` (8). Bolts in claimed chunks still hurt but start no fires.

**`permanent_effects`**: `effects` list of `{effect, max_level, message}`. Raises one random effect that is below its max, forever. Milk does not remove it.

**`block_cage`**: `block` (ancient debris), `offsets` list of `[x, y, z]`. Never replaces block entities, unbreakable blocks or claimed blocks.

**`teleport_to_biome`**: `biomes` list, `radius` (4480), `horizontal_step` (64), `vertical_step` (128). Searches off the main thread.

**`teleport_to_structure`**: `structures` list, `radius` in chunks (100), `offset_x` (100), `offset_z` (0), `yaw` (90).

**`teleport_to_world_edge`**: `margin` (10).

**`teleport_monsters`**: `radius` (100), `interval` in ticks (10), `distance` (5). Bosses are never pulled.

**`accelerate_time`**: `multiplier` (40), `duration` in ticks (6000).

**`apply_effect`**: `effect`, `duration` (600, -1 is infinite), `amplifier` (0), `ambient`, `visible`, `curable` (true). Skipped if the player already has the effect at the same or a higher level.

**`skyfall`**: `height` (200), `item` (water bucket). Overworld only, needs an empty hotbar slot and open sky at the target.

**`command`**: `commands` (all run in order) and/or `variants` (one random entry runs). Commands run as and at the roller with permission level 2 and no chat output, so `@s` is the roller. The event counts as failed, and the roll picks again, only if no command succeeded. Invalid commands are logged.

**`summon`**: `title` (optional text component shown as a title), `on_spawn` (commands run as and at each spawned entity 5 ticks after it appears, once other mods have set it up; `@s` is the entity), `spawns` list of groups:

| Group field | Default | Meaning |
|---|---|---|
| `entity` | required | Entity id. |
| `count` | `1` | How many. |
| `nbt` | none | SNBT applied like `/summon`. |
| `nbt_variants` | none | List of SNBT; one random entry is merged in per entity. |
| `placement` | front, 3 blocks | See below. |
| `delay`, `interval` | `0` | Ticks before the first spawn and between spawns. |
| `tame` | `false` | Tames tamable animals and horses to the roller. |
| `target_roller` | `false` | Mobs target the roller. |
| `persistent` | `false` | Mobs never despawn. |
| `attributes` | none | Map of attribute id to base value. |
| `full_health` | `false` | Heal to max after attributes are set. |
| `effects` | none | List of `{effect, amplifier, duration, ambient, visible}`. Duration -1 is infinite. |
| `random_effects` | `0` | That many random infinite effects. |
| `names` | none | One random name is given to each entity. |
| `sound` | none | Sound played at each spawn. |

Placement `type` is `front` (`distance`, `height`, `require_space`), `ring` (`distance` is the radius, `height`) or `scatter` (`min_radius`, `max_radius`, `height`, `heightmap`).

## Example

```json
{
  "conditions": [{"type": "forge:mod_loaded", "modid": "alexsmobs"}],
  "type": "summon",
  "rarity": 20,
  "category": "positive",
  "message": "mypack.event.crows",
  "spawns": [
    {
      "entity": "alexsmobs:crow",
      "count": 4,
      "placement": {"type": "ring", "distance": 2, "height": 3},
      "interval": 5,
      "tame": true
    }
  ]
}
```

## Commands

`/enigmaticdice roll [players]`, `/enigmaticdice trigger <event> [players]`, `/enigmaticdice odds [luck]` (writes the full table to the world folder), `/enigmaticdice luck [player]`.
