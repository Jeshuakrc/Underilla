package com.jkantrell.mc.underilla.core.generation;

import com.jkantrell.mc.underilla.core.api.ChunkData;

interface Merger {

    void merge(int chunkX, int chunkZ, ChunkData chunkData);
    void mergeLand(int chunkX, int chunkZ, ChunkData chunkData);
    void mergeBiomes(int chunkX, int chunkZ, ChunkData chunkData);
}
