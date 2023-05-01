package com.jkantrell.mc.underilla.core.generation;

import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mc.underilla.core.reader.WorldReader;
import com.jkantrell.mc.underilla.core.util.UnderillaUtils;
import com.jkantrell.mc.underilla.core.vector.Vector;
import com.jkantrell.mc.underilla.core.vector.VectorIterable;

class AbsoluteMerger implements Merger {

    //FIELDS
    private final WorldReader worldReader_;
    private final int height_;


    //CONSTRUCTORS
    AbsoluteMerger(WorldReader worldReader, int height) {
        this.worldReader_ = worldReader;
        this.height_ = height;
    }


    //IMPLEMENTATIONS
    @Override
    public void merge(int chunkX, int chunkZ, ChunkData chunkData) {
        this.mergeLand(chunkX, chunkZ, chunkData);
        this.mergeBiomes(chunkX, chunkZ, chunkData);
    }
    @Override
    public void mergeLand(int chunkX, int chunkZ, ChunkData chunkData) {
        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        Block airBlock = chunk.blockFromTag(UnderillaUtils.airBlockTag()).get();
        int airColumn = Math.max(chunk.airSectionsBottom(), this.height_);
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, airBlock);

        VectorIterable iterable = new VectorIterable(0, 16, this.height_, airColumn, 0, 16);
        for (Vector<Integer> v : iterable) {
            Block b = chunk.blockAt(v.x(),v.y(),v.z()).orElse(airBlock);
            chunkData.setBlock(v.x(), v.y(), v.z(), b);
        }
    }
    @Override
    public void mergeBiomes(int chunkX, int chunkZ, ChunkData chunkData) {
        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        //Setting
        VectorIterable iterable = new VectorIterable(0, 4, this.height_ >> 2, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector<Integer> v : iterable) {
            Biome biome = chunk.biomeAtCell(v).orElse(null);
            if (biome == null) { continue; }
            chunkData.setBiome(v.x(), v.y(), v.z(), biome);
        }
    }


}
