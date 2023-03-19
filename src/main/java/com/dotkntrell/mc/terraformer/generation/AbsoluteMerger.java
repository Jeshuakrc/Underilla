package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import com.dotkntrell.mc.terraformer.util.VectorIterable;
import org.bukkit.Material;
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
    public void merge(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData, ChunkReader chunkReader) {
        int maxHeight;
        ChunkReader.Range airColumn = chunkReader.airColumn();
        if (airColumn != null) {
            Vector c1 = airColumn.corner1(), c2 = airColumn.corner2();
            chunkData.setRegion(c1.getBlockX(), c1.getBlockY(), c1.getBlockZ(), c2.getBlockX(), c2.getBlockY(), c2.getBlockZ(), Material.AIR);
            maxHeight = c1.getBlockY();
        } else {
            maxHeight = chunkData.getMaxHeight() + 1;
        }

        VectorIterable iterable = new VectorIterable(0, 16, this.height_, maxHeight, 0, 16);
        for (Vector v : iterable) {
            Material material = chunkReader.materialAt(v.getBlockX(),v.getBlockY(),v.getBlockZ()).orElse(Material.AIR);
            chunkData.setBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ(), material);
        }
    }
}
