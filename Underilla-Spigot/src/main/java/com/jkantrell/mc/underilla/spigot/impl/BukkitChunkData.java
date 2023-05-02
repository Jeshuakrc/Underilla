package com.jkantrell.mc.underilla.spigot.impl;

import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.HeightMap;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftChunkData;
import java.util.Map;

public class BukkitChunkData implements ChunkData {

    //ASSETS
    private static final Map<HeightMap, Heightmap.Types> HEIGHT_MAP_TYPES_MAP = Map.of(
            HeightMap.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING,
            HeightMap.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            HeightMap.OCEAN_FLOOR, Heightmap.Types.OCEAN_FLOOR,
            HeightMap.OCEAN_FLOOR_WG, Heightmap.Types.OCEAN_FLOOR_WG,
            HeightMap.WORLD_SURFACE, Heightmap.Types.WORLD_SURFACE,
            HeightMap.WORLD_SURFACE_WG, Heightmap.Types.WORLD_SURFACE_WG
    );


    //FIELDS
    private CraftChunkData internal_;


    //CONSTRUCTORS
    public BukkitChunkData(CraftChunkData delegate) {
        this.internal_ = delegate;
    }


    @Override
    public int getMinHeight() {
        return this.internal_.getMinHeight();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        BlockData data = this.internal_.getBlockData(x, y, z);
        return new BukkitBlock(data);
    }

    @Override
    public int getMaxHeight() {
        return this.internal_.getMaxHeight();
    }

    @Override
    public com.jkantrell.mc.underilla.core.api.Biome getBiome(int x, int y, int z) {
        Biome b = this.internal_.getBiome(x, y, z);
        return new BukkitBiome(b);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) { return; }
        this.internal_.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, bukkitBlock.getBlockData());
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) { return; }
        this.internal_.setBlock(x, y, z, bukkitBlock.getBlockData());
    }

    @Override
    public void setBiome(int x, int y, int z, com.jkantrell.mc.underilla.core.api.Biome biome) {
        if (!(biome instanceof BukkitBiome bukkitBiome)) { return; }
        ChunkAccess access = this.internal_.getHandle();
        access.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(access.biomeRegistry ,bukkitBiome.getBiome()));
    }
}
