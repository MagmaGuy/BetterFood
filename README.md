<p align="center">
  <a href="https://discord.gg/GKyzTgT">
    <img src="https://imgur.com/mvyJwOf.png" alt="Discord" width="180"/>
  </a>
  <a href="https://nightbreak.io/plugin/betterfood/">
    <img src="https://imgur.com/OLYJlM8.png" alt="Nightbreak" width="180"/>
  </a>
  <a href="https://www.patreon.com/magmaguy">
    <img src="https://imgur.com/rWCe9W9.png" alt="Patreon" width="180"/>
  </a>
  <a href="https://wiki.nightbreak.io/BetterFood/info">
    <img src="https://imgur.com/M48vTUC.png" alt="Wiki" width="180"/>
  </a>
</p>

<div align="center">
  <iframe width="560" height="315" src="https://www.youtube.com/embed/Ev2KGfB3DDw?si=eLTrkWeibpnHy8F6" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>
</div>

# 🟠 How it Works:
A very small and simple as can be plugin that automatically makes players eat cooked and crafted food from their inventories.

The list of valid items is as follows:
- Dried kelp
- Beetroot
- Potatoes
- Cookies
- Melon slices
- Sweet berries
- Glow berries
- Carrots
- Apples
- Baked potatoes
- Bread
- Cooked cod
- Cooked rabbit
- Beetroot soup
- Cooked chicken
- Cooked mutton
- Cooked salmon
- Golden carrots
- Mushroom stew
- Cooked porkchop
- Pumpkin pie
- Cooked beef
- Rabbit stew

BetterFood also:
- Picks food that fits how hungry you are instead of always eating the most filling food first. [Selection rules](https://wiki.nightbreak.io/BetterFood/behavior#selection-rules)
- Skips renamed, enchanted or otherwise customized food, so custom items from other plugins are left alone. [Protected items](https://wiki.nightbreak.io/BetterFood/behavior#protected-items)
- Gives the bowl back after soups and stews. [Bowl foods](https://wiki.nightbreak.io/BetterFood/behavior#bowl-foods)
- Remembers each player's eating and message toggles across restarts.

# 🟠 Why?
Honestly I never liked how Mojang implemented the food system. It constantly takes players out of what they're doing in order to fill up a meaningless bar. I do understand that food is important for a survival game, but I think holding down right click for a few seconds every couple of minutes does not make for a compelling game mechanic.

# 🟠 What about other foods?
Raw foods and foods that give buffs are best left to manage by the player. I would not want players to waste raw food they're harvesting for cooking, nor am I making this plugin to make people lazier about food. If people are willing to put the effort in to cook or craft their food, they get to have it refill automatically. Sounds like a good balance, right?

# 🟠 How to Setup:
1. Put the BetterFood.jar into your server and restart your server
2. You're done!


# 🟠 Permissions:
- `betterfood.user` - plugin only applies to players with this permission (true by default).
- `betterfood.admin` - lets admins use the setup menu and plugin update commands (op by default).

Commands (`/bf` works too):
- `/betterfood toggleEating` - Toggles automatic eating for the player. `/betterfood` on its own does the same.
- `/betterfood toggleMessage` - Toggles eating messages for the player
- `/betterfood setup` - Opens the admin menu with the plugin update controls
- `/betterfood applypluginupdate` - Applies a downloaded plugin update without restarting the server

More on the [wiki](https://wiki.nightbreak.io/BetterFood/info).


# 🟠 View More by Magmaguy!

<p align="center">
  <a href="https://nightbreak.io/plugin/elitemobs">
    <img src="https://i.postimg.cc/zXQRn1jD/Elite-Mobs.png" width="300"/>
  </a>
  <a href="https://nightbreak.io/plugin/betterstructures">
    <img src="https://i.postimg.cc/3rVK1rPq/Better-Structures.png" width="300"/>
  </a>
</p>
<p align="center">
  <a href="https://nightbreak.io/plugin/extractioncraft">
    <img src="https://i.postimg.cc/gjV6VMYz/extractioncraft.png" width="300"/>
  </a>
  <a href="https://nightbreak.io/plugin/eternaltd">
    <img src="https://i.postimg.cc/Kjf8cCGM/ETD.png" width="300"/>
  </a>
</p>

<p align="center">
  <a href="https://nightbreak.io/plugin/resourcepackmanager">
    <img src="https://i.postimg.cc/Y0GdtBfX/Resource-Pack-Manager.png" width="300"/>
  </a>
  <a href="https://nightbreak.io/plugin/freeminecraftmodels">
    <img src="https://i.postimg.cc/0Qxv6ZHd/FMM.png" width="300"/>
  </a>
</p>
<p align="center">
  <a href="https://nightbreak.io/plugin/betterfood">
    <img src="https://i.postimg.cc/JzcDSB80/Better-Food.png" width="300"/>
  </a>
  <a href="https://nightbreak.io/plugin/resurrectionchest">
    <img src="https://i.imgur.com/qpgXCge.png" width="300"/>
  </a>
</p>

# 🟠 Partner servers:

**Matecraft is the official partner server!** It is run by me and is always **up-to-date with the latest experimental features and new dungeons**! Check it out if you want to see my plugins the way they were originally developed to work! It's got a cozy community of dedicated and welcoming players that call it home!

<p align="center">
  <strong>elitesurvival.matecraft.org</strong><br/>
  <img src="https://i.postimg.cc/wBhbQ8vs/header.gif"/>
</p>

# 🟠 Developers

- [Java API guide](https://wiki.nightbreak.io/developers#betterfood): integration scope (BetterFood has no custom event API)
- [Generated Java reference](https://wiki.nightbreak.io/javadoc/betterfood/index.html)
- [Maven repository and coordinates](https://wiki.nightbreak.io/developers)
- [Source code](https://github.com/MagmaGuy/BetterFood)
