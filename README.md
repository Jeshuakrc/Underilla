# Underilla for Spigot
Underilla is a Bukkit / Spigot based plugin for Minecraft Servers to 'merge' existing custom Minecraft word surfaces and vanilla undergrounds. It works by allowing the vanilla generation engine create chunks as normal, then intercepting the generator and forcing the surface of the original world, which works as a reference. In oder worlds, Underilla generates a brand-new world with vanilla undergrounds, but cloning the surface of an already existing world.

It's original purpose is adding vanilla caves to custom [WorldPainter](https://www.worldpainter.net/) worlds, but it would perfectly work for any pre-generated world.

## Main features
- Three merging strategies:
    - **None:** No actual vanilla underground noise is generated. `generate_noodle_caves` setting can still be on, to generate noodle caves.
    - **Absolute:** A Y coordinate value divides the original world surface and vanilla underground.
    - **Relative:** This is the cool one. The reference world's surface will be dynamically carved into vanilla underground; which means there's no actual height-based division.
- Custom caves also supported. If using Relative merge strategy, every non-solid block and surroundings will be preserved, thus, if the reference world has itself an underground system, it'll be transferred over to the merged world.
- Heightmap fixed. Underilla re-calculates heightmaps when merging chunks, getting rid of floating and buried structures. Vanilla villagers and other structures are placed at the right height.
- Biome overwrite. Biomes from the reference world will be transferred and overwrite biomes from de vanilla seed being used. Cave biomes underground will be preserved.

## Getting started
### Perquisites

- Java 17.
- A pre-generated world to use as a reference (Such as a WorldPainter world).
- A Spigot (or forks) Minecraft Server of version 1.20.2.

### Single player or non-Bukkit
Underilla is currently only implemented as a Spigot plugin, so it runs only on Spigot servers. If you have a Vanilla, Forge or non Bukkit-based server; or looking for a single player experience; you may [use a local Spigot server](https://www.spigotmc.org/wiki/spigot-installation/) to pre-generate a fully-merged world and then copy the resulting world folder to your actual `saves` folder.

### Installation

1. Set up your Spigot server.
2. Download Underilla's `.jar`.
3. Place Underilla's `.jar` file into the `./plugins` directory of your server. Create the folder if it doesn't exist already.
4. Into the `./plugins` folder, create a new folder called `Underilla` and place a `config.yml` file in it. You may get the file from [this repo](Underilla-Spigot/src/main/resources/config.yml).
5. Open the `bukkit.yml`file in your server's root and add the following lines on top:
   ```
   worlds:
     world:
       generator: Underilla
   ```
   This will tell Spigot to use Underilla's chunk generator.
6. In your server's root, create a new folder called `world_base`.
7. From the folder of your reference world, copy the `region` folder, and paste it into the `world_base` folder you just created.
8. If existing, delete the `world` folder fom your server's root.
9. (Optional) Open the `server.properties` file in your server's root, and tweak the `level-seed` property. This has a direct impact on the generated underground.
10. Run the server.
    You'll notice Underilla generating merged chunks during world creation.

**Important:** Make sure your server's main world is still set to `world`. Aside from this plugin, the server itself doesn't need to "know" about the reference world.

### Pregenerate
Underilla is significantly slower than the vanilla generator, as it doesn't relly on noise generation but on reading the reference world's region `nbt` files and analyzing its patterns to 'clone' its surface to a vanilla world. So, if your world is intended for heavy duty in a big server. It's recommended to pre-generate the whole reference world area with a chunk generator plugin, such as [Chunky](https://www.spigotmc.org/resources/chunky.81534/). I'm planning adding a build-in pre-generation system in the future.

## Known issues

- Underilla's generation disables Minecraft's chunk blender, which means there will be sharp old-school chunk borders at the edge of the reference world's chunks. This may be tackled by trimming your custom world chunks around the edges to generate blended chunks ahead of time.
- Due to Spigot's generation API, outside the reference world's area, heightmaps are broken, which has an impact on structures. You may work around this by pre-generating the whole reference world area, and then disabling Underilla.
- NMS is used for biome placing, so this plugin isn't cross-version-compatible. A new release per every Minecraft release, and thus, this plugin won't work for any new prior-1.19.4 version.
- Little underground lava and water pockets will translate to odd floating blobs in the final world if they overlap with large caves. Avoid such generation patterns.
- Minecraft vanilla generator places structures based on the seed, not the actual biomes. This results in structures sometimes in totally unrelated biomes. Shipwrecks, monuments and other ocean structures are the most noticeable. To work around this, you may blacklist structures as you wish in the config file, and spawn them manually using `/place`.

## WorldPainter considerations
If you're going to plug your custom WorldPainter world into Underilla, consider before exporting:
- Disable caves, caverns, and chasms altogether, allow Underilla to take over that step. This is due to biome placement, every underground non-solid block in the reference_world drags its biome over along with it, this interferes with proper underground vanilla biomes.
- Always disable the `Allow Minecraft to populate the entire terrain` option. Rather use the `vanilla_population` option in Underilla's `config.yml` file.
- Don't user the resource layer. Underilla will have the vanilla generator take care of that for you.
- The Populate layer has no effect. Weather all or none of the terrain will be populated based on the above point.
- If you have custom cave/tunnels layers and want to preserve them during the merge, you'd want to use the Relative merge strategy


## Build
Create working jar with `./gradlew buildDependents`