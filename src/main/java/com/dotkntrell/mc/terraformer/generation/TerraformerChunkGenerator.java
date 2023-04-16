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

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class TerraformerChunkGenerator extends ChunkGenerator {

    //ASSETS
    private final static Config CONFIG = Terraformer.CONFIG;


    //FIELDS
    private final WorldReader worldReader_;
    private final Merger merger_;


    //CONSTRUCTORS
    public TerraformerChunkGenerator(WorldReader worldReader) {
        this.worldReader_ = worldReader;
        this.merger_ = CONFIG.yMergeStrategy.equals(Config.YMergeStrategy.ABSOLUTE)
                ? new AbsoluteMerger(this.worldReader_, CONFIG.yMergeHeight)
                : new RelativeMerger(this.worldReader_, CONFIG.yMergeUpperLimit, CONFIG.yMergeLowerLimit, CONFIG.yMergeDepth, CONFIG.yMergeBlendRange);
    }


    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
        Predicate<Material> check = switch (heightMap) {
            case WORLD_SURFACE, WORLD_SURFACE_WG -> Material::isAir;
            case OCEAN_FLOOR, OCEAN_FLOOR_WG, MOTION_BLOCKING -> Material::isSolid;
            case MOTION_BLOCKING_NO_LEAVES -> m -> m.isSolid() || m.toString().contains("LEAVES");
        };
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        Optional<ChunkReader> chunkReader = this.worldReader_.readChunk(chunkX, chunkZ);
        if (chunkReader.isEmpty()) { return super.getBaseHeight(worldInfo, random, x, z, heightMap); }
        int y = worldInfo.getMaxHeight();
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
        AugmentedChunkData augmentedChunkData = new AugmentedChunkData((CraftChunkData) chunkData);
        Bukkit.getLogger().info("Generating chunk [" + chunkX + " - " + chunkZ + "] from " + this.worldReader_.getWorldName() + ".");
        this.merger_.mergeLand(worldInfo, random, chunkX, chunkZ, augmentedChunkData);
        this.merger_.mergeBiomes(worldInfo, random, chunkX, chunkZ, augmentedChunkData);
    }

    @Override
    public boolean shouldGenerateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        if ( CONFIG.yMergeEnabled ) { return true; }
        return this.worldReader_.readChunk(chunkX, chunkZ).isEmpty();
    }


    @Override
    public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        return this.worldReader_.readChunk(chunkX,chunkZ).isEmpty();
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
