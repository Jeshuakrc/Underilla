package com.jkantrell.mc.underilla.spigot.impl;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.generator.CraftChunkData;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import net.minecraft.core.Holder;

public class BukkitChunkData implements ChunkData {

    // FIELDS
    private CraftChunkData internal_;
    private org.bukkit.generator.ChunkGenerator.ChunkData chunkData;
    private static final Map<Biome, Holder.Reference<net.minecraft.world.level.biome.Biome>> biomeCache = new HashMap<>();
    private static final World world = Bukkit.getWorld("world");


    // CONSTRUCTORS
    // public BukkitChunkData(CraftChunkData delegate) { this.internal_ = delegate; }
    public BukkitChunkData(org.bukkit.generator.ChunkGenerator.ChunkData chunkData) {
        internal_ = (CraftChunkData) chunkData;
        this.chunkData = chunkData;
    }


    @Override
    public int getMinHeight() { return this.chunkData.getMinHeight(); }

    @Override
    public Block getBlock(int x, int y, int z) {
        BlockData data = this.chunkData.getBlockData(x, y, z);
        return new BukkitBlock(data);
    }

    @Override
    public int getMaxHeight() { return this.chunkData.getMaxHeight(); }

    @Override
    public com.jkantrell.mc.underilla.core.api.Biome getBiome(int x, int y, int z) {
        Biome b = this.chunkData.getBiome(x, y, z);
        return new BukkitBiome(b);
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            return;
        }
        this.chunkData.setRegion(xMin, yMin, zMin, xMax, yMax, zMax, bukkitBlock.getBlockData());
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        if (!(block instanceof BukkitBlock bukkitBlock)) {
            return;
        }
        this.chunkData.setBlock(x, y, z, bukkitBlock.getBlockData());
    }

    @Override
    public void setBiome(int x, int y, int z, com.jkantrell.mc.underilla.core.api.Biome biome) {
        // No need to set biome for chunk. It's done by the generator.
    }
}
