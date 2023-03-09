package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import com.dotkntrell.mc.terraformer.utils.coordinate.Coordinate;
import com.dotkntrell.mc.terraformer.utils.coordinate.CoordinateIterable;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import java.util.Random;

public class TerraformerChunkGenerator extends ChunkGenerator {

    //FIELDS
    private final WorldReader worldReader_;

    public TerraformerChunkGenerator(WorldReader worldReader) {
        this.worldReader_ = worldReader;
    }


    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        return super.getBaseHeight(worldInfo,random,x,z,heightMap);
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (this.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) { return; }

        Bukkit.getLogger().info("Generating chunk [" + chunkX + " - " + chunkZ + "] from " + this.worldReader_.getWorldName() + ".");
        CoordinateIterable iterable = new CoordinateIterable(
                0, 16,
                chunkData.getMinHeight(), chunkData.getMaxHeight(),
                0, 16
        );
        ChunkReader chunkReader = this.worldReader_.readChunk(chunkX, chunkZ).get();
        for (Coordinate c : iterable) {
            Material material = chunkReader.materialAt(c.x(),c.y(),c.z());
            chunkData.setBlock(c.x(), c.y(), c.z(), material);
        }
    }

    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return this.worldReader_.readChunk(chunkX, chunkZ).isEmpty();
    }


    @Override
    public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return this.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ);
    }


    @Override
    public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return true;
    }
}
