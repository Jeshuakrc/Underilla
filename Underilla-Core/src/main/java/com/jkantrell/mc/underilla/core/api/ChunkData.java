package com.jkantrell.mc.underilla.core.api;

import com.jkantrell.mc.underilla.core.vector.Vector;

public interface ChunkData {

    int getMaxHeight();
    int getMinHeight();
    Block getBlock(int x, int y, int z);
    default Block getBlock(Vector<Integer> vector) {
        return this.getBlock(vector.x(), vector.y(), vector.z());
    }
    Biome getBiome(int x, int y, int z);
    default Biome getBiome(Vector<Integer> vector) {
        return this.getBiome(vector.x(), vector.y(), vector.z());
    }
    void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Block material);
    void setBlock(int x, int y, int z, Block block);
    void setBiome(int x, int y, int z, Biome biome);

}
