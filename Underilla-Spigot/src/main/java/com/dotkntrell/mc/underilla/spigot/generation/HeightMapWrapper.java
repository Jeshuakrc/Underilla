package com.dotkntrell.mc.underilla.spigot.generation;

import com.jkantrell.mca.BinaryMap;
import org.bukkit.HeightMap;

public class HeightMapWrapper {

    //FIELDS
    private final HeightMap type_;
    private final BinaryMap map_;


    //CONSTRUCTORS
    public HeightMapWrapper(HeightMap type) {
        this.type_ = type;
        this.map_ = new BinaryMap(9, 256);
    }


    //GETTERS
    public HeightMap getType() {
        return this.type_;
    }
    public int get(int x, int z) {
        return this.map_.get(HeightMapWrapper.mapIndex(x, z));
    }
    public long[] getLongs() {
        return this.map_.getData();
    }


    //SETTERS
    public int set(int x, int z, int height) {
        int     index = HeightMapWrapper.mapIndex(x, z),
                old = this.map_.get(index);
        this.map_.set(index, height);
        return old;
    }


    //PRIVATE UTIL
    private static int mapIndex(int x, int z) {
        if (x > 15 || z > 15 || x < 0 || z < 0) {
            throw new IndexOutOfBoundsException("Out of bounds (" + x + ", " + z + "). Range must be between 0 and 15.");
        }
        return (z * 16) + x;
    }

}
