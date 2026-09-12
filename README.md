# BetterFood

BetterFood automatically eats ordinary food from a player's inventory when hunger falls far enough to use it. Players can toggle automatic eating and its action-bar notifications independently. Both preferences persist on the player's data.

[Download](https://nightbreak.io/plugin/betterfood/) · [Modrinth](https://modrinth.com/plugin/betterfood-magmaguy) · [Support](https://discord.gg/nightbreak)

## Installation

Use Java 21 or the newer Java version required by your server and a compatible Spigot/Paper server. The plugin declares Minecraft API version `1.21.4`; check the download page for release compatibility.

Put `BetterFood.jar` in `plugins/` and restart the server. No additional plugin or resource pack is required. MagmaCore is bundled, and BetterFood has no content packs to install.

Automatic eating is enabled by default for players with `betterfood.user`. Carry supported food and let hunger decrease through normal play. BetterFood consumes one suitable item during the hunger event, restores hunger and saturation, and returns the bowl when consuming supported soups or stews.

## Food selection

BetterFood chooses supported food that fits the hunger deficit. It skips modified items, including renamed food, lore-bearing items, and items carrying custom data. It respects cancelled hunger events.

Supported foods include cooked meats and fish, bread, baked potatoes, carrots, golden carrots, apples, beetroot, berries, melon, cookies, dried kelp, pumpkin pie, potatoes, and ordinary soups and stews. Raw meat and fish and foods with special effects are deliberately excluded. The complete list is in [Food.java](src/main/java/com/magmaguy/betterfood/Food.java).

## Commands and permissions

Use `/betterfood` or its alias `/bf`.

| Command | Purpose |
| --- | --- |
| `/bf` | Toggle automatic eating for yourself. |
| `/bf toggleEating` | Explicit automatic-eating toggle. |
| `/bf toggleMessage` | Toggle food-consumption notifications. |
| `/bf setup` | Open administration and update settings. |
| `/bf downloadpluginupdate` | Check for and download a plugin update. |
| `/bf recommendedplugins` | Open the Nightbreak plugin catalog. |

`betterfood.user` defaults to everyone and controls eating and personal preferences. `betterfood.admin` defaults to operators and controls setup and updates. Personal toggles must be used in-game.

## Troubleshooting

If food is not consumed, check the player's permission and toggle, the food type, and whether the stack has custom metadata. Hunger must actually decrease; eating is not a timer that constantly refills the bar. Other plugins can cancel hunger changes.

For support, include the server and plugin versions, the food type, whether it is an ordinary unmodified stack, and the steps that reproduce the problem.

## Building from source

Use JDK 21 and the included Gradle wrapper:

```powershell
.\gradlew.bat shadowJar
```

On Unix, use `./gradlew shadowJar`. The deployable file is `build/libs/BetterFood.jar`. `MC_DIST_DIR` optionally selects a shared output directory. Publish a changed MagmaCore dependency to Maven Local before rebuilding.

## Developer API

[Java class and method reference](https://wiki.nightbreak.io/javadoc/betterfood/index.html).

Integration scope and supported behavior: [BetterFood developer reference](https://wiki.nightbreak.io/developers#betterfood). See the [Java API index](https://wiki.nightbreak.io/developers) for dependency setup and lifecycle guidance.

Maven: `com.magmaguy:BetterFood:1.5.1` from [MagmaGuy's repository](https://repo.magmaguy.com/#/releases). Use `provided` or `compileOnly` scope for the installed plugin.
