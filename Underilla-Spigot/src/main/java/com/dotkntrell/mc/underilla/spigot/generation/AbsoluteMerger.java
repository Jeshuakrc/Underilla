package com.dotkntrell.mc.underilla.spigot.generation;

import com.dotkntrell.mc.underilla.spigot.io.reader.ChunkReader;
import com.dotkntrell.mc.underilla.spigot.io.reader.WorldReader;
import com.dotkntrell.mc.underilla.spigot.util.VectorIterable;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;
import java.util.Random;

class AbsoluteMerger implements Merger {

    //FIELDS
    private final WorldReader worldReader_;
    private final int height_;


    //CONSTRUCTORS
    AbsoluteMerger(WorldReader worldReader, int height) {
        this.worldReader_ = worldReader;
        this.height_ = height;
    }


    //OVERWRITES
    @Override
    public void mergeLand(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData) {
        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        int airColumn = Math.max(chunk.airColumnBottomHeight(), this.height_);
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, Material.AIR);

        VectorIterable iterable = new VectorIterable(0, 16, this.height_, airColumn, 0, 16);
        for (Vector v : iterable) {
            BlockData b = chunk.blockAt(v.getBlockX(),v.getBlockY(),v.getBlockZ()).orElse(Material.AIR.createBlockData());
            chunkData.setBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ(), b);
        }
    }

    @Override
    public void mergeBiomes(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, AugmentedChunkData chunkData) {
        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        //Setting
        VectorIterable iterable = new VectorIterable(0, 4, this.height_ >> 2, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector v : iterable) {
            Biome biome = chunk.biomeAtCell(v).orElse(null);
            if (biome == null) { continue; }
            chunkData.setBiome(v.getBlockX(), v.getBlockY(), v.getBlockZ(), biome);
        }
    }


}
