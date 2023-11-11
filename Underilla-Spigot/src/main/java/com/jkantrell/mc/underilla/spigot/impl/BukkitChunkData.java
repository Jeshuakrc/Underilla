package com.jkantrell.mc.underilla.spigot.impl;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.generator.CraftChunkData;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import net.minecraft.core.Holder;

public class BukkitChunkData implements ChunkData {

    // ASSETS
    // private static final Map<HeightMap, Heightmap.Types> HEIGHT_MAP_TYPES_MAP = Map.of(HeightMap.MOTION_BLOCKING,
    // Heightmap.Types.MOTION_BLOCKING, HeightMap.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    // HeightMap.OCEAN_FLOOR, Heightmap.Types.OCEAN_FLOOR, HeightMap.OCEAN_FLOOR_WG, Heightmap.Types.OCEAN_FLOOR_WG,
    // HeightMap.WORLD_SURFACE, Heightmap.Types.WORLD_SURFACE, HeightMap.WORLD_SURFACE_WG, Heightmap.Types.WORLD_SURFACE_WG);


    // FIELDS
    private CraftChunkData internal_;
    private org.bukkit.generator.ChunkGenerator.ChunkData chunkData;
    private static final Map<Biome, Holder.Reference<net.minecraft.world.level.biome.Biome>> biomeCache = new HashMap<>();
    private static final World world = Bukkit.getWorld("world");


    // CONSTRUCTORS
    // public BukkitChunkData(CraftChunkData delegate) { this.internal_ = delegate; }
    public BukkitChunkData(org.bukkit.generator.ChunkGenerator.ChunkData chunkData) {
        internal_ = (CraftChunkData) chunkData;
        this.chunkData = chunkData;
    }


    @Override
    public int getMinHeight() { return this.chunkData.getMinHeight(); }

    @Override
    public Block getBlock(int x, int y, int z) {
        BlockData data = this.chunkData.getBlockData(x, y, z);
        return new BukkitBlock(data);
    }

    @Override
    public int getMaxHeight() { return this.chunkData.getMaxHeight(); }

    @Override
    public com.jkantrell.mc.underilla.core.api.Biome getBiome(int x, int y, int z) {
        Biome b = this.chunkData.getBiome(x, y, z);
        return new BukkitBiome(b);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            return;
        }
        this.chunkData.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, bukkitBlock.getBlockData());
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            return;
        }
        this.chunkData.setBlock(x, y, z, bukkitBlock.getBlockData());
    }

    @Override
    public void setBiome(int x, int y, int z, com.jkantrell.mc.underilla.core.api.Biome biome) {
        // No need to set biome for chunk. It's done by the generator.

        // if (!(biome instanceof BukkitBiome bukkitBiome)) {
        // return;
        // }
        // Chunk chunk = world.getChunkAt(x, z);
        // ((CraftChunk) chunk).getHandle().getBiomeIndex();

        // System.out.println("Setting biome for column [" + x + ", " + y + ", " + z + "]: " + bukkitBiome.getBiome());
        // this.internal_.getBlockData(x, y, z)
        // set biome for full column
        // for (y = -64; y < 320; y++) {
        // Isn't set instantly
        // world.setBiome(x, y, z, bukkitBiome.getBiome());
        // }

        // // TODO to test. It have been changed for 1.20.2 (Save in map may cause issue)
        // // access.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(access.biomeRegistry, bukkitBiome.getBiome()));
        // System.out.println("Setting biome for block [" + x + ", " + y + ", " + z + "]: " + bukkitBiome.getBiome());
        // System.out.println(this.internal_.getClass());
        // // list method of internal_
        // for (java.lang.reflect.Method m : this.internal_.getClass().getMethods()) {
        // System.out.println(m.toString());
        // }

        // ChunkAccess access = this.internal_.getHandle();
        // if (!biomeCache.containsKey(bukkitBiome.getBiome())) { // if biome isn't in cache yet: add it
        // // Next line is from https://github.com/FreeSoccerHDX/AdvancedWorldCreatorAPI
        // Optional<Holder.Reference<net.minecraft.world.level.biome.Biome>> optional = access.biomeRegistry
        // .getHolder(ResourceKey.create(Registries.BIOME, CraftNamespacedKey.toMinecraft(bukkitBiome.getBiome().getKey())));
        // if (optional.isPresent()) {
        // biomeCache.put(bukkitBiome.getBiome(), optional.get());
        // } else {
        // Bukkit.getLogger().warning("Biome not found in " + x + " " + y + " " + z + ": " + bukkitBiome.getBiome().getKey());
        // return;
        // }
        // }
        // add biome from biome cache map.
        // access.setBiome(x, y, z, biomeCache.get(bukkitBiome.getBiome()));


        // if (!biomeCache.containsKey(bukkitBiome.getBiome())) { // if biome isn't in cache yet: add it
        // // Next line is from https://github.com/FreeSoccerHDX/AdvancedWorldCreatorAPI
        // Optional<Holder.Reference<net.minecraft.world.level.biome.Biome>> optional = this.internal_.getHandle().biomeRegistry
        // .getHolder(ResourceKey.create(Registries.BIOME, CraftNamespacedKey.toMinecraft(bukkitBiome.getBiome().getKey())));
        // if (optional.isPresent()) {
        // biomeCache.put(bukkitBiome.getBiome(), optional.get());
        // } else {
        // Bukkit.getLogger().warning("Biome not found in " + x + " " + y + " " + z + ": " + bukkitBiome.getBiome().getKey());
        // return;
        // }
        // }
        // // add biome from biome cache map.
        // this.internal_.getHandle().setBiome(x, y, z, biomeCache.get(bukkitBiome.getBiome()));
    }

    // @Override
    // public void setBiomes(com.jkantrell.mc.underilla.core.api.Biome[] biomes, int chunkX, int chunkZ) {
    // if (chunkX == -357 && chunkZ == -1653) {
    // System.out.println("Setting biomes for chunk [" + chunkX + ", " + chunkZ + "]: " + Arrays.toString(biomes));
    // }
    // new BukkitRunnable() { // work but create lag and don't influence caves generation.
    // @Override
    // public void run() {
    // Chunk chunk = Bukkit.getWorld("world").getChunkAt(chunkX, chunkZ);
    // for (int z = 0; z < 16; z++) {
    // for (int x = 0; x < 16; x++) {
    // Biome biome = ((BukkitBiome) biomes[z * 16 + x]).getBiome();
    // for (int y = -64; y < 320; y++) {
    // chunk.getBlock(x, y, z).setBiome(biome);
    // if (chunkX == -357 && chunkZ == -1653) {
    // System.out.println("Setting biome for block [" + x + ", " + y + ", " + z + "]: " + biome);
    // }
    // }
    // }
    // }
    // cancel();
    // }
    // }.runTaskTimer(Underilla.getPlugin(), 10, 10);
    // }

}
