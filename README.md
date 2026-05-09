# Villager Farm

A Fabric mod for Minecraft 1.21.11 that overhauls villager farming behavior.

## Features

- **Decoupled from `mob_griefing`** — villager harvest, replant, and pickup now ignore the global mob_griefing gamerule (toggle in config). Set `mob_griefing=false` to stop creepers/endermen damaging the world while villagers keep farming.
- **Atomic harvest + replant** — when a farmer breaks a mature crop, the seedling goes back in the same tick. Yields are vanilla loot minus one seed-equivalent.
- **Five extended crops** — sugar cane, nether wart, pumpkin, melon, and cocoa. Pumpkins and melons are only harvested when stem-grown (verified via [blockorigin](https://github.com/JackOtsig/blockorigin)) so player-placed decorations are left alone. Sugar cane harvest leaves the bottom stalk so the column regrows. Each crop is independently toggleable.
- **Food sharing + breeding integration** — pumpkin (food=4 by default), melon slice / sugar cane / cocoa beans (food=1) all count toward villager breeding readiness. Custom share path lets farmers throw any of these to neighbors as soon as they have ≥2 in a slot, so the new crops actually circulate.
- **Pickup status effects** — picking up sugar cane gives Speed (configurable duration + amplifier per cane). Cocoa beans give Regeneration. A cleric picking up nether wart rolls one of the configured positive effects.
- **Farmer anti-trample** — farmer-profession villagers stop wrecking their own farmland by jumping. Other entities still trample normally.
- **Wider AI scan** — vanilla's farmer brain only scans ±1 around the villager. The mod adds a configurable wider scan for extended crops (default ±2). Includes a fallback that populates `SECONDARY_JOB_SITE` for soul-sand and jungle-log farms; without it, the brain framework would refuse to even consider the farming task.
- **WTHIT + Jade tooltip integration** — hovering a villager shows their current activity (Working / Resting / Meeting / Idle / etc.) plus their inventory contents. Optional; works with either tooltip mod, or none.

Every feature has a config toggle, and every magic number is editable.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.2 and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.11.
2. Download [`blockorigin`](https://github.com/JackOtsig/blockorigin/releases) (required runtime dep — provides the cause-tracking that makes the stem-grown pumpkin/melon detection work).
3. Download `villagerfarm-x.y.z.jar` from the [releases page](../../releases) and drop it into your `mods/` folder alongside `blockorigin`.
4. Optional: install [WTHIT](https://modrinth.com/mod/wthit) or [Jade](https://modrinth.com/mod/jade) to get the villager tooltip overlay.

## Configuration

On first launch the mod creates `config/villagerfarm.json` with sensible defaults. Edit the file and **restart** the game/server to apply changes. There's no hot-reload command.

The schema is a single JSON object with two top-level sections:

### `features` — toggles

| Key | Default | Effect |
|---|---|---|
| `gamerule_split` | `true` | When true, villager farming + pickup ignore vanilla's `mob_griefing` gamerule. Set false to revert to vanilla coupling. |
| `atomic_harvest_replant` | `true` | Vanilla wheat/beetroot/potato/carrot harvested + replanted in the same tick. Disable for vanilla two-step behavior. |
| `anti_trample` | `true` | Farmer-profession villagers don't trample farmland by jumping. |
| `secondary_job_site_patcher` | `true` | Forces farmers near soul-sand / jungle-log / etc. to consider the FarmerVillagerTask. Without this, vanilla brain refuses to run the task for non-farmland farms. |
| `tooltip_integration` | `true` | WTHIT/Jade villager tooltip displays activity + inventory. |
| `extended_crops.enabled` | `true` | Master switch for the five extra crops. |
| `extended_crops.sugar_cane / nether_wart / cocoa / pumpkin / melon` | `true` | Per-crop toggles, layered under the master switch. |
| `food_sharing.enabled` | `true` | Master switch for the custom low-threshold share path. |
| `food_sharing.share_pumpkin / share_melon_slice / share_sugar_cane / share_cocoa_beans / share_nether_wart_to_cleric` | `true` | Per-item share toggles. Wart is farmer→cleric only when its toggle is on. |
| `pickup_effects.sugar_cane.enabled` | `true` | Speed effect on sugar-cane pickup. |
| `pickup_effects.sugar_cane.ticks_per_item` | `200` | Ticks added per cane (20t/s, so 200 = 10s). |
| `pickup_effects.sugar_cane.amplifier` | `0` | 0 = level I, 1 = level II, etc. |
| `pickup_effects.cocoa_beans.*` | `true / 100 / 0` | Regen effect on cocoa pickup, 5s/bean by default. |
| `pickup_effects.nether_wart.enabled` | `true` | Random positive effect on nether-wart pickup. |
| `pickup_effects.nether_wart.cleric_only` | `true` | Only fires for cleric villagers. |
| `pickup_effects.nether_wart.duration_ticks` | `600` | 30 seconds at 20 t/s. |
| `pickup_effects.nether_wart.amplifier` | `0` | Effect level. |
| `pickup_effects.nether_wart.allowed_effects` | 11 entries | Identifier list rolled from. Bad ids are silently dropped. |

### `values` — numeric knobs

| Key | Default | Effect |
|---|---|---|
| `food_values` | pumpkin=4, melon_slice=1, sugar_cane=1, cocoa_beans=1 | Map of `minecraft:item_id` → food points. Items not in this map are not villager food. |
| `search.extended_radius_xyz` | `2` | The outer search shell for extended crops (vanilla is 1). |
| `search.extended_harvest_distance_squared` | `16.0` | Max squared distance the villager will harvest an extended crop from. |
| `search.unstuck_min/max_distance_squared` | `1.0 / 6.25` | Vanilla CropBlock targets in this squared-distance window get an unstuck-harvest so the brain doesn't lock onto unreachable wheat. |
| `search.secondary_job_site_radius_xz / radius_y / tick_interval` | `4 / 2 / 40` | Patcher scan dimensions and refresh interval (ticks). |
| `sharing.min_stack_size_to_toss` | `2` | Farmer needs at least this count in a slot to toss it to a neighbor. |
| `sharing.max_share_distance_squared` | `5.0` | Max squared distance between farmer and target villager for the custom share path. |

The config does **not** validate its inputs. If you set `ticks_per_item: -50` or rename `enabled` to `enabled_lol`, you get whatever fallback Gson decides — usually defaults for missing fields, weird behavior for nonsense values. Edit responsibly.

## Building from source

```sh
git clone https://github.com/JackOtsig/blockorigin.git ../mcblockorigin
git clone https://github.com/JackOtsig/villagerfarm.git mcvillagerfarm
cd mcvillagerfarm
./gradlew build
```

The produced jar is at `build/libs/villagerfarm-x.y.z.jar`.

`settings.gradle` declares a Gradle composite-build include of `../mcblockorigin`, so the two repos must be cloned as siblings.

## License

MIT — see [LICENSE](LICENSE).
