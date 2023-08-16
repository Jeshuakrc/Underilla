package com.jkantrell.mc.underilla.core.generation;

import java.util.List;
import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mc.underilla.core.reader.WorldReader;
import com.jkantrell.mc.underilla.core.vector.Vector;
import com.jkantrell.mc.underilla.core.vector.VectorIterable;
import com.jkantrell.mca.MCAUtil;

class AbsoluteMerger implements Merger {

    //FIELDS
    private final WorldReader worldReader_;
    private final int height_;
    private final List<? extends Biome> preserveBiomes_;


    //CONSTRUCTORS
    AbsoluteMerger(WorldReader worldReader, int height, List<? extends Biome> preserveBiomes) {
        this.worldReader_ = worldReader;
        this.height_ = height;
        this.preserveBiomes_ = preserveBiomes;
    }


    //IMPLEMENTATIONS
    @Override
    public void merge(ChunkReader reader, ChunkData chunkData) {
        this.mergeLand(reader, chunkData);
        this.mergeBiomes(reader, chunkData);
    }
    @Override
    public void mergeLand(ChunkReader reader, ChunkData chunkData) {
        Block airBlock = reader.blockFromTag(MCAUtil.airBlockTag()).get();
        int airColumn = Math.max(reader.airSectionsBottom(), this.height_);
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, airBlock);
        // TODO use preserveBiomes

        VectorIterable iterable = new VectorIterable(0, 16, this.height_, airColumn, 0, 16);
        for (Vector<Integer> v : iterable) {
            Block b = reader.blockAt(v).orElse(airBlock);
            chunkData.setBlock(v, b);
        }
    }
    @Override
    public void mergeBiomes(ChunkReader reader, ChunkData chunkData) {
        VectorIterable iterable = new VectorIterable(0, 4, this.height_ >> 2, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector<Integer> v : iterable) {
            Biome biome = reader.biomeAtCell(v).orElse(null);
            if (biome == null) { continue; }
            chunkData.setBiome(v, biome);
        }
    }
}
