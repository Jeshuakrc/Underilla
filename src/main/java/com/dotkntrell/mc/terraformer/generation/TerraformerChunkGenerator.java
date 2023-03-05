package com.dotkntrell.mc.terraformer.generation;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class TerraformerChunkGenerator extends ChunkGenerator {

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        if (this.shouldGenerateNoise(worldInfo, random, x / 16, z / 16)) {
            return super.getBaseHeight(worldInfo, random, x, z, heightMap);
        }
        return 200;
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        if (this.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) { return; }

        Bukkit.getLogger().warning("Generating chunk [" + chunkX + " - " + chunkZ + "]");
        for(int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 100; y++) {
                    if (y < 50) {
                        chunkData.setBlock(x, y, z, Material.STONE);
                        continue;
                    }
                    if (y < 99) {
                        chunkData.setBlock(x, y, z, Material.DIRT);
                        continue;
                    }
                    chunkData.setBlock(x, y, z, Material.GOLD_BLOCK);
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return  !((Math.abs(chunkX) < 15) && (Math.abs(chunkZ) < 15));
    }


    @Override
    public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return true;
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
