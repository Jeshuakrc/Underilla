package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.Terraformer;
import com.dotkntrell.mc.terraformer.io.Config;
import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import com.dotkntrell.mc.terraformer.listener.VectorIterable;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Random;

public class TerraformerChunkGenerator extends ChunkGenerator {

    //FIELDS
    private final WorldReader worldReader_;
    private final static Config CONFIG = Terraformer.CONFIG;

    public TerraformerChunkGenerator(WorldReader worldReader) {
        this.worldReader_ = worldReader;
    }


    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        return super.getBaseHeight(worldInfo,random,x,z,heightMap);
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        Optional<ChunkReader> reader = this.worldReader_.readChunk(chunkX, chunkZ);
        if (reader.isEmpty()) { return; }

        Bukkit.getLogger().info("Generating chunk [" + chunkX + " - " + chunkZ + "] from " + this.worldReader_.getWorldName() + ".");
        VectorIterable iterable = new VectorIterable(
                0, 16,
                chunkData.getMinHeight(), chunkData.getMaxHeight(),
                0, 16
        );
        for (Vector v : iterable) {
            Material material = reader.get().materialAt(v.getBlockX(),v.getBlockY(),v.getBlockZ());
            chunkData.setBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ(), material);
        }
    }

    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        if ( CONFIG.yMergeEnabled && CONFIG.yMergeHeight > worldInfo.getMinHeight() ) { return true; }
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
