<div align="center">
  
<a href="https://modrinth.com/mod/emerald-pouch/settings/versions?l=neoforge"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/cozy/supported/neoforge_64h.png" alt="Available for NeoForge"></a>
<a href="https://modrinth.com/mod/emerald-pouch/settings/versions?l=forge"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/cozy/supported/forge_64h.png" alt="Available for Forge"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/toucanlib"><img src="https://raw.githubusercontent.com/JevenDev/toucanLib/refs/heads/1.21.1/docs/badges/toucanlib_toucanlib_cozy_64h.png" alt="Requires toucanLib"></a>
<br>
<a href="https://modrinth.com/mod/emerald-pouch" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/modrinth_46h.png" alt="Available on Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/emerald-pouch" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/curseforge_46h.png" alt="Available on CurseForge"></a>
<a href="https://github.com/JevenDev/Emerald-Pouch" target="_blank" rel="noopener noreferrer"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/refs/heads/v3/assets/compact-minimal/available/github_46h.png" alt="Available on GitHub"></a>
  
</div>

![All large bundle colours in a banner](https://cdn.modrinth.com/data/cached_images/40f7e6fc57f02cf9bed765f5b98cb66385128109.png)

![Banner image, says "Emerald Pouch"](https://cdn.modrinth.com/data/cached_images/2dc4c89f856b52119a5b50dc1c943890d355f1e3.png)

<div align="center">
  <p><em>A clean way to store and manage your emeralds on the go.</em></p>
</div>

Emerald Pouch adds two tiers of **emerald storage pouches** to help keep your inventory tidy. In many modpacks, loot tables can flood players with emeralds, and that clutter adds up fast.

The mod is designed to be simple, practical, and easy to drop into any pack.

- **Store large amounts of emeralds** inside a dedicated pouch
- Keep your inventory cleaner while still carrying your currency with you
- **Open the first pouch in your inventory** with a keybind for quick access
- **Right-click a pouch in hand** to open it directly
- Optionally **compact emeralds automatically** as they enter the pouch
- When auto-pickup is enabled, emeralds will **prioritize filling the first pouch found**, then continue to the next one
- **Trade directly with villagers** using emeralds stored inside your pouch(es)

While Emerald Pouch takes some visual inspiration from vanilla bundles, it is not meant to be restricted by the same design. Each slot functions as a proper emerald storage slot and can hold **full stacks**, making the mod especially useful in packs where emeralds are much more common.

Realistically, you will probably only need one pouch in a normal playthrough, but nothing is stopping you from filling your inventory with them anyway. Or collecting one of every colour just because you can :D

![All large (open) bundle colours in a banner](https://cdn.modrinth.com/data/cached_images/a62ad3f843a5eb9fdfda095835491cd6c0064616.png)

![features](https://cdn.modrinth.com/data/cached_images/ec0e4dc78ec1a652eb11b233dd2926f7461fe770.png)

<div align="center">
  <img
    src="https://i.imgur.com/OPJqOnh.gif"
    alt='emeralds being automatically stored by the pouch "auto pick-up" feature'
    width="49%"
  />
  <img
    src="https://i.imgur.com/d9VySYx.gif"
    alt='emeralds being automatically compacted by the pouch "auto compact" feature'
    width="49%"
  />
</div>

## Features

Current functionality includes:

- Dedicated emerald pouch items in multiple colour variants, with two different sizes
- Custom pouch container screens
- Right-click in either hand to open
- Inventory keybind access with `N` by default
- Automatic emerald intake toggle
- Optional instant compaction toggle
- Sequential pouch filling when multiple pouches are present
- Full villager trading support with pouch-backed emerald payment
- Automatic emerald block breakdown during trades when needed
- Automatic return of unused pouch-sourced emeralds when closing the merchant screen

Trading is fully supported. When using the villager trading screen, selecting a trade will automatically pull emeralds from your pouch(es) into the payment slots as needed. If there are not enough loose emeralds available, stored emerald blocks can be broken down automatically to complete the cost.

A normal click will perform a single trade and fill the emerald slots once. **Shift-clicking the result** will perform the trade and then automatically refill the emerald slots from your pouch, making it easy to repeat the same trade continuously.

When the merchant screen is closed, any leftover emeralds that originally came from a pouch are deposited back into it automatically. A trade session tracks how many emeralds came from your pouches versus your regular inventory, so only pouch-sourced emeralds are returned.

When choosing where emeralds are pulled from, Emerald Pouch prioritizes **equipped pouches first** (Accessories/Curios belt slots), then **offhand**, then **inventory pouches**. Loose emeralds are always used before emerald blocks are broken down.

<div align="center">
  <img
    src="https://cdn.modrinth.com/data/cached_images/286fc94461e6bd805c682f566c5f5bc3f78adff1.gif"
    alt='villager trading pulls emeralds from the pouch directly'
    width="49%"
  />
  <img
    src="https://cdn.modrinth.com/data/cached_images/272e734c6fded4ad38d6c6c7076b61e10d6c081f.gif"
    alt='villager trading shift-clicking auto-refills the emerald slot'
    width="49%"
  />
</div>
<br>

![keybinds](https://cdn.modrinth.com/data/cached_images/201d5ce49ba16974e3c3b0b562c392e03f38e35f.png)

![All small bundle colours in a banner](https://cdn.modrinth.com/data/cached_images/7c92dd7caa93f90bc4d06ce26759bf54387e4638.png)

## Default keybinds

- **Open Emerald Pouch** - `N`
  - Opens the **first pouch found** in your inventory
  - If [Accessories](https://modrinth.com/mod/accessories) or [Curios](https://modrinth.com/mod/curios) is installed, it will prioritize an equipped belt-slot pouch first

## Controls

- **Right-click with pouch in hand**
  - Opens that pouch directly
- **Shift + Right-click with pouch in hand**
  - Swaps/equips it into the belt slot (if Accessories or Curios is installed)

From the pouch screen, you can manage stored emeralds through a container UI.

![All small (open) bundle colours in a banner](https://cdn.modrinth.com/data/cached_images/abfabb71649ac33ea31818592a1cb65d11c4021f.png)

![compatibility](https://cdn.modrinth.com/data/cached_images/1252c11050b7daf8b8621712b58dd1005e7ba982.png)

## Compatibility

Emerald Pouch currently includes built-in compatibility with:

- **[Accessories](https://modrinth.com/mod/accessories)**
  - Supports equipping pouches in the belt slot
  - The default `N` keybind will prioritize opening the equipped belt-slot pouch first
  - Villager trading will prioritize equipped belt-slot pouches before other pouch locations
  - **Shift + Right-click** can quickly equip a pouch into the belt slot

- **[Curios](https://modrinth.com/mod/curios)**
  - Supports equipping pouches in the belt slot
  - The default `N` keybind will prioritize opening the equipped belt-slot pouch first
  - Villager trading will prioritize equipped belt-slot pouches before other pouch locations
  - **Shift + Right-click** can quickly equip a pouch into the belt slot

- **[ShulkerBoxTooltip](https://modrinth.com/mod/shulkerboxtooltip)**
  - Allows pouch contents to be previewed directly in the tooltip, similar to other container-style items

If there is a specific mod you would like compatibility support for, feel free to open an issue on the [GitHub](https://github.com/JevenDev/Emerald-Pouch/issues) repo.

![roadmap](https://cdn.modrinth.com/data/cached_images/04825ea0e2e5462ffa075e783ca38b0c63a36d34.png)

## Version and Loader

- ✅ **NeoForge 1.21.1** [Active development]
- ⛔ **NeoForge 1.20.1** [Not planned]
- ⛔ **Forge 1.21.1** [Not planned]
- ✅ **Forge 1.20.1** [Active development]
- 🚧 **Fabric 1.21.1** [Planned port]
- 🚧 **Fabric 1.20.1** [Planned port]

## Planned Features

- Additional polish for the pouch container UI, including a more custom container texture
- Expanded configuration options, including the ability to move HUD/inventory elements to a custom location
- Broader compatibility support where needed

![credits & license](https://cdn.modrinth.com/data/cached_images/5fd3ad80e342e6985dd6ebda1f7afd9c48749fce.png)

## Credits

A huge thank you to **[BigWander](https://linktr.ee/BigWanderPixelArt)** for the original pouch pixel art that helped inspire and support Emerald Pouch.

The pouch textures used in this mod are **modified versions** of assets from their asset pack **"[Travellers pouch](https://bigwander.itch.io/travellers-pouch)"**. Please go show them some love and check out their work! :D

## License

This project is licensed under the **[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)**.

Feel free to use this mod in modpacks, videos, etc. Just provide a link back to this page if possible :)

Looking to port the mod to your favourite loader/version outside of my scope? Feel free to, and let me know so I can add a sub-section to direct users to it!

For any general queries/unlisted questions, DM me on Twitter (@prodbyjvn) / Discord (ijvn).

<div align="center">
  
  <p><strong>⚠ <em>This mod ONLY exists on Modrinth & CurseForge as of April 2026. Any sites hosting this mod outside of Modrinth/CurseForge are not official releases.</em> ⚠</strong></p>
  
</div>
