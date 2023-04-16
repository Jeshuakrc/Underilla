package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.util.VectorIterable;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import java.util.Map;
import java.util.function.Predicate;

public class AugmentedChunkData implements ChunkGenerator.ChunkData {

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
    public AugmentedChunkData(CraftChunkData delegate) {
        this.internal_ = delegate;
    }


    @Override
    public int getMinHeight() {
        return this.internal_.getMinHeight();
    }

    @Override
    public int getMaxHeight() {
        return this.internal_.getMaxHeight();
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return this.internal_.getBiome(x, y, z);
    }

    @Override
    public void setBlock(int x, int y, int z, Material material) {
        this.internal_.setBlock(x, y, z, material);
    }

    @Override
    public void setBlock(int x, int y, int z, MaterialData material) {
        this.internal_.setBlock(x, y, z, material);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockData blockData) {
        this.internal_.setBlock(x, y, z, blockData);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {
        this.internal_.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, material);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData material) {
        this.internal_.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, material);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData blockData) {
        this.internal_.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, blockData);
    }

    @Override
    public Material getType(int x, int y, int z) {
        return this.internal_.getType(x, y, z);
    }

    @Override
    public MaterialData getTypeAndData(int x, int y, int z) {
        return this.internal_.getTypeAndData(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return this.internal_.getBlockData(x, y, z);
    }

    @Override
    public byte getData(int x, int y, int z) {
        return this.internal_.getData(x, y, z);
    }

    public void setBiome(int x, int y, int z, Biome biome) {
        ChunkAccess access = this.internal_.getHandle();
        access.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(access.biomeRegistry ,biome));
    }

    public boolean containsMaterial(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Predicate<Material> check) {
        for (Vector v : new VectorIterable(minX, maxX, minY, maxY, minZ, maxZ)) {
            Material m = this.getType(v.getBlockX(), v.getBlockY(), v.getBlockZ());
            if (check.test(m)) { return true; }
        }
        return false;
    }

    public boolean containsMaterial(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Material material) {
        return this.containsMaterial(minX, minY, minZ, maxX, maxY, maxZ, m -> m.equals(material));
    }

    public void setHeightMap(HeightMapWrapper heightMap) {
        ChunkAccess access = this.internal_.getHandle();
        access.setHeightmap(AugmentedChunkData.HEIGHT_MAP_TYPES_MAP.get(heightMap.getType()), heightMap.getLongs());
    }
}
