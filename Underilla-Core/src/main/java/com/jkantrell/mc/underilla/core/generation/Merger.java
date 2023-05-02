package com.jkantrell.mc.underilla.core.generation;

import com.jkantrell.mc.underilla.core.api.ChunkData;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;

interface Merger {

    void merge(ChunkReader reader, ChunkData chunkData);
    void mergeLand(ChunkReader reader, ChunkData chunkData);
    void mergeBiomes(ChunkReader reader, ChunkData chunkData);
}
