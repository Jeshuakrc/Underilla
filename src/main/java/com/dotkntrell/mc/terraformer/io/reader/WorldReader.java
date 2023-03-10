package com.dotkntrell.mc.terraformer.io.reader;

import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAFile;
import com.jkantrell.mca.MCAUtil;
import com.jkantrell.nbt.tag.CompoundTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class WorldReader {

    //CONSTANTS
    private static final String REGION_DIRECTORY = "region";

    //FIELDS
    private final File world_;
    private final File regions_;
    private final WorldReader.MCAFileCache cache_;


    //CONSTRUCTORS
    public WorldReader(String worldPath) throws NoSuchFieldException {
        this(new File(worldPath));
    }
    public WorldReader(String worldPath, int cacheSize) throws NoSuchFieldException {
        this(new File(worldPath), cacheSize);
    }
    public WorldReader(File worldDir) throws NoSuchFieldException {
        this(worldDir, 16);
    }
    public WorldReader(File worldDir, int cacheSize) throws NoSuchFieldException {
        if (!(worldDir.exists() && worldDir.isDirectory())) {
            throw new NoSuchFieldException("World directory '" + worldDir.getPath() + "' does not exist.");
        }
        File regionDir = new File(worldDir, WorldReader.REGION_DIRECTORY);
        if (!(regionDir.exists() && regionDir.isDirectory())) {
            throw new NoSuchFieldException("World '" + worldDir.getName() + "' doesn't have a 'region' directory.");
        }
        this.world_ = worldDir;
        this.regions_ = regionDir;
        this.cache_ = new MCAFileCache(cacheSize);
    }


    //GETTERS
    public String getWorldName() {
        return this.world_.getName();
    }


    //UTIL
    public Optional<Material> materialAt(int x, int y, int z) {
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        return this.readChunk(chunkX, chunkZ)
                .map(c -> c.materialAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)));
    }
    public Optional<ChunkReader> readChunk(int x, int z) {
        MCAFile r = this.readRegion(x >> 5, z >> 5);
        return Optional.ofNullable(r)
                .map(reg -> reg.getChunk(Math.floorMod(x, 32), Math.floorMod(z, 32)))
                .map(ChunkReader::new);
    }


    //PRIVATE UTIL
    private MCAFile readRegion(int x, int z) {
        MCAFile region = this.cache_.get(x, z);
        if (region != null) { return region; }
        File regionFile = new File(this.regions_, "r." + x + "." + z + ".mca");
        if (!regionFile.exists() || regionFile.isDirectory()) { return null; }
        try {
            region = MCAUtil.read(regionFile);
            this.cache_.put(x, z, region);
            return region;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //CLASSES
    private static class MCAFileCache {

        //FIELDS
        private final Map<Pair<Integer, Integer>, MCAFile> map_ = new HashMap<>();
        private final Deque<Pair<Integer, Integer>> queue_ = new LinkedList<>();
        private final int capacity_;


        //CONSTRUCTOR
        MCAFileCache(int capacity) {
            this.capacity_ = capacity;
        }


        //UTIL
        MCAFile get(int x, int z) {
            Pair<Integer, Integer> pair = ImmutablePair.of(x, z);
            MCAFile cached = this.map_.get(pair);
            if (cached == null) { return null; }
            Bukkit.getLogger().warning("Retrieving region [" + x + " - " + z + "] from cache.");
            this.queue_.remove(pair);
            this.queue_.addFirst(pair);
            return cached;
        }
        void put(int x, int z, MCAFile file) {
            Pair<Integer, Integer> pair = ImmutablePair.of(x,z);
            if (map_.containsKey(pair)) {
                this.queue_.remove(pair);
            } else if (this.queue_.size() >= this.capacity_) {
                Pair<Integer, Integer> temp = this.queue_.removeLast();
                this.map_.remove(temp);
            }
            this.map_.put(pair, file);
            this.queue_.addFirst(pair);
        }
    }
}
