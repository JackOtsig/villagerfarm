# Villager Farm

A Fabric mod for Minecraft 1.21.11 that overhauls villager farming behavior.

## Features

- **Splits villager farming off the `mob_griefing` gamerule** — `villagerfarm:villager_farming` (default `true`) controls villager harvest, replant, and pickup independently. Set `mob_griefing=false` to stop creepers/endermen damaging the world while villagers keep farming.
- **Atomic harvest + replant** — when a farmer breaks a mature crop, the seedling goes back in the same tick. Yields are vanilla loot minus one seed-equivalent (the one that goes back into the ground).
- **Five extended crops** — sugar cane, nether wart, pumpkin, melon, and cocoa. Gated by a separate gamerule `villagerfarm:villager_extended_farming` (default `true`). Pumpkins and melons are only harvested when stem-grown (verified via [blockorigin](https://github.com/JackOtsig/blockorigin)) so player-placed decorations are left alone. Sugar cane harvest leaves the bottom stalk so the column regrows.
- **Food sharing + breeding integration** — pumpkin (food value 4), melon slice / sugar cane / cocoa beans (food value 1) all count toward villager breeding readiness. Custom share path lets farmers throw any of these to neighbors as soon as they have ≥2 in a slot, so the new crops actually circulate.
- **Pickup status effects** — villagers picking up sugar cane gain Speed I (+10s per cane, additive). Cocoa beans give Regeneration I (+5s per bean). A cleric picking up nether wart rolls one of 11 villager-safe positive effects for 30 seconds.
- **Farmer anti-trample** — farmer-profession villagers stop wrecking their own farmland by jumping. Other entities still trample normally.
- **Wider AI scan** — vanilla's farmer brain only scans ±1 around the villager. The mod adds a ±2 outer-shell scan for extended crops (so cocoa stacked on jungle logs and similar setups actually get noticed). Includes a fallback that populates `SECONDARY_JOB_SITE` for soul-sand and jungle-log farms; without it, the brain framework would refuse to even consider the farming task.
- **WTHIT + Jade tooltip integration** — hovering a villager shows their current activity (Working / Resting / Meeting / Idle / etc.) plus their inventory contents. Optional; works with either tooltip mod, or none.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.2 and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.11.
2. Download [`blockorigin`](https://github.com/JackOtsig/blockorigin/releases) (required runtime dep — provides the cause-tracking that makes the stem-grown pumpkin/melon detection work).
3. Download `villagerfarm-x.y.z.jar` from the [releases page](../../releases) and drop it into your `mods/` folder alongside `blockorigin`.
4. Optional: install [WTHIT](https://modrinth.com/mod/wthit) or [Jade](https://modrinth.com/mod/jade) to get the villager tooltip overlay.

## Gamerules

- `villagerfarm:villager_farming` (boolean, default `true`) — master switch. Replaces `mob_griefing` for the entire farming behavior. When this is `false`, villagers don't harvest, replant, or pick up anything (regardless of `mob_griefing`).
- `villagerfarm:villager_extended_farming` (boolean, default `true`) — layered under `villager_farming`. Set to `false` to keep the vanilla 4 crops working but disable the extended five.

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
