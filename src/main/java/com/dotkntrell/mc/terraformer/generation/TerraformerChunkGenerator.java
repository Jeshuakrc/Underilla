package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.Terraformer;
import com.dotkntrell.mc.terraformer.io.Config;
import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import com.jkantrell.mca.MCAUtil;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R3.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TerraformerChunkGenerator extends ChunkGenerator {

    //ASSETS
    private final static Config CONFIG = Terraformer.CONFIG;


    //FIELDS
    private final WorldReader worldReader_;
    private final Merger merger_;


    //CONSTRUCTORS
    public TerraformerChunkGenerator(WorldReader worldReader) {
        this.worldReader_ = worldReader;
        this.merger_ = CONFIG.mergeStrategy.equals(Config.MergeStrategy.RELATIVE)
                ? new RelativeMerger(this.worldReader_, CONFIG.mergeUpperLimit, CONFIG.mergeLowerLimit, CONFIG.mergeDepth, CONFIG.mergeBlendRange, CONFIG.keepUndergroundBiomes)
                : new AbsoluteMerger(this.worldReader_, CONFIG.mergeStrategy.equals(Config.MergeStrategy.NONE) ? -64 : CONFIG.mergeLimit);
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        Optional<ChunkReader> chunkReader = this.worldReader_.readChunk(chunkX, chunkZ);
        if (chunkReader.isEmpty()) { return 0; }

        Predicate<Material> check = switch (heightMap) {
            case WORLD_SURFACE, WORLD_SURFACE_WG -> Material::isAir;
            case OCEAN_FLOOR, OCEAN_FLOOR_WG, MOTION_BLOCKING -> Material::isSolid;
            case MOTION_BLOCKING_NO_LEAVES -> m -> m.isSolid() || m.toString().contains("LEAVES");
        };
        int y = chunkReader.get().airColumnBottomHeight();
        Material m;
        do {
            y--;
            if (y < worldInfo.getMinHeight()) { break; }
            m = chunkReader.get().materialAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)).orElse(Material.AIR);
        } while (check.test(m));
        return y + 1;
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        Optional<ChunkReader> reader = this.worldReader_.readChunk(chunkX, chunkZ);
        if (reader.isEmpty()) { return; }
        Bukkit.getLogger().info("Generating chunk [" + chunkX + " - " + chunkZ + "] from " + this.worldReader_.getWorldName() + ".");
        AugmentedChunkData augmentedChunkData = new AugmentedChunkData((CraftChunkData) chunkData);
        this.merger_.mergeLand(worldInfo, random, chunkX, chunkZ, augmentedChunkData);
        this.merger_.mergeBiomes(worldInfo, random, chunkX, chunkZ, augmentedChunkData);
        return;
    }

    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return !CONFIG.mergeStrategy .equals(Config.MergeStrategy.NONE);
    }


    @Override
    public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        //Must always return true, bedrock and deepslate layers are generated in this step
        return true;
    }


    @Override
    public boolean shouldGenerateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return CONFIG.generateCaves;
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

    private void setHeightMap(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, AugmentedChunkData chunkData) {
        Map<HeightMap, HeightMapWrapper> maps = new HashMap<>();
        Stream.of(HeightMap.WORLD_SURFACE, HeightMap.WORLD_SURFACE_WG, HeightMap.OCEAN_FLOOR, HeightMap.OCEAN_FLOOR_WG, HeightMap.MOTION_BLOCKING, HeightMap.MOTION_BLOCKING_NO_LEAVES)
                .forEach(m -> maps.put(m, new HeightMapWrapper(m)));

        int height, absX, absZ;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                absX = chunkX + x; absZ = chunkZ + z;

                height = this.getBaseHeight(worldInfo, random, absX, absZ, HeightMap.WORLD_SURFACE);
                maps.get(HeightMap.WORLD_SURFACE).set(x, z, height + 64);
                maps.get(HeightMap.WORLD_SURFACE_WG).set(x, z, height + 64);

                height = this.getBaseHeight(worldInfo, random, absX, absZ, HeightMap.OCEAN_FLOOR);
                maps.get(HeightMap.OCEAN_FLOOR).set(x, z, height);
                maps.get(HeightMap.OCEAN_FLOOR_WG).set(x, z, height);
                maps.get(HeightMap.MOTION_BLOCKING).set(x, z, height);

                height = this.getBaseHeight(worldInfo, random, absX, absZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                maps.get(HeightMap.MOTION_BLOCKING_NO_LEAVES).set(x, z, height);
            }
        }
    }
}
